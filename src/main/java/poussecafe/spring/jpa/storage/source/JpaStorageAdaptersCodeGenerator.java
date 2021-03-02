package poussecafe.spring.jpa.storage.source;

import java.io.InputStream;
import java.nio.file.Path;
import org.eclipse.core.runtime.preferences.IScopeContext;
import poussecafe.source.analysis.ClassName;
import poussecafe.source.generation.AggregatePackage;
import poussecafe.source.generation.NamingConventions;
import poussecafe.source.generation.StorageAdaptersCodeGenerator;
import poussecafe.source.generation.internal.CodeGeneratorBuilder;
import poussecafe.source.generation.tools.CompilationUnitEditor;
import poussecafe.source.model.Aggregate;
import poussecafe.spring.jpa.storage.SpringJpaStorage;

import static java.util.Objects.requireNonNull;

public class JpaStorageAdaptersCodeGenerator extends StorageAdaptersCodeGenerator {

    @Override
    public void generate(Aggregate aggregate) {
        super.generate(aggregate);
        addJpaDataRepository(aggregate);
    }

    @Override
    protected void updateDefaultAttributesImplementation(Aggregate aggregate, CompilationUnitEditor compilationUnitEditor) {
        var editor = new JpaAttributesImplementationEditor.Builder()
                .compilationUnitEditor(compilationUnitEditor)
                .aggregate(aggregate)
                .build();
        editor.edit();
    }

    @Override
    protected void addDataAccessImplementation(Aggregate aggregate, CompilationUnitEditor compilationUnitEditor) {
        var editor = new JpaDataAccessImplementationEditor.Builder()
                .compilationUnitEditor(compilationUnitEditor)
                .aggregate(aggregate)
                .build();
        editor.edit();
    }

    private void addJpaDataRepository(Aggregate aggregate) {
        var typeName = aggregateJpaRepositoryTypeName(aggregate.aggregatePackage());
        var compilationUnitEditor = compilationUnitEditor(typeName);
        if(compilationUnitEditor.isNew()) {
            var editor = new JpaDataRepositoryEditor.Builder()
                    .compilationUnitEditor(compilationUnitEditor)
                    .aggregate(aggregate)
                    .build();
            editor.edit();
        }
    }

    public static ClassName aggregateJpaRepositoryTypeName(AggregatePackage aggregate) {
        return new ClassName(NamingConventions.adaptersPackageName(aggregate),
                NamingConventions.aggregateAttributesImplementationTypeName(aggregate).simple() + "JpaRepository");
    }

    @Override
    protected String storageName() {
        return SpringJpaStorage.NAME;
    }

    public static class Builder implements CodeGeneratorBuilder {

        private JpaStorageAdaptersCodeGenerator generator = new JpaStorageAdaptersCodeGenerator();

        @Override
        public JpaStorageAdaptersCodeGenerator build() {
            requireNonNull(generator.sourceDirectory);
            requireNonNull(generator.formatterOptions);
            return generator;
        }

        @Override
        public Builder sourceDirectory(Path sourceDirectory) {
            generator.sourceDirectory = sourceDirectory;
            return this;
        }

        @Override
        public Builder codeFormatterProfile(Path profile) {
            generator.loadProfileFromFile(profile);
            return this;
        }

        @Override
        public Builder codeFormatterProfile(InputStream profile) {
            generator.loadProfileFromFile(profile);
            return this;
        }

        @Override
        public CodeGeneratorBuilder preferencesContext(IScopeContext context) {
            generator.loadPreferencesFromContext(context);
            return this;
        }
    }

    private JpaStorageAdaptersCodeGenerator() {

    }
}
