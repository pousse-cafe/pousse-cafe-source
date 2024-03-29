package poussecafe.source.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.Test;
import poussecafe.source.PathSource;
import poussecafe.source.analysis.ClassName;
import poussecafe.source.analysis.SafeClassName;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ModelBuilderPersistenceTest {

    @Test
    public void serializable() throws IOException, ClassNotFoundException {
        givenModelBuilder();
        whenSerializeDeserialize();
        thenDeserializedModelMatchesExpected();
    }

    private void givenModelBuilder() {
        builder = new SourceModelBuilder();

        command1 = new Command.Builder()
                .name("Command1")
                .packageName("package.commands")
                .source(new PathSource(Path.of("package/commands/Command1.java")))
                .build();
        builder.replaceCommand(command1);

        event1 = new DomainEvent.Builder()
                .name("Event1")
                .packageName("package.events")
                .source(new PathSource(Path.of("package/events/Event1.java")))
                .build();
        builder.replaceDomainEvent(event1);

        process = new ProcessModel.Builder()
                .name("Process")
                .packageName("package.process")
                .source(new PathSource(Path.of("package/process/Process.java")))
                .build();
        builder.addProcess(process);

        listener = new MessageListener.Builder()
                .withConsumedMessage(new Message.Builder()
                        .name("Event1")
                        .type(MessageType.DOMAIN_EVENT)
                        .build())
                .withContainer(new MessageListenerContainer.Builder()
                        .aggregateName("Aggregate")
                        .containerIdentifier("Aggregate.Root")
                        .type(MessageListenerContainerType.INNER_ROOT)
                        .containerClass(new ClassName("package.Aggregate", "Root"))
                        .build())
                .withConsumesFromExternal(singletonList("ExternalSource"))
                .withMethodName("listener")
                .withProcessName("Process")
                .withRunnerClass(Optional.of("package.Runner"))
                .withSource(new PathSource(Path.of("package/Aggregate.java")))
                .build();
        builder.addMessageListener(listener);

        runner = new Runner.Builder()
                .withClassName("package.Runner")
                .withRunnerSource(new PathSource(Path.of("package/Runner.java")))
                .build();
        builder.addRunner(runner);

        var containerBuilder = new AggregateContainer.Builder()
                .typeComponent(new TypeComponent.Builder()
                        .name(SafeClassName.ofRootClass(new ClassName("package.Aggregate")))
                        .source(new PathSource(Path.of("package/Aggregate.java")))
                        .build())
                .innerRoot(new InnerAggregateRoot.Builder()
                        .name(new SafeClassName.Builder()
                                .rootClassName(new ClassName("package", "Aggregate"))
                                .appendPathElement("Root")
                                .build())
                        .build());
        expectedAggregateContainer = containerBuilder.build();
        builder.addAggregateContainer(containerBuilder);

        standaloneAggregateFactory = new StandaloneAggregateFactory.Builder()
                .typeComponent(new TypeComponent.Builder()
                        .name(SafeClassName.ofRootClass(new ClassName("package.Aggregate1Factory")))
                        .source(new PathSource(Path.of("package/Aggregate1Factory.java")))
                        .build())
                .build();
        builder.addStandaloneAggregateFactory(standaloneAggregateFactory);

        standaloneAggregateRoot = new StandaloneAggregateRoot.Builder()
                .typeComponent(new TypeComponent.Builder()
                        .name(SafeClassName.ofRootClass(new ClassName("package.Aggregate1Root")))
                        .source(new PathSource(Path.of("package/Aggregate1Root.java")))
                        .build())
                .build();
        builder.addStandaloneAggregateRoot(standaloneAggregateRoot);

        standaloneAggregateRepository = new StandaloneAggregateRepository.Builder()
                .typeComponent(new TypeComponent.Builder()
                        .name(SafeClassName.ofRootClass(new ClassName("package.Aggregate1Repository")))
                        .source(new PathSource(Path.of("package/Aggregate1Repository.java")))
                        .build())
                .build();
        builder.addStandaloneAggregateRepository(standaloneAggregateRepository);

        providedAggregate = new Aggregate.Builder();
        providedAggregate.containerSource(Optional.of(new PathSource(Path.of("package/ProvidedAggregate.java"))));
        providedAggregate.innerFactory(true);
        providedAggregate.innerRoot(true);
        providedAggregate.innerRepository(true);
        providedAggregate.name("ProvidedAggregate");
        providedAggregate.className(new SafeClassName.Builder()
                .rootClassName(new ClassName("package", "ProvidedAggregate"))
                .innerClassPath(singletonList("Root"))
                .build());
        providedAggregate.provided(true);
        builder.putAggregate(providedAggregate);

        builder.build();
    }

    private SourceModelBuilder builder;

    private Command command1;

    private DomainEvent event1;

    private ProcessModel process;

    private MessageListener listener;

    private Runner runner;

    private AggregateContainer expectedAggregateContainer;

    private StandaloneAggregateFactory standaloneAggregateFactory;

    private StandaloneAggregateRoot standaloneAggregateRoot;

    private StandaloneAggregateRepository standaloneAggregateRepository;

    private Aggregate.Builder providedAggregate;

    private void whenSerializeDeserialize() throws IOException, ClassNotFoundException {
        var bytes = serialize();
        deserializeAndBuildModel(bytes);
    }

    private ByteArrayOutputStream serialize() throws IOException {
        var bytes = new ByteArrayOutputStream();
        var oos = new ObjectOutputStream(bytes);
        oos.writeObject(builder);
        oos.close();
        return bytes;
    }

    private void deserializeAndBuildModel(ByteArrayOutputStream bytes) throws IOException, ClassNotFoundException {
        var ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        var builder2 = (SourceModelBuilder) ois.readObject();
        model = builder2.build();
    }

    private SourceModel model;

    private void thenDeserializedModelMatchesExpected() {
        assertThat(model.command("Command1").orElseThrow(), equalTo(command1));
        assertThat(model.event("Event1").orElseThrow(), equalTo(event1));
        assertThat(model.process("Process").orElseThrow(), equalTo(process));

        assertThat(model.aggregate("Aggregate").orElseThrow().containerSource().orElseThrow(), equalTo(expectedAggregateContainer.typeComponent().source()));

        assertThat(model.aggregateListeners("Aggregate").stream()
                .filter(listener -> listener.methodName().equals("listener"))
                .findFirst().orElseThrow(), equalTo(listener));

        assertThat(model.runner("package.Runner").orElseThrow(), equalTo(runner));

        assertThat(model.aggregate("Aggregate1").orElseThrow().standaloneFactorySource().orElseThrow(), equalTo(standaloneAggregateFactory.typeComponent().source()));
        assertThat(model.aggregate("Aggregate1").orElseThrow().standaloneRootSource().orElseThrow(), equalTo(standaloneAggregateRoot.typeComponent().source()));
        assertThat(model.aggregate("Aggregate1").orElseThrow().standaloneRepositorySource().orElseThrow(), equalTo(standaloneAggregateRepository.typeComponent().source()));
    }
}
