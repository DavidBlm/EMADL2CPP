/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.cmake;

import de.monticore.lang.monticar.emadl.AbstractSymtabTest;
import de.monticore.lang.monticar.emadl.generator.emadlgen.GeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;

public class CMakeGenerationTest extends AbstractSymtabTest {
    public CMakeGenerationTest() {

    }

    //Todo: Move this one to EMAM2CPP?
    @Test
    @Ignore // TODO fix
    public void testModelWithIpopt() {
        Log.getFindings().clear();


        String[] args = {"-m", "src/test/resources/models", "-r", "ipopt.QuadraticOpt", "-b", "NONE", "-c", "n"};
        GeneratorCli.main(args);

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/ipopt"),
                Arrays.asList(
                        "CMakeLists.txt"
                )
        );
        assertTrue(Log.getFindings().isEmpty());
    }
}