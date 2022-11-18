package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ParallelCompositeElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.SerialCompositeElementSymbol;
import de.monticore.mlpipelines.ModelLoader;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class CandidateBuilderTest extends TestCase {
    private final CandidateBuilder candidateBuilder = new CandidateBuilder();

    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testBuildAddsEnoughLayers() {
        ArchitectureSymbol refArch = ModelLoader.load("src/test/resources/models/adanet/", "adaNetBase");
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());
        ParallelCompositeElementSymbol newArch = (ParallelCompositeElementSymbol) candidateBuilder.build(candidate);
        SerialCompositeElementSymbol serial = (SerialCompositeElementSymbol) newArch.getElements().get(0);
        assertEquals(6, serial.getElements().size());
    }

    @Test
    public void testBuildAddsMultipleElementsInParallelLayer() {
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        previousComponents.add(new AdaNetComponent(1));
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), previousComponents);
        ParallelCompositeElementSymbol newArch = (ParallelCompositeElementSymbol) candidateBuilder.build(candidate);

        SerialCompositeElementSymbol serial = (SerialCompositeElementSymbol) newArch.getElements().get(0);
        ParallelCompositeElementSymbol firstLayer = (ParallelCompositeElementSymbol) serial.getElements().get(0);
        ParallelCompositeElementSymbol secondLayer = (ParallelCompositeElementSymbol) serial.getElements().get(2);
        assertEquals(2, firstLayer.getElements().size());
        assertEquals(1, secondLayer.getElements().size());

    }
}