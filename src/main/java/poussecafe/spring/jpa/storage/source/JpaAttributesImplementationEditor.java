package poussecafe.spring.jpa.storage.source;

import poussecafe.source.analysis.ClassName;
import poussecafe.source.generation.AggregateAttributesImplementationEditor;
import poussecafe.source.generation.tools.CompilationUnitEditor;
import poussecafe.source.model.Aggregate;

import static java.util.Objects.requireNonNull;

public class JpaAttributesImplementationEditor {

    public void edit() {
        var typeEditor = compilationUnitEditor.typeDeclaration();

        var entityAnnotationTypeName = new ClassName("javax.persistence.Entity");
        if(!compilationUnitEditor.hasImport(entityAnnotationTypeName.toString())) {
            compilationUnitEditor.addImport(entityAnnotationTypeName);
            typeEditor.modifiers().markerAnnotation(entityAnnotationTypeName);
        }

        var idAnnotationTypeName = new ClassName("javax.persistence.Id");
        if(!compilationUnitEditor.hasImport(idAnnotationTypeName.toString())
                && typeEditor.hasField(AggregateAttributesImplementationEditor.IDENTIFIER_FIELD_NAME)) {
            compilationUnitEditor.addImport(idAnnotationTypeName);
            var identifierField = typeEditor.field(AggregateAttributesImplementationEditor.IDENTIFIER_FIELD_NAME).get(0);
            identifierField.modifiers().markerAnnotation(idAnnotationTypeName);
        }

        var versionAnnotationTypeName = new ClassName("javax.persistence.Version");
        if(!compilationUnitEditor.hasImport(versionAnnotationTypeName.toString())
                && typeEditor.hasField(AggregateAttributesImplementationEditor.VERSION_FIELD_NAME)) {
            compilationUnitEditor.addImport(versionAnnotationTypeName);
            var versionField = typeEditor.field(AggregateAttributesImplementationEditor.VERSION_FIELD_NAME).get(0);
            versionField.modifiers().markerAnnotation(versionAnnotationTypeName);
            versionField.modifiers().removeAnnotations(SuppressWarnings.class);
        }

        compilationUnitEditor.flush();
    }

    private Aggregate aggregate;

    public static class Builder {

        private JpaAttributesImplementationEditor editor = new JpaAttributesImplementationEditor();

        public JpaAttributesImplementationEditor build() {
            requireNonNull(editor.compilationUnitEditor);
            requireNonNull(editor.aggregate);
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

    private JpaAttributesImplementationEditor() {

    }

    private CompilationUnitEditor compilationUnitEditor;
}
