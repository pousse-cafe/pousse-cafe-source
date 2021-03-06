package poussecafe.source.emil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.Test;
import poussecafe.source.DiscoveryTest;
import poussecafe.source.analysis.ClassLoaderClassResolver;
import poussecafe.source.analysis.SourceModelBuilder;
import poussecafe.source.model.SourceModel;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class EmilExporterTest {

    @Test
    public void exportProcess1() throws IOException, URISyntaxException {
        givenModel(DiscoveryTest.testModelDirectory);
        whenExporting(Optional.of("Process1"));
        thenEmilIsExpected("Process1.emil");
    }

    private void givenModel(Path treePath) throws IOException {
        var builder = new SourceModelBuilder(new ClassLoaderClassResolver());
        builder.includeTree(treePath);
        model = builder.build();
    }

    private SourceModel model;

    private void whenExporting(Optional<String> process) {
        exported = new EmilExporter.Builder()
                .model(model)
                .processName(process)
                .build()
                .toEmil();
    }

    private String exported;

    private void thenEmilIsExpected(String resourceName) throws IOException, URISyntaxException {
        assertThat(exported, equalTo(readResource(resourceName)));
    }

    private String readResource(String string) throws IOException, URISyntaxException {
        return new String(Files.readString(Path.of(this.getClass().getResource("/" + string).toURI())));
    }

    @Test
    public void exportAll() throws IOException, URISyntaxException {
        givenModel(DiscoveryTest.testModelDirectory);
        whenExporting(Optional.empty());
        thenEmilIsExpected("all.emil");
    }

    @Test // Regression test for bug https://github.com/pousse-cafe/pousse-cafe/issues/214
    public void exportP214() throws IOException, URISyntaxException {
        givenModel(p214Directory);
        whenExporting(Optional.of("P214"));
        thenEmilIsExpected("P214.emil");
    }

    private static final Path p214Directory = Path.of("", "src", "test", "java", "poussecafe", "source", "models", "p214");
}
