/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstantiationSymbol;
import de.monticore.lang.math._symboltable.MathStatementsSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;
import de.monticore.lang.monticar.cnnarch.generator.CNNArchGenerator;
import de.monticore.lang.monticar.cnnarch.generator.CNNTrainGenerator;
import de.monticore.lang.monticar.cnnarch.generator.DataPathConfigParser;
import de.monticore.lang.monticar.cnnarch.generator.WeightsPathConfigParser;
import de.monticore.lang.monticar.cnnarch.generator.annotations.ArchitectureAdapter;
import de.monticore.lang.monticar.cnnarch.generator.preprocessing.PreprocessingComponentParameterAdapter;
import de.monticore.lang.monticar.cnnarch.generator.preprocessing.PreprocessingPortChecker;
import de.monticore.lang.monticar.cnnarch.generator.training.TrainingComponentsContainer;
import de.monticore.lang.monticar.cnnarch.generator.training.TrainingConfiguration;
import de.monticore.lang.monticar.cnnarch.generator.validation.TrainedArchitectureChecker;
import de.monticore.lang.monticar.cnnarch.gluongenerator.CNNTrain2Gluon;
import de.monticore.lang.monticar.emadl._cocos.DataPathCocos;
import de.monticore.lang.monticar.emadl._cocos.EMADLCocos;
import de.monticore.lang.monticar.emadl.generator.utils.ChecksumGenerator;
import de.monticore.lang.monticar.emadl.generator.utils.DependencyInstaller;
import de.monticore.lang.monticar.emadl.generator.utils.MavenSettings;
import de.monticore.lang.monticar.emadl.tagging.artifacttag.DatasetArtifactSymbol;
import de.monticore.lang.monticar.emadl.tagging.artifacttag.LayerArtifactParameterSymbol;
import de.monticore.lang.monticar.emadl.tagging.dltag.DataPathSymbol;
import de.monticore.lang.monticar.emadl.tagging.dltag.LayerPathParameterSymbol;
import de.monticore.lang.monticar.generator.EMAMGenerator;
import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.monticar.generator.MathCommandRegister;
import de.monticore.lang.monticar.generator.cmake.CMakeConfig;
import de.monticore.lang.monticar.generator.cpp.*;
import de.monticore.lang.monticar.generator.cpp.converter.TypeConverter;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapperFactory;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapperStandaloneApi;
import de.monticore.lang.monticar.generator.pythonwrapper.symbolservices.data.ComponentPortInformation;
import de.monticore.lang.monticar.semantics.Constants;
import de.monticore.lang.monticar.semantics.util.BasicLibrary;
import de.monticore.lang.tagging._symboltable.TagSymbol;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.Splitters;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static de.monticore.lang.monticar.cnnarch.generator.validation.Constants.ROOT_SCHEMA_MODEL_PATH;

public class EMADLGenerator implements EMAMGenerator {
    private boolean generateCMake = false;
    private CMakeConfig cMakeConfig = new CMakeConfig("");
    private GeneratorCPP emamGen;
    private CNNArchGenerator cnnArchGenerator;
    private CNNTrainGenerator cnnTrainGenerator;
    private GeneratorPythonWrapperStandaloneApi pythonWrapper;
    private Backend backend;

    private String modelsPath;
    private String customFilesPath = "";
    private String pythonPath = "";
    private String adaNetUtils = "./src/main/resources/AdaNet/";
    private boolean useDgl = false;
    private Map<String, ArchitectureSymbol> processedArchitecture;


    public EMADLGenerator(Backend backend) {
        this.backend = backend;
        emamGen = new GeneratorCPP();
        emamGen.useArmadilloBackend();
        emamGen.setGenerationTargetPath("./target/generated-sources-emadl/");
        GeneratorPythonWrapperFactory pythonWrapperFactory = new GeneratorPythonWrapperFactory();
        pythonWrapper = new GeneratorPythonWrapperStandaloneApi();
        cnnArchGenerator = backend.getCNNArchGenerator();
        cnnTrainGenerator = backend.getCNNTrainGenerator();
    }

    public String getAdaNetUtils() {
        return adaNetUtils;
    }

    public void setAdaNetUtils(String adaNetUtils) {
        this.adaNetUtils = adaNetUtils;
    }

    public String getModelsPath() {
        return modelsPath;
    }

    public void setModelsPath(String modelsPath) {
        if (!(modelsPath.endsWith("/"))) {
            this.modelsPath = modelsPath + "/";
        }
        else {
            this.modelsPath = modelsPath;
        }
    }

    public String getCustomFilesPath() { return  customFilesPath; }

    public void setCustomFilesPath(String customPythonFilesPath) {
        if (!(customPythonFilesPath.endsWith("/"))){
            this.customFilesPath = customPythonFilesPath + "/";
        }
        else {
            this.customFilesPath = customPythonFilesPath;
        }
    }

    public boolean getUseDgl() { return useDgl; }

    public void setUseDgl(boolean useDgl){
        this.useDgl = useDgl;
    }

    public String getPythonPath() {return pythonPath;}

    public void setPythonPath (String pythonPath){
        if(!pythonPath.startsWith("/")){
            pythonPath = "/" + pythonPath;
        }
        this.pythonPath = pythonPath;
    }

    public void setGenerationTargetPath(String generationTargetPath){
        if (!(generationTargetPath.endsWith("/"))){
            getEmamGen().setGenerationTargetPath(generationTargetPath + "/");
        }
        else {
            getEmamGen().setGenerationTargetPath(generationTargetPath);
        }
    }

    public String getGenerationTargetPath() {
        return getEmamGen().getGenerationTargetPath();
    }

    public GeneratorCPP getEmamGen() {
        return emamGen;
    }


    private void copyPythonFilesFromResource(String folder) throws URISyntaxException, IOException {
        // this function copys the passed folder to the generation target
        // important for AdaNet
        String jarPath = getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
        String target_path = getGenerationTargetPath() + folder;
        if (!target_path.endsWith("/")) {
            target_path = target_path + '/';
        }
        Files.createDirectories(Paths.get(target_path));
        URI uri = URI.create("jar:file:" + jarPath);
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            for (Path path : Files.walk(fs.getPath(folder)).filter(Files::isRegularFile).collect(Collectors.toList())) {
                if (path.toString().endsWith(".py")) {
                    String destination = target_path + path.getFileName();

                    Files.copy(path, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            setAdaNetUtils(getGenerationTargetPath()+"/"+folder+ "/");
        }catch (UnsupportedOperationException e){
            System.out.println("this should only be printed if the generator is run unpacked");
        }
    }


    public void generate(String modelPath, String qualifiedName, String pythonPath, String forced, boolean doCompile, String useDgl) throws IOException, TemplateException {
        processedArchitecture = new HashMap<>();
        setModelsPath( modelPath );
        setPythonPath(pythonPath);
        setUseDgl(useDgl.equals("y"));
        TaggingResolver symtab = getSymTabAndTaggingResolver();
        EMAComponentInstanceSymbol instance = resolveComponentInstanceSymbol(qualifiedName, symtab);
        try {
            // copy the AdaNet files to
            copyPythonFilesFromResource("AdaNet");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        generateFiles(symtab, instance, pythonPath, forced);

        if (doCompile) {
            if (!generateCMake) // do it either way
                generateCMakeFiles(instance);
            compile();
        }
        processedArchitecture = null;
    }

    private TaggingResolver getSymTabAndTaggingResolver() {
        BasicLibrary.extract();
        return EMADLAbstractSymtab.createSymTabAndTaggingResolver(getCustomFilesPath(), getPythonPath() ,this.backend, getModelsPath(),
                Constants.SYNTHESIZED_COMPONENTS_ROOT, BasicLibrary.BASIC_LIBRARY_ROOT);
    }

    private EMAComponentInstanceSymbol resolveComponentInstanceSymbol(String qualifiedName, TaggingResolver symtab) {
        String simpleName = Names.getSimpleName(qualifiedName);
        if (!Character.isUpperCase(simpleName.charAt(0))) {
            String packageName = qualifiedName.substring(0, qualifiedName.length() - simpleName.length() - 1);
            qualifiedName = Names.getQualifiedName(packageName, StringUtils.capitalize(simpleName));
        }

        EMAComponentSymbol component = symtab.<EMAComponentSymbol>resolve(qualifiedName, EMAComponentSymbol.KIND).orElse(null);

        List<String> splitName = Splitters.DOT.splitToList(qualifiedName);
        String componentName = splitName.get(splitName.size() - 1);
        String instanceName = componentName.substring(0, 1).toLowerCase() + componentName.substring(1);

        if (component == null){
            String errMsg = "Component with name '" + componentName + "' does not exist.";
            Log.error(errMsg);
            throw new RuntimeException(errMsg);
        }

        Scope c1 = component.getEnclosingScope();
        Optional<EMAComponentInstanceSymbol> c2 = c1.resolve(instanceName, EMAComponentInstanceSymbol.KIND);
        EMAComponentInstanceSymbol c3 = c2.get();
        return c3;
    }

    public void compile() throws IOException {
        File tempScript = createTempScript();
        try {
            ProcessBuilder pb;
            if (!SystemUtils.IS_OS_WINDOWS)
                pb = new ProcessBuilder("bash", tempScript.toString());
            else
                pb = new ProcessBuilder("cmd", tempScript.toString());
            pb.inheritIO();
            Process process = pb.start();
            int returnCode = process.waitFor();
            if(returnCode != 0) {
                String errMsg = "During compilation, an error occured. See above for more details.";
                Log.error(errMsg);
                throw new RuntimeException(errMsg);
            }
        }catch(Exception e){
            String errMsg ="During compilation, the following error occured: '" + e + "'";
            Log.error(errMsg);
            throw new RuntimeException(errMsg);
        } finally {
            tempScript.delete();
        }
    }

    public File createTempScript() throws IOException {
        File tempScript = File.createTempFile("script", null);
        if (!SystemUtils.IS_OS_WINDOWS) {
            try {
                Writer streamWriter = new OutputStreamWriter(new FileOutputStream(
                        tempScript));
                PrintWriter printWriter = new PrintWriter(streamWriter);
                final String currentDir = System.getProperty("user.dir");


                printWriter.println("#!/bin/bash");
                printWriter.println("cd " + getGenerationTargetPath());
                printWriter.println("mkdir -p build");
                printWriter.println("cd build");
                printWriter.println("rm -r -f *");
                printWriter.println("pwd");
                printWriter.println("cp -R " + currentDir + "/model .");
                printWriter.println("cmake ..");
                printWriter.println("make");

                printWriter.close();
            } catch (Exception e) {
                Log.error(e.getMessage());
            }
        } else {
            try {
                Writer streamWriter = new OutputStreamWriter(new FileOutputStream(
                        tempScript));
                PrintWriter printWriter = new PrintWriter(streamWriter);

                printWriter.println("cd " + getGenerationTargetPath());
                printWriter.println("if exist build del /F /Q /S build");
                printWriter.println("mkdir build");
                printWriter.println("cd build");
                printWriter.println("cmake ..");
                printWriter.println("cmake --build .  --config release");

                printWriter.close();
            } catch (Exception e) {
                Log.error(e.getMessage());
            }
        }

        return tempScript;
    }

    public String getChecksumForFile(String filePath) throws IOException {
        Path wiki_path = Paths.get(filePath);
        MessageDigest md5;

        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(Files.readAllBytes(wiki_path));
            byte[] digest = md5.digest();

            return hex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "No_Such_Algorithm_Exception";
        }
    }

    public static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02X", aByte));
        }
        return result.toString();
    }

    public List<File> generateFiles(TaggingResolver taggingResolver, EMAComponentInstanceSymbol EMAComponentSymbol, String pythonPath, String forced) throws IOException {
        Set<EMAComponentInstanceSymbol> allInstances = new HashSet<>();

        List<FileContent> fileContents = generateStrings(taggingResolver, EMAComponentSymbol, allInstances, forced);
        List<File> generatedFiles = new ArrayList<>();

        Log.info("Generating adapters...", EMADLGenerator.class.getName());
        emamGen.generateAdapters(fileContents, EMAComponentSymbol);


        for (FileContent fileContent : fileContents) {
            generatedFiles.add(emamGen.generateFile(fileContent));
        }

        // train
        Map<String, String> fileContentMap = new HashMap<>();
        for (FileContent f : fileContents) {
            fileContentMap.put(f.getFileName(), f.getFileContent());
        }

        List<FileContent> fileContentsTrainingHashes = new ArrayList<>();
        for (EMAComponentInstanceSymbol componentInstance : allInstances) {
            Optional<ArchitectureSymbol> architecture = componentInstance.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
            // added for future use if one wants to change the location of the AdaNet python files

            if (!architecture.isPresent()) {
                continue;
            }

            if (forced.equals("n")) {
                continue;
            }

            if (architecture.get().getDataPaths().isEmpty()) {
                throw new RuntimeException("No dataset found. Please specify a dataset using the Network.tag file");
            }

            if (!computeChanges(architecture, componentInstance) && !forced.equals("y")) {
                Log.error("Training of model " + componentInstance.getFullName() + " skipped");
                continue;
            }

            String parsedFullName = componentInstance.getFullName().
                    substring(0, 1).toLowerCase() + componentInstance.getFullName().substring(1).replaceAll("\\.", "_");
            String trainerScriptName = "CNNTrainer_" + parsedFullName + ".py";
            Log.warn("Training of model " + trainerScriptName);
            String trainingPath = getGenerationTargetPath() + trainerScriptName;
            if (Files.exists(Paths.get(trainingPath))) {
                ProcessBuilder pb = new ProcessBuilder(Arrays.asList(pythonPath, trainingPath)).inheritIO();
                Process p = pb.start();

                int exitCode = 0;
                try {
                    exitCode = p.waitFor();
                }
                catch(InterruptedException e) {
                    String errMsg = "Training aborted: exit code " + exitCode;

                    Log.error(errMsg);
                    throw new RuntimeException(errMsg);
                }

                if(exitCode != 0) {
                    String errMsg = "Training failed: exit code " + exitCode;

                    Log.error(errMsg);
                    throw new RuntimeException(errMsg);
                }
            } else {
                Log.warn("Training file " + trainingPath + " not found.");
            }
        }

        for (FileContent fileContent : fileContentsTrainingHashes) {
            generatedFiles.add(emamGen.generateFile(fileContent));
        }

        if (isGenerateCMake())
            generateCMakeFiles(EMAComponentSymbol);

        return generatedFiles;
    }

    /**
     * This function hashes all dataset files, emadl and cnnt files.
     * The hashes are packed into a JSON file and get saved to disk.
     * @param architecture
     * @param componentInstance
     * @return JSONObject with the individual hashes of all files.
     */
    private JSONObject generateHashFile(Optional<ArchitectureSymbol> architecture, EMAComponentInstanceSymbol componentInstance) {

        String componentConfigFilename = "hashes/hashes.json";
        JSONObject newDatasetsHashes = new JSONObject();

        List<File> datasets = architecture.get().getDataPaths().stream().map(File::new).map(
                file -> file.listFiles((FilenameFilter) new WildcardFileFilter("*.json"))
        ).flatMap(Arrays::stream).collect(Collectors.toList());
        if(!datasets.isEmpty()){
            String content = "";
            for(File child : datasets){
                try(FileInputStream inputStream = new FileInputStream(child.getAbsolutePath())) {
                    content = IOUtils.toString(inputStream, Charset.defaultCharset());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Exception while reading dataset metadata " + child.getAbsolutePath());
                }

                JSONObject datasetMetadata = new JSONObject(content);

                String datasetFilename = datasetMetadata.getString("id") + "." + datasetMetadata.getString("filetype");
                datasetMetadata.put("hash", ChecksumGenerator.getChecksumForFileSHA1(child.getParent() + File.separator + datasetFilename));
                datasetMetadata.put("path", new File(child.getParentFile(), datasetFilename).getAbsolutePath());
                newDatasetsHashes.put(datasetMetadata.getString("id"), datasetMetadata);
            }
        } else {
            Log.info("No metadata information about datasets found. Fallback to hashing all files.", this.getClass().getName());
            datasets = architecture.get().getDataPaths().stream().map(File::new).map(
                    file -> file.listFiles()).flatMap(Arrays::stream).collect(Collectors.toList());
            for (File child : datasets) {
                if(child.isDirectory()){
                    continue;
                }
                String newChecksum = ChecksumGenerator.getChecksumForFileSHA1(child.getPath());

                JSONObject datasetMetadata = new JSONObject();

                String baseFileName = child.getName().split("\\.")[0];
                if(baseFileName.equals("testing") || baseFileName.equals("test")){
                    datasetMetadata.put("testing", true);
                }
                datasetMetadata.put("hash", newChecksum);
                datasetMetadata.put("path", child.getAbsolutePath());
                newDatasetsHashes.put(child.getName(), datasetMetadata);
            }
        }

        String mainComponentConfigFilename = componentInstance.getComponentType().getFullName().replaceAll("\\.", "/");
        String instanceConfigFilename = componentInstance.getFullName().replaceAll("\\.", "/") + "_" + componentInstance.getName();
        String configFilename = getConfigFilename(mainComponentConfigFilename, componentInstance.getFullName().replaceAll("\\.", "/"), instanceConfigFilename);
        String configFilepath = getModelsPath().concat(configFilename);
        String emadlPath = configFilepath.concat(".emadl");
        String cnntPath = configFilepath.concat(".conf");

        JSONObject newHashes = new JSONObject();
        newHashes.put("datasets", newDatasetsHashes);
        newHashes.put("emadl", ChecksumGenerator.getChecksumForFileSHA1(emadlPath));
        newHashes.put("cnnt", ChecksumGenerator.getChecksumForFileSHA1(cnntPath));

        File newHashFile = getNewHashFile();
        if(newHashFile.exists()){
            newHashFile.delete();
        }

        try {
            generateFile(new FileContent(newHashes.toString(), componentConfigFilename));
        } catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException("Could not write to file " + newHashFile.getAbsolutePath());
        }

        return newHashes;
    }

    /**
     * Computes the changes between the last execution and the files of this execution.
     * It will read the old_hashes.json file internally, where the old hashes are stored.
     * @param architecture
     * @param componentInstance
     * @return change status of datasets and emadl files. True if something changed, false if nothing changed.
     */
    private boolean computeChanges(Optional<ArchitectureSymbol> architecture, EMAComponentInstanceSymbol componentInstance){
        JSONObject oldHashes;
        boolean executionNecessary = false;

        try {
            String fileContent = "";
            try(FileInputStream hashInputStream = new FileInputStream(getGenerationTargetPath() + "hashes/hashes.json")) {
                fileContent = IOUtils.toString(hashInputStream, Charset.defaultCharset());
            }
            oldHashes = new JSONObject(fileContent);
        } catch (FileNotFoundException e){
            Log.info("Execution necessary: Hashfile not found.", this.getClass().getName());
            this.generateHashFile(architecture, componentInstance);
            executionNecessary = true;
            oldHashes = null;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Exception while reading old hash files");
        }

        JSONObject oldDatasetHashes;
        if(oldHashes != null){
            oldDatasetHashes = oldHashes.getJSONObject("datasets");
        } else {
            oldDatasetHashes = null;
        }

        JSONObject newHashes = this.generateHashFile(architecture, componentInstance);

        for(String key : newHashes.keySet()){
            if(!key.equals("datasets")){
                if(oldHashes == null || !newHashes.getString(key).equalsIgnoreCase(oldHashes.getString(key))){
                    Log.info("Input file " + key + " did change. Execution necessary", this.getClass().getName());
                    executionNecessary = true;
                } else {
                    Log.debug("Input file " + key + " did not change.", this.getClass().getName());
                }
            }
        }

        JSONObject newDatasetHashes = newHashes.getJSONObject("datasets");
        LinkedList<String> executionOrder = new LinkedList<>();

        JSONObject retrainingConf = new JSONObject();

        // Use kind of insertion sort to resolve references and determine execution order
        for(String id : newDatasetHashes.keySet()) {
            boolean testing = false;
            try {
                testing = newDatasetHashes.getJSONObject(id).getBoolean("testing");
            } catch (JSONException e){
                Log.debug("Found no testing flag. This does usually happen when using the old dataset style.", this.getClass().getName());
            }

            // Ignore testing dataset
            if(!testing){
                this.calculateExecutionOrder(executionOrder, newDatasetHashes, id);
            } else {
                retrainingConf.put("testing", newDatasetHashes.getJSONObject(id));
            }
        }

        Log.info("Determined execution order: " + executionOrder, this.getClass().getName());

        JSONArray changes = new JSONArray();
        for (String id : executionOrder){
            // Compute change
            JSONObject changeContent = new JSONObject();
            changeContent.put("id", id);
            changeContent.put("path", newDatasetHashes.getJSONObject(id).getString("path"));
            if(oldDatasetHashes == null) {
                Log.info("Old dataset hashes not found. Execution required.", this.getClass().getName());
                changeContent.put("retraining", true);
                executionNecessary = true;
            } else if(!oldDatasetHashes.has(id)){
                Log.info("Dataset " + id + " has been added: Execution required.", this.getClass().getName());
                changeContent.put("retraining", true);
                executionNecessary = true;
            } else if(!newDatasetHashes.getJSONObject(id).getString("hash")
                    .equalsIgnoreCase(oldDatasetHashes.getJSONObject(id).getString("hash"))){
                Log.info("Dataset " + id + " has changed: Execution required.", this.getClass().getName());
                changeContent.put("retraining", true);
                executionNecessary = true;
            } else {
                changeContent.put("retraining", false);
                Log.debug("Dataset hasn't changed.", this.getClass().getName());
            }

            changes.put(changeContent);
        }

        retrainingConf.put("changes", changes);

        // Write changes to file
        String componentChangeFilename = "/conf/retraining.json";
        try {
            generateFile(new FileContent(retrainingConf.toString(), componentChangeFilename));
        } catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException("Could not write to file " + componentChangeFilename);
        }

        if(!executionNecessary){
            Log.info("Execution not necessary: No files changed.", this.getClass().getName());
        }
        return executionNecessary;
    }

    private int calculateExecutionOrder(List<String> executionOrder, JSONObject datasets, String idToCheck){

        // If id is already in executionOrder list, return the position.
        int idIndex = executionOrder.indexOf(idToCheck);
        if(idIndex != -1){
            return idIndex;
        }

        JSONObject dataset = datasets.getJSONObject(idToCheck);

        String reference = null;
        try {
            reference = dataset.getString("references");
        } catch (JSONException e){
            Log.debug("Found no reference flag. This does usually happen when using the old dataset style.", this.getClass().getName());
        }

        // If reference is null, add dataset at position 0 (has to be trained first).
        if (reference == null) {
            Log.debug("Dataset " + idToCheck + " has no reference. Will use it as base.", this.getClass().getName());
            executionOrder.add(0, idToCheck);
            return 0;
        }

        // Resolve reference
        Log.debug("Dataset " + idToCheck + " has the reference " + reference + ". Trying to resolve reference...", this.getClass().getName());
        int referenceIndex = executionOrder.indexOf(reference);
        if (referenceIndex == -1) {
            if(datasets.has(reference)){
                referenceIndex = calculateExecutionOrder(executionOrder, datasets, reference);
                if(referenceIndex != -1){
                    executionOrder.add( referenceIndex + 1, idToCheck);
                }
                return referenceIndex + 1;
            } else {
                throw new RuntimeException("Reference " + reference + " of dataset not found. Please check your dataset configuration.");
            }
        } else {
            executionOrder.add(referenceIndex + 1, idToCheck);
            return referenceIndex + 1;
        }
    }

    private File getNewHashFile(){
        return Paths.get(this.getGenerationTargetPath(), "hashes", "new_hashes.json").toFile();
    }

    public List<File> generateCMakeFiles(EMAComponentInstanceSymbol componentInstanceSymbol) {
        List<File> files = new ArrayList<>();
        if (componentInstanceSymbol != null) {
            getCmakeConfig().getCMakeListsViewModel().setCompName(componentInstanceSymbol.getFullName().replace('.', '_').replace('[', '_').replace(']', '_'));
        }
        List<FileContent> contents = getCmakeConfig().generateCMakeFiles();
        try {
            for (FileContent content : contents)
                files.add(generateFile(content));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    public File generateFile(FileContent fileContent) throws IOException {
        File f = new File(getGenerationTargetPath() + fileContent.getFileName());
        Log.debug("FileCreation:" + f.getName(), this.getClass().getName());
        boolean contentEqual = false;
        //Actually slower than just saving and overwriting the file
        /*if (f.exists()) {
            String storedFileContent = new String(Files.readAllBytes(f.toPath()));
            if (storedFileContent.equals(fileContent.getFileContent())) {
                contentEqual = true;
            }
        } else*/
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            if (!f.createNewFile()) {
                Log.error("File could not be created: "+ f.getAbsolutePath());
            }
        }

        if (!contentEqual) {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f));
            bufferedWriter.write(fileContent.getFileContent(), 0, fileContent.getFileContent().length());
            bufferedWriter.close();
        }
        return f;
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    public List<FileContent> generateStrings(TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol, Set<EMAComponentInstanceSymbol> allInstances, String forced) {
        if (componentInstanceSymbol != null) {
            getCmakeConfig().getCMakeListsViewModel().setCompName(componentInstanceSymbol.getFullName().replace('.', '_').replace('[', '_').replace(']', '_'));
        }

        List<FileContent> fileContents = new ArrayList<>();
        // Add Helpers
        if (emamGen.usesArmadilloBackend()) {
            fileContents.add(ArmadilloHelper.getArmadilloHelperFileContent());
        }
        emamGen.searchForCVEverywhere(componentInstanceSymbol, taggingResolver);
        if (ConversionHelper.isUsedCV()) {
            fileContents.add(ConversionHelper.getConversionHelperFileContent(emamGen.isGenerateTests()));
        }

        processedArchitecture = new HashMap<>();
        generateComponent(fileContents, allInstances, taggingResolver, componentInstanceSymbol);

        String instanceName = componentInstanceSymbol.getComponentType().getFullName().replaceAll("\\.", "_");
        if (!Backend.getBackendString(this.backend).equals("PYTORCH")) {
            fileContents.addAll(generateCNNTrainer(allInstances, instanceName));
        }
        TypesGeneratorCPP tg = new TypesGeneratorCPP();
        fileContents.addAll(tg.generateTypes(TypeConverter.getTypeSymbols()));

//        if (cnnArchGenerator.isCMakeRequired()) {
//            cnnArchGenerator.setGenerationTargetPath(getGenerationTargetPath());
//            Map<String, String> cmakeContentsMap = cnnArchGenerator.generateCMakeContent(componentInstanceSymbol.getFullName());
//            for (String fileName : cmakeContentsMap.keySet()){
//                fileContents.add(new FileContent(cmakeContentsMap.get(fileName), fileName));
//            }
//        }

        if (emamGen.shouldGenerateMainClass()) {
            //fileContents.add(emamGen.getMainClassFileContent(componentInstanceSymbol, fileContents.get(0)));
        } else if (emamGen.shouldGenerateSimulatorInterface()) {
            fileContents.addAll(SimulatorIntegrationHelper.getSimulatorIntegrationHelperFileContent());
        }

        fixArmadilloImports(fileContents);

        processedArchitecture = null;
        return fileContents;
    }

    protected List<String> getDataPaths(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance) {
        List<TagSymbol> instanceTags = new LinkedList<>();

        boolean isChildComponent = instance.getEnclosingComponent().isPresent();

        if (isChildComponent) {
            // get all instantiated components of parent
            List<EMAComponentInstantiationSymbol> instantiationSymbols = (List<EMAComponentInstantiationSymbol>) instance
                    .getEnclosingComponent().get().getComponentType().getReferencedSymbol().getSubComponents();

            // filter corresponding instantiation of instance and add tags
            instantiationSymbols.stream().filter(e -> e.getName().equals(instance.getName())).findFirst()
                    .ifPresent(symbol -> {
                        instanceTags.addAll(taggingResolver.getTags(symbol, DataPathSymbol.KIND));
                        instanceTags.addAll(taggingResolver.getTags(symbol, DatasetArtifactSymbol.KIND));
                    });
        }

        // instance tags have priority
        List<TagSymbol> tags;
        if (!instanceTags.isEmpty()) {
            tags = instanceTags;
        }
        else {
            tags = Stream
                    .concat(taggingResolver.getTags(component, DataPathSymbol.KIND).stream(), taggingResolver.getTags(component, DatasetArtifactSymbol.KIND).stream())
                    .collect(Collectors.toList());
        }
        List<String> dataPaths = new LinkedList<>();

        if (!tags.isEmpty()) {
            for(TagSymbol tagSymbol : tags){
                if (tagSymbol instanceof DataPathSymbol) {
                    DataPathSymbol dataPathSymbol = (DataPathSymbol) tagSymbol;
                    DataPathCocos.check(dataPathSymbol);

                    String dataPath = dataPathSymbol.getPath();
                    dataPaths.add(dataPath);
                    Log.warn("Tagging info for DataPath symbol was found, ignoring data_paths.txt: " + dataPath);
                }
                else {
                    DatasetArtifactSymbol datasetArtifactSymbol = (DatasetArtifactSymbol) tagSymbol;

                    String groupId = datasetArtifactSymbol.getGroupId();
                    String artifactId = datasetArtifactSymbol.getArtifactId();
                    String version = datasetArtifactSymbol.getVersion();
                    DependencyInstaller.installDependency(groupId, artifactId, version);

                    for(Pair<Path, String> mavenPackage : DependencyInstaller.resolveDependencies(groupId, artifactId, version)){
                        Path dataPathPrefix = mavenPackage.getKey().resolve(mavenPackage.getValue() + "-dataset");
                        try {
                            unzipJar(dataPathPrefix.toString());
                        } catch (IOException e){
                            e.printStackTrace();
                        }

                        if(dataPathPrefix.resolve("training_data").toFile().exists()){
                            dataPaths.add(dataPathPrefix.resolve("training_data").toString());
                        } else if(dataPathPrefix.resolve( "data").toFile().exists()){
                            dataPaths.add(dataPathPrefix.resolve("data").toString());
                        } else {
                            System.out.println(dataPathPrefix.resolve( "data").toString());
                            throw new RuntimeException("No valid dataset artifact structure found for artifact " + groupId + ":" + artifactId + ":" + version);
                        }
                    }
                }
            }
            stopGeneratorIfWarning();


        }
        else {
            Path dataPathDefinition = Paths.get(getModelsPath(), "data_paths.txt");
            if (dataPathDefinition.toFile().exists()) {
                DataPathConfigParser newParserConfig = new DataPathConfigParser(getModelsPath() + "data_paths.txt");
                dataPaths.add(newParserConfig.getDataPath(component.getFullName()));
            } else {
                Log.warn("No data path definition found in " + dataPathDefinition + " found: "
                        + "Set data path to default ./data path");
                dataPaths.add("data");
            }
        }

        return dataPaths;
    }

    protected String getWeightsPath(EMAComponentSymbol component, EMAComponentInstanceSymbol instance) {
        String weightsPath;

        Path weightsPathDefinition = Paths.get(getModelsPath(), "weights_paths.txt");
        if (weightsPathDefinition.toFile().exists()) {
            WeightsPathConfigParser newParserConfig = new WeightsPathConfigParser(getModelsPath() + "weights_paths.txt");
            weightsPath = newParserConfig.getWeightsPath(component.getFullName());
        } else {
            Log.debug("No weights path definition found in " + weightsPathDefinition + ": "
                    + "No pretrained weights will be loaded.", this.getClass().getName());
            weightsPath = null;
        }
        return weightsPath;
    }

    protected HashMap getLayerPathParameterTags(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance) {
        List<TagSymbol> instanceTags = new LinkedList<>();

        boolean isChildComponent = instance.getEnclosingComponent().isPresent();

        if (isChildComponent) {
            // get all instantiated components of parent
            List<EMAComponentInstantiationSymbol> instantiationSymbols = (List<EMAComponentInstantiationSymbol>) instance
                    .getEnclosingComponent().get().getComponentType().getReferencedSymbol().getSubComponents();

            // filter corresponding instantiation of instance and add tags
            instantiationSymbols.stream().filter(e -> e.getName().equals(instance.getName())).findFirst()
                    .ifPresent(symbol -> instanceTags.addAll(taggingResolver.getTags(symbol, LayerPathParameterSymbol.KIND)));
        }

        List<TagSymbol> tags = !instanceTags.isEmpty() ? instanceTags
                : (List<TagSymbol>) taggingResolver.getTags(component, LayerPathParameterSymbol.KIND);

        HashMap layerPathParameterTags = new HashMap();
        if (!tags.isEmpty()) {
            for (TagSymbol tag : tags) {
                LayerPathParameterSymbol layerPathParameterSymbol = (LayerPathParameterSymbol) tag;
                layerPathParameterTags.put(layerPathParameterSymbol.getId(), layerPathParameterSymbol.getPath());
            }
            // TODO: Replace warinings with errors, until then use this method
            stopGeneratorIfWarning();
            Log.warn("Tagging info for LayerPathParameter symbols was found.");
        }
        return layerPathParameterTags;
    }

    protected HashMap getLayerArtifactParameterTags(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance){
        List<TagSymbol> instanceTags = new LinkedList<>();

        boolean isChildComponent = instance.getEnclosingComponent().isPresent();

        if (isChildComponent) {
            // get all instantiated components of parent
            List<EMAComponentInstantiationSymbol> instantiationSymbols = (List<EMAComponentInstantiationSymbol>) instance
                    .getEnclosingComponent().get().getComponentType().getReferencedSymbol().getSubComponents();

            // filter corresponding instantiation of instance and add tags
            instantiationSymbols.stream().filter(e -> e.getName().equals(instance.getName())).findFirst()
                    .ifPresent(symbol -> instanceTags.addAll(taggingResolver.getTags(symbol, LayerArtifactParameterSymbol.KIND)));
        }

        List<TagSymbol> tags = !instanceTags.isEmpty() ? instanceTags
                : (List<TagSymbol>) taggingResolver.getTags(component, LayerArtifactParameterSymbol.KIND);

        HashMap layerArtifactParameterTags = new HashMap();
        File localRepo = new MavenSettings().getLocalRepository();
        if (!tags.isEmpty()) {
            for(TagSymbol tag: tags) {
                LayerArtifactParameterSymbol layerArtifactParameterSymbol = (LayerArtifactParameterSymbol) tag;
                String path = getArtifactDestination(localRepo.toString(), layerArtifactParameterSymbol.getArtifact(), layerArtifactParameterSymbol.getJar());
                layerArtifactParameterTags.put(layerArtifactParameterSymbol.getId(), path);
            }
            stopGeneratorIfWarning();
            Log.warn("Tagging info for LayerArtifact symbols was found.");
        }
        return layerArtifactParameterTags;
    }

    private String getArtifactDestination(String localRepo, String artifact, String jar) {
        String destinationPath = localRepo + File.separator + artifact + File.separator + jar;
        try {
            unzipJar(destinationPath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return destinationPath;
    }

    private void unzipJar(String destinationPath) throws IOException {
        File destination = new File(destinationPath);
        if (destination.exists() && destination.isDirectory()) {
            return;
        }
        destination.mkdir();

        File jar = new File(destination.getAbsolutePath() + ".jar");
        if (!jar.exists()) {
            Log.error("dependency " + destination.getAbsolutePath() + " could not be found");
        }
        ZipFile zipFile = new ZipFile(jar);

        Enumeration jarFileEntries = zipFile.entries();
        while (jarFileEntries.hasMoreElements()) {
            ZipEntry jarEntry = (ZipEntry) jarFileEntries.nextElement();
            File outputFile = new File(destination.getAbsolutePath(), jarEntry.getName());
            outputFile.getParentFile().mkdirs();

            if (outputFile.isDirectory()) {
                outputFile.mkdirs();
            }
            else {
                BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(jarEntry));
                int currentByte;
                byte[] data = new byte[1024];

                FileOutputStream fos = new FileOutputStream(outputFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, 1024);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, 1024)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }

        }
    }

    protected void generateComponent(List<FileContent> fileContents,
                                     Set<EMAComponentInstanceSymbol> allInstances,
                                     TaggingResolver taggingResolver,
                                     EMAComponentInstanceSymbol componentInstanceSymbol) {
        emamGen.addSemantics(taggingResolver, componentInstanceSymbol);

        allInstances.add(componentInstanceSymbol);
        EMAComponentSymbol EMAComponentSymbol = componentInstanceSymbol.getComponentType().getReferencedSymbol();

        /* remove the following two lines if the component symbol full name bug with generic variables is fixed */
        EMAComponentSymbol.setFullName(null);
        EMAComponentSymbol.getFullName();
        /* */

        Optional<ArchitectureSymbol> architecture = componentInstanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);

        // set the path to AdaNet python files
        architecture.ifPresent(architectureSymbol -> {architectureSymbol.setAdaNetUtils(getAdaNetUtils());});
        Optional<MathStatementsSymbol> mathStatements = EMAComponentSymbol.getSpannedScope().resolve("MathStatements", MathStatementsSymbol.KIND);

        EMADLCocos.checkAll(componentInstanceSymbol);


        if (architecture.isPresent()) {
            cnnArchGenerator.check(architecture.get());
            List<String> dPaths = getDataPaths(taggingResolver, EMAComponentSymbol, componentInstanceSymbol);
            String wPath = getWeightsPath(EMAComponentSymbol, componentInstanceSymbol);
            HashMap layerPathParameterTags = getLayerPathParameterTags(taggingResolver, EMAComponentSymbol, componentInstanceSymbol);
            layerPathParameterTags.putAll(getLayerArtifactParameterTags(taggingResolver, EMAComponentSymbol, componentInstanceSymbol));
            architecture.get().setDataPaths(dPaths);
            architecture.get().setDataPath(dPaths.get(0));
            architecture.get().setWeightsPath(wPath);
            architecture.get().processLayerPathParameterTags(layerPathParameterTags);
            architecture.get().setComponentName(EMAComponentSymbol.getFullName());
            architecture.get().setUseDgl(getUseDgl());
            if(!getCustomFilesPath().equals("")) {
                architecture.get().setCustomPyFilesPath(getCustomFilesPath() + "python/" + Backend.getBackendString(this.backend).toLowerCase());
            }
            generateCNN(fileContents, taggingResolver, componentInstanceSymbol, architecture.get());
            if (processedArchitecture != null) {
                processedArchitecture.put(architecture.get().getComponentName(), architecture.get());
            }
        }
        else if (mathStatements.isPresent()){
            generateMathComponent(fileContents, taggingResolver, componentInstanceSymbol, mathStatements.get());
        }
        else {
            generateSubComponents(fileContents, allInstances, taggingResolver, componentInstanceSymbol);
        }
    }

    private void fixArmadilloImports(List<FileContent> fileContents) {
        for (FileContent fileContent : fileContents) {
            fileContent.setFileContent(fileContent.getFileContent()
                    .replaceFirst("#include \"armadillo.h\"",
                            "#include \"armadillo\""));
        }
    }

    public void generateCNN(List<FileContent> fileContents, TaggingResolver taggingResolver, EMAComponentInstanceSymbol instance, ArchitectureSymbol architecture) {
        List<FileContent> contents = cnnArchGenerator.generateStrings(taggingResolver, architecture);
        String fullName = instance.getFullName().replaceAll("\\.", "_");

        //get the components execute method
        String executeKey = "execute_" + fullName;
        List<String> executeMethods = getContentOf(contents, executeKey);
        if (executeMethods.size() != 1) {
            throw new IllegalStateException("execute method of " + fullName + " not found");
        }
        String executeMethod = executeMethods.get(0);
        contents.remove(executeKey);

        List<String> applyBeamSearchMethods = getContentOf(contents, "BeamSearch_" + fullName);
        String applyBeamSearchMethod = null;
        if (applyBeamSearchMethods.size() == 1) {
            applyBeamSearchMethod = applyBeamSearchMethods.get(0);
        }

        String component = emamGen.generateString(taggingResolver, instance, null);
        FileContent componentFileContent = new FileContent(
                transformComponent(component, "CNNPredictor_" + fullName,
                        applyBeamSearchMethod,
                        executeMethod,
                        architecture),
                instance);

        fileContents.addAll(contents);
        fileContents.add(componentFileContent);
        fileContents.add(new FileContent(readResource("CNNTranslator.h", Charsets.UTF_8), "CNNTranslator.h"));
    }

    private List<String> getContentOf(Collection<FileContent> fileContents, String fileName) {
        return fileContents
                .stream()
                .filter(fc -> fc.getFileName().equals(fileName))
                .map(FileContent::getFileContent)
                .collect(Collectors.toList());
    }

    protected String transformComponent(String component, String predictorClassName, String applyBeamSearchMethod, String executeMethod, ArchitectureSymbol architecture) {
        //insert includes
        component = component.replaceFirst("using namespace",
                "#include \"" + predictorClassName + ".h" + "\"\n" +
                        "#include \"CNNTranslator.h\"\n" +
                        "using namespace");

        //insert network attribute for predictor of each network
        String networkAttributes = "public:";

        int i = 0;
        for (NetworkInstructionSymbol networkInstruction : architecture.getNetworkInstructions()) {
            networkAttributes += "\n" + predictorClassName + "_" + i + " _predictor_" + i + "_;";

            ++i;
        }

        component = component.replaceFirst("public:", networkAttributes);

        //insert BeamSearch method
        //component = component.replaceFirst("void init\\(\\)", applyBeamSearchMethod + "\nvoid init()");

        //insert execute method
        component = component.replaceFirst("void execute\\(\\)\\s\\{\\s\\}",
                "void execute(){\n" + executeMethod + "\n}");
        return component;
    }

    public void generateMathComponent(List<FileContent> fileContents, TaggingResolver taggingResolver, EMAComponentInstanceSymbol EMAComponentSymbol, MathStatementsSymbol mathStatementsSymbol) {
        fileContents.add(new FileContent(
                emamGen.generateString(taggingResolver, EMAComponentSymbol, mathStatementsSymbol),
                EMAComponentSymbol));
    }

    public void generateSubComponents(List<FileContent> fileContents, Set<EMAComponentInstanceSymbol> allInstances, TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol) {
        fileContents.add(new FileContent(emamGen.generateString(taggingResolver, componentInstanceSymbol, null), componentInstanceSymbol));
        String lastNameWithoutArrayPart = "";
        for (EMAComponentInstanceSymbol instanceSymbol : componentInstanceSymbol.getSubComponents()) {
            int arrayBracketIndex = instanceSymbol.getName().indexOf("[");
            boolean generateComponentInstance = true;
            if (arrayBracketIndex != -1) {
                generateComponentInstance = !instanceSymbol.getName().substring(0, arrayBracketIndex).equals(lastNameWithoutArrayPart);
                lastNameWithoutArrayPart = instanceSymbol.getName().substring(0, arrayBracketIndex);
                Log.info("Without: " + lastNameWithoutArrayPart, this.getClass().getName());
                Log.info("Bool: " + generateComponentInstance, this.getClass().getName());
            }
            if (generateComponentInstance) {
                generateComponent(fileContents, allInstances, taggingResolver, instanceSymbol);
            }
        }
    }

    private String getConfigFilename(String mainComponentConfigFilename, String componentConfigFilename, String instanceConfigFilename) {
        String trainConfigFilename;
        if (Files.exists(Paths.get(getModelsPath() + instanceConfigFilename + ".conf"))) {
            trainConfigFilename = instanceConfigFilename;
        } else if (Files.exists(Paths.get(getModelsPath() + componentConfigFilename + ".conf"))) {
            trainConfigFilename = componentConfigFilename;
        } else if (Files.exists(Paths.get(getModelsPath() + mainComponentConfigFilename + ".conf"))) {
            trainConfigFilename = mainComponentConfigFilename;
        } else {
            return null;
        }
        return trainConfigFilename;
    }

    public List<FileContent> generateCNNTrainer(Set<EMAComponentInstanceSymbol> allInstances, String mainComponentName) {
        boolean copied = copySchemaFilesFromResource(ROOT_SCHEMA_MODEL_PATH);
        List<FileContent> fileContents = new ArrayList<>();
        TaggingResolver symTabAndTaggingResolver = getSymTabAndTaggingResolver();
        for (EMAComponentInstanceSymbol componentInstance : allInstances) {
            EMAComponentSymbol component = componentInstance.getComponentType().getReferencedSymbol();
            Optional<ArchitectureSymbol> architecture = component.getSpannedScope().resolve("", ArchitectureSymbol.KIND);

            if (architecture.isPresent()) {
                String mainComponentConfigFilename = mainComponentName.replaceAll("\\.", "/");
                String componentConfigFilename = component.getFullName().replaceAll("\\.", "/");
                String instanceConfigFilename = component.getFullName().replaceAll("\\.", "/") + "_" + component.getName();
                String trainConfigFilename = getConfigFilename(mainComponentConfigFilename, componentConfigFilename, instanceConfigFilename);
                if (Strings.isNullOrEmpty(trainConfigFilename)) {
                    String message = String.format("Missing training configuration. Could not find a file with any of the following names (only one needed): '%s.conf', '%s.conf', '%s.conf'. These files denote respectively the configuration for the single instance, the component or the whole system.",
                            getModelsPath() + instanceConfigFilename, getModelsPath() + componentConfigFilename, getModelsPath() + mainComponentConfigFilename);
                    Log.error(message);
                    throw new RuntimeException(String.format("Missing training configuration for network '%s'", mainComponentName));
                }

                cnnTrainGenerator.setGenerationTargetPath(getGenerationTargetPath());
                if (cnnTrainGenerator instanceof CNNTrain2Gluon) {
                    ((CNNTrain2Gluon) cnnTrainGenerator).setRootProjectModelsDir(getModelsPath());
                }
                List<String> names = Splitter.on("/").splitToList(trainConfigFilename);
                trainConfigFilename = names.get(names.size() - 1);
                Path modelPath = Paths.get(getModelsPath() + Joiner.on("/").join(names.subList(0, names.size() - 1)));
                TrainingConfiguration trainingConfiguration = cnnTrainGenerator.createTrainingConfiguration(modelPath,
                        trainConfigFilename, copied ? Paths.get(getGenerationTargetPath()) : null);

                // Annotate train configuration with architecture
                final String fullConfigName = String.join(".", names);
                ArchitectureSymbol correspondingArchitecture = this.processedArchitecture.get(fullConfigName);
                assert correspondingArchitecture != null : "No architecture found for train " + fullConfigName + " configuration!";

                TrainingComponentsContainer trainingComponentsContainer = new TrainingComponentsContainer();
                trainingComponentsContainer.setTrainedArchitecture(trainingConfiguration,
                        new ArchitectureAdapter(correspondingArchitecture));
                TrainedArchitectureChecker.checkComponents(trainingConfiguration, trainingComponentsContainer);

                // Resolve critic network if critic is present
                Optional<String> criticName = trainingConfiguration.getCritic();
                if (criticName.isPresent()) {
                    String fullCriticName = criticName.get();
                    int indexOfFirstNameCharacter = fullCriticName.lastIndexOf('.') + 1;
                    fullCriticName = fullCriticName.substring(0, indexOfFirstNameCharacter)
                            + fullCriticName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullCriticName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(fullCriticName,
                            symTabAndTaggingResolver);
                    EMADLCocos.checkAll(instanceSymbol);
                    Optional<ArchitectureSymbol> critic = instanceSymbol.getSpannedScope().resolve("",
                            ArchitectureSymbol.KIND);
                    if (!critic.isPresent()) {
                        String message = String.format("Resolving critic component failed. Critic component '%s' does not have a CNN implementation, but is required to have one.", fullCriticName);
                        Log.error(message);
                        throw new RuntimeException(message);
                    }
                    ArchitectureSymbol architectureSymbol = critic.get();
                    architectureSymbol.setComponentName(fullCriticName);
                    trainingComponentsContainer.setCriticNetwork(new ArchitectureAdapter(architectureSymbol));
                }

                // Resolve discriminator network if discriminator is present
                Optional<String> discriminatorName = trainingConfiguration.getDiscriminatorName();
                if (discriminatorName.isPresent()) {
                    String fullDiscriminatorName = discriminatorName.get();
                    int indexOfFirstNameCharacter = fullDiscriminatorName.lastIndexOf('.') + 1;
                    fullDiscriminatorName = fullDiscriminatorName.substring(0, indexOfFirstNameCharacter)
                            + fullDiscriminatorName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullDiscriminatorName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(
                            fullDiscriminatorName, getSymTabAndTaggingResolver());
                    EMADLCocos.checkAll(instanceSymbol);
                    Optional<ArchitectureSymbol> discriminator = instanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                    if (!discriminator.isPresent()) {
                        String message = String.format("Resolving discriminator component failed. Discriminator component '%s' does not have a CNN implementation, but is required to have one.", fullDiscriminatorName);
                        Log.error(message);
                        throw new RuntimeException(message);
                    }
                    ArchitectureSymbol architectureSymbol = discriminator.get();
                    architectureSymbol.setComponentName(fullDiscriminatorName);
                    trainingComponentsContainer.setDiscriminatorNetwork(
                            new ArchitectureAdapter(architectureSymbol));
                }

                // Resolve QNetwork if present
                Optional<String> qNetworkName = trainingConfiguration.getQNetwork();
                if (qNetworkName.isPresent()) {
                    String fullQNetworkName =  qNetworkName.get();
                    int indexOfFirstNameCharacter = fullQNetworkName.lastIndexOf('.') + 1;
                    fullQNetworkName = fullQNetworkName.substring(0, indexOfFirstNameCharacter)
                            + fullQNetworkName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullQNetworkName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(
                            fullQNetworkName, getSymTabAndTaggingResolver());
                    EMADLCocos.checkAll(instanceSymbol);
                    Optional<ArchitectureSymbol> qNetwork = instanceSymbol.getSpannedScope().resolve("",
                            ArchitectureSymbol.KIND);
                    if (!qNetwork.isPresent()) {
                        String message = String.format("Resolving Q-Network component failed. Q-Network component '%s' does not have a CNN implementation, but is required to have one.", fullQNetworkName);
                        Log.error(message);
                        throw new RuntimeException(message);
                    }
                    ArchitectureSymbol architectureSymbol = qNetwork.get();
                    architectureSymbol.setComponentName(fullQNetworkName);
                    trainingComponentsContainer.setQNetwork(new ArchitectureAdapter(architectureSymbol));
                }

                // Resolve encoder network if present
                Optional<String> encoderName = trainingConfiguration.getEncoderName();
                if (encoderName.isPresent()) {
                    String fullEncoderName = encoderName.get();
                    int indexOfFirstNameCharacter = fullEncoderName.lastIndexOf('.') + 1;
                    fullEncoderName = fullEncoderName.substring(0, indexOfFirstNameCharacter)
                            + fullEncoderName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullEncoderName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(
                            fullEncoderName, getSymTabAndTaggingResolver());
                    EMADLCocos.checkAll(instanceSymbol);
                    Optional<ArchitectureSymbol> encoder = instanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                    if (!encoder.isPresent()) {
                        String message = String.format("Resolving encoder component failed. Encoder component '%s' does not have a CNN implementation, but is required to have one.", fullEncoderName);
                        Log.error(message);
                        throw new RuntimeException(message);
                    }
                    ArchitectureSymbol architectureSymbol = encoder.get();
                    architectureSymbol.setComponentName(fullEncoderName);
                    trainingComponentsContainer.setEncoderNetwork(
                            new ArchitectureAdapter(architectureSymbol));

                    ArchitectureAdapter decoder = trainingComponentsContainer.getTrainedArchitecture().get();
                    ArchitectureSymbol decoderArchitectureSymbol = decoder.getArchitectureSymbol();

                    encoder.get().setAuxiliaryArchitecture(decoderArchitectureSymbol);

                }

                Optional<String> rewardFunctionNameOpt = trainingConfiguration.getRewardFunctionName();
                if (rewardFunctionNameOpt.isPresent()) {
                    String fullRewardFunctionName =  rewardFunctionNameOpt.get();
                    int indexOfFirstNameCharacter = fullRewardFunctionName.lastIndexOf('.') + 1;
                    fullRewardFunctionName = fullRewardFunctionName.substring(0, indexOfFirstNameCharacter)
                            + fullRewardFunctionName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullRewardFunctionName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(
                            fullRewardFunctionName, getSymTabAndTaggingResolver());
                    EMADLCocos.checkAll(instanceSymbol);
                    trainingComponentsContainer.setRewardFunction(instanceSymbol);
                }

                Optional<String> policyFunctionNameOpt = trainingConfiguration.getPolicyFunctionName();
                if (policyFunctionNameOpt.isPresent()) {
                    String fullPolicyFunctionName =  policyFunctionNameOpt.get();
                    int indexOfFirstNameCharacter = fullPolicyFunctionName.lastIndexOf('.') + 1;
                    fullPolicyFunctionName = fullPolicyFunctionName.substring(0, indexOfFirstNameCharacter)
                            + fullPolicyFunctionName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullPolicyFunctionName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(
                            fullPolicyFunctionName, getSymTabAndTaggingResolver());
                    EMADLCocos.checkAll(instanceSymbol);
                    trainingComponentsContainer.addTrainingComponent("policy",
                            instanceSymbol.getComponentType().getReferencedSymbol());
                }

                Optional<String> preprocessor = trainingConfiguration.getPreprocessor();
                if (preprocessor.isPresent()) {
                    String fullPreprocessorName = preprocessor.get();

                    int indexOfFirstNameCharacter = fullPreprocessorName.lastIndexOf('.') + 1;
                    fullPreprocessorName = fullPreprocessorName.substring(0, indexOfFirstNameCharacter)
                            + fullPreprocessorName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullPreprocessorName.substring(indexOfFirstNameCharacter + 1);
                    String instanceName = componentInstance.getFullName().replaceAll("\\.", "_");

                    TaggingResolver symtab = getSymTabAndTaggingResolver();
                    EMAComponentInstanceSymbol processor_instance = resolveComponentInstanceSymbol(fullPreprocessorName, symtab);
                    processor_instance.setFullName("CNNPreprocessor_" + instanceName);
                    List<FileContent> processorContents = new ArrayList<>();
                    generateComponent(processorContents, new HashSet<>(), symtab, processor_instance);
                    fixArmadilloImports(processorContents);

                    for (FileContent fileContent : processorContents) {
                        try {
                            emamGen.generateFile(fileContent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    String targetPath = getGenerationTargetPath();
                    ComponentPortInformation componentPortInformation;
                    componentPortInformation = pythonWrapper.generateAndTryBuilding(processor_instance, targetPath + "/pythonWrapper", targetPath);
                    PreprocessingComponentParameterAdapter componentParameter = new PreprocessingComponentParameterAdapter(componentPortInformation);
                    PreprocessingPortChecker.check(componentParameter);
                    trainingComponentsContainer.setPreprocessingComponentParameter(componentParameter);
                }

                cnnTrainGenerator.setInstanceName(componentInstance.getFullName().replaceAll("\\.", "_"));
                List<FileContent> fileContentList = cnnTrainGenerator.generateStrings(trainingConfiguration,
                        trainingComponentsContainer, copied ? Paths.get(getGenerationTargetPath()) : null);
                fileContents.addAll(fileContentList);
            }
        }
        return fileContents;
    }

    private boolean copySchemaFilesFromResource(String rootSchemaModelPath) {
        try {
            String jarPath = getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            String target_path = getGenerationTargetPath();
            if (!target_path.endsWith("/")) {
                target_path = target_path + '/';
            }
            URI uri = URI.create("jar:file:" + jarPath);
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                for (Path path : Files.walk(fs.getPath(rootSchemaModelPath)).filter(Files::isRegularFile).collect(Collectors.toList())) {
                    if (path.toString().endsWith(".scm") || path.toString().endsWith(".ema")) {
                        Path destination = Paths.get(target_path + path);
                        Files.createDirectories(destination.getParent());
                        Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (UnsupportedOperationException e){
                System.out.println("this should only be printed if the generator is run unpacked");
                return false;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String readResource(final String fileName, Charset charset) {
        try {
            return Resources.toString(Resources.getResource(fileName), charset);

        } catch (IllegalArgumentException e) {
            System.err.println("Resource file " + fileName + " not found");
            throw new RuntimeException("Resource file " + fileName + " not found");
        } catch (IOException e) {
            System.err.println("IO Error occurred");
            throw new RuntimeException("IO Error occurred");
        }
    }

    private void stopGeneratorIfWarning() {
        for (int i = 0; i < Log.getFindings().size(); i++) {
            if (Log.getFindings().get(i).toString().matches("Filepath '(.)*' does not exist!")) {
                throw new RuntimeException(Log.getFindings().get(i).toString());
            } else if (Log.getFindings().get(i).toString()
                    .equals("DatapathType is incorrect, must be of Type: HDF5 or LMDB")) {
                throw new RuntimeException(Log.getFindings().get(i).toString());
            }
        }
    }



    @Override
    public List<FileContent> generateStrings(TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol) {
        return this.generateStrings(taggingResolver, componentInstanceSymbol, new HashSet<>(), "UNSET");
    }

    @Override
    public List<File> generateFiles(TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol) throws IOException {
        return this.generateFiles(taggingResolver, componentInstanceSymbol, "", "UNSET");
    }

    @Override
    public CMakeConfig getCmakeConfig() {
        mergeCMakeConfigs();
        return cMakeConfig;
    }

    private void mergeCMakeConfigs() {
        emamGen.getCmakeConfig().getCMakeListsViewModel().getCmakeCommandList()
                .stream().forEach(s -> cMakeConfig.addCMakeCommand(s));
        emamGen.getCmakeConfig().getCMakeListsViewModel().getCmakeCommandListEnd()
                .stream().forEach(s -> cMakeConfig.addCMakeCommandEnd(s));
        emamGen.getCmakeConfig().getCMakeListsViewModel().getCmakeLibraryLinkageList()
                .stream().forEach(s -> cMakeConfig.addCmakeLibraryLinkage(s));
        emamGen.getCmakeConfig().getCMakeListsViewModel().getModuleDependencies()
                .stream().forEach(s -> cMakeConfig.addModuleDependency(s));

        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getCmakeCommandList()
                .stream().forEach(s -> cMakeConfig.addCMakeCommand(s));
        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getCmakeCommandListEnd()
                .stream().forEach(s -> cMakeConfig.addCMakeCommandEnd(s));
        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getCmakeLibraryLinkageList()
                .stream().forEach(s -> cMakeConfig.addCmakeLibraryLinkage(s));
        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getModuleDependencies()
                .stream().forEach(s -> cMakeConfig.addModuleDependency(s));
    }

    @Override
    public boolean isGenerateCMake() {
        return generateCMake;
    }

    @Override
    public void setGenerateCMake(boolean b) {
        generateCMake = b;
    }

    @Override
    public boolean useAlgebraicOptimizations() {
        return emamGen.useAlgebraicOptimizations();
    }

    @Override
    public void setUseAlgebraicOptimizations(boolean b) {
        emamGen.setUseAlgebraicOptimizations(b);
    }

    @Override
    public boolean useThreadingOptimizations() {
        return emamGen.useThreadingOptimizations();
    }

    @Override
    public void setUseThreadingOptimization(boolean b) {
        emamGen.setUseThreadingOptimization(b);
    }

    @Override
    public MathCommandRegister getMathCommandRegister() {
        return emamGen.getMathCommandRegister();
    }

    @Override
    public void setMathCommandRegister(MathCommandRegister mathCommandRegister) {
        emamGen.setMathCommandRegister(mathCommandRegister);

    }
}