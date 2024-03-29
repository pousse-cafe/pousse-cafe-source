package poussecafe.source.analysis;

import java.io.Serializable;
import java.util.List;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import poussecafe.source.WithPersistableState;
import poussecafe.source.model.AggregateContainer;
import poussecafe.source.model.Command;
import poussecafe.source.model.DomainEvent;
import poussecafe.source.model.InnerAggregateRoot;
import poussecafe.source.model.Message;
import poussecafe.source.model.MessageListener;
import poussecafe.source.model.MessageListenerContainer;
import poussecafe.source.model.MessageListenerContainerType;
import poussecafe.source.model.MessageType;
import poussecafe.source.model.ProcessModel;
import poussecafe.source.model.ProducedEvent;
import poussecafe.source.model.Runner;
import poussecafe.source.model.SourceModel;
import poussecafe.source.model.SourceModelBuilder;
import poussecafe.source.model.StandaloneAggregateFactory;
import poussecafe.source.model.StandaloneAggregateRepository;
import poussecafe.source.model.StandaloneAggregateRoot;
import poussecafe.source.model.TypeComponent;

import static java.util.stream.Collectors.toList;

public class SourceModelBuilderVisitor implements ResolvedCompilationUnitVisitor, WithPersistableState {

    @Override
    public boolean visit(ResolvedCompilationUnit unit) {
        compilationUnit = unit;
        return false;
    }

    private int typeLevel = -1;

    private ResolvedCompilationUnit compilationUnit;

    @Override
    public boolean visit(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        ++typeLevel;
        if(AggregateRootClass.isAggregateRoot(resolvedTypeDeclaration)) {
            visitAggregateRoot(resolvedTypeDeclaration);
            return true;
        } else if(FactoryClass.isFactory(resolvedTypeDeclaration)) {
            visitFactory(resolvedTypeDeclaration);
            return true;
        } else if(RepositoryClass.isRepository(resolvedTypeDeclaration)) {
            visitRepository(resolvedTypeDeclaration);
            return true;
        } else if(ProcessDefinitionType.isProcessDefinition(resolvedTypeDeclaration)) {
            visitProcessDefinition(resolvedTypeDeclaration);
            return false;
        } else if(AggregateContainerClass.isAggregateContainerClass(resolvedTypeDeclaration)) {
            visitAggregateContainer(resolvedTypeDeclaration);
            return true;
        } else if(RunnerClass.isRunner(resolvedTypeDeclaration)) {
            visitRunner(resolvedTypeDeclaration);
            return false;
        } else if(ModuleClass.isModule(resolvedTypeDeclaration)) {
            visitModule(resolvedTypeDeclaration);
            return false;
        } else if(EntityClass.isEntity(resolvedTypeDeclaration)) {
            visitEntity(resolvedTypeDeclaration);
            return false;
        } else if(ValueObjectClass.isValueObject(resolvedTypeDeclaration)) {
            visitValueObject(resolvedTypeDeclaration);
            return false;
        } else if(ServiceClass.isService(resolvedTypeDeclaration)) {
            visitService(resolvedTypeDeclaration);
            return false;
        } else if(MessageDefinitionType.isMessageDefinition(resolvedTypeDeclaration)) {
            visitMessageDefinition(resolvedTypeDeclaration);
            return false;
        } else {
            return false;
        }
    }

    private void visitAggregateRoot(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        AggregateRootClass aggregateRootClass = new AggregateRootClass(resolvedTypeDeclaration);
        containerLevel = typeLevel;
        String identifier;
        String aggregateName;
        if(typeLevel == 0) {
            aggregateName = aggregateRootClass.aggregateName();
            identifier = resolvedTypeDeclaration.name().simpleName();
            createStandaloneAggregateRoot(aggregateRootClass);
        } else {
            aggregateName = aggregateNameForInnerClass(resolvedTypeDeclaration);
            identifier = innerClassQualifiedName(resolvedTypeDeclaration);
            createInnerAggregateRoot(aggregateName, aggregateRootClass);
        }
        container = new MessageListenerContainer.Builder()
                .type(typeLevel == 0 ? MessageListenerContainerType.STANDALONE_ROOT : MessageListenerContainerType.INNER_ROOT)
                .aggregateName(aggregateName)
                .containerIdentifier(identifier)
                .containerClass(resolvedTypeDeclaration.name().name())
                .build();
    }

    private void createStandaloneAggregateRoot(AggregateRootClass aggregateRootClass) {
        modelBuilder.addStandaloneAggregateRoot(new StandaloneAggregateRoot.Builder()
                .typeComponent(typeComponent(aggregateRootClass.typeDeclaration()))
                .identifierClassName(aggregateRootClass.identifierClassName())
                .build());
    }

    private TypeComponent typeComponent(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        var referencesDiscovery = new TypeReferencesDiscovery(resolvedTypeDeclaration);
        return new TypeComponent.Builder()
                .source(compilationUnit.sourceFile())
                .name(resolvedTypeDeclaration.unresolvedName())
                .documentation(resolvedTypeDeclaration.documentation())
                .references(referencesDiscovery.references())
                .build();
    }

    private void createInnerAggregateRoot(String aggregateName, AggregateRootClass aggregateRootClass) {
        var referencesDiscovery = new TypeReferencesDiscovery(aggregateRootClass.typeDeclaration());
        modelBuilder.addInnerAggregateRoot(aggregateName, new InnerAggregateRoot.Builder()
                .name(aggregateRootClass.typeDeclaration().unresolvedName())
                .references(referencesDiscovery.references())
                .identifierClassName(aggregateRootClass.identifierClassName())
                .build());
    }

    private void visitProcessDefinition(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        var processDefinition = new ProcessDefinitionType(resolvedTypeDeclaration);
        modelBuilder.addProcess(new ProcessModel.Builder()
                .name(processDefinition.processName())
                .packageName(compilationUnit.packageName())
                .source(compilationUnit.sourceFile())
                .documentation(resolvedTypeDeclaration.documentation())
                .build());
    }

    private void visitFactory(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        FactoryClass factoryClass = new FactoryClass(resolvedTypeDeclaration);
        containerLevel = typeLevel;
        String identifier;
        String aggregateName;
        if(typeLevel == 0) {
            aggregateName = factoryClass.aggregateName();
            identifier = factoryClass.simpleName();
        } else {
            aggregateName = aggregateNameForInnerClass(resolvedTypeDeclaration);
            identifier = innerClassQualifiedName(resolvedTypeDeclaration);
        }
        if(typeLevel == 0) {
            modelBuilder.addStandaloneAggregateFactory(new StandaloneAggregateFactory.Builder()
                    .typeComponent(typeComponent(resolvedTypeDeclaration))
                    .build());
        }
        container = new MessageListenerContainer.Builder()
                .type(typeLevel == 0 ? MessageListenerContainerType.STANDALONE_FACTORY : MessageListenerContainerType.INNER_FACTORY)
                .aggregateName(aggregateName)
                .containerIdentifier(identifier)
                .containerClass(resolvedTypeDeclaration.name().name())
                .build();
    }

    private void visitRepository(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        RepositoryClass repositoryClass = new RepositoryClass(resolvedTypeDeclaration);
        containerLevel = typeLevel;
        String identifier;
        String aggregateName;
        if(typeLevel == 0) {
            aggregateName = repositoryClass.aggregateName();
            identifier = repositoryClass.simpleName();
        } else {
            aggregateName = aggregateNameForInnerClass(resolvedTypeDeclaration);
            identifier = innerClassQualifiedName(resolvedTypeDeclaration);
        }
        if(typeLevel == 0) {
            modelBuilder.addStandaloneAggregateRepository(new StandaloneAggregateRepository.Builder()
                    .typeComponent(typeComponent(resolvedTypeDeclaration))
                    .build());
        }
        container = new MessageListenerContainer.Builder()
                .type(typeLevel == 0 ? MessageListenerContainerType.STANDALONE_REPOSITORY : MessageListenerContainerType.INNER_REPOSITORY)
                .aggregateName(aggregateName)
                .containerIdentifier(identifier)
                .containerClass(resolvedTypeDeclaration.name().name())
                .build();
    }

    private int containerLevel;

    private MessageListenerContainer container;

    private String aggregateNameForInnerClass(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        var typeDeclaration = (TypeDeclaration) resolvedTypeDeclaration.typeDeclaration().getParent();
        return typeDeclaration.getName().getIdentifier();
    }

    private String innerClassQualifiedName(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        return innerClassQualifiedName(resolvedTypeDeclaration.typeDeclaration());
    }

    private String innerClassQualifiedName(TypeDeclaration typeDeclaration) {
        var parent = typeDeclaration.getParent();
        if(parent instanceof CompilationUnit) {
            return typeDeclaration.getName().getIdentifier();
        } else {
            return innerClassQualifiedName((TypeDeclaration) parent) + "." + typeDeclaration.getName().getIdentifier();
        }
    }

    private void visitAggregateContainer(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        var containerClass = new AggregateContainerClass(resolvedTypeDeclaration);
        modelBuilder.addAggregateContainer(new AggregateContainer.Builder()
                .typeComponent(typeComponent(resolvedTypeDeclaration))
                .identifierClassName(containerClass.identifierClassName()));
    }

    private void visitModule(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        modelBuilder.addModule(typeComponent(resolvedTypeDeclaration));
    }

    private void visitEntity(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        modelBuilder.addEntity(typeComponent(resolvedTypeDeclaration));
    }

    private void visitValueObject(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        modelBuilder.addValueObject(typeComponent(resolvedTypeDeclaration));
    }

    private void visitService(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        modelBuilder.addService(typeComponent(resolvedTypeDeclaration));
    }

    private void visitMessageDefinition(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        if(MessageDefinitionType.isCommand(resolvedTypeDeclaration)) {
            modelBuilder.replaceCommand(new Command.Builder()
                    .name(resolvedTypeDeclaration.unresolvedName().simpleName())
                    .packageName(resolvedTypeDeclaration.unresolvedName().asName().qualifier())
                    .source(compilationUnit.sourceFile())
                    .documentation(resolvedTypeDeclaration.documentation())
                    .build());
        } else if(MessageDefinitionType.isDomainEvent(resolvedTypeDeclaration)) {
            modelBuilder.replaceDomainEvent(new DomainEvent.Builder()
                    .name(resolvedTypeDeclaration.unresolvedName().simpleName())
                    .packageName(resolvedTypeDeclaration.unresolvedName().asName().qualifier())
                    .source(compilationUnit.sourceFile())
                    .documentation(resolvedTypeDeclaration.documentation())
                    .build());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void endVisit(ResolvedTypeDeclaration node) {
        if(typeLevel == containerLevel || typeLevel == 0) {
            container = null;
        }
        --typeLevel;
    }

    @Override
    public boolean visit(ResolvedEnumDeclaration resolvedEnumDeclaration) {
        visitValueObject(resolvedEnumDeclaration);
        return false;
    }

    private void visitValueObject(ResolvedEnumDeclaration resolvedEnumDeclaration) {
        modelBuilder.addValueObject(typeComponent(resolvedEnumDeclaration));
    }

    private TypeComponent typeComponent(ResolvedEnumDeclaration resolvedEnumDeclaration) {
        return new TypeComponent.Builder()
                .source(compilationUnit.sourceFile())
                .name(resolvedEnumDeclaration.unresolvedName())
                .documentation(resolvedEnumDeclaration.documentation())
                .build();
    }

    private SourceModelBuilder modelBuilder = new SourceModelBuilder();

    private void visitRunner(ResolvedTypeDeclaration resolvedTypeDeclaration) {
        var runnerClass = new RunnerClass(resolvedTypeDeclaration);
        modelBuilder.addRunner(new Runner.Builder()
                .withRunnerSource(compilationUnit.sourceFile())
                .withClassName(runnerClass.className())
                .build());
    }

    @Override
    public boolean visit(ResolvedMethod method) {
        if(container != null) {
            var annotatedMethod = method.asAnnotatedElement();
            if(MessageListenerMethod.isMessageListener(method)) {
                var listenerMethod = new MessageListenerMethod(method);
                var messageListener = new MessageListener.Builder()
                        .withContainer(container)
                        .withSource(compilationUnit.sourceFile())
                        .withMethodDeclaration(listenerMethod)
                        .build();
                modelBuilder.addMessageListener(messageListener);

                registerMessage(messageListener.consumedMessage(),
                        method.parameterTypeName(0).orElseThrow());
                registerMessages(messageListener.producedEvents(), annotatedMethod);
            }
        }
        return false;
    }

    private void registerMessage(Message message, ResolvedTypeName messageTypeName) {
        if(message.type() == MessageType.COMMAND) {
            modelBuilder.addCommandIfAbsent(new Command.Builder()
                    .name(message.name())
                    .packageName(messageTypeName.packageName())
                    .source(messageTypeName.resolvedClass().source())
                    .build());
        } else if(message.type() == MessageType.DOMAIN_EVENT) {
            modelBuilder.addEventIfAbsent(new DomainEvent.Builder()
                    .name(message.name())
                    .packageName(messageTypeName.packageName())
                    .source(messageTypeName.resolvedClass().source())
                    .build());
        } else {
            throw new UnsupportedOperationException("Unsupported message type " + message.type());
        }
    }

    private void registerMessages(List<ProducedEvent> producedEvents,
            AnnotatedElement<MethodDeclaration> method) {
        var producedEventAnnotations = method.findAnnotations(
                CompilationUnitResolver.PRODUCES_EVENT_ANNOTATION_CLASS).stream()
                .map(ProducedEventAnnotation::new)
                .map(ProducedEventAnnotation::event)
                .collect(toList());
        for(int i = 0; i < producedEvents.size(); ++i) {
            var producedEvent = producedEvents.get(i);
            var message = producedEvent.message();
            var eventType = producedEventAnnotations.get(i);
            registerMessage(message, eventType);
        }
    }

    public SourceModel buildModel() {
        return modelBuilder.build();
    }

    @Override
    public void forget(String sourceId) {
        modelBuilder.forget(sourceId);
    }

    @Override
    public Serializable getSerializableState() {
        return modelBuilder;
    }

    @Override
    public void loadSerializedState(Serializable state) {
        modelBuilder = (SourceModelBuilder) state;
    }
}
