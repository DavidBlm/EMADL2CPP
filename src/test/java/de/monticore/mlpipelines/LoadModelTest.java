package de.monticore.mlpipelines;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.symboltable.CommonScope;
import de.monticore.symboltable.Scope;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static de.monticore.lang.monticar.emadl.generator.EMADLAbstractSymtab.createSymTab;

public class LoadModelTest extends TestCase {

    @Test
    public void testLoadModelAsArchitectureSymbol() {
        Scope symTab = createSymTab("src/test/resources/models");
        EMAComponentInstanceSymbol c = symTab.<EMAComponentInstanceSymbol>resolve("efficientNetB0",
                EMAComponentInstanceSymbol.KIND).orElse(null);
        List<? extends Scope> subScopes = c.getEnclosingScope().getSubScopes();
        for (Scope s : subScopes) {
            int symbolsSize = s.getSubScopes().get(0).getSymbolsSize();
            System.out.println("Symbols size: " + symbolsSize);
        }

        ArchitectureSymbol arch1 = c.getSpannedScope().<ArchitectureSymbol>resolve("", ArchitectureSymbol.KIND).get();
        assertNotNull(arch1);
    }
}