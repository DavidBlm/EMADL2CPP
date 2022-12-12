package de.monticore.lang.monticar.emadl;

import de.monticore.lang.monticar.emadl.generator.emadlgen.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ModularTest extends AbstractSymtabTest {

    @Before
    public void setUp() {
        // ensure an empty log
        Log.getFindings().clear();
        Log.enableFailQuick(false);

    }

    public void runGenerator(String[] args, int expectedFindings, boolean exceptionAllowed){
        removeCNNFilesFromPreviousRuns();
        try {
            EMADLGeneratorCli.main(args);
            checkFindingsCount(expectedFindings);

            Log.getFindings().stream().forEach(finding -> {
                Log.info("FINDING: " + finding.toString(),"FINDINGS_LOG");
            });

        }catch (Exception e) {
            Log.info(e.toString(),"MODULAR_TEST_EXCEPTION");
            StackTraceElement[] stackTraceElements = e.getStackTrace();
            StringBuilder trace = new StringBuilder("Stack trace: \n");
            for (StackTraceElement element:stackTraceElements){
                trace.append(element.toString()).append("\n");
            }
            Log.info(trace.toString(),"MODULAR_TEST_EXCEPTION");

            if (!exceptionAllowed) {
                assertFalse(true);
                throw new RuntimeException();
            }
        }
    }

    @Test
    public void testModularSentiment() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/modularSentiment");
        String[] args = {"-m", "src/test/resources/models/ModularMNIST/modularSentiment", "-r", "sentimentanalyzer.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args,3,false);
    }

    @Ignore
    @Test
    public void testModularSentimentMultiOutput() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/modularSentimentMultiOutput");
        String[] args = {"-m", "src/test/resources/models/ModularMNIST/modularSentimentMultiOutput", "-r", "sentimentanalyzer.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args,3,false);
    }

    @Test
    public void testEmtpyNetwork() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/emptyNetwork");
        String[] args = {"-m", "src/test/resources/models/ModularMNIST/emptyNetwork", "-r", "calculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-adcm"};
        runGenerator(args,0,true);

    }

    @Test
    public void testModularNetworkSimpleMultiNet() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/modularNetworkSimpleMultiNet");
        String[] args = {"-m", "src/test/resources/models/ModularMNIST/modularNetworkSimpleMultiNet", "-r", "calculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-adcm"};
        runGenerator(args,14,false);
    }

    @Test
    public void testModularNetworkComplex() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/modularNetworkComplex");
        String[] args = {"-m", "src/test/resources/models/ModularMNIST/modularNetworkComplex", "-r", "calculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-adcm"};
        runGenerator(args,12,false);
    }

    @Test
    public void testModularNetworkSimple() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/modularNetworkSimple");
        String[] args = {"-m", "src/test/resources/models/ModularMNIST/modularNetworkSimple", "-r", "calculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-adcm"};
        runGenerator(args,12,false);
    }

    @Test
    public void testSingleNetwork() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/singleNetwork");
        String[] args = {"-m", "src/test/resources/models/ModularMNIST/singleNetwork", "-r", "calculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-adcm"};
        runGenerator(args,12,false);
    }



    public void removeMultipleTrainingHashes(String[] hashPaths)throws IOException{
        for (String path:hashPaths){
            removeDirectory(path);
        }

    }

    public void removeDirectory(String path) throws IOException{
        if (path.equals("")) {
            Log.info("Path empty","IO_INFO");
            return;
        }

        File files = new File(path);
        if (!files.exists()) {
            Log.info("Path does not exist","IO_INFO");
            return;
        }

        for (File file: files.listFiles()){
            if (file.isDirectory()) removeDirectory(file.getPath());

            boolean success = file.delete();
            if (!success) throw new IOException("File could not be deleted:" + file.toString());
        }
    }

    public void removeCNNFilesFromPreviousRuns(){
        String[] hashPaths = {"target/singleNetwork","target/emptyNetwork","target/modularNetworkComplex",
                "target/modularNetworkSimple","target/modularNetworkSimpleMultiNet","target/modularSentiment", "target/calculator", "target/sentimentanalyzer"};
        try {
            removeMultipleTrainingHashes(hashPaths);
            removeDirectory("model");
        } catch (IOException e){
            e.printStackTrace();
        }
    }





}
