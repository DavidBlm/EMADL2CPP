package de.monticore.lang.monticar.emadl;

import de.monticore.lang.monticar.emadl.generator.emadlgen.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ModularTest extends AbstractSymtabTest {

    @Before
    public void setUp() {
        // ensure an empty log
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }

    @Test
    public void testModularMNIST() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/ModularMNIST", "-r", "modularNetworks.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        boolean exceptionTriggered = false;
        try {
            EMADLGeneratorCli.main(args);
            checkFindingsCount(6);
        }catch (Exception e) {
            e.printStackTrace();
            exceptionTriggered = true;
        }
        assertFalse(exceptionTriggered);
    }

}
