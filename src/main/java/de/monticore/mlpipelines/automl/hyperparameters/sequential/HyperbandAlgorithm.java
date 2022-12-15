package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._symboltable.ConfigurationScope;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import static java.lang.Math.log;

public class HyperbandAlgorithm extends SequentialAlgorithm {

    private double max_iter ;
    private double eta;

    // private double
    private int s_max;
    private double B ;
    private double results[];
    private int counter;
    private double best_loss;
    private double best_counter ;
    private double validation_loss;
    ArrayList<Double> val_loss = new ArrayList<Double>();
    private Map<String, Double> stepSizeMap ;
    private Object artifactScope;


    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {
        max_iter = 81;
        eta = 3;
        s_max = (int) logeta( max_iter,eta );
        B = ( s_max + 1 ) * max_iter ;
        counter = 0;
        best_loss = Double.POSITIVE_INFINITY;
        best_counter = -1;
    }

    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        return null;
    }

    public double[] run(int skip_last, boolean dry_run){

        for (int s = this.s_max + 1; s >0; s--) {
            // initial number of configurations
            int n = (int) Math.ceil( this.B / this.max_iter / ( s + 1 ) * Math.pow(this.eta,s ));
            //initial number of iterations per config
            int r = (int) (this.max_iter * Math.pow(this.eta,( -s )));
            // n random configurations
            //configurations T  = [ self.get_params() for i in range( n )]

            //call searchspace for random config

            //call successive halving

        }
        return this.results;
    }

    private double logeta(double x, double eta) {
        return log(eta) / log(x);
    }


    public void random_sample(int n,ConfigurationScope hyperconfopt) {
        Random r = new Random();


        // TODO: Implement method
    }

    public void successive_halving(ConfigurationScope T, int s, int n, int r) {
        for(int i=0;i<s;i++){
            int n_configs = (int) (n * Math.pow(this.eta, -i));
            int n_iterations = (int) (r * Math.pow(this.eta, i));




        }
        // TODO: Implement method
    }
    public void validation_loss() {
        // TODO: Implement method
    }

    public void top_configurations() {
        // TODO: Implement method
    }



    public void setval_loss(double validation_loss) {

        this.val_loss.add(validation_loss);
    }

}
