/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator;

import de.monticore.lang.monticar.generator.cpp.GeneratorCppCli;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.*;

public class EMADLGeneratorCli {

    public static final Option OPTION_MODELS_PATH = Option.builder("m")
            .longOpt("models-dir")
            .desc("full path to directory with EMADL models e.g. C:\\Users\\vpupkin\\proj\\MyAwesomeAutopilot\\src\\main\\emam")
            .hasArg(true)
            .required(true)
            .build();

    public static final Option OPTION_ROOT_MODEL = Option.builder("r")
            .longOpt("root-model")
            .desc("fully qualified name of the root model e.g. de.rwth.vpupkin.modeling.mySuperAwesomeAutopilotComponent")
            .hasArg(true)
            .required(true)
            .build();

    public static final Option OPTION_OUTPUT_PATH = Option.builder("o")
            .longOpt("output-dir")
            .desc("full path to output directory for tests e.g. C:\\Users\\vpupkin\\proj\\MyAwesomeAutopilot\\target\\gen-cpp")
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
        Options options = GeneratorCppCli.getOptions();
        options.addOption(OPTION_MODELS_PATH);
        options.addOption(OPTION_ROOT_MODEL);
        options.addOption(OPTION_OUTPUT_PATH);
        options.addOption(OPTION_FLAG_TESTS);
        options.addOption(OPTION_FLAG_ARMADILLO);
        options.addOption(OPTION_FLAG_AUTOPILOT_ADAPTER);
        options.addOption(OPTION_FLAG_CHECK_MODEL_DIR);
        options.addOption(OPTION_FLAG_SERVER_WRAPPER);
        options.addOption(OPTION_FLAG_ALGEBRAIC);
        options.addOption(OPTION_FLAG_THREADING);
        options.addOption(OPTION_FLAG_EXEC_LOGGING);
        options.addOption(OPTION_FLAG_CMAKE);

        options.addOption(OPTION_BACKEND);
        options.addOption(OPTION_RESTRAINED_TRAINING);
        options.addOption(OPTION_TRAINING_PYTHON_PATH);
        options.addOption(OPTION_COMPILE);
        return options;
    }

    // Add EMADL Options
    public static void addEMADL2CPPOptions(Options options) {
        options.addOption(OPTION_BACKEND);
        options.addOption(OPTION_RESTRAINED_TRAINING);
        options.addOption(OPTION_TRAINING_PYTHON_PATH);
        options.addOption(OPTION_COMPILE);
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
        Path modelsDirPath = Paths.get(cliArgs.getOptionValue(OPTION_MODELS_PATH.getOpt()));
        generator.getEmamGen().setUseAlgebraicOptimizations(false);
        generator.getEmamGen().setUseThreadingOptimization(false);
        generator.getEmamGen().setModelsDirPath(modelsDirPath);
//        generator.getEmamGen().setGenerationTargetPath(outputPath); // done by EMADLGenerator
        generator.getEmamGen().setGenerateTests(cliArgs.hasOption(OPTION_FLAG_TESTS.getOpt()));
        if (cliArgs.hasOption(OPTION_FLAG_ARMADILLO.getOpt())) {
            generator.getEmamGen().useArmadilloBackend();
        }
        generator.getEmamGen().setCheckModelDir(cliArgs.hasOption(OPTION_FLAG_CHECK_MODEL_DIR.getLongOpt()));
        generator.getEmamGen().setGenerateServerWrapper(cliArgs.hasOption(OPTION_FLAG_SERVER_WRAPPER.getLongOpt()));
        generator.getEmamGen().setGenerateAutopilotAdapter(cliArgs.hasOption(OPTION_FLAG_AUTOPILOT_ADAPTER.getLongOpt()));

        generator.getEmamGen().setUseAlgebraicOptimizations(cliArgs.hasOption(OPTION_FLAG_ALGEBRAIC.getLongOpt()));
        generator.getEmamGen().setUseThreadingOptimization(cliArgs.hasOption(OPTION_FLAG_THREADING.getLongOpt()));
        generator.getEmamGen().setExecutionLoggingActive(cliArgs.hasOption(OPTION_FLAG_EXEC_LOGGING.getLongOpt()));
        generator.getEmamGen().setGenerateCMake(cliArgs.hasOption(OPTION_FLAG_CMAKE.getLongOpt()));
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
