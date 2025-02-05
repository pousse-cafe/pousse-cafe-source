package poussecafe.source;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;

@SuppressWarnings("serial")
public class PathSource extends Source implements Serializable {

    @Override
    public void configure(ASTParser parser) {
        parser.setSource(readAllChars().toCharArray());
        var options = JavaCore.getOptions(); // NOSONAR
        JavaCore.setComplianceOptions(JavaCore.VERSION_11, options);
        parser.setCompilerOptions(options);
    }

    private String readAllChars() {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read path content", e);
        }
    }

    public PathSource(Path path) {
        super(path.toString());
        this.path = path;
    }

    private transient Path path;
}
