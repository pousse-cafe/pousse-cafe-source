package poussecafe.source;

import java.io.IOException;
import org.junit.Test;

public class AggregateDiscoveryTest extends DiscoveryTest {

    @Test
    public void findAggregates() throws IOException { // NOSONAR - assertions in ModelAssertions
        givenModelBuilder();
        whenIncludingTestModelTree();
        thenAggregatesFound();
    }

    private void thenAggregatesFound() {
        new ModelAssertions(model()).thenProcess1AggregatesFound();
    }
}
