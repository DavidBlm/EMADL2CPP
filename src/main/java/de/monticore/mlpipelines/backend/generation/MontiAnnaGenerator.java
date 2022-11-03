package de.monticore.mlpipelines.backend.generation;

import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;

/***
 * A generator that shall replace EMADLGenerator
 *
 */
public class MontiAnnaGenerator {
    private final MontiAnnaContext montiAnnaContext ;


    public MontiAnnaGenerator(final MontiAnnaContext montiAnnaContext) {
        this.montiAnnaContext = montiAnnaContext;
    }

    //TODO FMU break this down gradually with calls in the EMADLGenerator
    public void generateTargetBackendArtefacts() {
        String[] args = {"-m", montiAnnaContext.getParentModelPath(), "-r", montiAnnaContext.getRootModelName(),
                "-o", montiAnnaContext.getExperimentConfiguration().getGenerationTargetPath(),
                "-b", montiAnnaContext.getTargetBackend().toString(), "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
    }
}
