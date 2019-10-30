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
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class GenerationTest extends AbstractSymtabTest {
    @Before
    public void setUp() {
        // ensure an empty log
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }

    @Test
    public void testCifar10Generation() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "cifar10.Cifar10Classifier", "-b", "MXNET", "-f",
                "n", "-c", "n" };
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code"),
                Arrays.asList(
                        "cifar10_cifar10Classifier.cpp",
                        "cifar10_cifar10Classifier.h",
                        "CNNCreator_cifar10_cifar10Classifier_net.py",
                        "CNNBufferFile.h",
                        "CNNPredictor_cifar10_cifar10Classifier_net.h",
                        "cifar10_cifar10Classifier_net.h",
                        "CNNTranslator.h",
                        "cifar10_cifar10Classifier_calculateClass.h",
                        "CNNTrainer_cifar10_cifar10Classifier_net.py"));
    }

    @Test
    public void testSimulatorGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "simulator.MainController", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());
    }

    @Test
    public void testAddGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "Add", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());
    }

    @Test
    public void testAlexnetGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "Alexnet", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());
    }

    @Test
    public void testResNeXtGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "ResNeXt50", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());
    }

    /*@Test
    public void testThreeInputGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "ThreeInputCNN_M14", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().size() == 1);
    }

    @Test
    public void testMultipleOutputsGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "MultipleOutputs", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().size() == 1);
    }*/

    @Test
    public void testVGGGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "VGG16", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());
    }

    @Test
    public void testMultipleInstances() throws IOException, TemplateException {
        try {
            Log.getFindings().clear();
            String[] args = {"-m", "src/test/resources/models/", "-r", "InstanceTest.MainB", "-b", "MXNET", "-f", "n", "-c", "n"};
            EMADLGeneratorCli.main(args);
            assertTrue(Log.getFindings().isEmpty());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMnistClassifierForCaffe2() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "mnist.MnistClassifier", "-b", "CAFFE2", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code"),
                Arrays.asList(
                        "mnist_mnistClassifier.cpp",
                        "mnist_mnistClassifier.h",
                        "CNNCreator_mnist_mnistClassifier_net.py",
                        "CNNPredictor_mnist_mnistClassifier_net.h",
                        "mnist_mnistClassifier_net.h",
                        "CNNTranslator.h",
                        "mnist_mnistClassifier_calculateClass.h",
                        "CNNTrainer_mnist_mnistClassifier_net.py"));
    }

    @Ignore
    @Test
    public void testMnistClassifierForGluon() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "mnist.MnistClassifier", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());

        /*checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon"),
                Arrays.asList(
                        "CNNBufferFile.h",
                        "CNNNet_mnist_mnistClassifier_net.py",
                        "mnist_mnistClassifier.cpp",
                        "mnist_mnistClassifier.h",
                        "CNNCreator_mnist_mnistClassifier_net.py",
                        "CNNPredictor_mnist_mnistClassifier_net.h",
                        "CNNDataLoader_mnist_mnistClassifier_net.py",
                        "CNNSupervisedTrainer_mnist_mnistClassifier_net.py",
                        "mnist_mnistClassifier_net.h",
                        "HelperA.h",
                        "CNNTranslator.h",
                        "mnist_mnistClassifier_calculateClass.h",
                        "CNNTrainer_mnist_mnistClassifier_net.py",
                        "mnist_mnistClassifier_net.h"));*/
    }

    @Test
    public void testInvariantForGluon() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "Invariant", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().size() == 0);
    }


    @Test
    public void testRNNtestForGluon() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "RNNtest", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().size() == 0);
    }


    /*@Test
    public void testGluonReinforcementModelGymEnvironment() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/reinforcementModel", "-r", "cartpole.Master", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().stream().filter(Finding::isError).collect(Collectors.toList()).isEmpty());
        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon/reinforcementModel/cartpole"),
                Arrays.asList(
                        "cartpole_master.cpp",
                        "cartpole_master.h",
                        "cartpole_master_dqn.h",
                        "cartpole_master_policy.h",
                        "CMakeLists.txt",
                        "CNNBufferFile.h",
                        "CNNCreator_cartpole_master_dqn.py",
                        "CNNNet_cartpole_master_dqn.py",
                        "CNNPredictor_cartpole_master_dqn.h",
                        "CNNTrainer_cartpole_master_dqn.py",
                        "CNNTranslator.h",
                        "HelperA.h",
                        "start_training.sh",
                        "reinforcement_learning/__init__.py",
                        "reinforcement_learning/strategy.py",
                        "reinforcement_learning/agent.py",
                        "reinforcement_learning/environment.py",
                        "reinforcement_learning/replay_memory.py",
                        "reinforcement_learning/util.py",
                        "reinforcement_learning/cnnarch_logger.py"
                )
        );
    }*/

    @Test
    public void testHashFunction() {
        EMADLGenerator tester = new EMADLGenerator(Backend.MXNET);
        
        try{
            tester.getChecksumForFile("invalid Path!");
            assertTrue("Hash method should throw IOException on invalid path", false);
        } catch(IOException e){
        }
    }

    /*@Test
    public void gluonDdpgTest() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/reinforcementModel", "-r", "mountaincar.Master", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertEquals(0, Log.getFindings().stream().filter(Finding::isError).count());

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon/reinforcementModel/mountaincar"),
                Arrays.asList(
                        "mountaincar_master.cpp",
                        "mountaincar_master.h",
                        "mountaincar_master_actor.h",
                        "CMakeLists.txt",
                        "CNNBufferFile.h",
                        "CNNCreator_mountaincar_master_actor.py",
                        "CNNNet_mountaincar_master_actor.py",
                        "CNNPredictor_mountaincar_master_actor.h",
                        "CNNTrainer_mountaincar_master_actor.py",
                        "CNNTranslator.h",
                        "HelperA.h",
                        "start_training.sh",
                        "reinforcement_learning/__init__.py",
                        "reinforcement_learning/CNNCreator_mountaincar_agent_mountaincarCritic.py",
                        "reinforcement_learning/CNNNet_mountaincar_agent_mountaincarCritic.py",
                        "reinforcement_learning/strategy.py",
                        "reinforcement_learning/agent.py",
                        "reinforcement_learning/environment.py",
                        "reinforcement_learning/replay_memory.py",
                        "reinforcement_learning/util.py",
                        "reinforcement_learning/cnnarch_logger.py"
                )
        );
    }*/

    @Test
    public void testAlexNetTagging() {
        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.Alexnet", "-b", "MXNET", "-f", "n", "-c",
                "n" };
        EMADLGeneratorCli.main(args);
        assertEquals(Log.getFindings().size(), 1);
        assertEquals(Log.getFindings().get(0).toString(),
                "Tagging info for symbol was found, ignoring data_paths.txt: src/test/resources/models");
        assertTrue(Log.getErrorCount() == 0);
    }

    @Test
    public void testAlexNetTaggingForInstances() {
        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.Parent", "-b", "MXNET", "-f", "n", "-c",
                "n" };
        EMADLGeneratorCli.main(args);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidPathCoCos() {
        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.AlexnetInvalid", "-b", "MXNET", "-f", "n",
                "-c", "n" };
        EMADLGeneratorCli.main(args);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidPathCoCosInstances() {
        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.ParentInvalidPath", "-b", "MXNET", "-f",
                "n", "-c", "n" };
        EMADLGeneratorCli.main(args);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidTypeCocos() {

        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.AlexnetInvalidType", "-b", "MXNET", "-f",
                "n", "-c", "n" };
        EMADLGeneratorCli.main(args);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidTypeCocosInstances() {

        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.ParentInvalidType", "-b", "MXNET", "-f",
                "n", "-c", "n" };
        EMADLGeneratorCli.main(args);
    }
}
