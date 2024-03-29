package poussecafe.source.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("serial")
public class SourceModelBuilder implements Serializable {

    public SourceModelBuilder putAggregate(Aggregate.Builder source) {
        source.provided(true);
        aggregates.put(source.name().orElseThrow(), source);
        return this;
    }

    private Map<String, Aggregate.Builder> aggregates = new HashMap<>();

    public void addStandaloneAggregateRoot(StandaloneAggregateRoot root) {
        standaloneAggregateRoots.put(root.aggregateName(), root);
    }

    private Map<String, StandaloneAggregateRoot> standaloneAggregateRoots = new HashMap<>();

    public void addStandaloneAggregateFactory(StandaloneAggregateFactory factory) {
        standaloneAggregateFactories.put(factory.aggregateName(), factory);
    }

    private Map<String, StandaloneAggregateFactory> standaloneAggregateFactories = new HashMap<>();

    public void addStandaloneAggregateRepository(StandaloneAggregateRepository repository) {
        standaloneAggregateRepositories.put(repository.aggregateName(), repository);
    }

    private Map<String, StandaloneAggregateRepository> standaloneAggregateRepositories = new HashMap<>();

    public void addAggregateContainer(AggregateContainer.Builder container) {
        aggregateContainers.put(container.aggregateName().orElseThrow(), container);
    }

    private Map<String, AggregateContainer.Builder> aggregateContainers = new HashMap<>();

    public void addInnerAggregateRoot(String aggregateName, InnerAggregateRoot innerRoot) {
        var container = aggregateContainers.get(aggregateName);
        container.innerRoot(innerRoot);
    }

    public void addProcess(ProcessModel process) {
        String name = process.simpleName();
        processes.computeIfAbsent(name, key -> process);
        processesBySourceId.put(process.source().id(), process);
    }

    private Map<String, ProcessModel> processes = new HashMap<>();

    private Map<String, ProcessModel> processesBySourceId = new HashMap<>();

    public Optional<ProcessModel> process(String name) {
        return Optional.ofNullable(processes.get(name));
    }

    public Collection<ProcessModel> processes() {
        return Collections.unmodifiableCollection(processes.values());
    }

    public void addMessageListener(MessageListener source) {
        listeners.add(source);
    }

    private List<MessageListener> listeners = new ArrayList<>();

    public List<MessageListener> aggregateListeners(String aggregateName) {
        return listeners.stream()
                .filter(listener -> listener.container().aggregateName().isPresent())
                .filter(listener -> listener.container().aggregateName().orElseThrow().equals(aggregateName))
                .collect(toList());
    }

    public List<MessageListener> processListeners(String process) {
        return listeners.stream()
                .filter(listener -> listener.processNames().contains(process))
                .collect(toList());
    }

    public List<MessageListener> messageListeners() {
        return unmodifiableList(listeners);
    }

    public void addCommandIfAbsent(Command command) {
        var existingCommand = commands.get(command.simpleName());
        if(existingCommand == null) {
            commands.put(command.simpleName(), command);
        } else if(!existingCommand.name().equals(command.name())) {
            throw new IllegalArgumentException(
                    "A command with this name already exists but qualifiers do not match: " + existingCommand
                            .name()
                            .getQualifier() + " <> " + command.name().getQualifier());
        }
    }

    private Map<String, Command> commands = new HashMap<>();

    public Collection<Command> commands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    public Optional<Command> command(String name) {
        return Optional.ofNullable(commands.get(name));
    }

    public void replaceCommand(Command command) {
        commands.put(command.simpleName(), command);
    }

    public void addEventIfAbsent(DomainEvent event) {
        var existingEvent = events.get(event.simpleName());
        if(existingEvent == null) {
            events.put(event.simpleName(), event);
        } else if(!existingEvent.name().equals(event.name())) {
            throw new IllegalArgumentException("An event with this name already exists but qualifiers do not match: "
                    + existingEvent.name().getQualifier() + " <> " + event.name().getQualifier());
        }
    }

    private Map<String, DomainEvent> events = new HashMap<>();

    public Collection<DomainEvent> events() {
        return Collections.unmodifiableCollection(events.values());
    }

    public Optional<DomainEvent> event(String name) {
        return Optional.ofNullable(events.get(name));
    }

    public void replaceDomainEvent(DomainEvent event) {
        events.put(event.simpleName(), event);
    }

    public void addRunner(Runner runnerClass) {
        runners.put(runnerClass.className(), runnerClass);
    }

    private Map<String, Runner> runners = new HashMap<>();

    public void addModule(TypeComponent typeComponent) {
        modules.add(typeComponent);
    }

    private List<TypeComponent> modules = new ArrayList<>();

    public void addEntity(TypeComponent typeComponent) {
        entities.add(typeComponent);
    }

    private List<TypeComponent> entities = new ArrayList<>();

    public void addValueObject(TypeComponent typeComponent) {
        valueObjects.add(typeComponent);
    }

    private List<TypeComponent> valueObjects = new ArrayList<>();

    public void addService(TypeComponent typeComponent) {
        services.add(typeComponent);
    }

    private List<TypeComponent> services = new ArrayList<>();

    public void forget(String sourceId) {
        forget(sourceId, standaloneAggregateFactories.values());
        forget(sourceId, standaloneAggregateRoots.values());
        forget(sourceId, standaloneAggregateRepositories.values());
        forget(sourceId, aggregateContainers.values());
        forget(sourceId, processes.values());
        forget(sourceId, commands.values());
        forget(sourceId, events.values());
        forget(sourceId, runners.values());
        listeners.removeIf(listener -> listener.source().id().equals(sourceId));
        forgetTypeComponents(sourceId, modules);
        forgetTypeComponents(sourceId, entities);
        forgetTypeComponents(sourceId, valueObjects);
        forgetTypeComponents(sourceId, services);
    }

    private <T extends WithTypeComponent> void forget(
            String sourceId,
            Collection<T> components) {
        components.removeIf(component -> component.typeComponent().source().id().equals(sourceId));
    }

    private <T extends TypeComponent> void forgetTypeComponents(
            String sourceId,
            Collection<T> components) {
        components.removeIf(component -> component.source().id().equals(sourceId));
    }

    public SourceModel build() {
        var model = new SourceModel();
        modules.forEach(model::addModule);
        entities.forEach(model::addEntity);
        valueObjects.forEach(model::addValueObject);
        processes.values().forEach(model::addProcess);
        commands.values().forEach(model::addCommand);
        events.values().forEach(model::addEvent);
        services.forEach(model::addService);
        buildModelAggregates(model);
        listeners.forEach(model::addMessageListener);
        runners.values().forEach(model::addRunner);
        return model;
    }

    private void buildModelAggregates(SourceModel model) {
        initAggregateBuilders();
        for(MessageListener listener : listeners) {
            if(listener.isLinkedToAggregate()) {
                var aggregateName = listener.aggregateName();
                var builder = aggregates.get(aggregateName);
                if(builder == null) {
                    throw new IllegalStateException("Listener " + listener.methodName() + " refers to missing aggregate "
                            + aggregateName);
                }
                if(listener.container().type().isFactory()) {
                    builder.innerFactory(listener.container().type() == MessageListenerContainerType.INNER_FACTORY);
                } else if(listener.container().type().isRoot()) {
                    builder.innerRoot(listener.container().type() == MessageListenerContainerType.INNER_ROOT);
                } else if(listener.container().type().isRepository()) {
                    builder.innerRepository(listener.container().type() == MessageListenerContainerType.INNER_REPOSITORY);
                }
            }
        }

        for(Aggregate.Builder builder : aggregates.values()) {
            builder.ensureDefaultLocations();
        }

        aggregates.values().stream()
            .filter(Aggregate.Builder::isValid)
            .map(Aggregate.Builder::build)
            .forEach(model::addAggregate);
    }

    private void initAggregateBuilders() {
        aggregates.entrySet().removeIf(entry -> !entry.getValue().provided());

        for(StandaloneAggregateFactory factory : standaloneAggregateFactories.values()) {
            var aggregate = aggregates.computeIfAbsent(factory.aggregateName(), this::newBuilder);
            aggregate.innerFactory(false);
            aggregate.standaloneFactorySource(Optional.of(factory.typeComponent().source()));
        }

        for(StandaloneAggregateRoot root : standaloneAggregateRoots.values()) {
            var aggregate = aggregates.computeIfAbsent(root.aggregateName(), this::newBuilder);
            aggregate.innerRoot(false);
            aggregate.standaloneRootSource(Optional.of(root.typeComponent().source()));
            aggregate.documentation(root.typeComponent().documentation());
            aggregate.rootIdentifierClassName(root.identifierClassName());
            aggregate.rootReferences(root.typeComponent().references());
            aggregate.className(root.typeComponent().typeName());
        }

        for(StandaloneAggregateRepository repository : standaloneAggregateRepositories.values()) {
            var aggregate = aggregates.computeIfAbsent(repository.aggregateName(), this::newBuilder);
            aggregate.standaloneRepositorySource(Optional.of(repository.typeComponent().source()));
            aggregate.innerRepository(false);
        }

        for(AggregateContainer.Builder containerBuilder : aggregateContainers.values()) {
            var container = containerBuilder.build();
            var aggregate = aggregates.computeIfAbsent(container.aggregateName(), this::newBuilder);
            aggregate.containerSource(Optional.of(container.typeComponent().source()));
            aggregate.documentation(container.typeComponent().documentation());
            if(container.innerRoot().isPresent()) {
                aggregate.rootIdentifierClassName(container.identifierClassName());

                var innerRoot = container.innerRoot().orElseThrow();
                aggregate.rootReferences(innerRoot.references());
                aggregate.className(innerRoot.typeName());
            }
        }
    }

    private Aggregate.Builder newBuilder(String name) {
        var aggregateBuilder = new Aggregate.Builder();
        aggregateBuilder.name(name);
        return aggregateBuilder;
    }
}
