package poussecafe.source.analysis;

import static java.util.Objects.requireNonNull;

public class ValueObjectClass {

    public static boolean isValueObject(ResolvedTypeDeclaration declaration) {
        return declaration.name().instanceOf(CompilationUnitResolver.VALUE_OBJECT_INTERFACE);
    }

    public ValueObjectClass(ResolvedTypeDeclaration declaration) {
        requireNonNull(declaration);
        this.declaration = declaration;
    }

    private ResolvedTypeDeclaration declaration;

    public ResolvedTypeDeclaration declaration() {
        return declaration;
    }
}
