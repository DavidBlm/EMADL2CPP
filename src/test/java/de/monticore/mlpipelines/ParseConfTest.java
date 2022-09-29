package de.monticore.mlpipelines;

import conflang.ConfLangFacade;
import conflang._symboltable.ConfigurationScope;
import de.monticore.symboltable.Symbol;

import junit.framework.TestCase;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

public class ParseConfTest extends TestCase {

    public void testParseConfFile() {
        Path modelPath = Paths.get("src/test/resources/models");
        ConfLangFacade confLangFacade = ConfLangFacade.create(modelPath, "VGG16.conf");
        ConfigurationScope artifactScope = confLangFacade.getArtifactScope();

        Map<String, Collection<Symbol>> symbols = artifactScope.getLocalSymbols();

        assertEquals(artifactScope.getSymbolsSize(), 6);
        assertTrue(symbols.containsKey("learning_method"));
        assertTrue(symbols.containsKey("num_epoch"));
        assertTrue(symbols.containsKey("batch_size"));
        assertTrue(symbols.containsKey("normalize"));
        assertTrue(symbols.containsKey("load_checkpoint"));
        assertTrue(symbols.containsKey("optimizer"));
    }
}
