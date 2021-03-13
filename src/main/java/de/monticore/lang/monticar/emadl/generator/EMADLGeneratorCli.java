/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator;

import de.monticore.lang.monticar.generator.cpp.GeneratorCPP;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.*;

public class EMADLGeneratorCli {

    public static final Option OPTION_OUTPUT_PATH = Option.builder("o")
            .longOpt("output-dir")
            .desc("full path to output directory for tests e.g. C:\\Users\\vpupkin\\proj\\MyAwesomeAutopilot\\target\\gen-cpp\n" +
                    "default is ./target/generated-sources-emadl/")
            .hasArg(true)
            .required(false)
            .build();
    
    public static final Option OPTION_BACKEND = Option.builder("b")
            .longOpt("backend")
            .desc("deep-learning-framework backend. Options: MXNET, CAFFE2, GLUON, TENSORFLOW")
            .hasArg(true)
            .required(false)
            .build();

    public static final Option OPTION_TRAINING_PYTHON_PATH = Option.builder("p")
            .longOpt("python")
            .desc("path to python. Default is /usr/bin/python, or python (PATH) for windows")
            .hasArg(true)
            .required(false)
            .build();

    public static final Option OPTION_RESTRAINED_TRAINING = Option.builder("f")
            .longOpt("forced")
            .desc("no training or a forced training. Options: y (a forced training), n (no training)")
            .hasArg(true)
            .required(false)
            .build();

    public static final Option OPTION_COMPILE = Option.builder("c")
            .longOpt("compile")
            .desc("Compile the generated c code. Options: y (compile), n (don't compile). Default is y")
            .hasArg(true)
            .required(false)
            .build();

    public static final Option OPTION_CUSTOM_PYTHON_FILES_PATH = Option.builder("cpf")
            .longOpt("custom-python-files-path")
            .desc("full path to directory with custom python files exp. layers e.g. C::\\Users\\vpupkin\\prok\\My>AwsomeAutopilot\\src\\main\\python")
            .hasArg(true)
            .required(false)
            .build();

    private EMADLGeneratorCli() {
    }

    public static void main(String[] args) {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cliArgs = parseArgs(options, parser, args);
        if (cliArgs != null) {
            runGenerator(cliArgs);
        }
    }

    public static Options getOptions() {
        Options options = new Options();
        addBaseOptions(options);
        addEMAM2CPPOptions(options);
        addEMADL2CPPOptions(options);
        return options;
    }

    // Add EMADL2CPP Options
    public static void addEMADL2CPPOptions(Options options) {
        options.addOption(OPTION_BACKEND);
        options.addOption(OPTION_RESTRAINED_TRAINING);
        options.addOption(OPTION_TRAINING_PYTHON_PATH);
        options.addOption(OPTION_COMPILE);
        options.addOption(OPTION_CUSTOM_PYTHON_FILES_PATH);
    }

    private static CommandLine parseArgs(Options options, CommandLineParser parser, String[] args) {
        CommandLine cliArgs;
        try {
            cliArgs = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("argument parsing exception: " + e.getMessage());
            System.exit(1);
            return null;
        }
        return cliArgs;
    }

    private static void runGenerator(CommandLine cliArgs) {
        String rootModelName = cliArgs.getOptionValue(OPTION_ROOT_MODEL.getOpt());
        String outputPath = cliArgs.getOptionValue(OPTION_OUTPUT_PATH.getOpt());
        String backendString = cliArgs.getOptionValue(OPTION_BACKEND.getOpt());
        String forced = cliArgs.getOptionValue(OPTION_RESTRAINED_TRAINING.getOpt());
        String pythonPath = cliArgs.getOptionValue(OPTION_TRAINING_PYTHON_PATH.getOpt());
        String compile = cliArgs.getOptionValue(OPTION_COMPILE.getOpt());
        final String DEFAULT_BACKEND = "MXNET";
        final String DEFAULT_FORCED = "UNSET";
        final String DEFAULT_COMPILE = "y";

        if (backendString == null) {
            Log.warn("backend not specified. backend set to default value " + DEFAULT_BACKEND);
            backendString = DEFAULT_BACKEND;
        }

        Optional<Backend> backend = Backend.getBackendFromString(backendString);
        if (!backend.isPresent()){
            Log.warn("specified backend " + backendString + " not supported. backend set to default value " + DEFAULT_BACKEND);
            backend = Backend.getBackendFromString(DEFAULT_BACKEND);
        }

        if (pythonPath == null) {
            pythonPath = SystemUtils.IS_OS_WINDOWS ? "python" : "/usr/bin/python";
        }

        if (forced == null) {
            forced = DEFAULT_FORCED;
        }
        else if (!forced.equals("y") && !forced.equals("n")) {
            Log.warn("specified setting ("+forced+") for forcing/preventing training not supported. set to default value " + DEFAULT_FORCED);
            forced = DEFAULT_FORCED;
        }

        EMADLGenerator generator = new EMADLGenerator(backend.get());

        if (compile == null) {
            compile = DEFAULT_COMPILE;
        }
        else if(!compile.equals("y") && !compile.equals("n")) {
            Log.warn("specified setting ("+compile+") for skipping the compilation not supported. set to default value " + DEFAULT_COMPILE);
            compile = DEFAULT_COMPILE;
        }

        if (outputPath != null){
            generator.setGenerationTargetPath(outputPath);
        }

        generator.setGenerateCMake(true);


        // EMAM2CPP options
        GeneratorCPP emamGen = generator.getEmamGen();
        Path modelsDirPath = Paths.get(cliArgs.getOptionValue(OPTION_MODELS_PATH.getOpt()));
        if (cliArgs.hasOption(OPTION_CUSTOM_PYTHON_FILES_PATH.getOpt())) {
            generator.setCustomPythonFilesPath(cliArgs.getOptionValue(OPTION_CUSTOM_PYTHON_FILES_PATH.getOpt()));
        }
        emamGen.setUseAlgebraicOptimizations(false);
        emamGen.setUseThreadingOptimization(false);
        emamGen.setModelsDirPath(modelsDirPath);
//        emamGen.setGenerationTargetPath(outputPath); // done by EMADLGenerator
        emamGen.setGenerateTests(cliArgs.hasOption(OPTION_FLAG_TESTS.getOpt()));

        emamGen.setImportArmadillo(cliArgs.hasOption(OPTION_IMPORT_ARMADILLO.getLongOpt()));
        emamGen.setGenerateDynamicInterface(cliArgs.hasOption(OPTION_FLAG_DYNAMIC_INTERFACE.getLongOpt()));
        emamGen.setGenerateServerAdapter(cliArgs.hasOption(OPTION_FLAG_GEN_TCP_SERVER.getLongOpt()));
        emamGen.setGenerateDDCAdapter(cliArgs.hasOption(OPTION_FLAG_GEN_DDC_ADAPTER.getLongOpt()));
        emamGen.setOutputName(cliArgs.getOptionValue(OPTION_OUTPUT_NAME.getOpt()));
        if (cliArgs.hasOption(OPTION_FLAG_ARMADILLO.getOpt())) {
            emamGen.useArmadilloBackend();
        }
        emamGen.setCheckModelDir(cliArgs.hasOption(OPTION_FLAG_CHECK_MODEL_DIR.getLongOpt()));
        emamGen.setGenerateServerWrapper(cliArgs.hasOption(OPTION_FLAG_SERVER_WRAPPER.getLongOpt()));

        emamGen.setUseAlgebraicOptimizations(cliArgs.hasOption(OPTION_FLAG_ALGEBRAIC.getLongOpt()));
        emamGen.setUseThreadingOptimization(cliArgs.hasOption(OPTION_FLAG_THREADING.getLongOpt()));
        emamGen.setExecutionLoggingActive(cliArgs.hasOption(OPTION_FLAG_EXEC_LOGGING.getLongOpt()));
        emamGen.setGenerateCMake(cliArgs.hasOption(OPTION_FLAG_CMAKE.getLongOpt()));
        // end EMAM2CPP options


        try{
            generator.generate(cliArgs.getOptionValue(OPTION_MODELS_PATH.getOpt()), rootModelName, pythonPath, forced, compile.equals("y"));
        }
        catch (IOException e){
            Log.error("io error during generation", e);
            System.exit(1);
        }
        catch (TemplateException e){
            Log.error("template error during generation", e);
            System.exit(1);
        }
    }
}
