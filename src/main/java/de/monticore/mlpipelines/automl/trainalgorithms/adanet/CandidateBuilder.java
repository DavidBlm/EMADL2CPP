package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;
import de.monticore.mlpipelines.automl.helper.FileLoader;

import java.util.ArrayList;
import java.util.List;

public class CandidateBuilder {
    private ArchitectureSymbol architecture;
    private final String modelName;
    private final String modelDirPath;


    public CandidateBuilder() {
        modelDirPath = System.getProperty("java.io.tmpdir");
        modelName = "model.emadl";
    }

    public ArchitectureSymbol candidateToArchitectureSymbol(AdaNetCandidate candidate) {
        List<String> emadl = createEmadlFileContent(candidate);
        FileLoader fileLoader = new FileLoader();
        String pathString = modelDirPath + modelName;
        fileLoader.writeToFile(emadl, pathString);
        ArchitectureSymbol architecture = ModelLoader.load(modelDirPath, modelName);

        return architecture;
    }

    private List<String> createEmadlFileContent(AdaNetCandidate candidate) {
        List<String> emadlFileContent = new ArrayList<>();
        int layerWidth = candidate.getComponent().getLayerWidth();
        int classes = 10;
        int imageSize = 32;
        String header = String.format("component Adanet<classes=%s, layerWidth=%s, imageSize=%s>{"
                , classes, layerWidth, imageSize);

        emadlFileContent.add(header);
        emadlFileContent.add("   ports in Z(0:255)^{1, imageSize, imageSize} image,");
        emadlFileContent.add("         out Q(0:1)^{classes} predictions;");
        emadlFileContent.add("");
        emadlFileContent.add("   implementation CNN {");
        emadlFileContent.add("       image ->");
        emadlFileContent.addAll(getFormattedCandidateEmadl(candidate));
        emadlFileContent.add("       FullyConnected(units=classes) ->");
        emadlFileContent.add("       predictions;");
        emadlFileContent.add("   }");
        emadlFileContent.add("}");

        return emadlFileContent;
    }

    private List<String> getFormattedCandidateEmadl(AdaNetCandidate candidate) {
        List<String> emadlFileContent = candidate.getEmadl();
        List<String> formattedEmadlFileContent = new ArrayList<>();
        for (String line : emadlFileContent) {
            formattedEmadlFileContent.add("       " + line);
        }
        return formattedEmadlFileContent;
    }
}
