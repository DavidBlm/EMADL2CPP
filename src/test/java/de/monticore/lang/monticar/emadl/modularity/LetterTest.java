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
        removeDirectory("model");
        removeDirectory("target/letterpred");
        String[] args = {"-m", "src/test/resources/models/ModularCNN/letterPredictor", "-r", "letterpred.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-ad", "-dn", "Network"};
        runGenerator(args, hashPaths,14,false);
    }
}
