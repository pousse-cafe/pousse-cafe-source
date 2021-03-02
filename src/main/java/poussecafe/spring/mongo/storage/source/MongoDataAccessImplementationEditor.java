package poussecafe.spring.mongo.storage.source;

import poussecafe.discovery.DataAccessImplementation;
import poussecafe.source.analysis.ClassName;
import poussecafe.source.analysis.Visibility;
import poussecafe.source.generation.NamingConventions;
import poussecafe.source.generation.tools.AstWrapper;
import poussecafe.source.generation.tools.CompilationUnitEditor;
import poussecafe.source.model.Aggregate;
import poussecafe.spring.mongo.storage.MongoDataAccess;
import poussecafe.spring.mongo.storage.SpringMongoDbStorage;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unchecked")
public class MongoDataAccessImplementationEditor {

    public void edit() {
        compilationUnitEditor.setPackage(NamingConventions.adaptersPackageName(aggregate.aggregatePackage()));

        var autowiredTypeName = new ClassName("org.springframework.beans.factory.annotation.Autowired");
        compilationUnitEditor.addImport(autowiredTypeName);
        compilationUnitEditor.addImport(DataAccessImplementation.class);
        compilationUnitEditor.addImport(MongoDataAccess.class);
        compilationUnitEditor.addImport(SpringMongoDbStorage.class);
        compilationUnitEditor.addImport(NamingConventions.aggregateRootTypeName(aggregate));
        compilationUnitEditor.addImport(NamingConventions.aggregateDataAccessTypeName(aggregate));
        compilationUnitEditor.addImport(NamingConventions.aggregateIdentifierTypeName(aggregate));

        var typeEditor = compilationUnitEditor.typeDeclaration();

        var dataAccessImplementationAnnotation = typeEditor.modifiers().normalAnnotation(
                DataAccessImplementation.class).get(0);
        dataAccessImplementationAnnotation.setAttribute("aggregateRoot", ast.newTypeLiteral(
                NamingConventions.aggregateRootTypeName(aggregate).getIdentifier()));
        dataAccessImplementationAnnotation.setAttribute("dataImplementation", ast.newTypeLiteral(
                NamingConventions.aggregateAttributesImplementationTypeName(aggregate.aggregatePackage()).getIdentifier()));

        var storageNameAccess = ast.ast().newFieldAccess();
        storageNameAccess.setExpression(ast.ast().newSimpleName(SpringMongoDbStorage.class.getSimpleName()));
        storageNameAccess.setName(ast.ast().newSimpleName("NAME"));
        dataAccessImplementationAnnotation.setAttribute("storageName", storageNameAccess);

        typeEditor.modifiers().setVisibility(Visibility.PUBLIC);
        var typeName = NamingConventions.aggregateDataAccessImplementationTypeName(aggregate.aggregatePackage(),
                SpringMongoDbStorage.NAME).getIdentifier();
        typeEditor.setName(typeName);

        var superclassType = ast.newParameterizedType(MongoDataAccess.class);
        superclassType.typeArguments().add(ast.newSimpleType(
                NamingConventions.aggregateIdentifierTypeName(aggregate).getIdentifier()));
        superclassType.typeArguments().add(ast.newSimpleType(
                NamingConventions.aggregateAttributesImplementationTypeName(aggregate.aggregatePackage()).getIdentifier()));
        superclassType.typeArguments().add(ast.newSimpleType("String"));
        typeEditor.setSuperclass(superclassType);

        var superinterfaceType = ast.newParameterizedType(
                NamingConventions.aggregateDataAccessTypeName(aggregate).getIdentifier());
        superinterfaceType.typeArguments().add(ast.newSimpleType(
                NamingConventions.aggregateAttributesImplementationTypeName(aggregate.aggregatePackage()).getIdentifier()));
        typeEditor.addSuperinterface(superinterfaceType);

        var convertIdEditor = typeEditor.method("convertId").get(0);
        convertIdEditor.modifiers().markerAnnotation(Override.class);
        convertIdEditor.modifiers().setVisibility(Visibility.PROTECTED);
        var convertIdReturnType = ast.newSimpleType("String");
        convertIdEditor.setReturnType(convertIdReturnType);
        convertIdEditor.clearParameters();
        convertIdEditor.addParameter(
                NamingConventions.aggregateIdentifierTypeName(aggregate).getIdentifier(), "key");
        var convertIdBody = ast.ast().newBlock();
        var convertIdReturnStatement = ast.ast().newReturnStatement();
        var keyStringValue = ast.ast().newMethodInvocation();
        keyStringValue.setExpression(ast.ast().newSimpleName("key"));
        keyStringValue.setName(ast.ast().newSimpleName("stringValue"));
        convertIdReturnStatement.setExpression(keyStringValue);
        convertIdBody.statements().add(convertIdReturnStatement);
        convertIdEditor.setBody(convertIdBody);

        var mongoRepositoryEditor = typeEditor.method("mongoRepository").get(0);
        mongoRepositoryEditor.modifiers().markerAnnotation(Override.class);
        mongoRepositoryEditor.modifiers().setVisibility(Visibility.PROTECTED);
        var mongoRepositoryReturnType = ast.newSimpleType(
                MongoStorageAdaptersCodeGenerator.aggregateMongoRepositoryTypeName(aggregate.aggregatePackage()).getIdentifier());
        mongoRepositoryEditor.setReturnType(mongoRepositoryReturnType);
        var mongoRepositoryBody = ast.ast().newBlock();
        var mongoRepositoryReturnStatement = ast.ast().newReturnStatement();
        mongoRepositoryReturnStatement.setExpression(ast.ast().newSimpleName("repository"));
        mongoRepositoryBody.statements().add(mongoRepositoryReturnStatement);
        mongoRepositoryEditor.setBody(mongoRepositoryBody);

        var repositoryFieldEditor = typeEditor.field("repository").get(0);
        repositoryFieldEditor.modifiers().markerAnnotation(autowiredTypeName.getIdentifier());
        repositoryFieldEditor.modifiers().setVisibility(Visibility.PRIVATE);
        repositoryFieldEditor.setType(ast.newSimpleType(
                MongoStorageAdaptersCodeGenerator.aggregateMongoRepositoryTypeName(aggregate.aggregatePackage()).getIdentifier()));

        compilationUnitEditor.flush();
    }

    private Aggregate aggregate;

    public static class Builder {

        private MongoDataAccessImplementationEditor editor = new MongoDataAccessImplementationEditor();

        public MongoDataAccessImplementationEditor build() {
            requireNonNull(editor.compilationUnitEditor);
            requireNonNull(editor.aggregate);

            editor.ast = editor.compilationUnitEditor.ast();

            return editor;
        }

        public Builder compilationUnitEditor(CompilationUnitEditor compilationUnitEditor) {
            editor.compilationUnitEditor = compilationUnitEditor;
            return this;
        }

        public Builder aggregate(Aggregate aggregate) {
            editor.aggregate = aggregate;
            return this;
        }
    }

    private MongoDataAccessImplementationEditor() {

    }

    private CompilationUnitEditor compilationUnitEditor;

    private AstWrapper ast;
}
