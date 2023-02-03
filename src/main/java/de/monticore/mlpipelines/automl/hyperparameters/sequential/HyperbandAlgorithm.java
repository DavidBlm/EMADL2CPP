package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.log;
import de.monticore.mlpipelines.workflow.HyperparameterOptimizationWorkflowHyperband;


public class HyperbandAlgorithm extends SequentialAlgorithm {

    private double max_iter ;
    private double eta;

    // private double
    private int s_max;
    private double B ;

    ArrayList<Map<String, Object>> results = new ArrayList<>();
    private int counter;
    private double best_loss;
    private double best_counter ;
    private double validation_loss;
    ArrayList<Double> val_loss = new ArrayList<Double>();
    private Map<String, Double> stepSizeMap ;
    private Object artifactScope;
    private Set<ASTConfLangCompilationUnit> nConfigurations1;
    private int n ;

    public HyperbandAlgorithm() {
        this.max_iter = 81;
        this.eta = 3;
        this.s_max = (int) logeta( max_iter,eta );;
        this.B = ( s_max + 1 ) * max_iter ;
        this.results = results;
        this.counter = 0;
        this.best_loss = Double.POSITIVE_INFINITY;
        this.best_counter = -1;
        this.n = 1;
        HyperparameterOptimizationWorkflowHyperband as = new HyperparameterOptimizationWorkflowHyperband();
        as.set

    }


    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit searchSpace, Pipeline pipeline) {

        for (int s = this.s_max + 1; s >0; s--) {
            // initial number of configurations
            this.n = (int) Math.ceil( this.B / this.max_iter / ( s + 1 ) * Math.pow(this.eta,s ));
            //initial number of iterations per config
            int r = (int) (this.max_iter * Math.pow(this.eta,( -s )));

            Collection<ASTConfLangCompilationUnit> nConfigurations = getFullSetOfNewHyperparamsCandidate(searchSpace, n);
            Double valLoss;
            Map<ASTConfLangCompilationUnit, Double> map = new HashMap<>();
            Map<String,Object> result = new HashMap<>();

            for (int i = 0; i<s; i++) {
                int n_configs = (int) (n * Math.pow(this.eta, ( -i )));
                int n_iterations = (int) (r * Math.pow(this.eta, i));

                Iterator<ASTConfLangCompilationUnit> iterator = nConfigurations.iterator();
                while (iterator.hasNext()) {
                    this.counter=+1;
                    ASTConfLangCompilationUnit element = iterator.next();
                    pipeline.setTrainingConfiguration(element);

                    pipeline.execute();

                    valLoss = validation_loss(element,n_iterations);
                    if (valLoss < this.best_loss){
                        this.best_loss = valLoss;
                        this.best_counter = this.counter;
                    }
                    result.put("counter",this.counter);
                    result.put("params",element);
                    result.put("interations",n_iterations);
                    result.put("loss",valLoss);
                    map.put(element,valLoss);
                    this.results.add( result);
                }

                nConfigurations = top_configurations(map,n_configs,eta);
            }
           
        }

        this.currBestHyperparams = bestPerformingConfiguration(results);
    }
/*
    @Override
    public ASTConfLangCompilationUnit getInitialHyperparams(ASTConfLangCompilationUnit searchSpace) {
        Set<ASTConfLangCompilationUnit> nConfigurations = new HashSet<>();
        ASTConfLangCompilationUnit currentHyperparams;
        for(int i=0;i<this.n;i++){
            currentHyperparams= getHyperparams(searchSpace);
            nConfigurations.add(currentHyperparams);
        }
        this.nConfigurations1 = nConfigurations;
        // first object of the set , of this.nConfigurations1.
        //return nConfigurations.stream().findFirst();
        return nConfigurations;
    }

*/
    public ASTConfLangCompilationUnit getHyperparams(ASTConfLangCompilationUnit searchSpace) {
        ASTConfLangCompilationUnit currentHyperparams = this.getCurrentHyperparameters().deepClone();
        Map<String, Boolean> params = ASTConfLangCompilationUnitHandler.getAllKeys(searchSpace);

        for (Map.Entry<String, Boolean> entry : params.entrySet()) {
            String key = entry.getKey();
            Boolean isNested = entry.getValue();

            if (isNested) {
                currentHyperparams = this.updateNestedHyperparamsValue(searchSpace, currentHyperparams, key);
            } else {
                currentHyperparams = this.updateHyperparamsValue(searchSpace, currentHyperparams, key);
            }
        }

        return currentHyperparams;
    }
    private ASTConfLangCompilationUnit bestPerformingConfiguration(ArrayList<Map<String, Object>> results) {

        Collections.sort(results, Comparator.comparing(map -> (Comparable) map.get("loss")));
        Map<String, Object> firstMap = results.get(0);

        // Get the value for the specific key in the first HashMap
        currBestHyperparams = (ASTConfLangCompilationUnit) firstMap.get("params");
        return currBestHyperparams;
    }


    private double validation_loss(ASTConfLangCompilationUnit element, int nIterations) {
        double loss = 0.0;
        //dry run the training pipeline
        return loss;
    }

    public Set<ASTConfLangCompilationUnit> top_configurations(Map<ASTConfLangCompilationUnit, Double> map, int n_configs, double eta) {
        Map<ASTConfLangCompilationUnit, Double> sortedMap = map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        int v = (int) (n_configs / eta);
        Set<ASTConfLangCompilationUnit> nConfigurations = new HashSet<>();

        nConfigurations= sortedMap.entrySet()
                .stream()
                .limit(v)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return nConfigurations ;
    }


    public Collection<ASTConfLangCompilationUnit> getFullSetOfNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace, int n) {
        Set<ASTConfLangCompilationUnit> nConfigurations = new HashSet<>();
        ASTConfLangCompilationUnit currentHyperparams;
        for(int i=0;i<n;i++){
            currentHyperparams= getNewHyperparamsCandidate(searchSpace);
            nConfigurations.add(currentHyperparams);
        }
        return nConfigurations;
    }
    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
         //return this.nConfigurations1.
    }

    private ASTConfLangCompilationUnit updateHyperparamsValue(ASTConfLangCompilationUnit searchSpace, ASTConfLangCompilationUnit currentHyperparams, String key) {
        Object searchSpaceValue = ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, key);
        if (searchSpaceValue instanceof Map) {
            Map<String, Object> valueMap = (Map<String, Object>) searchSpaceValue;
            Object currentValue = ASTConfLangCompilationUnitHandler.getValueByKey(currentHyperparams, key);
            Object newValue;
            newValue = this.createValueFromRange(valueMap);
            currentHyperparams = ASTConfLangCompilationUnitHandler.setValueForKey(currentHyperparams, key, newValue);
        }

        return currentHyperparams;
    }

    private ASTConfLangCompilationUnit updateNestedHyperparamsValue(ASTConfLangCompilationUnit searchSpace, ASTConfLangCompilationUnit currentHyperparams, String key) {
        Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, key);
        Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");
        for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
            String nestedKey = nestedEntry.getKey();
            Object nestedValue = nestedEntry.getValue();
            if (nestedValue instanceof Map) {
                Map<String, Object> currentValueMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(currentHyperparams, key);
                Map<String, Object> currentNestedMap = (Map<String, Object>) currentValueMap.get("nestedMap");
                Object currentValue = currentNestedMap.get(nestedKey);
                Object newValue;
                Map<String, Object> nestedValueMap = (Map<String, Object>) nestedValue;
                newValue = this.createValueFromRange(nestedValueMap);
                currentHyperparams = ASTConfLangCompilationUnitHandler.setNestedValueForKeys(currentHyperparams, key, nestedKey, newValue);
            }
        }

        return currentHyperparams;
    }

    private double logeta(double x, double eta) {
        return log(eta) / log(x);
    }

}
