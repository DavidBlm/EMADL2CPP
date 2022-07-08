package de.monticore.lang.monticar.emadl;

import de.monticore.lang.monticar.emadl.generator.emadlgen.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class SimpleTest extends AbstractSymtabTest {

    @Before
    public void setUp() {
        // ensure an empty log
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }

    @Test
    public void testSimple() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/ModularMNIST", "-r", "cNNCalculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        try {
            EMADLGeneratorCli.main(args);
            checkFindingsCount(6);
        }catch (Exception e) {
            e.printStackTrace();
        }



    }

}
