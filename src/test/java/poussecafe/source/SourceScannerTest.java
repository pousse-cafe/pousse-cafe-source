package poussecafe.source;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Test;
import org.mockito.Mockito;

import poussecafe.source.analysis.TypeResolvingCompilationUnitVisitor;

public class SourceScannerTest {

    @Test
    public void walksSourceDir() throws IOException {
        var mock = Mockito.mock(TypeResolvingCompilationUnitVisitor.class);
        var scanner = new SourceScanner(mock);
        scanner.includeTree(Path.of("src", "test", "java", "poussecafe", "source", "tree"));
        verify(mock, times(1)).visit(any(PathSource.class));
    }

    @Test
    public void walksJar() throws IOException {
        var mock = Mockito.mock(TypeResolvingCompilationUnitVisitor.class);
        var scanner = new SourceScanner(mock);
        scanner.includeTree(Path.of("src", "test", "resources", "sources.jar"));
        verify(mock, times(4)).visit(any(PathSource.class));
    }
}
