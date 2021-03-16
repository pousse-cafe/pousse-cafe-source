package poussecafe.source.model;

import poussecafe.source.analysis.ClassName;

import static java.util.Objects.requireNonNull;

public class TypeReference {

    private ClassName typeClassName;

    public ClassName typeClassName() {
        return typeClassName;
    }

    private boolean ignored;

    public boolean ignored() {
        return ignored;
    }

    private ComponentType type;

    public ComponentType type() {
        return type;
    }

    public static class Builder {

        public TypeReference build() {
            requireNonNull(typeReference.typeClassName);
            return typeReference;
        }

        private TypeReference typeReference = new TypeReference();

        public Builder typeClassName(ClassName typeClassName) {
            typeReference.typeClassName = typeClassName;
            return this;
        }

        public Builder ignored(boolean ignored) {
            typeReference.ignored = ignored;
            return this;
        }

        public Builder type(ComponentType type) {
            typeReference.type = type;
            return this;
        }
    }

    private TypeReference() {

    }
}
