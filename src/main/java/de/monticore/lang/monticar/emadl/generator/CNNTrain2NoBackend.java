package de.monticore.lang.monticar.emadl.generator;

import de.monticore.io.paths.ModelPath;
import de.monticore.lang.monticar.cnnarch.generator.CNNTrainGenerator;
import de.monticore.lang.monticar.cnnarch.generator.ConfigurationData;
import de.monticore.lang.monticar.cnnarch.generator.TemplateConfiguration;
import de.monticore.lang.monticar.cnntrain._symboltable.ConfigurationSymbol;
import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.monticar.generator.cpp.GeneratorCPP;
import de.se_rwth.commons.logging.Log;

import java.nio.file.Path;
import java.io.IOException;
import java.util.*;

public class CNNTrain2NoBackend extends CNNTrainGenerator {

    public CNNTrain2NoBackend() {

    }

    @Override
    public void generate(Path modelsDirPath, String rootModelName) {
        //ToDo: Throw exceptions. We can't generate anything here.
    }

    @Override
    public List<FileContent> generateStrings(ConfigurationSymbol configuration) {
        FileContent temp = new FileContent("print ('No Backend Selected!')","CNNTrainer_" + getInstanceName() + ".py");
        List<FileContent>  ret = new ArrayList<>();
        ret.add(temp);
        return ret;
    }
}