package de.monticore.lang.monticar.emadl.generator;

        import de.monticore.lang.monticar.cnnarch.generator.ArchitectureSupportChecker;
        import de.monticore.lang.monticar.cnnarch.generator.CNNArchGenerator;
        import de.monticore.lang.monticar.cnnarch.generator.CNNArchSymbolCompiler;
        import de.monticore.lang.monticar.cnnarch.generator.DataPathConfigParser;
        import de.monticore.lang.monticar.cnnarch.generator.LayerSupportChecker;
        import de.monticore.lang.monticar.cnnarch.generator.Target;
        import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
        import de.monticore.lang.monticar.generator.FileContent;
        import de.monticore.lang.monticar.generator.cmake.CMakeConfig;
        import de.monticore.lang.monticar.generator.cmake.CMakeFindModule;
        import de.monticore.lang.tagging._symboltable.TaggingResolver;
        import de.monticore.symboltable.Scope;

        import java.io.File;
        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

public class CNNArch2NoBackend extends CNNArchGenerator {
    CMakeConfig cMakeConfig = new CMakeConfig("");

    public CNNArch2NoBackend() {

    }

    @Override
    public CMakeConfig getCmakeConfig() {
        return this.cMakeConfig;
    }

    //check cocos with CNNArchCocos.checkAll(architecture) before calling this method.
    public List<FileContent> generateStrings(TaggingResolver taggingResolver, ArchitectureSymbol architecture){
        List<FileContent> fileContentMap = new ArrayList<>();

        return fileContentMap;
    }

    public boolean check(ArchitectureSymbol architecture) {
        //return architectureSupportChecker.check(architecture) && layerSupportChecker.check(architecture);
        return false;
    }


    public List<FileContent> generateCMakeContent(String rootModelName) {
        // Comment Haller: Why? Other generators require UpperCase Model Names.
        // model name should start with a lower case letter. If it is a component, replace dot . by _
        rootModelName = rootModelName.replace('.', '_').replace('[', '_').replace(']', '_');
        rootModelName = rootModelName.substring(0, 1).toLowerCase() + rootModelName.substring(1);

        this.cMakeConfig = new CMakeConfig(rootModelName);
        this.cMakeConfig.addModuleDependency(new CMakeFindModule("Armadillo", true));

        //ToDo: Has to be dynamically added, depending on SymbolTable, not only here, but everywhere.
        //The following line breaks armadillo, as a binary file is suddenly treated as cpp source..
        //cMakeConfig.addModuleDependency(new CMakeFindModule("Ipopt", false));
        //cMakeConfig.addCMakeCommand("set(LIBS ${LIBS} ipopt)");

        List<FileContent> fileContentMap = new ArrayList<>();
        for (FileContent fileContent : cMakeConfig.generateCMakeFiles()){
            fileContentMap.add(fileContent);
        }
        return fileContentMap;
    }


}
