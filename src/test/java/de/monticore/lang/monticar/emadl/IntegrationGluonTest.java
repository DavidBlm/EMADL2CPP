/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl;

import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class IntegrationGluonTest extends IntegrationTest {

    public IntegrationGluonTest() {
        super("GLUON", "39253EC049D4A4E5FA0536AD34874B9D#1DBAEE1B1BD83FB7CB5F70AE91B29638#C4C23549E737A759721D6694C75D9771#5AF0CE68E408E8C1F000E49D72AC214A");
    }

    @Test
    public void testMultipleStreams() {
        Log.getFindings().clear();

        deleteHashFile(Paths.get("./target/generated-sources-emadl/MultipleStreams.training_hash"));

        String[] args = {"-m", "src/test/resources/models/", "-r", "MultipleStreams", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);

        assertTrue(Log.getFindings().isEmpty());
    }

    @Ignore
    @Test
    public void testRNNencdec() {
        Log.getFindings().clear();

        deleteHashFile(Paths.get("./target/generated-sources-emadl/rnnencdec/Network.training_hash"));

        String[] args = {"-m", "src/test/resources/models", "-r", "rnnencdec.Main", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);

        assertTrue(Log.getFindings().isEmpty());
    }

    @Ignore
    @Test
    public void testRNNsearch() {
        Log.getFindings().clear();

        deleteHashFile(Paths.get("./target/generated-sources-emadl/rnnsearch/Network.training_hash"));

        String[] args = {"-m", "src/test/resources/models", "-r", "rnnsearch.Main", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);

        assertTrue(Log.getFindings().isEmpty());
    }

    @Test
    public void testShowAttendTell() {
        Log.getFindings().clear();

        deleteHashFile(Paths.get("./target/generated-sources-emadl/showAttendTell/Show_attend_tell.training_hash"));

        String[] args = {"-m", "src/test/resources/models", "-r", "showAttendTell.Main", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);

        assertTrue(Log.getFindings().isEmpty());
    }

    @Test
    public void testGluonPreprocessingWithSupervised() {
        Log.getFindings().clear();
        deleteHashFile(Paths.get("./target/generated-sources-emadl/PreprocessingNetwork.training_hash"));
        String[] args = {"-m", "src/test/resources/models/", "-r", "PreprocessingNetwork", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().size() == 0);
    }

    @Test
    public void testGluonPreprocessingWithGAN() {
        Log.getFindings().clear();
        deleteHashFile(Paths.get("./target/generated-sources-emadl/defaultGANPreprocessing/GeneratorWithPreprocessing.training_hash"));
        String[] args = {"-m", "src/test/resources/models/ganModel", "-r", "defaultGANPreprocessing.GeneratorWithPreprocessing", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().size() == 0);
    }

    private void deleteHashFile(Path hashFile) {
        try {
            Files.delete(hashFile);
        }
        catch (NoSuchFileException e) {

        }
        catch(Exception e) {
            assertFalse("Could not delete hash file", true);
        }
    }
}
