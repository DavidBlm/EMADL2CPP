package de.monticore.mlpipelines;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.symboltable.Scope;
import junit.framework.TestCase;
import org.junit.Test;

import static de.monticore.lang.monticar.emadl.generator.EMADLAbstractSymtab.createSymTab;

public class ModelLoaderTest extends TestCase {

    @Test
    public void testLoadModelAsArchitectureSymbol() {
        String modelFolderPath = "src/test/resources/models";
        String modelName = "efficientNetB0";

        ArchitectureSymbol arch = ModelLoader.load(modelFolderPath, modelName);
        assertNotNull(arch);
    }
}