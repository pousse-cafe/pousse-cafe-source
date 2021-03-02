package poussecafe.spring.jpa.storage.source;

import poussecafe.source.analysis.ClassName;
import poussecafe.source.analysis.Visibility;
import poussecafe.source.generation.NamingConventions;
import poussecafe.source.generation.tools.AstWrapper;
import poussecafe.source.generation.tools.CompilationUnitEditor;
import poussecafe.source.model.Aggregate;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unchecked")
public class JpaDataRepositoryEditor {

    public void edit() {
        compilationUnitEditor.setPackage(NamingConventions.adaptersPackageName(aggregate.aggregatePackage()));

        var jpaRepositoryTypeName = new ClassName("org.springframework.data.jpa.repository.JpaRepository");
        compilationUnitEditor.addImport(jpaRepositoryTypeName);

        var typeEditor = compilationUnitEditor.typeDeclaration();

        typeEditor.modifiers().setVisibility(Visibility.PUBLIC);
        typeEditor.setInterface(true);
        var typeName = JpaStorageAdaptersCodeGenerator.aggregateJpaRepositoryTypeName(aggregate.aggregatePackage());
        typeEditor.setName(typeName);

        var mongoRepositoryType = ast.newParameterizedType(jpaRepositoryTypeName.getIdentifier());
        mongoRepositoryType.typeArguments().add(ast.newSimpleType(
                NamingConventions.aggregateAttributesImplementationTypeName(aggregate.aggregatePackage()).getIdentifier()));
        mongoRepositoryType.typeArguments().add(ast.newSimpleType("String"));
        typeEditor.addSuperinterface(mongoRepositoryType);

        compilationUnitEditor.flush();
    }

    private Aggregate aggregate;

    public static class Builder {

        private JpaDataRepositoryEditor editor = new JpaDataRepositoryEditor();

        public JpaDataRepositoryEditor build() {
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

    private JpaDataRepositoryEditor() {

    }

    private CompilationUnitEditor compilationUnitEditor;

    private AstWrapper ast;
}
