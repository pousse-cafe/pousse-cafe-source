package poussecafe.source;

import java.io.IOException;
import java.util.Optional;
import org.junit.Test;
import poussecafe.source.analysis.ClassName;
import poussecafe.source.model.Documentation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class DocumentationDiscoveryTest extends DiscoveryTest {

    @Test
    public void valueObjectHasExpectedDocumentation() throws IOException {
        givenModelBuilder();
        whenIncludingTestModelTree();
        thenValueObjectHasExpectedDocumentation();
    }

    private void thenValueObjectHasExpectedDocumentation() {
        var model = model();
        var valueObject = model.valueObjects().stream()
                .filter(item -> item.typeName().simpleName().equals("ValueObject1"))
                .findFirst().orElseThrow();
        assertThat(valueObject.documented(), equalTo(new Documentation.Builder()
                .description("ValueObject1 documentation.")
                .shortDescription("ValueObject1 short")
                .build()));
        assertThat(valueObject.typeName().qualifiedName(), equalTo(basePackage() + ".model.aggregate1.ValueObject1"));
    }

    @Test
    public void aggregatesHaveExpectedDocumentation() throws IOException {
        givenModelBuilder();
        whenIncludingTestModelTree();
        thenAggregatesHaveExpectedDocumentation();
    }

    private void thenAggregatesHaveExpectedDocumentation() {
        var model = model();

        var aggregate1 = model.aggregates().stream()
                .filter(item -> item.simpleName().equals("Aggregate1"))
                .findFirst().orElseThrow();
        assertThat(aggregate1.documentation(), equalTo(new Documentation.Builder()
                .description("Aggregate1 documentation.")
                .shortDescription("Aggregate1 short")
                .build()));
        ClassName identifierClassName = new ClassName(basePackage() + ".model.aggregate1.Identifier1");
        assertThat(aggregate1.identifierClassName(), equalTo(Optional.of(identifierClassName)));

        var aggregate2 = model.aggregates().stream()
                .filter(item -> item.simpleName().equals("Aggregate2"))
                .findFirst().orElseThrow();
        ClassName identifier2ClassName = new ClassName(basePackage() + ".model.aggregate2.Identifier2");
        assertThat(aggregate2.identifierClassName(), equalTo(Optional.of(identifier2ClassName)));
    }

    @Test
    public void entityHasExpectedDocumentation() throws IOException {
        givenModelBuilder();
        whenIncludingTestModelTree();
        thenEntityHasExpectedDocumentation();
    }

    private void thenEntityHasExpectedDocumentation() {
        var model = model();
        var entity = model.entities().stream()
                .filter(item -> item.typeName().simpleName().equals("Entity1"))
                .findFirst().orElseThrow();
        assertThat(entity.documented(), equalTo(new Documentation.Builder()
                .description("Entity1 documentation.")
                .shortDescription("Entity1 short")
                .build()));
    }

    @Test
    public void processHasExpectedDocumentation() throws IOException {
        givenModelBuilder();
        whenIncludingTestModelTree();
        thenProcessHasExpectedDocumentation();
    }

    private void thenProcessHasExpectedDocumentation() {
        var model = model();
        var process = model.processes().stream()
                .filter(item -> item.typeComponent().typeName().simpleName().equals("Process1"))
                .findFirst().orElseThrow();
        assertThat(process.documentation(), equalTo(new Documentation.Builder()
                .description("Process1 documentation.")
                .shortDescription("Process1 short")
                .build()));
    }

    @Test
    public void commandHasExpectedDocumentation() throws IOException {
        givenModelBuilder();
        whenIncludingTestModelTree();
        thenCommandHasExpectedDocumentation();
    }

    private void thenCommandHasExpectedDocumentation() {
        var model = model();
        var command = model.commands().stream()
                .filter(item -> item.typeComponent().typeName().simpleName().equals("Command1"))
                .findFirst().orElseThrow();
        assertThat(command.documentation(), equalTo(new Documentation.Builder()
                .description("Command1 documentation.")
                .shortDescription("Command1 short")
                .build()));
    }

    @Test
    public void domainEventHasExpectedDocumentation() throws IOException {
        givenModelBuilder();
        whenIncludingTestModelTree();
        thenDomainEventHasExpectedDocumentation();
    }

    private void thenDomainEventHasExpectedDocumentation() {
        var model = model();
        var event = model.events().stream()
                .filter(item -> item.typeComponent().typeName().simpleName().equals("Event1"))
                .findFirst().orElseThrow();
        assertThat(event.documentation(), equalTo(new Documentation.Builder()
                .description("Event1 documentation.")
                .shortDescription("Event1 short")
                .build()));
    }
}
