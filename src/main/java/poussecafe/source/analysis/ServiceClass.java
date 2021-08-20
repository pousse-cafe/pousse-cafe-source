package poussecafe.source.analysis;

import static java.util.Objects.requireNonNull;

public class ServiceClass {

    public static boolean isService(ResolvedTypeDeclaration declaration) {
        return declaration.name().instanceOf(CompilationUnitResolver.SERVICE_INTERFACE);
    }

    public ServiceClass(ResolvedTypeDeclaration declaration) {
        requireNonNull(declaration);
        this.declaration = declaration;
    }

    private ResolvedTypeDeclaration declaration;

    public ResolvedTypeDeclaration declaration() {
        return declaration;
    }
}
