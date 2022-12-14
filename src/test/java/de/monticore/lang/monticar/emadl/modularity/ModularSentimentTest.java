package de.monticore.lang.monticar.emadl.modularity;

import de.se_rwth.commons.logging.Log;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Ignore
public class ModularSentimentTest extends ModularTestSetup{
    String[] hashPaths = {"target/singleNetwork","target/emptyNetwork","target/modularNetworkComplex",
            "target/modularNetworkSimple","target/modularNetworkSimpleMultiNet","target/modularSentiment", "target/calculator", "target/sentimentanalyzer"};

    @Test
    public void testModularSentiment() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/modularSentiment");
        String[] args = {"-m", "src/test/resources/models/ModularCNN/modularSentiment", "-r", "sentimentanalyzer.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args, hashPaths,3,false);
    }

    @Test
    public void testSingleSentiment() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/singleSentiment");
        String[] args = {"-m", "src/test/resources/models/ModularCNN/singleSentiment", "-r", "sentimentanalyzer.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args, hashPaths,3,false);
    }

    @Ignore
    @Test
    public void testModularSentimentMultiOutput() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/modularSentimentMultiOutput");
        String[] args = {"-m", "src/test/resources/models/ModularCNN/modularSentimentMultiOutput", "-r", "sentimentanalyzer.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args, hashPaths,3,false);
    }

}
