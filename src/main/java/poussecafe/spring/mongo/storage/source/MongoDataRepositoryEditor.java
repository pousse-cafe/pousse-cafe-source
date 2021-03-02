package poussecafe.spring.mongo.storage.source;

import poussecafe.source.analysis.ClassName;
import poussecafe.source.analysis.Visibility;
import poussecafe.source.generation.NamingConventions;
import poussecafe.source.generation.tools.AstWrapper;
import poussecafe.source.generation.tools.CompilationUnitEditor;
import poussecafe.source.model.Aggregate;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unchecked")
public class MongoDataRepositoryEditor {

    public void edit() {
        compilationUnitEditor.setPackage(NamingConventions.adaptersPackageName(aggregate.aggregatePackage()));

        var mongoRepositoryTypeName = new ClassName("org.springframework.data.mongodb.repository.MongoRepository");
        compilationUnitEditor.addImport(mongoRepositoryTypeName);

        var typeEditor = compilationUnitEditor.typeDeclaration();

        typeEditor.modifiers().setVisibility(Visibility.PUBLIC);
        typeEditor.setInterface(true);
        var typeName = MongoStorageAdaptersCodeGenerator.aggregateMongoRepositoryTypeName(aggregate.aggregatePackage());
        typeEditor.setName(typeName);

        var mongoRepositoryType = ast.newParameterizedType(mongoRepositoryTypeName.getIdentifier());
        mongoRepositoryType.typeArguments().add(ast.newSimpleType(
                NamingConventions.aggregateAttributesImplementationTypeName(aggregate.aggregatePackage()).getIdentifier()));
        mongoRepositoryType.typeArguments().add(ast.newSimpleType("String"));
        typeEditor.addSuperinterface(mongoRepositoryType);

        compilationUnitEditor.flush();
    }

    private Aggregate aggregate;

    public static class Builder {

        private MongoDataRepositoryEditor editor = new MongoDataRepositoryEditor();

        public MongoDataRepositoryEditor build() {
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

    private MongoDataRepositoryEditor() {

    }

    private CompilationUnitEditor compilationUnitEditor;

    private AstWrapper ast;
}
