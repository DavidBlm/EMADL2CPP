/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator.reinforcementlearning;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch.gluongenerator.reinforcement.RewardFunctionSourceGenerator;
import de.monticore.lang.monticar.emadl.generator.EMADLAbstractSymtab;
import de.monticore.lang.monticar.generator.cpp.GeneratorEMAMOpt2CPP;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;

import java.io.IOException;
import java.util.Optional;

public class RewardFunctionCppGenerator implements RewardFunctionSourceGenerator{
    public RewardFunctionCppGenerator() {
    }


    @Override
    public EMAComponentInstanceSymbol resolveSymbol(TaggingResolver taggingResolver, String rootModel) {
        Optional<EMAComponentInstanceSymbol> instanceSymbol = taggingResolver
                .<EMAComponentInstanceSymbol>resolve(rootModel, EMAComponentInstanceSymbol.KIND);

        if (!instanceSymbol.isPresent()) {
            Log.error("Generation of reward function is not possible: Cannot resolve component instance "
                    + rootModel);
        }

        return instanceSymbol.get();
    }

    @Override
    public void generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver,
                         String targetPath) {
        GeneratorEMAMOpt2CPP generator = new GeneratorEMAMOpt2CPP();
        generator.useArmadilloBackend();

        generator.setGenerationTargetPath(targetPath);

        try {
            generator.generate(componentInstanceSymbol, taggingResolver);
        } catch (IOException e) {
            Log.error("Generation of reward function is not possible: " + e.getMessage());
        }

    }

    @Override
    public void generate(String modelPath, String rootModel, String targetPath) {
        TaggingResolver taggingResolver = createTaggingResolver(modelPath);
        EMAComponentInstanceSymbol instanceSymbol = resolveSymbol(taggingResolver, rootModel);
        generate(instanceSymbol, taggingResolver, targetPath);
    }

    @Override
    public TaggingResolver createTaggingResolver(final String modelPath) {
        return EMADLAbstractSymtab.createSymTabAndTaggingResolver(modelPath);
    }
}
