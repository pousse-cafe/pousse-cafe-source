package poussecafe.source.generation;

import java.util.Arrays;
import org.junit.Test;
import poussecafe.source.analysis.ClassName;
import poussecafe.source.analysis.SafeClassName;
import poussecafe.source.model.Aggregate;

import static java.util.stream.Collectors.joining;

public abstract class GeneratorTest extends GenerationTest {

    @Test
    public void newAggregate() { // NOSONAR - add assert - see parent
        givenCoreGenerator();
        givenStorageGenerator();
        givenAggregate();
        whenGeneratingCode();
        thenGeneratedCodeMatchesExpected();
    }

    protected void givenAggregate() {
        aggregate = new Aggregate.Builder()
                .name("MyAggregate")
                .className(new SafeClassName.Builder()
                        .rootClassName(new ClassName(Arrays.stream(
                                packageNameSegments()).collect(joining(".")), "MyAggregate"))
                        .build())
                .ensureDefaultLocations()
                .build();
    }

    private Aggregate aggregate;

    @Override
    protected Aggregate aggregate() {
        return aggregate;
    }

    @Override
    protected void whenGeneratingCoreCode() {
        generator().generate(aggregate);
    }

    @Test
    public void updateExistingAggregate() { // NOSONAR - add assert - see parent
        givenCoreGenerator();
        givenStorageGenerator();
        givenAggregate();
        givenExisingCode();
        whenGeneratingCode();
        thenGeneratedCodeMatchesExpected();
    }
}
