package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.se_rwth.commons.logging.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class NetworkStructureScanner {
    public NetworkStructureScanner() {

    }

    public boolean isComposedNet(String searched) {
        ArrayList<String> networks = new ArrayList<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("ComposedNetworks.txt"));
            String line = reader.readLine();
            while (line != null) {
                line = line.replaceAll("\n", "");
                networks.add(line);
                line = reader.readLine();
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (networks.contains(searched)) {
            Log.info("COMPOSED NETWORK HIT","COMPOSED_NETWORK_HIT");
            return true;
        }

        return false;

    }


    /*
    public void transformNetwork(Set<EMAComponentInstanceSymbol> allInstances) {

        Set<EMAComponentInstanceSymbol> networkInstances = new HashSet<>();

        for (EMAComponentInstanceSymbol componentInstance : allInstances){
            EMAComponentSymbol component = componentInstance.getComponentType().getReferencedSymbol();
            Optional<ArchitectureSymbol> architecture = component.getSpannedScope().resolve("", ArchitectureSymbol.KIND);

            //TODO: FIlter out duplicates
            if (architecture.isPresent() /* && !(networkInstances.contains(componentInstance))*/// ) networkInstances.add(componentInstance);
        //}

        /*
        Log.info("networkInstances size: " + String.valueOf(networkInstances.size()),"TRANSFORM_NETWORK");
        for (EMAComponentInstanceSymbol symbol : networkInstances){
            Log.info("TO_STRING: " + symbol.toString(),"TRANSFORM_NETWORK");
        }

        EMAComponentInstanceSymbol root = getNetworkRoot(networkInstances);

    }

    public EMAComponentInstanceSymbol getNetworkRoot(Set<EMAComponentInstanceSymbol> networkInstances){




        return null;
    }
    */
}
