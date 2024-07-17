package poussecafe.source.generation.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CompilationUnitEditorTest {

    @Test
    public void noEditOnlyOrdersImports() {
        givenExistingCodeWithOrderedImports();
        whenEditingWithoutChange();
        thenContentUnchanged();
    }

    private void givenExistingCodeWithOrderedImports() {
        try {
            sourceFile = File.createTempFile(getClass().getSimpleName(), ".java");
            sourceFile.deleteOnExit();
            referenceFile = new File("src/test/java/poussecafe/source/generation/existingcode/myaggregate/adapters/MyAggregateAttributes.java");
            Files.copy(referenceFile.toPath(), sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File sourceFile;

    private File referenceFile;

    private void whenEditingWithoutChange() {
        var editor = new CompilationUnitEditor.Builder()
                .packageName("poussecafe.source.generation.existingcode.myaggregate.adapters")
                .fileDirectory(sourceFile.getParentFile().toPath())
                .fileName(sourceFile.getName())
                .formatterOptions(new CodeFormatterOptionsBuilder()
                        .build())
                .build();
        editor.flush();
    }

    private void thenContentUnchanged() {
        try {
            assertTrue(Arrays.equals(Files.readAllBytes(referenceFile.toPath()), Files.readAllBytes(sourceFile.toPath())));
        } catch (IOException e) {
            assertTrue(false);
        }
    }
}
