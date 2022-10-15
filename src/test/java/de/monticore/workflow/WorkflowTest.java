package de.monticore.workflow;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.parser.ConfFile2ConfigurationParser;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowTest extends TestCase {

    @Test
    public void testConstructor() {
        AutoMLWorkflow workflow = new AutoMLWorkflow();
        assertNotNull(workflow);
    }

    @Test
    public void testExecuteSetsArchitecture() {
        AutoMLWorkflow workflow = createWorkflow();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        workflow.execute(architecture, null);
        assertNotNull(workflow.getArchitecture());
    }

    @Test
    public void testExecuteSetsConfiguration() {
        AutoMLWorkflow workflow = createWorkflow();
        String configurationName = "configuration";
        ArchitectureSymbol architecture = mock(ArchitectureSymbol.class);
        workflow.execute(architecture, configurationName);
        assertNotNull(workflow.getConfiguration());
    }

    @Test
    public void testSetResourcePath() {
        String resourcePath = "resourcePath";
        AutoMLWorkflow workflow = createWorkflow();
        workflow.setResourcePath(resourcePath);
        assertEquals(resourcePath, workflow.getResourcePath());
    }

    private AutoMLWorkflow createWorkflow() {
        ConfFile2ConfigurationParser parser = mock(ConfFile2ConfigurationParser.class);
        Configuration configuration = new Configuration();
        when(parser.getConfiguration(any(), any())).thenReturn(configuration);
        AutoMLWorkflow workflow = new AutoMLWorkflow();
        workflow.setConfFile2ConfigurationParser(parser);

        return workflow;
    }
}