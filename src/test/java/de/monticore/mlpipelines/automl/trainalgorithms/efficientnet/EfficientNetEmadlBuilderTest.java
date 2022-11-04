package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import junit.framework.TestCase;

import java.util.List;

public class EfficientNetEmadlBuilderTest extends TestCase {

    public void testConstructor() {
        ArchitectureSymbol architecture = ModelLoader.loadEfficientnetB0();
        int phi = 1;
        EfficientNetConfig config = createConfig();
        EfficientNetEmadlBuilder efficientNetEmadlBuilder = new EfficientNetEmadlBuilder(architecture, config);
        assertNotNull(efficientNetEmadlBuilder);
    }

    private EfficientNetConfig createConfig() {
        EfficientNetConfig config = new EfficientNetConfig();
        config.setPhi(1);
        config.setNumberClasses(10);
        config.setMinimumImageWidthAndHeight(8);
        return config;
    }

    public void testGetEmadl() {
        ArchitectureSymbol architecture = ModelLoader.loadEfficientnetB0();
        EfficientNetConfig config = createConfig();
        EfficientNetEmadlBuilder efficientNetEmadlBuilder = new EfficientNetEmadlBuilder(architecture, config);
        List<String> emadl = efficientNetEmadlBuilder.getEmadl();

        assertEquals("component EfficientNetB1<classes=10>{", emadl.get(0));
        assertEquals("}", emadl.get(1));
    }
}