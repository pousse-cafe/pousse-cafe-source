package poussecafe.source.analysis;

import java.util.Optional;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class AggregateContainerClass {

    public static boolean isAggregateContainerClass(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        AnnotatedElement<TypeDeclaration> annotatedElement = resolvedTypeDeclaration.asAnnotatedElement();
        return annotatedElement.findAnnotation(CompilationUnitResolver.AGGREGATE_ANNOTATION_CLASS).isPresent();
    }

    public AggregateContainerClass(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        this.resolvedTypeDeclaration = resolvedTypeDeclaration;
    }

    private ResolvedTypeDeclaration resolvedTypeDeclaration;

    public String aggregateName() {
        return resolvedTypeDeclaration.unresolvedName().simpleName();
    }

    public Optional<ClassName> identifierClassName() {
        return resolvedTypeDeclaration.innerTypes().stream()
                .filter(type -> AggregateRootClass.isAggregateRoot(type))
                .findFirst()
                .map(type -> type.superclassType())
                .filter(Optional::isPresent).map(Optional::orElseThrow)
                .filter(type -> type.isParametrized())
                .filter(type -> type.typeParameters().size() > 0)
                .map(type -> type.typeParameters().get(0).toTypeName().qualifiedClassName());
    }
}
