package poussecafe.source.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import poussecafe.source.Source;
import poussecafe.source.analysis.ClassName;
import poussecafe.source.analysis.SafeClassName;
import poussecafe.source.generation.AggregatePackage;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("serial")
public class Aggregate implements Serializable {

    public String name() {
        return name;
    }

    private String name;

    public SafeClassName className() {
        return className;
    }

    private SafeClassName className;

    public String packageName() {
        return className.rootClassName().qualifier();
    }

    public boolean innerFactory() {
        return innerFactory;
    }

    private boolean innerFactory;

    public boolean innerRoot() {
        return innerRoot;
    }

    private boolean innerRoot;

    public boolean innerRepository() {
        return innerRepository;
    }

    private boolean innerRepository;

    public boolean requiresContainer() {
        return innerFactory
                || innerRoot
                || innerRepository;
    }

    public Optional<Source> containerSource() {
        return Optional.ofNullable(containerSource);
    }

    private Source containerSource;

    public Optional<Source> standaloneFactorySource() {
        return Optional.ofNullable(standaloneFactorySource);
    }

    private Source standaloneFactorySource;

    public Optional<Source> standaloneRootSource() {
        return Optional.ofNullable(standaloneRootSource);
    }

    private Source standaloneRootSource;

    public Optional<Source> standaloneRepositorySource() {
        return Optional.ofNullable(standaloneRepositorySource);
    }

    private Source standaloneRepositorySource;

    public AggregatePackage aggregatePackage() {
        return new AggregatePackage(className.rootClassName().qualifier(), name);
    }

    public Documentation documentation() {
        return documentation;
    }

    private Documentation documentation = Documentation.empty();

    public Optional<ClassName> rootIdentifierClassName() {
        return Optional.ofNullable(rootIdentifierClassName);
    }

    private ClassName rootIdentifierClassName;

    public List<TypeReference> rootReferences() {
        return Collections.unmodifiableList(rootReferences);
    }

    private List<TypeReference> rootReferences = new ArrayList<>();

    public static class Builder implements Serializable {

        private Aggregate aggregate = new Aggregate();

        public Aggregate build() {
            requireNonNull(aggregate.name);
            requireNonNull(aggregate.className, "Aggregate " + aggregate.name + " lacks a class");
            requireNonNull(aggregate.documentation, "Aggregate " + aggregate.name + " lacks documentation");

            aggregate.innerFactory = innerFactory.booleanValue();
            aggregate.innerRoot = innerRoot.booleanValue();
            aggregate.innerRepository = innerRepository.booleanValue();

            return aggregate;
        }

        public boolean isValid() {
            return aggregate.name != null && aggregate.className != null && aggregate.documentation != null;
        }

        public Optional<String> name() {
            return Optional.ofNullable(aggregate.name);
        }

        public Builder startingFrom(Aggregate other) {
            aggregate.name = other.name;
            aggregate.className = other.className;

            innerFactory = other.innerFactory;
            innerRoot = other.innerRoot;
            innerRepository = other.innerRepository;

            aggregate.containerSource = other.containerSource;
            aggregate.standaloneFactorySource = other.standaloneFactorySource;
            aggregate.standaloneRootSource = other.standaloneRootSource;
            aggregate.standaloneRepositorySource = other.standaloneRepositorySource;

            aggregate.documentation = other.documentation;
            aggregate.rootIdentifierClassName = other.rootIdentifierClassName;

            return this;
        }

        public Builder name(String name) {
            aggregate.name = name;
            return this;
        }

        public Builder className(SafeClassName className) {
            aggregate.className = className;
            return this;
        }

        public Builder innerFactory(boolean inner) {
            consistentLocationOrElseThrow(innerFactory, inner, "factory");
            innerFactory = inner;
            return this;
        }

        private void consistentLocationOrElseThrow(Boolean inner, boolean newValue, String componentName) {
            if(inner != null
                    && inner.booleanValue() != newValue) {
                throw new IllegalArgumentException("Inconsistent " + componentName + " location for aggregate "
                    + aggregate.name + ": must be in a container or not");
            }
        }

        private Boolean innerFactory;

        public Builder innerRoot(boolean inner) {
            consistentLocationOrElseThrow(innerRoot, inner, "root");
            innerRoot = inner;
            return this;
        }

        private Boolean innerRoot;

        public Builder innerRepository(boolean inner) {
            consistentLocationOrElseThrow(innerRepository, inner, "repository");
            innerRepository = inner;
            return this;
        }

        private Boolean innerRepository;

        public Builder ensureDefaultLocations() {
            boolean aPrioriInnerFactory = innerFactory != null && innerFactory.booleanValue();
            boolean aPrioriInnerRoot = innerRoot != null && innerRoot.booleanValue();
            boolean aPrioriInnerRepository = innerRepository != null && innerRepository.booleanValue();
            boolean noAPriori = innerFactory == null
                    && innerRoot == null
                    && innerRepository == null;
            if(innerFactory == null) {
                innerFactory(!noAPriori && (aPrioriInnerRoot || aPrioriInnerRepository));
            }
            if(innerRoot == null) {
                innerRoot(!noAPriori && (aPrioriInnerFactory || aPrioriInnerRepository));
            }
            if(innerRepository == null) {
                innerRepository(!noAPriori && (aPrioriInnerFactory || aPrioriInnerRoot));
            }
            return this;
        }

        public Builder containerSource(Optional<Source> containerSource) {
            aggregate.containerSource = containerSource.orElse(null);
            return this;
        }

        public Builder standaloneFactorySource(Optional<Source> standaloneFactorySource) {
            aggregate.standaloneFactorySource = standaloneFactorySource.orElse(null);
            return this;
        }

        public Builder standaloneRootSource(Optional<Source> standaloneRootSource) {
            aggregate.standaloneRootSource = standaloneRootSource.orElse(null);
            return this;
        }

        public Builder standaloneRepositorySource(Optional<Source> standaloneRepositorySource) {
            aggregate.standaloneRepositorySource = standaloneRepositorySource.orElse(null);
            return this;
        }

        public Builder provided(boolean provided) {
            this.provided = provided;
            return this;
        }

        private boolean provided;

        public boolean provided() {
            return provided;
        }

        public Builder documentation(Documentation documentation) {
            aggregate.documentation = documentation;
            return this;
        }

        public Builder rootIdentifierClassName(Optional<ClassName> identifierClassName) {
            aggregate.rootIdentifierClassName = identifierClassName.orElse(null);
            return this;
        }

        public Builder rootReferences(List<TypeReference> references) {
            aggregate.rootReferences.addAll(references);
            return this;
        }
    }

    private Aggregate() {

    }
}
