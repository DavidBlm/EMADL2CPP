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

    public void testGetEmadlCreatesHeader() {
        ArchitectureSymbol architecture = ModelLoader.loadEfficientnetB0();
        EfficientNetConfig config = createConfig();
        EfficientNetEmadlBuilder efficientNetEmadlBuilder = new EfficientNetEmadlBuilder(architecture, config);
        List<String> emadl = efficientNetEmadlBuilder.getEmadl();

        assertEquals("component EfficientNetB1<classes=10>{", emadl.get(0));
        assertEquals("}", emadl.get(emadl.size() - 1));
    }

    private EfficientNetConfig createConfig() {
        EfficientNetConfig config = new EfficientNetConfig();
        config.setPhi(1);
        config.setNumberClasses(10);
        config.setMinimumImageWidthAndHeight(8);
        return config;
    }

    public void testGetEmadlCreatesPorts() {
        ArchitectureSymbol architecture = ModelLoader.loadEfficientnetB0();
        EfficientNetConfig config = createConfig();
        EfficientNetEmadlBuilder efficientNetEmadlBuilder = new EfficientNetEmadlBuilder(architecture, config);
        List<String> emadl = efficientNetEmadlBuilder.getEmadl();

        assertEquals("    ports in Z(0:255)^{1, 16, 16} image,", emadl.get(1));
        assertEquals("        out Q(0:1)^{10} predictions;", emadl.get(2));
    }
}