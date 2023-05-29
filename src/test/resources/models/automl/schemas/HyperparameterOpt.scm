/* (c) https://github.com/MontiCore/monticore */

schema HyperparameterOpt {

    optimizer_algorithm {
        values:
            SA,
            BO,
            WeightedRS,
            GA,
            PSO,
            Hyperband,
            BOHB,
            DEHB;

            define SA {
                initial_temperature = 50.0: Q
            }

            define GA {
                crossover_rate = 0.8: Q
                mutation_rate = 0.3: Q
            }

            define PSO {
                c1 = 2.0: Q
                c2 = 2.0: Q
            }
    }

}