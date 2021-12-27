package poussecafe.source.analysis;

import org.eclipse.jdt.core.dom.EnumDeclaration;
import poussecafe.source.model.Documentation;

import static java.util.Objects.requireNonNull;

public class ResolvedEnumDeclaration {

    public SafeClassName unresolvedName() {
        return new SafeClassName.Builder()
                .rootClassName(new ClassName(resolver.compilationUnit().getPackage().getName().getFullyQualifiedName(),
                        declaration.getName().getIdentifier()))
                .build();
    }

    public Documentation documentation() {
        return DocumentationFactory.documentation(declaration.getJavadoc(), asAnnotatedElement());
    }

    public EnumDeclaration declaration() {
        return declaration;
    }

    private EnumDeclaration declaration;

    public AnnotatedElement<EnumDeclaration> asAnnotatedElement() {
        return new AnnotatedElement.Builder<EnumDeclaration>()
                .withElement(declaration)
                .withResolver(resolver)
                .build();
    }

    private CompilationUnitResolver resolver;

    public static class Builder {

        public ResolvedEnumDeclaration build() {
            requireNonNull(declaration.declaration);
            requireNonNull(declaration.resolver);
            return declaration;
        }

        private ResolvedEnumDeclaration declaration = new ResolvedEnumDeclaration();

        public Builder withDeclaration(EnumDeclaration declaration) {
            this.declaration.declaration = declaration;
            return this;
        }

        public Builder withResolver(CompilationUnitResolver resolver) {
            declaration.resolver = resolver;
            return this;
        }
    }

    private ResolvedEnumDeclaration() {

    }
}
