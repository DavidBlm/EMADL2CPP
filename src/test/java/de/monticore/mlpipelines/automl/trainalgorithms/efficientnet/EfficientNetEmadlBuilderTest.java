package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import junit.framework.TestCase;

public class EfficientNetEmadlBuilderTest extends TestCase {

//    public void testConstructor() {
//        ArchitectureSymbol architecture = ModelLoader.loadEfficientnetB0();
//        EfficientNetConfig config = createConfig();
//        EfficientNetEmadlBuilder efficientNetEmadlBuilder = new EfficientNetEmadlBuilder(architecture, config);
//        assertNotNull(efficientNetEmadlBuilder);
//    }
//
//    public void testGetEmadlCreatesHeader() {
//        ArchitectureSymbol architecture = ModelLoader.loadEfficientnetB0();
//        EfficientNetConfig config = createConfig();
//        EfficientNetEmadlBuilder efficientNetEmadlBuilder = new EfficientNetEmadlBuilder(architecture, config);
//        List<String> emadl = efficientNetEmadlBuilder.getEmadl();
//
//        assertEquals("component EfficientNetB1<classes=10>{", emadl.get(0));
//        assertEquals("}", emadl.get(emadl.size() - 1));
//    }
//
//    private EfficientNetConfig createConfig() {
//        EfficientNetConfig config = new EfficientNetConfig(mock(ASTConfLangCompilationUnit.class));
//        config.set(1);
//        config.setNumberClasses(10);
//        config.setMinimumImageWidthAndHeight(8);
//        return config;
//    }
//
//    public void testGetEmadlCreatesPorts() {
//        ArchitectureSymbol architecture = ModelLoader.loadEfficientnetB0();
//        EfficientNetConfig config = createConfig();
//        EfficientNetEmadlBuilder efficientNetEmadlBuilder = new EfficientNetEmadlBuilder(architecture, config);
//        List<String> emadl = efficientNetEmadlBuilder.getEmadl();
//
//        assertEquals("    ports in Z(0:255)^{1, 16, 16} image,", emadl.get(1));
//        assertEquals("          out Q(0:1)^{classes} predictions;", emadl.get(2));
//    }
//
//    public void testGetEmadlReturnsScalesNetworkEmadl() {
//        NetworkScaler networkScaler = new NetworkScaler();
//        ArchitectureSymbol architecture = ModelLoader.loadEfficientnetB0();
//        ScalingFactors scalingFactors = new ScalingFactors(2, 2, 2);
//        int phi = 1;
//        ArchitectureSymbol scaledNetwork = networkScaler.scale(architecture, scalingFactors, phi);
//        EfficientNetEmadlBuilder efficientNetEmadlBuilder = new EfficientNetEmadlBuilder(scaledNetwork, createConfig());
//        List<String> actualEmadl = efficientNetEmadlBuilder.getEmadl();
//        String expectedEmadlFile = "models/efficientnet/EfficientNetScalingTest.emadl";
//        List<String> expectedEmadl = new FileLoader().loadResourceFile(expectedEmadlFile);
//
//        for (int i = 0; i < actualEmadl.size(); i++) {
//            if (!expectedEmadl.get(i).equals(actualEmadl.get(i))) {
//                System.out.println(i);
//                System.out.println("Expected: " + expectedEmadl.get(i));
//                System.out.println("Actual: " + actualEmadl.get(i));
//            }
//        }
//
//        assertEquals("The emadl of the scaled network is not correct.", expectedEmadl, actualEmadl);
//    }
}