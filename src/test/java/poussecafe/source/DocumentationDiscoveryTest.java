package poussecafe.source;

import java.io.IOException;
import java.util.Optional;
import org.junit.Test;
import poussecafe.source.analysis.ClassName;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

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
        assertTrue(valueObject.documentation().isPresent());
        assertThat(valueObject.documentation().orElseThrow(), equalTo("ValueObject1 documentation."));
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
        assertTrue(aggregate1.documentation().isPresent());
        assertThat(aggregate1.documentation().orElseThrow(), equalTo("Aggregate1 documentation."));
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
        assertTrue(entity.documentation().isPresent());
        assertThat(entity.documentation().orElseThrow(), equalTo("Entity1 documentation."));
    }

    @Test
    public void processHasExpectedDocumentation() throws IOException {
        givenModelBuilder();
        whenIncludingTestModelTree();
        thenProcessHasExpectedDocumentation();
    }

    private void thenProcessHasExpectedDocumentation() {
        var model = model();
        var entity = model.processes().stream()
                .filter(item -> item.typeComponent().typeName().simpleName().equals("Process1"))
                .findFirst().orElseThrow();
        assertTrue(entity.typeComponent().documentation().isPresent());
        assertThat(entity.typeComponent().documentation().orElseThrow(), equalTo("Process1 documentation."));
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
        assertTrue(command.typeComponent().documentation().isPresent());
        assertThat(command.typeComponent().documentation().orElseThrow(), equalTo("Command1 documentation."));
    }

    @Test
    public void domainEventHasExpectedDocumentation() throws IOException {
        givenModelBuilder();
        whenIncludingTestModelTree();
        thenDomainEventHasExpectedDocumentation();
    }

    private void thenDomainEventHasExpectedDocumentation() {
        var model = model();
        var command = model.events().stream()
                .filter(item -> item.typeComponent().typeName().simpleName().equals("Event1"))
                .findFirst().orElseThrow();
        assertTrue(command.typeComponent().documentation().isPresent());
        assertThat(command.typeComponent().documentation().orElseThrow(), equalTo("Event1 documentation."));
    }
}
