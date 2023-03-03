package de.monticore.mlpipelines.automl.hyperparameters;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.automl.hyperparameters.parallel.GeneticAlgorithm;
import de.monticore.mlpipelines.automl.hyperparameters.parallel.ParticleSwarmOptimization;
import de.monticore.mlpipelines.automl.hyperparameters.sequential.*;
import de.se_rwth.commons.logging.Log;

import java.util.Map;

public class HyperparamsOptAlgGenerator {

    public static AbstractHyperparameterAlgorithm generateAlgorithm(ASTConfLangCompilationUnit hyperparamsOptConf) {
        Map<String, Object> optimizerMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(hyperparamsOptConf, "optimizer");
        String optimizerName = (String) optimizerMap.get("optimizer");
        Map<String, Object> nestedMap = (Map<String, Object>) optimizerMap.get("nestedMap");
        Log.info(String.format("Use %s for hyperparameter optimization", optimizerName),
                HyperparamsOptAlgGenerator.class.getName());
        switch (optimizerName) {
            case "SA":
                return getSimulatedAnnealing(nestedMap);
            case "BO":
                return getBayesianOptimization(nestedMap);
            case "WeightedRS":
                return getWeightedRS(nestedMap);
            case "GA":
                return getGeneticAlgorithm(nestedMap);
            case "PSO":
                return getParticleSwarmOptimization(nestedMap);
            case "Hyperband":
                return getHyperbandAlgorithm(nestedMap);
            case "RS":
                return getRandomSearchAlgorithm(nestedMap);
            //TODO: Add cases for other optimization algorithms
            default:
                throw new IllegalArgumentException("Optimizer name in HyperparameterOpt.conf not valid.");
        }
    }

    private static SimulatedAnnealing getSimulatedAnnealing(Map<String, Object> nestedMap) {
        SimulatedAnnealing sa = new SimulatedAnnealing();
        sa.setInitialTemperature((Double) nestedMap.get("initial_temperature"));
        return sa;
    }

    private static BayesianOptimization getBayesianOptimization(Map<String, Object> nestedMap) {
        BayesianOptimization bo = new BayesianOptimization();
        bo.setNumRandomIter((Integer) nestedMap.get("num_random_iter"));
        bo.setTradeOff((Double) nestedMap.get("tradeoff"));
        return bo;
    }

    private static WeightedRS getWeightedRS(Map<String, Object> nestedMap) {
        WeightedRS wrs = new WeightedRS();
        // TODO: set parameters for WeightedRS using nestedKey
        return wrs;
    }

    private static GeneticAlgorithm getGeneticAlgorithm(Map<String, Object> nestedMap) {
        GeneticAlgorithm ga = new GeneticAlgorithm();
        ga.setSelectionRate((Double) nestedMap.get("selection_rate"));
        ga.setCrossoverConfig((Double) nestedMap.get("crossover_rate"));
        ga.setMutationConfig((Double) nestedMap.get("mutation_rate"));
        ga.setPopulationSize((Integer) nestedMap.get("population_size"));
        return ga;
    }

    private static ParticleSwarmOptimization getParticleSwarmOptimization(Map<String, Object> nestedMap) {
        ParticleSwarmOptimization pso = new ParticleSwarmOptimization();
        pso.setC1((Double) nestedMap.get("c1"));
        pso.setC2((Double) nestedMap.get("c2"));
        pso.setPopulationSize((Integer) nestedMap.get("population_size"));
        return pso;
    }
    private static HyperbandAlgorithm getHyperbandAlgorithm(Map<String, Object> nestedMap) {
        HyperbandAlgorithm hyperband = new HyperbandAlgorithm();
        hyperband.setMaxIter((int) nestedMap.get("max_iter"));
        hyperband.setEta((int) nestedMap.get("eta"));
        hyperband.setSkipLast((int) nestedMap.get("skip_last"));
        return hyperband;
    }

    private static RandomSearchAlgorithm getRandomSearchAlgorithm(Map<String, Object> nestedMap) {
        RandomSearchAlgorithm rs = new RandomSearchAlgorithm();
        rs.setMaxIter((int) nestedMap.get("max_iter"));
        return rs;
    }
}
