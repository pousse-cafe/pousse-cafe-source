package poussecafe.spring.jpa.storage.codegeneration;

import poussecafe.source.generation.GeneratorTest;
import poussecafe.spring.jpa.storage.source.JpaStorageAdaptersCodeGenerator;

public class JpaStorageAdaptersCodeGeneratorTest extends GeneratorTest {

    @Override
    protected String[] packageNameSegments() {
        return new String[] { "poussecafe", "spring", "jpa", "storage", "codegeneration", "generated" };
    }

    @Override
    protected void givenStorageGenerator() {
        generator = new JpaStorageAdaptersCodeGenerator.Builder()
                .sourceDirectory(sourceDirectory())
                .codeFormatterProfile(getClass().getResourceAsStream("/CodeFormatterProfileSample.xml"))
                .build();
    }

    private JpaStorageAdaptersCodeGenerator generator;

    @Override
    protected void whenGeneratingStorageCode() {
        generator.generate(aggregate());
    }
}
