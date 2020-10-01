package de.monticore.lang.monticar.emadl;

import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class IntegrationNoBackendTest extends AbstractSymtabTest {
    public IntegrationNoBackendTest() {

    }
    @Ignore
    @Test
    public void testErrorOut() {
        Log.getFindings().clear();

        String[] args = {"-m", "src/test/resources/models/", "-r", "MultipleStreams", "-b", "NONE"};

        try {
            EMADLGeneratorCli.main(args);
        }catch (Exception e){
            // We should have an exception.
        }
        assertFalse(Log.getFindings().isEmpty());
    }

    @Ignore
    @Test
    public void testModelWithoutCNN() {
        Log.getFindings().clear();

        String[] args = {"-m", "src/test/resources/models/", "-r", "NoNetwork", "-b", "NONE"};
        EMADLGeneratorCli.main(args);

        assertTrue(Log.getFindings().isEmpty());
    }
}
