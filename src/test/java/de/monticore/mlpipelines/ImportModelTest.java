package de.monticore.mlpipelines;

import com.google.common.io.Resources;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;
import junit.framework.TestCase;
import org.junit.Test;

import java.net.URL;

import static de.monticore.lang.monticar.emadl.generator.EMADLAbstractSymtab.createSymTab;

public class ImportModelTest extends TestCase {

    @Test
    public void testLoadModelAsArchitectureSymbol() {
        Scope symTab = createSymTab("src/test/resources/models");
        EMAComponentInstanceSymbol c = symTab.<EMAComponentInstanceSymbol>resolve("vGG16",
                EMAComponentInstanceSymbol.KIND).orElse(null);
        ArchitectureSymbol arch1 = c.getSpannedScope().<ArchitectureSymbol>resolve("", ArchitectureSymbol.KIND).get();
        assertNotNull(arch1);
    }
}