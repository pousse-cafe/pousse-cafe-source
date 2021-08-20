package poussecafe.source.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import poussecafe.source.analysis.ClassName;
import poussecafe.source.analysis.SafeClassName;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("serial")
public class InnerAggregateRoot implements Serializable {

    private SafeClassName typeName;

    public SafeClassName typeName() {
        return typeName;
    }

    public List<TypeReference> references() {
        return Collections.unmodifiableList(references);
    }

    private List<TypeReference> references = new ArrayList<>();

    public Optional<ClassName> identifierClassName() {
        return Optional.ofNullable(identifierClassName);
    }

    private ClassName identifierClassName;

    public static class Builder {

        private InnerAggregateRoot aggregate = new InnerAggregateRoot();

        public InnerAggregateRoot build() {
            requireNonNull(aggregate.typeName);
            return aggregate;
        }

        public Builder name(SafeClassName typeName) {
            aggregate.typeName = typeName;
            return this;
        }

        public Builder identifierClassName(Optional<ClassName> identifierClassName) {
            aggregate.identifierClassName = identifierClassName.orElse(null);
            return this;
        }

        public Builder references(List<TypeReference> references) {
            references.forEach(aggregate.references::add);
            return this;
        }
    }

    private InnerAggregateRoot() {

    }
}
