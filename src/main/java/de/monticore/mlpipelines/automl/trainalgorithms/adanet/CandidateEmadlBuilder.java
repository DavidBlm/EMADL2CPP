package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;
import de.monticore.mlpipelines.automl.helper.FileLoader;

import java.util.ArrayList;
import java.util.List;

public class CandidateEmadlBuilder {
    private final String sourceModelPath;
    private final String generatedModelPath;


    public CandidateEmadlBuilder(String sourceModelPath, String generatedModelPath) {
        this.sourceModelPath = sourceModelPath;
        this.generatedModelPath = generatedModelPath;
    }

    public ArchitectureSymbol createArchitectureFromCandidate(AdaNetCandidate candidate) {
        List<String> emadl = createEmadlFileContent(candidate);
        FileLoader.writeToFile(emadl, generatedModelPath);

        //Get filename and directory from generatedModelPath
        String[] pathParts = generatedModelPath.split("/");
        String modelName = getComponentNameFromPath(generatedModelPath);
        modelName = lowercaseFirstChar(modelName);
        String directory = getDirectoryFromPath(generatedModelPath);

        ArchitectureSymbol architecture = ModelLoader.load(directory, modelName);

        return architecture;
    }

    private List<String> createEmadlFileContent(AdaNetCandidate candidate) {
        List<String> emadlFileContent = FileLoader.loadFile(sourceModelPath);
        String oldComponentName = getComponentNameFromPath(sourceModelPath);
        String newComponentName = getComponentNameFromPath(generatedModelPath);
        emadlFileContent.set(0, emadlFileContent.get(0).replace(oldComponentName, newComponentName));
        replaceAdanetWithAdanetCandidateEmadl(candidate, emadlFileContent);
        return emadlFileContent;
    }

    private String getComponentNameFromPath(String path) {
        String[] pathParts = path.split("/");
        String filename = pathParts[pathParts.length - 1];
        String name = filename.replace(".emadl", "");
        return name;
    }

    private String lowercaseFirstChar(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    private String getDirectoryFromPath(String generatedModelPath) {
        String[] pathParts = generatedModelPath.split("/");
        String filename = pathParts[pathParts.length - 1];
        String directory = generatedModelPath.replace(filename, "");
        return directory;
    }

    private void replaceAdanetWithAdanetCandidateEmadl(AdaNetCandidate candidate, List<String> emadlFileContent) {
        int adanetLineIndex = getAdaNetLineIndex(emadlFileContent);
        List<String> candidateEmadl = getFormattedCandidateEmadl(candidate);
        emadlFileContent.remove(adanetLineIndex);
        emadlFileContent.addAll(adanetLineIndex, candidateEmadl);
    }

    private static int getAdaNetLineIndex(List<String> emadlFileContent) {
        int adanetIndex = -1;
        for (int i = 1; i < emadlFileContent.size(); i++) {
            String adaNet = "AdaNet";
            if (emadlFileContent.get(i).contains(adaNet)) {
                adanetIndex = i;
                break;
            }
        }

        if (adanetIndex == -1) {
            throw new IllegalArgumentException("AdaNet not found in emadl file");
        }

        return adanetIndex;
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
