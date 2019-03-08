/**
 *
 *  ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package de.monticore.lang.monticar.emadl;

import de.monticore.lang.monticar.emadl.generator.Backend;
import de.monticore.lang.monticar.emadl.generator.EMADLGenerator;
import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class IntegrationCaffe2Test extends AbstractSymtabTest {

    private Path netTrainingHashFile = Paths.get("./target/generated-sources-emadl/simpleCifar10/CifarNetwork.training_hash");

    private void createHashFile() {
        try {
            netTrainingHashFile.toFile().getParentFile().mkdirs();
            List<String> lines = Arrays.asList("39253EC049D4A4E5FA0536AD34874B9D#1DBAEE1B1BD83FB7CB5F70AE91B29638#13D139510DC5681639AA91D7250288D3#1A42D4842D0664937A9F6B727BD60CEF");
            Files.write(netTrainingHashFile, lines, Charset.forName("UTF-8"));
        }
        catch(Exception e) {
            assertFalse("Hash file could not be created", true);
        }
    }

    private void deleteHashFile() {
        try {
            Files.delete(netTrainingHashFile);
        }
        catch(Exception e) {
            assertFalse("Could not delete hash file", true);
        }
    }

    @Before
    public void setUp() {
        // ensure an empty log
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }

    @Test
    public void testDontRetrain1() {
        // The training hash is stored during the first training, so the second one is skipped
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "simpleCifar10.Cifar10Classifier", "-b", "CAFFE2"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());
        
        Log.getFindings().clear();
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().size() == 1);
        assertTrue(Log.getFindings().get(0).getMsg().contains("skipped"));

        deleteHashFile();
    }

 @Test
    public void testDontRetrain2() {
        // The training hash is written manually, so even the first training should be skipped
        Log.getFindings().clear();
        createHashFile();

        String[] args = {"-m", "src/test/resources/models/", "-r", "simpleCifar10.Cifar10Classifier", "-b", "CAFFE2"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().size() == 1);
        assertTrue(Log.getFindings().get(0).getMsg().contains("skipped"));

        deleteHashFile();
    }

    @Test
    public void testDontRetrain3() {
        // Multiple instances of the first NN are used. Only the first one should cause a training
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "instanceTestCifar.MainC", "-b", "CAFFE2"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().size() == 1);
        assertTrue(Log.getFindings().get(0).getMsg().contains("skipped"));
    }

    @Test
    public void testForceRetrain() {
        // The training hash is written manually, but training is forced
        Log.getFindings().clear();
        createHashFile();

        String[] args = {"-m", "src/test/resources/models/", "-r", "simpleCifar10.Cifar10Classifier", "-b", "CAFFE2", "-f", "y"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());

        deleteHashFile();
    }

    
    
}
