package de.monticore.lang.monticar.emadl;

import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class CustomLayerTest {

    @Test(expected = NoSuchElementException.class)
    public void NegativeCustomLayerTest (){
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/customMNISTCalculator", "-r", "cNNCalculator.Connector", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
    }
}
