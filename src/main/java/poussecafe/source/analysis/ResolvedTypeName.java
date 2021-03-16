package poussecafe.source.analysis;

import static java.util.Objects.requireNonNull;

public class ResolvedTypeName {

    private Resolver resolver;

    private ClassName name;

    public ClassName name() {
        return name;
    }

    private ResolvedClass resolvedClass;

    public ResolvedClass resolvedClass() {
        return resolvedClass;
    }

    public boolean isClass(ResolvedClass expectedClass) {
        return isClass(expectedClass.name().qualified());
    }

    public boolean isClass(String expectedFullyQualifiedName) {
        return resolvedClass.name().qualified().equals(expectedFullyQualifiedName);
    }

    public String simpleName() {
        return resolvedClass.name().simple();
    }

    public boolean instanceOf(String superType) {
        try {
            return resolvedClass.instanceOf(superType);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public String qualifiedName() {
        return qualifiedClassName().qualified();
    }

    public ClassName qualifiedClassName() {
        return resolvedClass.name();
    }

    public String packageName() {
        return qualifiedClassName().getQualifier().toString();
    }

    public static class Builder {

        private ResolvedTypeName resolved = new ResolvedTypeName();

        public Builder withResolver(Resolver resolver) {
            resolved.resolver = resolver;
            return this;
        }

        public Builder withName(ClassName name) {
            resolved.name = name;
            return this;
        }

        public Builder withResolvedClass(ResolvedClass resolvedClass) {
            resolved.resolvedClass = resolvedClass;
            return this;
        }

        public ResolvedTypeName build() {
            requireNonNull(resolved.resolver);
            requireNonNull(resolved.name);
            requireNonNull(resolved.resolvedClass);
            return resolved;
        }
    }

    private ResolvedTypeName() {

    }
}
