package de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.gluon;

import java.util.ArrayList;

public class LayerSubstitute {
    private ArrayList<String> allowedSubstitues = new ArrayList<>();
    private String originalLayerName = null;

    public LayerSubstitute(String originalLayerName){
        this.originalLayerName = originalLayerName;
    }

    public LayerSubstitute(String originalLayerName, ArrayList<String> allowedSubstitues){
        this.originalLayerName = originalLayerName;
        this.allowedSubstitues = allowedSubstitues;
    }

    public void addSubstitute(String sub){
        this.allowedSubstitues.add(sub);
    }

    public boolean isAllowedSubstitute(String layer){
        for (String sub: this.allowedSubstitues){
            if (sub.equals(layer)) return true;
        }
        return false;
    }

    public String getLayerSubstitute(String layer){
        if (isAllowedSubstitute(layer)){
            for (String sub : this.allowedSubstitues){
                if (sub.equals(layer)) return sub;
            }
        }
        return null;
    }

    public boolean hasLayerSubstitute(String originalLayerName, String searchedSubstitute){
        for (String sub : this.allowedSubstitues){
            if (this.originalLayerName.equals(originalLayerName) && sub.equals(searchedSubstitute)) return true;
        }
        return false;
    }

    public String getOriginalLayerName(){
        return this.originalLayerName;
    }
}
