package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.pipelines.Pipeline;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.log;


public class HyperbandAlgorithm extends SequentialAlgorithm {

    private double max_iter ;
    private double eta;

    // private double
    private int s_max;
    private double B ;

    ArrayList<Map<String, Object>> results = new ArrayList<>();
   // private int counter;
    private double best_loss;
    private double best_counter ;
    private double validation_loss;
    ArrayList<Double> val_loss = new ArrayList<Double>();
    private int skipLast;


    @Override
    public void executeOptimization( Pipeline pipeline, ASTConfLangCompilationUnit searchSpace,ASTConfLangCompilationUnit evaluationCriteria) {
        this.max_iter = 81;
        this.eta = 3;
        this.s_max = (int) logeta( this.max_iter,eta );
        this.B = ( this.s_max + 1 ) * this.max_iter ;
        //this.results = results;
       // this.counter = 0;
        this.best_loss = Double.POSITIVE_INFINITY;
        this.best_counter = -1;
        this.skipLast=0;
        int counter =0;

        for (int s = this.s_max; s >=0; s--) {
            // initial number of configurations
            int n = (int) Math.ceil( this.B / this.max_iter / ( s + 1 ) * Math.pow(this.eta,s ));
            //initial number of iterations per config
            int r = (int) (this.max_iter * Math.pow(this.eta,( -s )));
            Set<ASTConfLangCompilationUnit> nConfigurations = getFullSetOfNewHyperparamsCandidate(searchSpace, n);
            Double valLoss;
            Map<ASTConfLangCompilationUnit, Double> map = new HashMap<>();
            Map<String,Object> result = new HashMap<>();

            for (int i = 0; i<=s-(skipLast); i++) {
                int n_configs = (int) (n * Math.pow(this.eta, ( -i )));
                int n_iterations = (int) (r * Math.pow(this.eta, i));
                Iterator<ASTConfLangCompilationUnit> iterator = nConfigurations.iterator();
                while (iterator.hasNext()) {
                    counter++;
                    ASTConfLangCompilationUnit element = iterator.next();
                    ASTConfLangCompilationUnitHandler.setValueForKey(element, "num_epoch", n_iterations);
                    //pipeline.setTrainingConfiguration(element);
                    //pipeline.execute();
                    valLoss = validation_loss();
                    if (valLoss < this.best_loss){
                        this.best_loss = valLoss;
                        this.best_counter = counter;
                    }
                    result.put("counter",counter);
                    result.put("params",element);
                    result.put("interations",n_iterations);
                    result.put("loss",valLoss);
                    map.put(element,valLoss);
                    System.out.println(counter);
                }

                nConfigurations = top_configurations(map,n_configs,eta);
            }
            if(!result.isEmpty()) {
                this.results.add(result);
            }
           
        }

        this.currBestHyperparams = bestPerformingConfiguration(results);
        System.out.println(this.results);
        System.out.println(currBestHyperparams);
        System.out.println(this.best_loss);
    }

    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {

    }


    private ASTConfLangCompilationUnit bestPerformingConfiguration(ArrayList<Map<String, Object>> results) {

        Collections.sort(results, Comparator.comparing(map -> (Comparable) map.get("loss")));
        Map<String, Object> firstMap = results.get(0);

        // Get the value for the specific key in the first HashMap
        currBestHyperparams = (ASTConfLangCompilationUnit) firstMap.get("params");
        return currBestHyperparams;
    }


    public double validation_loss() {
        Random rd = new Random();
        double loss = rd.nextDouble();
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


    public Set<ASTConfLangCompilationUnit> getFullSetOfNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace, int n) {
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
        ASTConfLangCompilationUnit currentHyperparams= searchSpace.deepClone();

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

    private ASTConfLangCompilationUnit updateHyperparamsValue(ASTConfLangCompilationUnit searchSpace, ASTConfLangCompilationUnit currentHyperparams, String key) {
        Object searchSpaceValue = ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, key);
        if (searchSpaceValue instanceof Map) {
            Map<String, Object> valueMap = (Map<String, Object>) searchSpaceValue;
            //Object currentValue = ASTConfLangCompilationUnitHandler.getValueByKey(currentHyperparams, key);
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

    public double logeta(double x, double eta) {
        return log(x) / log(eta);
    }
    public int getConfigurationCount(int s) {
        int n = (int) Math.ceil( this.B / this.max_iter / ( s + 1 ) * Math.pow(this.eta,s ));
        return n;
    }
    public double getIterationCount(int s) {
        double r = this.max_iter * Math.pow(this.eta, -(s) );
        return r;
    }

}
