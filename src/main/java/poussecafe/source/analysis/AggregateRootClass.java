package poussecafe.source.analysis;

import java.util.Optional;
import poussecafe.source.generation.NamingConventions;

public class AggregateRootClass {

    public static boolean isAggregateRoot(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        Optional<ResolvedTypeName> superclassType = resolvedTypeDeclaration.superclass();
        return superclassType.isPresent()
                && superclassType.get().isClass(CompilationUnitResolver.AGGREGATE_ROOT_CLASS);
    }

    public AggregateRootClass(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        if(!isAggregateRoot(resolvedTypeDeclaration)) {
            throw new IllegalArgumentException();
        }
        this.resolvedTypeDeclaration = resolvedTypeDeclaration;
    }

    private ResolvedTypeDeclaration resolvedTypeDeclaration;

    public String aggregateName() {
        if(isInnerClass()) {
            return name().resolvedClass().declaringClass().orElseThrow().name().simple();
        } else {
            return NamingConventions.aggregateNameFromSimpleRootName(name().simpleName());
        }
    }

    private ResolvedTypeName name() {
        return resolvedTypeDeclaration.name();
    }

    public boolean isInnerClass() {
        return name().resolvedClass().declaringClass().isPresent();
    }

    public ResolvedTypeDeclaration typeDeclaration() {
        return resolvedTypeDeclaration;
    }

    public Optional<ClassName> identifierClassName() {
        return resolvedTypeDeclaration.superclassType()
                .filter(type -> type.isParametrized())
                .filter(type -> type.typeParameters().size() > 0)
                .map(type -> type.typeParameters().get(0).toTypeName().qualifiedClassName());
    }
}
