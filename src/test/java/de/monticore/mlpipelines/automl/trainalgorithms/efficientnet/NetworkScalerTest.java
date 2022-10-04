package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

public class NetworkScalerTest extends TestCase {

    private ArchitectureSymbol architecture;
    private NetworkScaler networkScaler;
    private ScalingFactors scalingFactors;

    @Before
    public void setUp(){
        this.architecture = new ArchitectureSymbol();
        this.networkScaler = new NetworkScaler();
        this.scalingFactors = new ScalingFactors(1,1,1);
    }

    @Test
    public void testScale() {
        ArchitectureSymbol archSymbol = networkScaler.scale(this.architecture, this.scalingFactors);
        assertNotNull(archSymbol);
    }

    public void testGetDepth() {
    }

    public void testSetDepth() {
    }

    public void testGetWidth() {
    }

    public void testSetWidth() {
    }

    public void testGetResolution() {
    }

    public void testSetResolution() {
    }

    public void testGetLocArchitectureSymbol() {
    }

    public void testSetLocArchitectureSymbol() {
    }
}