package poussecafe.source.model;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import poussecafe.source.analysis.ClassName;

import static java.util.Objects.requireNonNull;
import static poussecafe.util.Equality.referenceEquals;

@SuppressWarnings("serial")
public class TypeReference implements Serializable {

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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(typeClassName)
                .append(ignored)
                .append(type)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        return referenceEquals(this, obj).orElse(other -> new EqualsBuilder()
                .append(typeClassName, other.typeClassName)
                .append(ignored, other.ignored)
                .append(type, other.type)
                .build());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(typeClassName)
                .append(ignored)
                .append(type)
                .build();
    }
}
