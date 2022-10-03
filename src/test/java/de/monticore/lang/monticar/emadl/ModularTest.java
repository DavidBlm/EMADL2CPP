package de.monticore.lang.monticar.emadl;

import de.monticore.lang.monticar.emadl.generator.emadlgen.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
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

        /*
        String[] hashPaths = {"target/singleNetwork","target/emptyNetwork","target/modularNetworkComplex","target/modularNetworkSimple"};
        try {
            removeMultipleTrainingHashes(hashPaths);
        } catch (IOException e){
            e.printStackTrace();
        }
        */
    }

    public void runGenerator(String[] args, int expectedFindings, boolean exceptionAllowed){

        try {
            EMADLGeneratorCli.main(args);
            checkFindingsCount(expectedFindings);

            Log.getFindings().stream().forEach(finding -> {
                Log.info("FINDING: " +finding.toString(),"FINDINGS_LOG");
            });

        }catch (Exception e) {
            e.printStackTrace();
            if (!exceptionAllowed) assertFalse(true);
        }

    }



    @Test
    public void testEmtpyNetwork() throws IOException {
        Log.getFindings().clear();
        removeTrainingHash("target/emptyNetwork");
        String[] args = {"-m", "src/test/resources/models/ModularMNIST", "-r", "emptyNetwork.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args,0,true);

    }

    @Test
    public void testModularNetworkComplex() throws IOException {
        Log.getFindings().clear();
        removeTrainingHash("target/modularNetworkComplex");
        String[] args = {"-m", "src/test/resources/models/ModularMNIST", "-r", "modularNetworkComplex.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args,6,false);
    }

    @Test
    public void testModularNetworkSimple() throws IOException {
        Log.getFindings().clear();
        removeTrainingHash("target/modularNetworkSimple");
        String[] args = {"-m", "src/test/resources/models/ModularMNIST", "-r", "modularNetworkSimple.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args,6,false);
    }

    @Test
    public void testSingleNetwork() throws IOException {
        Log.getFindings().clear();
        removeTrainingHash("target/singleNetwork");
        String[] args = {"-m", "src/test/resources/models/ModularMNIST", "-r", "singleNetwork.Connector", "-o", "target", "-b", "GLUON", "-c", "y"};
        runGenerator(args,12,false);
    }


    public void removeMultipleTrainingHashes(String[] hashPaths)throws IOException{
        for (String path:hashPaths){
            removeTrainingHash(path);
        }

    }

    public void removeTrainingHash(String path) throws IOException{
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
            if (file.isDirectory())removeTrainingHash(file.getPath());

            boolean success = file.delete();
            if (!success) throw new IOException("File could not be deleted:" + file.toString());
        }
    }





}
