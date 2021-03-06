package poussecafe.source.generation.internal;

import java.io.Serializable;
import poussecafe.source.generation.tools.AstWrapper;
import poussecafe.source.generation.tools.CompilationUnitEditor;
import poussecafe.source.generation.tools.SuppressWarningsEditor;
import poussecafe.source.model.Aggregate;

import static java.util.Objects.requireNonNull;

public class InternalAttributesImplementationEditor {

    public void edit() {
        compilationUnitEditor.addImport(Serializable.class);

        var typeEditor = compilationUnitEditor.typeDeclaration();

        var annotationEditor = new SuppressWarningsEditor(
                typeEditor.modifiers().singleMemberAnnotation(SuppressWarnings.class).get(0));
        annotationEditor.addWarning("serial");

        typeEditor.addSuperinterfaceFirst(ast.newSimpleType(Serializable.class));

        compilationUnitEditor.flush();
    }

    private Aggregate aggregate;

    public static class Builder {

        private InternalAttributesImplementationEditor editor = new InternalAttributesImplementationEditor();

        public InternalAttributesImplementationEditor build() {
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

    private InternalAttributesImplementationEditor() {

    }

    private CompilationUnitEditor compilationUnitEditor;

    private AstWrapper ast;
}
