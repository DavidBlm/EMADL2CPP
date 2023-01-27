package de.monticore.lang.monticar.emadl.modularity;

import de.se_rwth.commons.logging.Log;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class LetterTest extends ModularTestSetup {

    String[] hashPaths = {"target/letterPredictor"};

    @Ignore
    @Test
    public void testLetterPredictor() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/modularNetworkSimpleMultiNet");
        String[] args = {"-m", "src/test/resources/models/ModularCNN/letterPredictor", "-r", "letterpred.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-ad", "-dn", "Network,Net1,Net2"};
        runGenerator(args, hashPaths,14,false);
    }
}
