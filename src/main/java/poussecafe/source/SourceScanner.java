package poussecafe.source;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poussecafe.source.analysis.TypeResolvingCompilationUnitVisitor;

public class SourceScanner implements SourceConsumer {

    @Override
    public void includeFile(Path sourceFilePath) throws IOException {
        includeSource(new PathSource(sourceFilePath));
    }

    @Override
    public void includeSource(Source source) {
        String sourceId = source.id();
        forget(sourceId);

        CompilationUnit unit = source.compilationUnit();
        if(unit.getMessages().length > 0) {
            logger.warn("Skipping {} because of compilation issues", sourceId);
            for(int i = 0; i < unit.getMessages().length; ++i) {
                Message message = unit.getMessages()[i];
                logger.warn("Line {}: {}", unit.getLineNumber(message.getStartPosition()), message.getMessage());
            }
        } else if(unit.types().size() != 1) {
            logger.debug("Skipping {} because it does not contain a single type", sourceId);
        } else {
            typeResolvingVisitor.visit(source);
            if(!typeResolvingVisitor.errors().isEmpty()) {
                logger.error("Visitor errors:");
                for(Exception e : typeResolvingVisitor.errors()) {
                    logger.error("-", e);
                }
                throw new IllegalStateException("Error while scanning source " + sourceId);
            }
        }
    }

    public void forget(String sourceId) {
        typeResolvingVisitor.forget(sourceId);
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void includeTree(Path sourceDirectory) throws IOException {
        if(sourceDirectory.toFile().isDirectory()) {
            Files.walkFileTree(sourceDirectory, new JavaSourceFileVisitor());
        } else {
            try (var fs = FileSystems.newFileSystem(sourceDirectory)) {
                Files.walkFileTree(fs.getPath("/"), new JavaSourceFileVisitor());
            }
        }
    }

    private class JavaSourceFileVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if(file.toString().endsWith(".java")) {
                includeFile(file);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    public SourceScanner(TypeResolvingCompilationUnitVisitor typeResolvingVisitor) {
        this.typeResolvingVisitor = typeResolvingVisitor;
    }

    private TypeResolvingCompilationUnitVisitor typeResolvingVisitor;
}
