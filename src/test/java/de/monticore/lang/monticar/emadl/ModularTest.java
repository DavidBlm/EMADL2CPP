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

    public void runGenerator(String[] args, boolean exceptionAllowed){

        try {
            EMADLGeneratorCli.main(args);
            checkFindingsCount(6);

            Log.getFindings().stream().forEach(finding -> {
                Log.info("FINDING: " +finding.toString(),"FINDINGS_LOG");
            });

        }catch (Exception e) {
            e.printStackTrace();
            if (!exceptionAllowed) assertFalse(true);
        }


    }

    @Test
    public void testEmtpyNetwork() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/ModularMNIST", "-r", "emptyNetwork.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args,true);

    }

    @Test
    public void testModularNetworkComplex() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/ModularMNIST", "-r", "modularNetworkComplex.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        //runGenerator(args,false);
    }

    @Test
    public void testModularNetworkSimple() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/ModularMNIST", "-r", "modularNetworkSimple.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        //runGenerator(args,false);
    }

    @Test
    public void testSingleNetwork() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/ModularMNIST", "-r", "singleNetwork.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args,false);
    }

    /*
    @Test
    public void testCustomMNIST() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/customMNISTCalculator", "-r", "cNNCalculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args,true);
    }
    */


}
