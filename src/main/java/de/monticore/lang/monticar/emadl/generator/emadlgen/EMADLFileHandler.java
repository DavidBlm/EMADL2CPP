package de.monticore.lang.monticar.emadl.generator.emadlgen;

import com.google.common.io.Resources;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstantiationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch.generator.DataPathConfigParser;
import de.monticore.lang.monticar.cnnarch.generator.WeightsPathConfigParser;
import de.monticore.lang.monticar.emadl._cocos.DataPathCocos;
import de.monticore.lang.monticar.emadl.generator.backend.Backend;
import de.monticore.lang.monticar.emadl.generator.modularcnn.NetworkCompositionHandler;
import de.monticore.lang.monticar.emadl.tagging.artifacttag.DatasetArtifactSymbol;
import de.monticore.lang.monticar.emadl.tagging.dltag.DataPathSymbol;
import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.tagging._symboltable.TagSymbol;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EMADLFileHandler {

    private EMADLGenerator emadlGen;

    private String adaNetUtils = "./src/main/resources/AdaNet/";
    private String modelsPath;
    private String customFilesPath = "";
    private String pythonPath = "";
    private String rootConfigFileName = "";

    private String composedNetworksFilePath = "";
    private Set<EMAComponentInstanceSymbol> instanceVault = null;

    public EMADLFileHandler(EMADLGenerator emadlGen, String composedNetworksFilePath){
        this.emadlGen =  emadlGen;
        this.composedNetworksFilePath = composedNetworksFilePath;
    }

    public Set<EMAComponentInstanceSymbol> getInstanceVault(){
        return this.instanceVault;
    }

    protected String getAdaNetUtils() {
        return adaNetUtils;
    }

    protected void setAdaNetUtils(String adaNetUtils) {
        this.adaNetUtils = adaNetUtils;
    }

    protected String getModelsPath() {
        return modelsPath;
    }

    protected void setModelsPath(String modelsPath) {
        if (!(modelsPath.substring(modelsPath.length() - 1).equals("/"))) {
            this.modelsPath = modelsPath + "/";
        }
        else {
            this.modelsPath = modelsPath;
        }
    }

    protected String getCustomFilesPath() { return  customFilesPath; }

    protected void setCustomFilesPath(String customPythonFilesPath) {
        if (!(customPythonFilesPath.endsWith("/"))){
            this.customFilesPath = customPythonFilesPath + "/";
        }
        else {
            this.customFilesPath = customPythonFilesPath;
        }

    }

    public String getPythonPath() {return pythonPath;}

    protected void setPythonPath (String pythonPath){
        if(!pythonPath.startsWith("/")){
            pythonPath = "/" + pythonPath;
        }
        this.pythonPath = pythonPath;
    }

    public void setComposedNetworksFilePath(String path){
        this.composedNetworksFilePath = path;
    }

    public String getComposedNetworksFilePath(){
        return this.composedNetworksFilePath;
    }



    protected void copyPythonFilesFromResource(String folder) throws URISyntaxException, IOException {
        // this function copys the passed folder to the generation target
        // important for AdaNet
        String jarPath = getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
        String target_path = emadlGen.getGenerationTargetPath() + folder;
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
            setAdaNetUtils(emadlGen.getGenerationTargetPath()+"/"+folder+ "/");
        }catch (UnsupportedOperationException e){
            System.out.println("this should only be printed if the generator is run unpacked");
        }
    }

    protected String getDataPath(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance) {
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
        String dataPath;

        if (!tags.isEmpty()) {
            if (tags.get(0) instanceof DataPathSymbol) {
                DataPathSymbol dataPathSymbol = (DataPathSymbol) tags.get(0);
                DataPathCocos.check(dataPathSymbol);

                dataPath = dataPathSymbol.getPath();
                Log.warn("Tagging info for DataPath symbol was found, ignoring data_paths.txt: " + dataPath);
            }
            else {
                DatasetArtifactSymbol datasetArtifactSymbol = (DatasetArtifactSymbol) tags.get(0);

                String localRepo = System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository";
                dataPath = getArtifactDestination(localRepo, datasetArtifactSymbol.getArtifact(), datasetArtifactSymbol.getJar()) + File.separator + "training_data";
                Log.warn("Tagging info for DatasetArtifact symbol was found, ignoring data_paths.txt: " + dataPath);

            }
            emadlGen.stopGeneratorIfWarning();


        }
        else {
            Path dataPathDefinition = Paths.get(getModelsPath(), "data_paths.txt");
            if (dataPathDefinition.toFile().exists()) {
                DataPathConfigParser newParserConfig = new DataPathConfigParser(getModelsPath() + "data_paths.txt");
                dataPath = newParserConfig.getDataPath(component.getFullName());
            } else {
                Log.warn("No data path definition found in " + dataPathDefinition + " found: "
                        + "Set data path to default ./data path");
                dataPath = "data";
            }
        }

        return dataPath;
    }

    protected String getWeightsPath(EMAComponentSymbol component, EMAComponentInstanceSymbol instance) {
        String weightsPath;

        Path weightsPathDefinition = Paths.get(getModelsPath(), "weights_paths.txt");
        if (weightsPathDefinition.toFile().exists()) {
            WeightsPathConfigParser newParserConfig = new WeightsPathConfigParser(getModelsPath() + "weights_paths.txt");
            weightsPath = newParserConfig.getWeightsPath(component.getFullName());
        } else {
            Log.info("No weights path definition found in " + weightsPathDefinition + ": "
                    + "No pretrained weights will be loaded.", "EMADLGenerator");
            weightsPath = null;
        }
        return weightsPath;
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
    protected static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02X", aByte));
        }
        return result.toString();
    }

    protected String getArtifactDestination(String localRepo, String artifact, String jar) {
        String destinationPath = localRepo + File.separator + artifact + File.separator + jar;
        try {
            unzipJar(destinationPath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return destinationPath;
    }


    protected String getChecksumForLargerFile(String filePath) throws IOException {
        try {
            return (new File(filePath)).lastModified() + "";
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception_calculating_hash_large_file";
        }
    }

    protected List<File> generateCMakeFiles(EMAComponentInstanceSymbol componentInstanceSymbol) {
        List<File> files = new ArrayList<>();
        if (componentInstanceSymbol != null) {
            emadlGen.getCmakeConfig().getCMakeListsViewModel().setCompName(componentInstanceSymbol.getFullName().replace('.', '_').replace('[', '_').replace(']', '_'));
        }
        List<FileContent> contents = emadlGen.getCmakeConfig().generateCMakeFiles();
        try {
            for (FileContent content : contents)
                files.add(generateFile(content));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    protected File generateFile(FileContent fileContent) throws IOException {
        File f = new File(emadlGen.getGenerationTargetPath() + fileContent.getFileName());
        Log.info(f.getName(), "FileCreation:");
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
                Log.error("File could not be created");
            }
        }

        if (!contentEqual) {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f));
            bufferedWriter.write(fileContent.getFileContent(), 0, fileContent.getFileContent().length());
            bufferedWriter.close();
        }
        return f;
    }

    protected List<File> generateFiles(TaggingResolver taggingResolver, EMAComponentInstanceSymbol EMAComponentSymbol, String pythonPath, String forced) throws IOException {
        Set<EMAComponentInstanceSymbol> allInstances = new HashSet<>();

        Set<EMAComponentInstanceSymbol> newInstanceVault = new HashSet<>();

        emadlGen.generateStrings(taggingResolver, EMAComponentSymbol, newInstanceVault, forced);
        this.instanceVault = newInstanceVault;

        Log.clearFindings();


        List<FileContent> fileContents = emadlGen.generateStrings(taggingResolver, EMAComponentSymbol, allInstances, forced);
        List<File> generatedFiles = new ArrayList<>();

        Log.info("Generating adapters ...", EMADLGenerator.class.getName());
        emadlGen.getEmamGen().generateAdapters(fileContents, EMAComponentSymbol);

        for (FileContent fileContent : fileContents) {
            generatedFiles.add(emadlGen.getEmamGen().generateFile(fileContent));
        }

        // train
        Map<String, String> fileContentMap = new HashMap<>();
        for (FileContent f : fileContents) {
            fileContentMap.put(f.getFileName(), f.getFileContent());
        }

        List<FileContent> fileContentsTrainingHashes = new ArrayList<>();
        List<String> newHashes = new ArrayList<>();

        NetworkCompositionHandler networkCompositionHandler = new NetworkCompositionHandler(this.composedNetworksFilePath, this.instanceVault,
                emadlGen.getEmadlCNNHandler().getCachedComposedArchitectureSymbols(), emadlGen.getBackend());
        //composedNetworkHandler.refreshInformation(allInstances);
        //Set<EMAComponentInstanceSymbol> networks = composedNetworkHandler.getSortedNetworksFromAtomicToComposed(allInstances);
        ArrayList<EMAComponentInstanceSymbol> networks = networkCompositionHandler.processComponentInstances(allInstances);

        for (EMAComponentInstanceSymbol componentInstance : networks) {
            Optional<ArchitectureSymbol> architecture = networkCompositionHandler.resolveArchitectureSymbolOfInstance(componentInstance);
            //Optional<ArchitectureSymbol> architecture = componentInstance.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
            // added for future use if one wants to change the location of the AdaNet python files

            /*
            if (!architecture.isPresent()) {
                continue;
            }

            if (forced.equals("n")) {
                continue;
            }

            */

            if (!architecture.isPresent() || forced.equals("n")) {
                continue;
            }

            if ( (!architecture.isPresent() && !networkCompositionHandler.isComposedNet(componentInstance)) || networkCompositionHandler.isPartOfComposedNet(componentInstance) ) {
                continue;
            }

            String mainComponentConfigFilename = componentInstance.getComponentType().getFullName().replaceAll("\\.", "/");
            String instanceConfigFilename = componentInstance.getFullName().replaceAll("\\.", "/") + "_" + componentInstance.getName();
            String configFilename = getConfigFilename(mainComponentConfigFilename, componentInstance.getFullName().replaceAll("\\.", "/"), instanceConfigFilename);
            String configFilepath = getModelsPath().concat(configFilename);
            String emadlPath = configFilepath.concat(".emadl");
            String cnntPath = configFilepath.concat(".conf");

            String emadlHash = getChecksumForFile(emadlPath);
            String cnntHash = getChecksumForFile(cnntPath);

            String componentConfigFilename = componentInstance.getComponentType().getReferencedSymbol().getFullName().replaceAll("\\.", "/");

            String b = Backend.getBackendString(emadlGen.getBackend());
            String trainingDataHash = "";
            String testDataHash = "";

            if (architecture.get().getDataPath() != null) {
                if (b.equals("CAFFE2")) {
                    trainingDataHash = getChecksumForLargerFile(architecture.get().getDataPath() + "/train_lmdb/data.mdb");
                    testDataHash = getChecksumForLargerFile(architecture.get().getDataPath() + "/test_lmdb/data.mdb");
                } else {
                    trainingDataHash = getChecksumForLargerFile(architecture.get().getDataPath() + "/train.h5");
                    testDataHash = getChecksumForLargerFile(architecture.get().getDataPath() + "/test.h5");
                }
            }
            String dp = architecture.get().getDataPath();
            String trainingHash = emadlHash + "#" + cnntHash + "#" + trainingDataHash + "#" + testDataHash;


            boolean alreadyTrained = newHashes.contains(trainingHash) || emadlGen.isAlreadyTrained(trainingHash, componentInstance);
            if (alreadyTrained && !forced.equals("y")) {
                Log.warn("Training of model " + componentInstance.getFullName() + " skipped");
            }
            else {
                String parsedFullName = componentInstance.getFullName().substring(0, 1).toLowerCase() + componentInstance.getFullName().substring(1).replaceAll("\\.", "_");
                String trainerScriptName = "CNNTrainer_" + parsedFullName + ".py";
                Log.warn("Training of model " + trainerScriptName);
                String trainingPath = emadlGen.getGenerationTargetPath() + trainerScriptName;
                if (Files.exists(Paths.get(trainingPath))) {
                    ProcessBuilder pb = new ProcessBuilder(Arrays.asList(pythonPath, trainingPath)).inheritIO();
                    Process p = pb.start();

                    int exitCode = 0;
                    try {
                        exitCode = p.waitFor();
                    }
                    catch(InterruptedException e) {
                        String errMsg = "Training aborted: exit code " + Integer.toString(exitCode);

                        Log.error(errMsg);
                        throw new RuntimeException(errMsg);
                    }

                    if(exitCode != 0) {
                        String errMsg = "Training failed: exit code " + Integer.toString(exitCode);

                        Log.error(errMsg);
                        throw new RuntimeException(errMsg);
                    }

                    fileContentsTrainingHashes.add(new FileContent(trainingHash, componentConfigFilename + ".training_hash"));
                    newHashes.add(trainingHash);
                } else {
                    Log.warn("Training file " + trainingPath + " not found.");
                }
            }
        }

        for (FileContent fileContent : fileContentsTrainingHashes) {
            generatedFiles.add(emadlGen.getEmamGen().generateFile(fileContent));
        }

        if (emadlGen.isGenerateCMake())
            generateCMakeFiles(EMAComponentSymbol);

        return generatedFiles;
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
                byte data[] = new byte[1024];

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

    public void fixArmadilloImports(List<FileContent> fileContents) {
        for (FileContent fileContent : fileContents) {
            fileContent.setFileContent(fileContent.getFileContent()
                    .replaceFirst("#include \"armadillo.h\"",
                            "#include \"armadillo\""));
        }
    }

    public List<String> getContentOf(Collection<FileContent> fileContents, String fileName) {
        return fileContents
                .stream()
                .filter(fc -> fc.getFileName().equals(fileName))
                .map(FileContent::getFileContent)
                .collect(Collectors.toList());
    }

    public void setRootConfigurationFile() {
        Log.info("RootConfFileValue: " + rootConfigFileName, "ROOT_CONFIG_FILE_SET");
        if (!rootConfigFileName.equals("")) {
            return;
        }
        Log.info("Trying to find root conf", "ROOT_CONFIG_FILE_SET");
        try {
            ArrayList<String> confFiles = new ArrayList<>();
            Stream<Path> files  = Files.list(Paths.get(getModelsPath() + "modularNetworks/"));
            //files.forEach(file -> Log.info("Available File: " + file.toString(),"ROOT_CONFIG_FILE_SET"));
            files.filter(file -> file.toString().endsWith(".conf")).forEach(file -> confFiles.add(file.toString()));
            if (confFiles.size() > 0){
                for (String s: confFiles) {
                    Log.info("Available Config File: " + s,"ROOT_CONFIG_FILE_SET");
                }
                Log.info("Setting RootConfFile File to: " + confFiles.get(0),"ROOT_CONFIG_FILE_SET");
                rootConfigFileName = confFiles.get(0);
            } else{
                Log.info("No RootConfigFile found.","ROOT_CONFIG_FILE_SET");
            }
        } catch (IOException e){
            Log.error("RootConfigFileError: " + e.getMessage());

        }
    }

    public String getConfigFilename(String mainComponentConfigFilename, String componentConfigFilename, String instanceConfigFilename) {
        Log.info("CompNames(main|comp|inst): "+ mainComponentConfigFilename + " | " + componentConfigFilename + " | " + instanceConfigFilename ,"CONFIG_FILE_SEARCH");
        Log.info("RootConfFileValue: " + rootConfigFileName, "CONFIG_FILE_SEARCH");
        if (!rootConfigFileName.equals("")) {
            return rootConfigFileName;
        }

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

    public boolean copySchemaFilesFromResource(String rootSchemaModelPath) {
        try {
            String jarPath = getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            String target_path = emadlGen.getGenerationTargetPath();
            if (!target_path.endsWith("/")) {
                target_path = target_path + '/';
            }
            URI uri = URI.create("jar:file:" + jarPath);
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                for (Path path : Files.walk(fs.getPath(rootSchemaModelPath)).filter(Files::isRegularFile).collect(Collectors.toList())) {
                    if (path.toString().endsWith(".scm") || path.toString().endsWith(".ema")) {
                        Path destination = Paths.get(target_path + path.toString());
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


    public File createTempScript() throws IOException {
        File tempScript = File.createTempFile("script", null);
        if (!SystemUtils.IS_OS_WINDOWS) {
            try {
                Writer streamWriter = new OutputStreamWriter(new FileOutputStream(
                        tempScript));
                PrintWriter printWriter = new PrintWriter(streamWriter);

                printWriter.println("#!/bin/bash");
                printWriter.println("cd " + emadlGen.getGenerationTargetPath());
                printWriter.println("mkdir -p build");
                printWriter.println("cd build");
                printWriter.println("rm -r -f *");
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

                printWriter.println("cd " + emadlGen.getGenerationTargetPath());
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
}
