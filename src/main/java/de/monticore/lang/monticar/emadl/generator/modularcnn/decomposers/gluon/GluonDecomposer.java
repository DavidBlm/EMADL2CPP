package de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.gluon;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.BackendDecomposer;
import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.LayerInformation;
import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.NetworkStructure;
import de.se_rwth.commons.logging.Log;

import java.io.*;
import java.util.*;

public class GluonDecomposer implements BackendDecomposer {

    ArrayList<LayerSubstitute> allowedLayerSubstitutes = new ArrayList<>();
    String pythonPath = "";
    String pythonTool = "src/main/resources/GluonParameterSplitter.py";

    public GluonDecomposer(String pythonPath) {
        this.pythonPath = pythonPath;

        LayerSubstitute reluSub = new LayerSubstitute("Relu");
        reluSub.addSubstitute("Activation");
        allowedLayerSubstitutes.add(reluSub);

        LayerSubstitute softmaxSub = new LayerSubstitute("Softmax");
        softmaxSub.addSubstitute("softmax");
        allowedLayerSubstitutes.add(softmaxSub);

        LayerSubstitute loadSub = new LayerSubstitute("LoadNetwork");
        loadSub.addSubstitute("Reshape");
        allowedLayerSubstitutes.add(loadSub);


    }

    @Override
    public void decomposeNetwork(String modelPath, NetworkStructure networkStructure) {
        ArrayList<File> fileList = scanForFiles(modelPath, networkStructure);

        File networkJsonFile = null;
        File paramsFile = null;



        for (File file : fileList) {
            String fileName = file.getName();
            if (fileName.contains("newest-symbol.json")) networkJsonFile = file;
            else if (fileName.contains("newest-0000.params")) paramsFile = file;
        }

        splitComposedNetworkIntoAtomicNetworks(modelPath, networkStructure, networkJsonFile, paramsFile);
    }

    private void splitComposedNetworkIntoAtomicNetworks(String modelPath, NetworkStructure composedNetworkStructure, File networkFile, File paramsFile) {

        if (networkFile == null || paramsFile == null) return;
        ArrayList<GluonRepresentation> splitGluonNets = splitNetworkJsonFile(modelPath, composedNetworkStructure, networkFile);

        boolean reExport = true;

        for (GluonRepresentation gluonNet : splitGluonNets) {
            String jsonContent = mapToJson(gluonNet.getGluonJsonRepresentation());
            ArrayList<String> parameterLayers = gluonNet.getParameterLayerCandidates();

            String decomposedNetDirectory = modelPath + composedNetworkStructure.getComponentName() + "." + gluonNet.getNetworkName() + "_decomposed";
            File directory = new File(decomposedNetDirectory);

            if (!directory.exists()) {
                directory.mkdir();
            }

            String decomposedFileName = "model_" + gluonNet.getNetworkName() + "_decomposed";

            String decomposedNetPath = decomposedNetDirectory + "/" + decomposedFileName + "-old-symbol.json";
            writeFile(decomposedNetPath, jsonContent);
            //Log.warn("Running Decomposition for: " + gluonNet.getNetworkName());
            generateNewParamsFileWithPython(decomposedNetDirectory, decomposedFileName, decomposedNetPath, gluonNet.getParameterLayerCandidates(), paramsFile, networkFile, gluonNet, reExport);
            if (reExport) reExport = false;
        }
    }

    private void generateNewParamsFileWithPython(String networkDirectory, String networkName, String decomposedNetFullPath,
                                                 ArrayList<String> parameterLayers, File originalParamFile, File originalNetworkFile, GluonRepresentation gluonNet, boolean reExport) {
        ArrayList<String> pythonCall = new ArrayList<>();
        pythonCall.add(pythonPath);
        pythonCall.add(pythonTool);

        pythonCall.add("-in");
        pythonCall.add(gluonNet.getNetworkStructure().getNetworkLayers().get(0).getLayerName());

        pythonCall.add("-mp");
        pythonCall.add(decomposedNetFullPath);

        pythonCall.add("-pp");
        pythonCall.add(originalParamFile.getPath());

        pythonCall.add("-nmd");
        pythonCall.add(networkDirectory);

        pythonCall.add("-nmn");
        pythonCall.add(networkName);

        pythonCall.add("-onp");
        pythonCall.add(originalNetworkFile.getPath());

        if (reExport){
            //pythonCall.add("-re");
        }

        StringBuilder layerList = new StringBuilder();
        for (int i=0; i<parameterLayers.size(); i++){
            String layer = parameterLayers.get(i);
            layerList.append(layer);

            if  (i != parameterLayers.size()-1) layerList.append(",");
        }

        pythonCall.add("-pl");
        pythonCall.add(layerList.toString());

        ProcessBuilder pb = new ProcessBuilder(pythonCall).inheritIO();
        int exitCode = 0;

        try {
            Process p = pb.start();

            exitCode = p.waitFor();
        } catch (IOException e) {
            String errMsg = "IOException when writing new gluon param file with exit code: " + Integer.toString(exitCode);
            Log.error(errMsg);
            throw new RuntimeException(errMsg);
        } catch (InterruptedException e) {
            String errMsg = "Interrupted Exception when writing new gluon param file with exit code: " + Integer.toString(exitCode);
            Log.error(errMsg);
            throw new RuntimeException(errMsg);
        }

        if (exitCode != 0) {
            String errMsg = "Exit code that is not 0. Exit code: " + Integer.toString(exitCode);
            Log.error(errMsg);
            throw new RuntimeException(errMsg);
        } else {
            Log.info("Wrote new params file for network " + networkName, "DECOMPOSITION");
        }
    }

    private ArrayList<GluonRepresentation> splitNetworkJsonFile(String modelPath, NetworkStructure networkStructure, File file) {
        String jsonContent = readFile(file.getPath());
        Map<String, Object> contentMap = jsonToMap(jsonContent);

        ArrayList<Object> nodes = (ArrayList<Object>) contentMap.get("nodes");
        Map<String, Object> attributes = (Map<String, Object>) contentMap.get("attrs");
        ArrayList<NetworkStructure> subNets = networkStructure.getNetsToDecompose();
        ArrayList<GluonRepresentation> gluonNets = new ArrayList<>();

        Map<String, Object> lastNode = (Map<String, Object>) nodes.get(nodes.size() - 1);
        ArrayList<Object> lastInputs = (ArrayList<Object>) lastNode.get("inputs");
        ArrayList<Integer> lastInputIntList = (ArrayList<Integer>) lastInputs.get(0);
        int headSize = lastInputIntList.size() - 1;
        int nodeDifference = 0;
        int generatedNodesMalus = 0;

        for (int i = 0, layerPointer = -1; i < subNets.size(); i++) {
            generatedNodesMalus = 0;

            NetworkStructure currentNetwork = subNets.get(i);
            ArrayList<Map<String, Object>> networkNodes = new ArrayList<>();
            String op = null;
            String name = null;

            for (int j=0;j< currentNetwork.getNetworkLayers().size();j++) {
                LayerInformation layer = currentNetwork.getNetworkLayers().get(j);

                if (layer.isInputLayer()) {
                    if (i == 0) {
                        if (layer.isParallel()){
                            layerPointer++;
                            Map<String, Object> node = (Map<String, Object>) nodes.get(layerPointer);
                            op = (String) node.get("op");
                            name = (String) node.get("name");

                            int allowedNullOps = layer.getParallelNames().size();
                            int processedNullOps = 0;

                            int nextLayerPointer = layerPointer + 1;
                            int nextLayerInformationPointer = j + 1;
                            if (nextLayerPointer < nodes.size() && nextLayerInformationPointer < currentNetwork.getNetworkLayers().size()){


                                while (nextLayerPointer < nodes.size() && nextLayerInformationPointer < currentNetwork.getNetworkLayers().size()
                                        && processedNullOps < allowedNullOps ){
                                    if (op.equals("null") && layer.parallelNamesContain(name)) {
                                        networkNodes.add(node);
                                        processedNullOps++;
                                    } else {
                                        networkNodes.add(node);
                                    }
                                    layerPointer++;
                                    node = (Map<String, Object>) nodes.get(layerPointer);
                                    op = (String) node.get("op");
                                    name = (String) node.get("name");
                                }

                                nextLayerPointer = layerPointer + 1;
                                Map<String, Object> nextNode = (Map<String, Object>) nodes.get(nextLayerPointer);
                                String nextOp = (String) nextNode.get("op");
                                String nextName = (String) nextNode.get("name");
                                LayerInformation nextLayerInfo = currentNetwork.getNetworkLayers().get(nextLayerInformationPointer);



                                boolean lastRound = false;
                                while (nextLayerPointer < nodes.size() && nextLayerInformationPointer < currentNetwork.getNetworkLayers().size() && !lastRound ){
                                    if(nextNode != null && (nextOp.equals("null") || nextOp.equals(nextLayerInfo.getLayerName()) || nextOp.equals(checkLayerSubstitutesForMatch(nextLayerInfo.getLayerName(),nextOp)))){
                                        lastRound = true;
                                    }

                                    networkNodes.add(node);

                                    if (!lastRound) {
                                        layerPointer++;
                                        node = (Map<String, Object>) nodes.get(layerPointer);
                                        op = (String) node.get("op");
                                        name = (String) node.get("name");

                                        nextLayerPointer++;
                                        nextNode = (Map<String, Object>) nodes.get(nextLayerPointer);
                                        if (nextNode != null){
                                            nextOp = (String) nextNode.get("op");
                                            nextName = (String) nextNode.get("name");
                                        }
                                    }
                                }
                            }
                        } else {
                            layerPointer++;
                            Map<String, Object> node = (Map<String, Object>) nodes.get(layerPointer);
                            op = (String) node.get("op");
                            name = (String) node.get("name");
                            if (op.equals("null") && name.equals(layer.getLayerName())) {
                                networkNodes.add(node);
                            }
                        }

                    } else {
                        networkNodes.add(buildInput(layer.getLayerName()));
                        generatedNodesMalus++;
                        //nodeDifference--;
                    }
                } else if (layer.isDefaultLayer()) {
                    layerPointer++;
                    Map<String, Object> node = (Map<String, Object>) nodes.get(layerPointer);
                    op = (String) node.get("op");
                    name = (String) node.get("name");

                    boolean substituteCheck = checkLayerSubstitutesForMatch(layer.getLayerName(), op);
                    while (!op.equals(layer.getLayerName()) && !substituteCheck) {

                        networkNodes.add(node);
                        layerPointer++;
                        node = (Map<String, Object>) nodes.get(layerPointer);
                        op = (String) node.get("op");
                        name = (String) node.get("name");
                        substituteCheck = checkLayerSubstitutesForMatch(layer.getLayerName(), op);
                    }

                    substituteCheck = checkLayerSubstitutesForMatch(layer.getLayerName(), op);
                    if (op.equals(layer.getLayerName()) || substituteCheck) {
                        networkNodes.add(node);
                        node = (Map<String, Object>) nodes.get(layerPointer);
                        op = (String) node.get("op");
                        name = (String) node.get("name");
                    }

                } else if (layer.isOutputLayer()) {
                    if (i == subNets.size() - 1) {
                        if (layer.isParallel()){
                            layerPointer++;
                            Map<String, Object> node = (Map<String, Object>) nodes.get(layerPointer);
                            op = (String) node.get("op");
                            name = (String) node.get("name");

                            int allowedNullOps = layer.getParallelNames().size();
                            int processedNullOps = 0;

                            int nextLayerPointer = layerPointer + 1;
                            int nextLayerInformationPointer = j + 1;
                            if (nextLayerPointer < nodes.size() && nextLayerInformationPointer < currentNetwork.getNetworkLayers().size()){


                                while (nextLayerPointer < nodes.size() && nextLayerInformationPointer < currentNetwork.getNetworkLayers().size()
                                        && processedNullOps < allowedNullOps ){
                                    if (op.equals("null") && layer.parallelNamesContain(name)) {
                                        networkNodes.add(node);
                                        processedNullOps++;
                                    } else {
                                        networkNodes.add(node);
                                    }
                                    layerPointer++;
                                    node = (Map<String, Object>) nodes.get(layerPointer);
                                    op = (String) node.get("op");
                                    name = (String) node.get("name");
                                }

                                nextLayerPointer = layerPointer + 1;
                                Map<String, Object> nextNode = (Map<String, Object>) nodes.get(nextLayerPointer);
                                String nextOp = (String) nextNode.get("op");
                                String nextName = (String) nextNode.get("name");
                                LayerInformation nextLayerInfo = currentNetwork.getNetworkLayers().get(nextLayerInformationPointer);



                                boolean lastRound = false;
                                while (nextLayerPointer < nodes.size() && nextLayerInformationPointer < currentNetwork.getNetworkLayers().size() && !lastRound ){
                                    if(nextNode != null && (nextOp.equals("null") || nextOp.equals(nextLayerInfo.getLayerName()) || nextOp.equals(checkLayerSubstitutesForMatch(nextLayerInfo.getLayerName(),nextOp)))){
                                        lastRound = true;
                                    }

                                    networkNodes.add(node);

                                    if (!lastRound) {
                                        layerPointer++;
                                        node = (Map<String, Object>) nodes.get(layerPointer);
                                        op = (String) node.get("op");
                                        name = (String) node.get("name");

                                        nextLayerPointer++;
                                        nextNode = (Map<String, Object>) nodes.get(nextLayerPointer);
                                        if (nextNode != null){
                                            nextOp = (String) nextNode.get("op");
                                            nextName = (String) nextNode.get("name");
                                        }

                                    }
                                }
                            }
                        } else {
                            layerPointer++;
                            Map<String, Object> node = (Map<String, Object>) nodes.get(layerPointer);
                            op = (String) node.get("op");
                            name = (String) node.get("name");
                            if (op.equals("_copy") && name.equals("identity0")) {
                                networkNodes.add(node);
                            }
                        }

                    } else {
                        networkNodes.add(buildOutput());
                        generatedNodesMalus++;
                    }
                } else {
                    throw new RuntimeException("Unknown Layer type when splitting. Type: " + layer.getLayerType().toString());
                }
            }

            gluonNets.add(new GluonRepresentation(currentNetwork, networkNodes, attributes, nodeDifference, headSize));
            nodeDifference += networkNodes.size() - generatedNodesMalus;
            if (i == 0) nodeDifference--;
        }
        return gluonNets;
    }

    private boolean checkLayerSubstitutesForMatch(String originalLayer, String layerSubs) {
        for (LayerSubstitute layerSubstitute : this.allowedLayerSubstitutes) {
            boolean match = layerSubstitute.hasLayerSubstitute(originalLayer, layerSubs);
            if (match) return true;
        }
        return false;
    }

    private ArrayList<File> scanForFiles(String modelPath, NetworkStructure composedNetworkStructure) {
        ArrayList<File> files = new ArrayList<>();
        String path = modelPath + "/" + composedNetworkStructure.getComponentName();
        File modelDirectory = new File(path);
        File[] fileList = modelDirectory.listFiles();

        if (fileList != null) {
            for (File file : fileList) {
                if (file.isFile()) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    private void writeFile(String path, String content) {
        File file = new File(path);
        try {
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            Log.error("Error while writing decomposition files for GLUON backend");
            e.printStackTrace();
        }
    }

    private String readFile(String path) {
        StringBuilder content = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new File(path));

            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
            }
            scanner.close();
        } catch (Exception e) {
            Log.error("Error while reading GLUON file");
            e.printStackTrace();
        }
        return content.toString();
    }

    private Map<String, Object> jsonToMap(String jsonContent) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> contentMap = new HashMap<>();
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<HashMap<String, Object>>() {
        };

        try {
            contentMap = mapper.readValue(jsonContent, typeReference);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error("Error while mapping json content to map");
        }

        return contentMap;
    }

    private String mapToJson(Map<String, Object> contentMap) {
        String jsonResult = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(contentMap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error("Error while creating json string from map");
        }

        return jsonResult;
    }

    private Map<String, Object> buildInput(String inputName) {
        return buildIONode("null", inputName, new ArrayList<Object>());
    }

    private Map<String, Object> buildOutput() {
        return buildIONode("_copy", "identity0", new ArrayList<Object>());
    }

    private Map<String, Object> buildIONode(String op, String name, ArrayList<Object> inputs) {
        Map<String, Object> ioNode = new LinkedHashMap<String, Object>();
        ioNode.put("op", op);
        ioNode.put("name", name);
        ioNode.put("inputs", inputs);
        return ioNode;
    }
}