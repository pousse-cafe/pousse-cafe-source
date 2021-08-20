package poussecafe.source;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import poussecafe.source.analysis.ClassLoaderClassResolver;
import poussecafe.source.analysis.ResolvedCompilationUnit;
import poussecafe.source.analysis.ResolvedCompilationUnitVisitor;
import poussecafe.source.analysis.ResolvedTypeDeclaration;
import poussecafe.source.analysis.SourceModelBuilder;
import poussecafe.source.model.SourceModel;

public abstract class DiscoveryTest {

    protected void givenModelBuilder() {
        modelBuilder = new SourceModelBuilder(new ClassLoaderClassResolver());
    }

    private SourceModelBuilder modelBuilder;

    protected void whenIncludingTree(Path sourceTreePath) throws IOException {
        modelBuilder.includeTree(sourceTreePath);
    }

    protected void whenIncludingTestModelTree() throws IOException {
        whenIncludingTree(testModelDirectory);
    }

    public static final Path testModelDirectory = Path.of("", "src", "test", "java", "poussecafe", "source", "testmodel");

    protected SourceModel model() {
        return modelBuilder.build();
    }

    protected String basePackage() {
        return "poussecafe.source.testmodel";
    }

    public static ResolvedTypeDeclaration resolveTypeDeclaration(Path testModelFile) throws IOException {
        return resolveTypeDeclaration(testModelFile, typeDeclarationReference -> new ResolvedCompilationUnitVisitor() {
            @Override
            public boolean visit(ResolvedCompilationUnit unit) {
                return true;
            }

            @Override
            public boolean visit(ResolvedTypeDeclaration type) {
                typeDeclarationReference.set(type);
                return false;
            }
        });
    }

    private static ResolvedTypeDeclaration resolveTypeDeclaration(Path testModelFile,
            Function<AtomicReference<ResolvedTypeDeclaration>, ResolvedCompilationUnitVisitor> visitor) throws IOException {
        var typeDeclarationReference = new AtomicReference<ResolvedTypeDeclaration>();
        var modelBuilder = new SingleVisitorScanner(new ClassLoaderClassResolver()) {
            @Override
            protected ResolvedCompilationUnitVisitor visitor() {
                return visitor.apply(typeDeclarationReference);
            }
        };
        modelBuilder.includeFile(DiscoveryTest.testModelDirectory.resolve(testModelFile));
        return typeDeclarationReference.get();
    }

    public static ResolvedTypeDeclaration resolveTypeDeclaration(Path testModelFile, String innerClassName) throws IOException {
        return resolveTypeDeclaration(testModelFile, typeDeclarationReference -> new ResolvedCompilationUnitVisitor() {
            @Override
            public boolean visit(ResolvedCompilationUnit unit) {
                return true;
            }

            @Override
            public boolean visit(ResolvedTypeDeclaration type) {
                if(type.declaringType().isPresent() && type.name().simpleName().equals(innerClassName)) {
                    typeDeclarationReference.set(type);
                    return false;
                } else {
                    return true;
                }
            }
        });
    }
}
