package poussecafe.source.analysis;

import java.util.Optional;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import poussecafe.source.model.Documentation;
import poussecafe.source.model.Documented;

import static java.util.Objects.requireNonNull;

public class ResolvedMethod implements Documented {

    public AnnotatedElement<MethodDeclaration> asAnnotatedElement() {
        return new AnnotatedElement.Builder<MethodDeclaration>()
                .withResolver(resolver)
                .withElement(declaration)
                .build();
    }

    private Resolver resolver;

    private MethodDeclaration declaration;

    public MethodDeclaration declaration() {
        return declaration;
    }

    public Optional<ResolvedTypeName> parameterTypeName(int parameter) {
        if(declaration.parameters().size() <= parameter) {
            return Optional.empty();
        }

        SingleVariableDeclaration message = (SingleVariableDeclaration) declaration.parameters().get(parameter);
        Type parameterType = message.getType();
        if(parameterType instanceof SimpleType) {
            SimpleType messageType = (SimpleType) message.getType();
            return Optional.of(resolver.resolve(new ClassName(messageType.getName())));
        } else {
            return Optional.empty();
        }
    }

    public String name() {
        return declaration.getName().getIdentifier();
    }

    public Optional<ResolvedType> returnType() {
        Type returnType = declaration.getReturnType2();
        if(returnType == null
                || isVoid(returnType)) {
            return Optional.empty();
        } else {
            return Optional.of(new ResolvedType.Builder()
                    .resolver(resolver)
                    .type(returnType)
                    .build());
        }
    }

    private boolean isVoid(Type type) {
        if(type instanceof PrimitiveType) {
            var primitiveType = (PrimitiveType) type;
            return primitiveType.getPrimitiveTypeCode() == PrimitiveType.VOID;
        } else {
            return false;
        }
    }

    public Modifiers modifiers() {
        return new Modifiers.Builder()
                .modifiers(declaration.modifiers())
                .resolver(resolver)
                .target(target())
                .build();
    }

    private ModifiersTarget target() {
        if(declaringType.typeDeclaration().isInterface()) {
            return ModifiersTarget.INTERFACE_METHOD;
        } else {
            return ModifiersTarget.OTHER;
        }
    }

    public ResolvedTypeDeclaration declaringType() {
        return declaringType;
    }

    private ResolvedTypeDeclaration declaringType;

    @Override
    public Documentation documentation() {
        return DocumentationFactory.documentation(declaration.getJavadoc(), asAnnotatedElement());
    }

    public static class Builder {

        private ResolvedMethod resolvedMethod = new ResolvedMethod();

        public ResolvedMethod build() {
            requireNonNull(resolvedMethod.resolver);
            requireNonNull(resolvedMethod.declaration);
            requireNonNull(resolvedMethod.declaringType);
            return resolvedMethod;
        }

        public Builder withResolver(Resolver resolver) {
            resolvedMethod.resolver = resolver;
            return this;
        }

        public Builder withDeclaration(MethodDeclaration declaration) {
            resolvedMethod.declaration = declaration;
            return this;
        }

        public Builder withDeclaringType(ResolvedTypeDeclaration declaringType) {
            resolvedMethod.declaringType = declaringType;
            return this;
        }
    }

    private ResolvedMethod() {

    }
}
