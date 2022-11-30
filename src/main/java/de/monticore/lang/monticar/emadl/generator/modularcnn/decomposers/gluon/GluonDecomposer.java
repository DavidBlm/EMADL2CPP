package de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.gluon;

import afu.org.checkerframework.checker.oigj.qual.O;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.BackendDecomposer;
import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.AtomicNetworkStructure;
import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.ComposedNetworkStructure;
import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.LayerInformation;
import de.se_rwth.commons.logging.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GluonDecomposer implements BackendDecomposer {

    ArrayList<LayerSubstitute> allowedLayerSubstitutes = new ArrayList<>();

    public GluonDecomposer(){
        LayerSubstitute reluSub = new LayerSubstitute("Relu");
        reluSub.addSubstitute("Activation");
        allowedLayerSubstitutes.add(reluSub);
    }

    @Override
    public void decomposeNetwork(String modelPath, ComposedNetworkStructure composedNetworkStructure) {
        ArrayList<File> fileList = scanForFiles(modelPath, composedNetworkStructure);

        File networkJsonFile = null;
        File paramsFile = null;

        File lossNetworkJsonFile = null;
        File lossParamsFile = null;

        for (File file : fileList){
            String fileName = file.getName();
            if (fileName.contains("newest-symbol.json")) networkJsonFile = file;
            else if (fileName.contains("newest-0000.params")) paramsFile = file;
            else if (fileName.contains("loss-symbol.json")) lossNetworkJsonFile = file;
            else if (fileName.contains("loss-0000.params")) lossParamsFile = file;
        }

        splitComposedNetworkIntoAtomicNetworks(modelPath, composedNetworkStructure, networkJsonFile, paramsFile, lossNetworkJsonFile, lossParamsFile);
    }

    /*
    public void decomposeNetworks(String modelPath, HashMap<String, ComposedNetworkStructure> composedNetworkStructures) {
    }*/

    private void splitComposedNetworkIntoAtomicNetworks(String modelPath, ComposedNetworkStructure composedNetworkStructure, File networkFile, File paramsFile, File lossNetworkFile, File lossParamsFile){
        splitNetworkJsonFile(modelPath, composedNetworkStructure, networkFile);
        splitNetworkParamsFile(modelPath, composedNetworkStructure, paramsFile);
        splitLossNetworkJsonFile(modelPath, composedNetworkStructure, lossNetworkFile);
        splitLossNetworkParamsFile(modelPath, composedNetworkStructure, lossParamsFile);
    }

    private void splitNetworkJsonFile(String modelPath, ComposedNetworkStructure composedNetworkStructure, File file){
        String jsonContent = readFile(file.getPath());
        Map<String, Object> contentMap = jsonToMap(jsonContent);

        ArrayList<Object> nodes = (ArrayList<Object>) contentMap.get("nodes");
        ArrayList<AtomicNetworkStructure> atomicNets = composedNetworkStructure.getAtomicNetworkStructures();
        ArrayList<ArrayList<Map<String,Object>>> decomposedNets = new ArrayList<>();
        int layerPointer = 0;


        for (int i=0; i<atomicNets.size(); i++  /*int i=0, j=0; i<nodes.size() && j<atomicNets.size(); i++*/ ){
            AtomicNetworkStructure atomicNet = atomicNets.get(i);
            ArrayList<Map<String,Object>> atomicNodes = new ArrayList<>();
            for (LayerInformation layer : atomicNet.getNetworkLayers()){
                Map<String,Object> node = (Map<String, Object>) nodes.get(layerPointer);
                String op = (String) node.get("op");
                String name = (String) node.get("name");

                if (layer.isInputLayer()){
                    if (i==0){
                        if (op.equals("null") && name.equals(layer.getLayerName())){
                            atomicNodes.add(node);
                            layerPointer++;
                        }
                    } else {
                        //build new input for atomic net here
                        continue;
                    }





                } else if (layer.isDefaultLayer()){

                    boolean whileLoopEntered = false;
                    boolean substituteCheck = checkLayerSubstitutesForMatch(layer.getLayerName(),op);
                    while(!op.equals(layer.getLayerName()) && !substituteCheck){
                        atomicNodes.add(node);

                        Map<String, Object> oldNode = node;

                        node = (Map<String, Object>) nodes.get(layerPointer);
                        op = (String) node.get("op");
                        name = (String) node.get("name");


                        layerPointer++;

                        if (oldNode.equals(node)){
                            node = (Map<String, Object>) nodes.get(layerPointer);
                            op = (String) node.get("op");
                            name = (String) node.get("name");
                        }

                        if (!whileLoopEntered) whileLoopEntered = true;
                    }

                    substituteCheck = checkLayerSubstitutesForMatch(layer.getLayerName(),op);
                    if (op.equals(layer.getLayerName()) || substituteCheck){
                        atomicNodes.add(node);

                        node = (Map<String, Object>) nodes.get(layerPointer);
                        op = (String) node.get("op");
                        name = (String) node.get("name");
                        //layerPointer++;
                    }

                    if (!whileLoopEntered) layerPointer++;

                } else if (layer.isOutputLayer()){
                    if (i==atomicNets.size()-1){

                    } else {
                        //build new output for atomic net here
                        continue;
                    }






                } else {
                    throw new RuntimeException("Unknown Layer type when splitting. Type: " + layer.getLayerType().toString());
                }

            }

        }
    }

    private boolean checkLayerSubstitutesForMatch(String originalLayer, String layerSubs){
        for (LayerSubstitute layerSubstitute : this.allowedLayerSubstitutes) {
            boolean match = layerSubstitute.hasLayerSubstitute(originalLayer,layerSubs);
            if (match) return true;
        }
        return false;
    }

    private void splitNetworkParamsFile(String modelPath, ComposedNetworkStructure composedNetworkStructure, File file){

    }

    private void splitLossNetworkJsonFile(String modelPath, ComposedNetworkStructure composedNetworkStructure, File file){
        String jsonContent = readFile(file.getPath());
        Map<String, Object> contentMap = jsonToMap(jsonContent);
    }

    private void splitLossNetworkParamsFile(String modelPath, ComposedNetworkStructure composedNetworkStructure, File file){

    }

    private ArrayList<File> scanForFiles(String modelPath, ComposedNetworkStructure composedNetworkStructure){
        ArrayList<File> files = new ArrayList<>();
        String path = modelPath + "/" + composedNetworkStructure.getComponentName();
        File modelDirectory = new File(path);
        File[] fileList = modelDirectory.listFiles();

        if (fileList != null){
            for (File file: fileList){
                if (file.isFile()){
                    files.add(file);
                }
            }
        }
        return files;
    }

    private void writeFile(String path, String content){
        try{
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e){
            Log.error("Error while writing decomposition files for GLUON backend");
            e.printStackTrace();
        }
    }

    private String readFile(String path){
        StringBuilder content = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new File(path));

            while (scanner.hasNextLine()){
                content.append(scanner.nextLine());
            }
            scanner.close();
        } catch (Exception e){
            Log.error("Error while reading GLUON file");
            e.printStackTrace();
        }
        return content.toString();
    }

    private Map<String,Object> jsonToMap(String jsonContent){
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> contentMap = new HashMap<>();
        TypeReference<HashMap<String,Object>> typeReference = new TypeReference<HashMap<String, Object>>() {};

        try {
            contentMap =  mapper.readValue(jsonContent, typeReference);
        } catch (Exception e){
            e.printStackTrace();
            Log.error("Error while mapping json content to map");
        }

        return contentMap;
    }

    private String mapToJson(Map<String, Object> contentMap){
        String jsonResult = "";
        ObjectMapper mapper = new ObjectMapper();
        try{
            jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(contentMap);
        } catch (Exception e){
            e.printStackTrace();
            Log.error("Error while creating json string from map");
        }

        return jsonResult;
    }
}