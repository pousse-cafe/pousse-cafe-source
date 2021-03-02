package poussecafe.spring.mongo.storage.source;

import poussecafe.source.analysis.ClassName;
import poussecafe.source.generation.AggregateAttributesImplementationEditor;
import poussecafe.source.generation.tools.CompilationUnitEditor;
import poussecafe.source.model.Aggregate;

import static java.util.Objects.requireNonNull;

public class MongoAttributesImplementationEditor {

    public void edit() {
        var typeEditor = compilationUnitEditor.typeDeclaration();

        var idAnnotationTypeName = new ClassName("org.springframework.data.annotation.Id");
        if(!compilationUnitEditor.hasImport(idAnnotationTypeName.toString())
                && typeEditor.hasField(AggregateAttributesImplementationEditor.IDENTIFIER_FIELD_NAME)) {
            compilationUnitEditor.addImport(idAnnotationTypeName);
            var identifierField = typeEditor.field(AggregateAttributesImplementationEditor.IDENTIFIER_FIELD_NAME).get(0);
            identifierField.modifiers().markerAnnotation(idAnnotationTypeName);
        }

        var versionAnnotationTypeName = new ClassName("org.springframework.data.annotation.Version");
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

        private MongoAttributesImplementationEditor editor = new MongoAttributesImplementationEditor();

        public MongoAttributesImplementationEditor build() {
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

    private MongoAttributesImplementationEditor() {

    }

    private CompilationUnitEditor compilationUnitEditor;
}
