package de.monticore.mlpipelines.automl.hyperparameters;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._parser.ConfLangParser;
import de.monticore.mlpipelines.automl.hyperparameters.parallel.GeneticAlgorithm;
import de.monticore.mlpipelines.automl.hyperparameters.parallel.ParticleSwarmOptimization;
import de.monticore.mlpipelines.automl.hyperparameters.sequential.BayesianOptimization;
import de.monticore.mlpipelines.automl.hyperparameters.sequential.HyperbandAlgorithm;
import de.monticore.mlpipelines.automl.hyperparameters.sequential.SimulatedAnnealing;
import de.monticore.mlpipelines.automl.hyperparameters.sequential.RandomSearchAlgorithm;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;

public class HyperparamsOptAlgGeneratorTest extends TestCase {

    @Test
    public void testGenerateSimulatedAnnealing() throws IOException {
        AbstractHyperparameterAlgorithm hyperparameterAlgorithm = this.getAlgObjByName("SA");
        assertTrue(hyperparameterAlgorithm instanceof SimulatedAnnealing);

        SimulatedAnnealing sa = (SimulatedAnnealing) hyperparameterAlgorithm;
        assertEquals(sa.getInitialTemperature(), 50.0);
    }

    @Test
    public void testGenerateBayesianOptimization() throws IOException {
        AbstractHyperparameterAlgorithm hyperparameterAlgorithm = this.getAlgObjByName("BO");
        assertTrue(hyperparameterAlgorithm instanceof BayesianOptimization);

        BayesianOptimization bo = (BayesianOptimization) hyperparameterAlgorithm;
        assertEquals(bo.getNumRandomIter(), 10);
        assertEquals(bo.getTradeOff(), 0.01);
    }

    @Test
    public void testGenerateWeightedRS() throws IOException {
        // TODO: Add test for Weighted RS
    }

    @Test
    public void testGenerateHyperband() throws IOException {
        AbstractHyperparameterAlgorithm hyperparameterAlgorithm = this.getAlgObjByName("Hyperband");
        assertTrue(hyperparameterAlgorithm instanceof HyperbandAlgorithm);

        HyperbandAlgorithm ha = (HyperbandAlgorithm) hyperparameterAlgorithm;
        assertEquals(ha.getMaxIter(), 81);
        assertEquals(ha.getEta(), 3);
        assertEquals(ha.getSkipLast(),1);
    }

    @Test
    public void testGenerateRS() throws IOException {
        AbstractHyperparameterAlgorithm hyperparameterAlgorithm = this.getAlgObjByName("RS");
        assertTrue(hyperparameterAlgorithm instanceof RandomSearchAlgorithm);

        RandomSearchAlgorithm rs = (RandomSearchAlgorithm) hyperparameterAlgorithm;
        assertEquals(rs.getMaxIter(), 10);
    }

    @Test
    public void testGenerateGeneticAlgorithm() throws IOException {
        AbstractHyperparameterAlgorithm hyperparameterAlgorithm = this.getAlgObjByName("GA");
        assertTrue(hyperparameterAlgorithm instanceof GeneticAlgorithm);

        GeneticAlgorithm ga = (GeneticAlgorithm) hyperparameterAlgorithm;
        assertEquals(ga.getSelectionRate(), 0.5);
        assertEquals(ga.getCrossoverConfig(), 0.5);
        assertEquals(ga.getMutationConfig(), 0.1);
        assertEquals(ga.getPopulationSize(), 100);
    }

    @Test
    public void testGenerateParticleSwarmOptimization() throws IOException {
        AbstractHyperparameterAlgorithm hyperparameterAlgorithm = this.getAlgObjByName("PSO");
        assertTrue(hyperparameterAlgorithm instanceof ParticleSwarmOptimization);

        ParticleSwarmOptimization pso = (ParticleSwarmOptimization) hyperparameterAlgorithm;
        assertEquals(pso.getC1(), 2.0);
        assertEquals(pso.getC2(), 2.0);
        assertEquals(pso.getPopulationSize(), 100);
    }

    private AbstractHyperparameterAlgorithm getAlgObjByName(String algorithmName) throws IOException {
        String path = "src/test/resources/models/automl/hyperparams_opt/%s/HyperparameterOpt.conf";
        path = String.format(path, algorithmName);
        ConfLangParser parser = new ConfLangParser();
        ASTConfLangCompilationUnit hyperparamsOptConf = parser.parse(path).get();
        return HyperparamsOptAlgGenerator.generateAlgorithm(hyperparamsOptConf);
    }
}
