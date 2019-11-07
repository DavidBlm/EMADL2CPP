/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator;

import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

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
            .desc("path to python. Default is /usr/bin/python")
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
            .desc("Compile the generated c code. Needs to be disabled eg. on Windows. Options: y (compile), n (don't compile). Default is y")
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

    private static Options getOptions() {
        Options options = new Options();
        options.addOption(OPTION_MODELS_PATH);
        options.addOption(OPTION_ROOT_MODEL);
        options.addOption(OPTION_OUTPUT_PATH);
        options.addOption(OPTION_BACKEND);
        options.addOption(OPTION_RESTRAINED_TRAINING);
        options.addOption(OPTION_TRAINING_PYTHON_PATH);
        options.addOption(OPTION_COMPILE);
        return options;
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
            pythonPath = "/usr/bin/python";
        }

        if (forced == null) {
            forced = DEFAULT_FORCED;
        }
        else if (!forced.equals("y") && !forced.equals("n")) {
            Log.error("specified setting ("+forced+") for forcing/preventing training not supported. set to default value " + DEFAULT_FORCED);
            forced = DEFAULT_FORCED;
        }

        EMADLGenerator generator = new EMADLGenerator(backend.get());

        if (compile == null) {
            compile = DEFAULT_COMPILE;
        }
        else if(!compile.equals("y") && !compile.equals("n")) {
            Log.error("specified setting ("+compile+") for skipping the compilation not supported. set to default value " + DEFAULT_COMPILE);
            compile = DEFAULT_COMPILE;
        }

        if (outputPath != null){
            generator.setGenerationTargetPath(outputPath);
        }
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
