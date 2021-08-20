package poussecafe.source.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;
import poussecafe.source.DiscoveryTest;
import poussecafe.source.model.ComponentType;
import poussecafe.source.model.TypeReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class TypeReferencesDiscoveryTest {

    @Test
    public void attributesLessEntityReferences() throws IOException {
        givenAttributesLessResolvedEntity();
        whenDiscoveringReferences();
        thenFound(1);
        thenFound(new TypeReference.Builder()
                .type(ComponentType.VALUE_OBJECT)
                .typeClassName(new ClassName("poussecafe.source.testmodel.model.aggregate2.Identifier2"))
                .ignored(false)
                .build());
    }

    private void givenAttributesLessResolvedEntity() throws IOException {
        typeDeclaration = DiscoveryTest.resolveTypeDeclaration(Path.of("model", "aggregate2", "Aggregate2Root.java"));
    }

    private ResolvedTypeDeclaration typeDeclaration;

    private void whenDiscoveringReferences() {
        references = new TypeReferencesDiscovery(typeDeclaration).references();
    }

    private List<TypeReference> references;

    private void thenFound(int expectedSize) {
        assertThat(references.size(), is(expectedSize));
    }

    private void thenFound(TypeReference reference) {
        assertTrue(references.contains(reference));
    }

    @Test
    public void entityReferences() throws IOException {
        givenResolvedEntity();
        whenDiscoveringReferences();
        thenFound(3);
        thenFound(new TypeReference.Builder()
                .type(ComponentType.VALUE_OBJECT)
                .typeClassName(new ClassName("poussecafe.source.testmodel.model.aggregate1.Identifier1"))
                .ignored(false)
                .build());
        thenFound(new TypeReference.Builder()
                .type(ComponentType.VALUE_OBJECT)
                .typeClassName(new ClassName("poussecafe.source.testmodel.model.aggregate1.ValueObject1"))
                .ignored(false)
                .build());
        thenFound(new TypeReference.Builder()
                .type(ComponentType.ENTITY)
                .typeClassName(new ClassName("poussecafe.source.testmodel.model.aggregate1.Entity1"))
                .ignored(false)
                .build());
    }

    private void givenResolvedEntity() throws IOException {
        typeDeclaration = DiscoveryTest.resolveTypeDeclaration(Path.of("model", "aggregate1", "Aggregate1.java"), "Root");
    }
}
