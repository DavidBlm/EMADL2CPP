/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.integration;

import de.monticore.lang.monticar.emadl.AbstractSymtabTest;
import de.monticore.lang.monticar.emadl.generator.emadlgen.GeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class IntegrationGluonTest extends IntegrationTest {

    public IntegrationGluonTest() {
        super("GLUON", "39253EC049D4A4E5FA0536AD34874B9D#1DBAEE1B1BD83FB7CB5F70AE91B29638#C4C23549E737A759721D6694C75D9771#5AF0CE68E408E8C1F000E49D72AC214A");
    }

    @Before
    public void setUp() {
        Log.initWARN();
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }

    @Test
    public void testModelWithoutCNN() {
        Log.getFindings().clear();


        String[] args = {"-m", "src/test/resources/models/", "-r", "Add", "-b", "GLUON"};
        GeneratorCli.main(args);

        AbstractSymtabTest.checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/no_backend"),
                Arrays.asList(
                        "add.cpp",
                        "add.h",
                        "CMakeLists.txt",
                        "HelperA.h"
                )
        );
        assertTrue(Log.getFindings().isEmpty());
    }

    @Test
    public void testMultipleStreams() {
        Log.getFindings().clear();

        deleteHashFile(Paths.get("./target/generated-sources-emadl/MultipleStreams.training_hash"));

        String[] args = {"-m", "src/test/resources/models/", "-r", "MultipleStreams", "-b", "GLUON"};
        GeneratorCli.main(args);

        checkFindingsCount(3);
    }

    @Ignore
    @Test
    public void testRNNencdec() {
        Log.getFindings().clear();

        deleteHashFile(Paths.get("./target/generated-sources-emadl/rnnencdec/Network.training_hash"));

        String[] args = {"-m", "src/test/resources/models", "-r", "rnnencdec.Main", "-b", "GLUON"};
        GeneratorCli.main(args);

        checkFindingsCount(2);
    }

    @Ignore
    @Test
    public void testRNNsearch() {
        Log.getFindings().clear();

        deleteHashFile(Paths.get("./target/generated-sources-emadl/rnnsearch/Network.training_hash"));

        String[] args = {"-m", "src/test/resources/models", "-r", "rnnsearch.Main", "-b", "GLUON"};
        GeneratorCli.main(args);

        checkFindingsCount(2);
    }

    @Test
    public void testShowAttendTell() {
        Log.getFindings().clear();

        deleteHashFile(Paths.get("./target/generated-sources-emadl/showAttendTell/Show_attend_tell.training_hash"));

        String[] args = {"-m", "src/test/resources/models", "-r", "showAttendTell.Main", "-b", "GLUON"};
        GeneratorCli.main(args);

        checkFindingsCount(3);
    }

    @Test
    @Ignore // TODO fix
    public void testEpisodicMemorySimple() {
        Log.getFindings().clear();

        deleteHashFile(Paths.get("./target/generated-sources-emadl/episodicMemorySimple/episodicMemorySimple.training_hash"));

        String[] args = {"-m", "src/test/resources/models", "-r", "episodicMemorySimple.Network", "-b", "GLUON", "-f", "y"};
        GeneratorCli.main(args);
    }

    @Test
    @Ignore // TODO fix
    public void testGluonPreprocessingWithSupervised() {
        Log.getFindings().clear();
        deleteHashFile(Paths.get("./target/generated-sources-emadl/PreprocessingNetwork.training_hash"));
        String[] args = {"-m", "src/test/resources/models/", "-r", "PreprocessingNetwork", "-b", "GLUON"};
        GeneratorCli.main(args);
        checkFindingsCount(3);
    }

    @Test
    @Ignore // TODO fix
    public void testGluonPreprocessingWithGAN() {
        Log.getFindings().clear();
        deleteHashFile(Paths.get("./target/generated-sources-emadl/defaultGANPreprocessing/GeneratorWithPreprocessing.training_hash"));
        String[] args = {"-m", "src/test/resources/models/ganModel", "-r", "defaultGANPreprocessing.GeneratorWithPreprocessing", "-b", "GLUON"};
        GeneratorCli.main(args);

        checkFindingsCount(3);
    }

    @Test
    public void testMNISTCalculatorWithCustomLayer() {
        Log.getFindings().clear();

        deleteHashFile(Paths.get("./target/generated-sources-emadl/cNNCalculator/Network.training_hash"));

        String[] args = {"-m", "src/test/resources/models/customMNISTCalculator", "-r", "cNNCalculator.Connector", "-b", "GLUON", "-cfp", "src/test/resources/custom_files"};
        GeneratorCli.main(args);

        checkFindingsCount(8);
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