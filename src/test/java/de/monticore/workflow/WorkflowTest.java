package de.monticore.workflow;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.parser.ConfFile2ConfigurationParser;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowTest extends TestCase {

    @Test
    public void testConstructor() {
        Workflow workflow = new Workflow();
        assertNotNull(workflow);
    }

    @Test
    public void testExecuteSetsArchitecture() {
        Workflow workflow = createWorkflow();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        workflow.execute(architecture, null);
        assertNotNull(workflow.getArchitecture());
    }

    @Test
    public void testExecuteSetsConfiguration() {
        Workflow workflow = createWorkflow();
        String configurationName = "configuration";
        workflow.execute(null, configurationName);
        assertNotNull(workflow.getConfiguration());
    }

    @Test
    public void testSetResourcePath() {
        String resourcePath = "resourcePath";
        Workflow workflow = createWorkflow();
        workflow.setResourcePath(resourcePath);
        assertEquals(resourcePath, workflow.getResourcePath());
    }

    private Workflow createWorkflow() {
        ConfFile2ConfigurationParser parser = mock(ConfFile2ConfigurationParser.class);
        Configuration configuration = new Configuration();
        when(parser.getConfiguration(any(), any())).thenReturn(configuration);
        Workflow workflow = new Workflow();
        workflow.setConfFile2ConfigurationParser(parser);

        return workflow;
    }
}