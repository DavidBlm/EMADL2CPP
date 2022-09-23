/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl;

import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;

public class CustomLayerTest {

    @Before
    public void setUp() {
        Log.initWARN();
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }

    @Test(expected = NoSuchElementException.class)
    public void NegativeCustomLayerTest (){
        String[] args = {"-m", "src/test/resources/models/customMNISTCalculator", "-r", "cNNCalculator.Connector", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
    }

    @Test(expected = NoSuchElementException.class)
    public void NegativeCustomLayerPyTorchTest (){
        String[] args = {"-m", "src/test/resources/models/customMNISTCalculator", "-r", "cNNCalculator.Connector", "-b", "PYTORCH", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
    }
}