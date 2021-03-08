package poussecafe.source.analysis;

import static java.util.Objects.requireNonNull;

public class EntityClass {

    public static boolean isEntity(ResolvedTypeDeclaration declaration) {
        return declaration.name().instanceOf(CompilationUnitResolver.ENTITY_CLASS);
    }

    public EntityClass(ResolvedTypeDeclaration declaration) {
        requireNonNull(declaration);
        this.declaration = declaration;
    }

    private ResolvedTypeDeclaration declaration;

    public ResolvedTypeDeclaration declaration() {
        return declaration;
    }
}
