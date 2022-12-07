package de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.gluon;

import afu.org.checkerframework.checker.oigj.qual.O;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class GluonRepresentation {

    private ArrayList<Map<String,Object>> nodes = new ArrayList<>();
    private ArrayList<Integer> argNodes = new ArrayList<>();
    private ArrayList<Integer> nodeRowPointer = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> heads = new ArrayList<>();
    private Map<String, Object> attributes = new LinkedHashMap<>();
    private ArrayList<String> parameterLayerCandidates = new ArrayList<>();
    Map<String, Object> jsonRepresentation = null;
    private int headSize = 0;
    private int nodeDifference = 0;


    public GluonRepresentation(ArrayList<Map<String,Object>> nodes, Map<String, Object> attributes, int nodeDifference, int headSize){
        this.nodes = nodes;
        this.attributes = attributes;
        this.headSize = headSize;
        this.nodeDifference = nodeDifference;
        rebuildNetworkAttributes();
    }

    public Map<String, Object> getGluonJsonRepresentation(){
        return jsonRepresentation;
    }

    public ArrayList<String> getParameterLayerCandidates(){
        return this.parameterLayerCandidates;
    }

    private void rebuildNetworkAttributes(){
        fixNodeNumberRefs();
        fixNodeInputs();
        assembleGluonJsonRepresentation();
    }

    private void assembleGluonJsonRepresentation(){
        Map<String, Object> jsonRep = new HashMap<>();
        jsonRep.put("nodes", this.nodes);
        jsonRep.put("node_row_ptr", this.nodeRowPointer);
        jsonRep.put("heads", this.heads);
        jsonRep.put("arg_nodes", this.argNodes);
        jsonRep.put("attrs", this.attributes);

        this.jsonRepresentation = jsonRep;
    }

    private void fixNodeInputs(){
        if (this.nodeDifference > 0){
            for(int i=0; i<this.nodes.size(); i++){
                Map<String,Object> node = (Map<String, Object>) nodes.get(i);
                ArrayList<Object> inputs = (ArrayList<Object>) node.get("inputs");
                if (inputs.size() > 0){
                    for (Object inputArray: inputs){
                        ArrayList<Integer> inputIntegers = (ArrayList<Integer>) inputArray;
                        for (int j=0; j<inputIntegers.size(); j++){
                            int elementValue = inputIntegers.get(j);

                            if (elementValue > 0 && elementValue-this.nodeDifference >= 0) {
                                inputIntegers.set(j, elementValue-this.nodeDifference);
                            }
                        }
                    }
                }
            }
        }

        Map<String, Object> lastNode = (Map<String, Object>) nodes.get(nodes.size()-1);
        ArrayList<Object> lastInputs = (ArrayList<Object>) lastNode.get("inputs");

        if (lastInputs.size() > 0) return;

        ArrayList<Integer> lastInputIntList = new ArrayList<>();
        lastInputIntList.add(nodes.size()-2);

        Map<String, Object> penultimateNode = (Map<String, Object>) nodes.get(nodes.size()-2);
        ArrayList<Object> penultimateInputs = (ArrayList<Object>) penultimateNode.get("inputs");
        ArrayList<Integer> penultimateInputIntList = (ArrayList<Integer>) penultimateInputs.get(0);
        int inputSize = penultimateInputIntList.size()-1;

        for (int i=0; i<inputSize; i++){
            lastInputIntList.add(0);
        }

        lastInputs.add(lastInputIntList);
    }

    private void fixNodeNumberRefs(){
        for (int i=0; i<this.nodes.size(); i++){
            nodeRowPointer.add(i);

            Map<String,Object> node = (Map<String, Object>) nodes.get(i);
            String op = (String) node.get("op");
            String name = (String) node.get("name");
            if (op.equals("null") && !name.equals("") && !name.equals("null")){
                this.argNodes.add(i);
                this.parameterLayerCandidates.add(name);
            }
        }

        ArrayList<Integer> newHeads = new ArrayList<>();
        newHeads.add(nodeRowPointer.size()-1);
        for (int i=0; i < this.headSize; i++){
            newHeads.add(0);
        }
        heads.add(newHeads);
    }
}
