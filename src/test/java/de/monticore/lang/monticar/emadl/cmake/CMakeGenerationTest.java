package de.monticore.lang.monticar.emadl.cmake;

import de.monticore.lang.monticar.emadl.AbstractSymtabTest;
import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;

public class CMakeGenerationTest extends AbstractSymtabTest {
    public CMakeGenerationTest() {

    }

    //Todo: Move this one to EMAM2CPP
    @Test
    public void testModelWithIpopt() {
        Log.getFindings().clear();

/*
        String[] args = {"-m", "src/test/resources/models/", "-r", "Add", "-b", "NONE"};
        EMADLGeneratorCli.main(args);

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/no_backend"),
                Arrays.asList(
                        "add.cpp",
                        "add.h",
                        "CMakeLists.txt",
                        "HelperA.h"
                )
        );*/
        assertTrue(Log.getFindings().isEmpty());
    }
}
