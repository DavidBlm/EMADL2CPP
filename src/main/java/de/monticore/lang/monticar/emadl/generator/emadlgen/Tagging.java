package de.monticore.lang.monticar.emadl.generator.emadlgen;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstantiationSymbol;
import de.monticore.lang.monticar.emadl.tagging.artifacttag.LayerArtifactParameterSymbol;
import de.monticore.lang.monticar.emadl.tagging.dltag.LayerPathParameterSymbol;
import de.monticore.lang.tagging._symboltable.TagSymbol;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;
import de.monticore.lang.monticar.semantics.Constants;
import de.monticore.lang.monticar.semantics.util.BasicLibrary;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Tagging {

    private Generator emadlGen;

    public Tagging(Generator emadlGen){
        this.emadlGen =  emadlGen;
    }

    protected TaggingResolver getSymTabAndTaggingResolver() {
        BasicLibrary.extract();
        return AbstractSymtab.createSymTabAndTaggingResolver(emadlGen.getEmadlFileHandler().getCustomFilesPath(), emadlGen.getEmadlFileHandler().getPythonPath(), emadlGen.getBackend(), emadlGen.getComposedNetworkFilePath(), emadlGen.getEmadlFileHandler()
                        .getModelsPath(),
                Constants.SYNTHESIZED_COMPONENTS_ROOT, BasicLibrary.BASIC_LIBRARY_ROOT);
    }

    protected HashMap getLayerPathParameterTags(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance) {
        List<TagSymbol> instanceTags = new LinkedList<>();

        boolean isChildComponent = instance.getEnclosingComponent().isPresent();

        if (isChildComponent) {
            // get all instantiated components of parent
            List<EMAComponentInstantiationSymbol> instantiationSymbols = (List<EMAComponentInstantiationSymbol>) instance
                    .getEnclosingComponent().get().getComponentType().getReferencedSymbol().getSubComponents();

            // filter corresponding instantiation of instance and add tags
            instantiationSymbols.stream().filter(e -> e.getName().equals(instance.getName())).findFirst()
                    .ifPresent(symbol -> instanceTags.addAll(taggingResolver.getTags(symbol, LayerPathParameterSymbol.KIND)));
        }

        List<TagSymbol> tags = !instanceTags.isEmpty() ? instanceTags
                : (List<TagSymbol>) taggingResolver.getTags(component, LayerPathParameterSymbol.KIND);

        HashMap layerPathParameterTags = new HashMap();
        if (!tags.isEmpty()) {
            for (TagSymbol tag : tags) {
                LayerPathParameterSymbol layerPathParameterSymbol = (LayerPathParameterSymbol) tag;
                layerPathParameterTags.put(layerPathParameterSymbol.getId(), layerPathParameterSymbol.getPath());
            }
            // TODO: Replace warinings with errors, until then use this method
            emadlGen.stopGeneratorIfWarning();
            Log.warn("Tagging info for LayerPathParameter symbols was found.");
        }
        return layerPathParameterTags;
    }

    protected HashMap getLayerArtifactParameterTags(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance){
        List<TagSymbol> instanceTags = new LinkedList<>();

        boolean isChildComponent = instance.getEnclosingComponent().isPresent();

        if (isChildComponent) {
            // get all instantiated components of parent
            List<EMAComponentInstantiationSymbol> instantiationSymbols = (List<EMAComponentInstantiationSymbol>) instance
                    .getEnclosingComponent().get().getComponentType().getReferencedSymbol().getSubComponents();

            // filter corresponding instantiation of instance and add tags
            instantiationSymbols.stream().filter(e -> e.getName().equals(instance.getName())).findFirst()
                    .ifPresent(symbol -> instanceTags.addAll(taggingResolver.getTags(symbol, LayerArtifactParameterSymbol.KIND)));
        }

        List<TagSymbol> tags = !instanceTags.isEmpty() ? instanceTags
                : (List<TagSymbol>) taggingResolver.getTags(component, LayerArtifactParameterSymbol.KIND);

        HashMap layerArtifactParameterTags = new HashMap();
        String localRepo = System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository";
        if (!tags.isEmpty()) {
            for(TagSymbol tag: tags) {
                LayerArtifactParameterSymbol layerArtifactParameterSymbol = (LayerArtifactParameterSymbol) tag;
                String path = emadlGen.getEmadlFileHandler().getArtifactDestination(localRepo, layerArtifactParameterSymbol.getArtifact(), layerArtifactParameterSymbol.getJar());
                layerArtifactParameterTags.put(layerArtifactParameterSymbol.getId(), path);
            }
            emadlGen.stopGeneratorIfWarning();
            Log.warn("Tagging info for LayerArtifact symbols was found.");
        }
        return layerArtifactParameterTags;
    }
}
