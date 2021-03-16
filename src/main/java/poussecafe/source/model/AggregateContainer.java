package poussecafe.source.model;

import java.io.Serializable;
import java.util.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import poussecafe.source.analysis.ClassName;
import poussecafe.source.generation.NamingConventions;

import static java.util.Objects.requireNonNull;
import static poussecafe.util.Equality.referenceEquals;

@SuppressWarnings("serial")
public class AggregateContainer implements Serializable, WithTypeComponent {

    private TypeComponent typeComponent;

    @Override
    public TypeComponent typeComponent() {
        return typeComponent;
    }

    public String aggregateName() {
        return NamingConventions.aggregateNameFromContainer(typeComponent.typeName().rootClassName().simple());
    }

    public Optional<ClassName> identifierClassName() {
        return Optional.ofNullable(identifierClassName);
    }

    private ClassName identifierClassName;

    public static class Builder {

        private AggregateContainer aggregate = new AggregateContainer();

        public AggregateContainer build() {
            requireNonNull(aggregate.typeComponent);
            return aggregate;
        }

        public Builder typeComponent(TypeComponent typeComponent) {
            aggregate.typeComponent = typeComponent;
            return this;
        }

        public Builder identifierClassName(Optional<ClassName> identifierClassName) {
            aggregate.identifierClassName = identifierClassName.orElse(null);
            return this;
        }
    }

    private AggregateContainer() {

    }

    @Override
    public boolean equals(Object obj) {
        return referenceEquals(this, obj).orElse(other -> new EqualsBuilder()
                .append(typeComponent, other.typeComponent)
                .build());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(typeComponent)
                .build();
    }
}
