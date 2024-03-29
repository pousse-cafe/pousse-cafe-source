package poussecafe.source.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import poussecafe.source.ModuleResolver;
import poussecafe.source.analysis.SafeClassName;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public class SourceModel {

    void addAggregate(Aggregate source) {
        aggregates.put(source.name(), source);
    }

    private Map<String, Aggregate> aggregates = new HashMap<>();

    public Optional<Aggregate> aggregate(String name) {
        return Optional.ofNullable(aggregates.get(name));
    }

    void addProcess(ProcessModel source) {
        String name = source.simpleName();
        if(!processes.containsKey(name)) {
            processes.put(name, source);
        }
    }

    private Map<String, ProcessModel> processes = new HashMap<>();

    public Optional<ProcessModel> process(String name) {
        return Optional.ofNullable(processes.get(name));
    }

    public Collection<ProcessModel> processes() {
        return Collections.unmodifiableCollection(processes.values());
    }

    public Stream<ProcessModel> moduleProcesses(TypeComponent module) {
        var modulePackage = module.typeName().asName().qualifier();
        return processes.values().stream()
                .filter(service -> service.name().qualifier().startsWith(modulePackage));
    }

    void addMessageListener(MessageListener source) {
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

    public Stream<MessageListener> moduleMessageListeners(TypeComponent module) {
        var modulePackage = module.typeName().asName().qualifier();
        return listeners.stream()
                .filter(aggregate -> aggregate.container().containerClass().qualifier().startsWith(modulePackage));
    }

    void addCommand(Command command) {
        var existingCommand = commands.get(command.simpleName());
        if(existingCommand != null
                && !existingCommand.name().equals(command.name())) {
            throw new IllegalArgumentException("A command with this name already exists but qualifiers do not match: "
                    + existingCommand.name().getQualifier() + " <> " + command.name().getQualifier());
        }
        commands.put(command.simpleName(), command);
    }

    private Map<String, Command> commands = new HashMap<>();

    public Collection<Command> commands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    public Optional<Command> command(String name) {
        return Optional.ofNullable(commands.get(name));
    }

    void addEvent(DomainEvent event) {
        var existingEvent = events.get(event.simpleName());
        if(existingEvent != null
                && !existingEvent.name().equals(event.name())) {
            throw new IllegalArgumentException("An event with this name already exists but qualifiers do not match: "
                    + existingEvent.name().getQualifier() + " <> " + event.name().getQualifier());
        }
        events.put(event.simpleName(), event);
    }

    private Map<String, DomainEvent> events = new HashMap<>();

    public Collection<DomainEvent> events() {
        return Collections.unmodifiableCollection(events.values());
    }

    public Optional<DomainEvent> event(String name) {
        return Optional.ofNullable(events.get(name));
    }

    public Collection<Aggregate> aggregates() {
        return Collections.unmodifiableCollection(aggregates.values());
    }

    void addRunner(Runner runner) {
        runners.put(runner.className(), runner);
    }

    private Map<String, Runner> runners = new HashMap<>();

    public Optional<Runner> runner(String className) {
        return Optional.ofNullable(runners.get(className));
    }

    void addModule(TypeComponent module) {
        modules.add(module);
    }

    private List<TypeComponent> modules = new ArrayList<>();

    public List<TypeComponent> modules() {
        return Collections.unmodifiableList(modules);
    }

    void addEntity(TypeComponent entity) {
        entities.add(entity);
    }

    private List<TypeComponent> entities = new ArrayList<>();

    public List<TypeComponent> entities() {
        return Collections.unmodifiableList(entities);
    }

    void addValueObject(TypeComponent valueObject) {
        valueObjects.add(valueObject);
    }

    private List<TypeComponent> valueObjects = new ArrayList<>();

    public List<TypeComponent> valueObjects() {
        return Collections.unmodifiableList(valueObjects);
    }

    public Stream<Aggregate> moduleAggregates(TypeComponent module) {
        var modulePackage = module.typeName().asName().qualifier();
        return aggregates.values().stream()
                .filter(aggregate -> aggregate.packageName().startsWith(modulePackage));
    }

    void addService(TypeComponent service) {
        services.add(service);
    }

    private List<TypeComponent> services = new ArrayList<>();

    public Collection<TypeComponent> services() {
        return Collections.unmodifiableCollection(services);
    }

    public Stream<TypeComponent> moduleServices(TypeComponent module) {
        var modulePackage = module.typeName().asName().qualifier();
        return services.stream()
                .filter(service -> service.typeName().rootClassName().qualifier().startsWith(modulePackage));
    }

    /**
     * Fixing package names implies the copy of all components of a given model but keeping
     * the package names as defined in this model. Only the components from given model are kept in the
     * result.
     *
     * @param newModel The new model to fix with current model.
     *
     * @return A new Model instance being the result of fixing given model if needed with this one.
     */
    public SourceModel fixPackageNames(SourceModel newModel) {
        var fixedModel = new SourceModel();
        newModel.events.values().stream().map(this::fixEvent).forEach(fixedModel::addEvent);
        newModel.commands.values().stream().map(this::fixCommand).forEach(fixedModel::addCommand);
        newModel.processes.values().stream().map(this::fixProcess).forEach(fixedModel::addProcess);
        newModel.aggregates.values().stream().map(this::fixAggregate).forEach(fixedModel::addAggregate);
        fixedModel.listeners.addAll(newModel.listeners);
        return fixedModel;
    }

    private DomainEvent fixEvent(DomainEvent event) {
        var thisEvent = events.get(event.name);
        if(thisEvent != null) {
            return thisEvent;
        } else {
            return event;
        }
    }

    private Command fixCommand(Command command) {
        var thisCommand = commands.get(command.name);
        if(thisCommand != null) {
            return thisCommand;
        } else {
            return command;
        }
    }

    private ProcessModel fixProcess(ProcessModel process) {
        var thisProcess = processes.get(process.name);
        if(thisProcess != null) {
            return thisProcess;
        } else {
            return process;
        }
    }

    private Aggregate fixAggregate(Aggregate aggregate) {
        var thisAggregate = aggregates.get(aggregate.name());
        if(thisAggregate != null) {
            return new Aggregate.Builder()
                    .startingFrom(aggregate)
                    .className(aggregate.className().moveToPackage(thisAggregate.packageName()))
                    .build();
        } else {
            return aggregate;
        }
    }

    public ModuleResolver moduleResolver() {
        return new ModuleResolver(modules.stream()
                .map(TypeComponent::typeName)
                .map(SafeClassName::asName)
                .collect(toList()));
    }
}
