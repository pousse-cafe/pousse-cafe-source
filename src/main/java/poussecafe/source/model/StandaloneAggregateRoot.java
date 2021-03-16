package poussecafe.source.model;

import java.io.Serializable;
import java.util.Optional;
import poussecafe.source.analysis.ClassName;
import poussecafe.source.generation.NamingConventions;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("serial")
public class StandaloneAggregateRoot implements Serializable, WithTypeComponent {

    private TypeComponent typeComponent;

    @Override
    public TypeComponent typeComponent() {
        return typeComponent;
    }

    public String aggregateName() {
        return NamingConventions.aggregateNameFromSimpleRootName(typeComponent.typeName().rootClassName().simple());
    }

    public Optional<ClassName> identifierClassName() {
        return Optional.ofNullable(identifierClassName);
    }

    private ClassName identifierClassName;

    public static class Builder {

        private StandaloneAggregateRoot aggregate = new StandaloneAggregateRoot();

        public StandaloneAggregateRoot build() {
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

    private StandaloneAggregateRoot() {

    }
}
