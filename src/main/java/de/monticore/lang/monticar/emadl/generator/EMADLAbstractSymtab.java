/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator;

import de.monticore.ModelingLanguageFamily;
import de.monticore.io.paths.ModelPath;
import de.monticore.lang.embeddedmontiarc.LogConfig;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarcmath._symboltable.EmbeddedMontiArcMathLanguage;
import de.monticore.lang.embeddedmontiarc.helper.ConstantPortHelper;
import de.monticore.lang.monticar.emadl._symboltable.EMADLLanguage;
import de.monticore.lang.monticar.emadl.tagging.dltag.DataPathTagSchema;
import de.monticore.lang.monticar.emadl.tagging.dltag.LayerPathParameterTagSchema;
import de.monticore.lang.monticar.enumlang._symboltable.EnumLangLanguage;
import de.monticore.lang.monticar.generator.cpp.converter.MathConverter;
import de.monticore.lang.monticar.generator.optimization.ThreadingOptimizer;
import de.monticore.lang.monticar.generator.order.nfp.TagBreakpointsTagSchema.TagBreakpointsTagSchema;
import de.monticore.lang.monticar.generator.order.nfp.TagDelayTagSchema.TagDelayTagSchema;
import de.monticore.lang.monticar.generator.order.nfp.TagExecutionOrderTagSchema.TagExecutionOrderTagSchema;
import de.monticore.lang.monticar.generator.order.nfp.TagInitTagSchema.TagInitTagSchema;
import de.monticore.lang.monticar.generator.order.nfp.TagMinMaxTagSchema.TagMinMaxTagSchema;
import de.monticore.lang.monticar.generator.order.nfp.TagTableTagSchema.TagTableTagSchema;
import de.monticore.lang.monticar.generator.order.nfp.TagThresholdTagSchema.TagThresholdTagSchema;
import de.monticore.lang.monticar.streamunits._symboltable.StreamUnitsLanguage;
import de.monticore.lang.monticar.struct._symboltable.StructLanguage;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Scope;

import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Paths;
import java.util.*;
import java.io.*;


public class EMADLAbstractSymtab {
    public EMADLAbstractSymtab() {
    }

    public static TaggingResolver createSymTabAndTaggingResolver(String... modelPath) {
        Scope scope = createSymTab(modelPath);
        TaggingResolver tagging = new TaggingResolver(scope, Arrays.asList(modelPath));
        TagMinMaxTagSchema.registerTagTypes(tagging);
        TagTableTagSchema.registerTagTypes(tagging);
        TagBreakpointsTagSchema.registerTagTypes(tagging);
        TagExecutionOrderTagSchema.registerTagTypes(tagging);
        TagInitTagSchema.registerTagTypes(tagging);
        TagThresholdTagSchema.registerTagTypes(tagging);
        TagDelayTagSchema.registerTagTypes(tagging);
        DataPathTagSchema.registerTagTypes(tagging);
        LayerPathParameterTagSchema.registerTagTypes(tagging);
        return tagging;
    }

    public static Scope createSymTab(String... modelPath) {
        ConstantPortHelper.resetLastID();
        MathConverter.resetIDs();
        ThreadingOptimizer.resetID();
        ModelingLanguageFamily fam = new ModelingLanguageFamily();

        // EMADLLanguage montiArcLanguage = new EMADLLanguage(getParameters(customPythonFilesPath));

        EMADLLanguage montiArcLanguage = new EMADLLanguage();

        fam.addModelingLanguage(montiArcLanguage);
        fam.addModelingLanguage(new EmbeddedMontiArcMathLanguage());
        fam.addModelingLanguage(new StreamUnitsLanguage());
        fam.addModelingLanguage(new StructLanguage());
        fam.addModelingLanguage(new EnumLangLanguage());
        final ModelPath mp = new ModelPath();
        for (String m : modelPath) {
            mp.addEntry(Paths.get(m));
        }
        LogConfig.init();//TODO comment for debug output
        GlobalScope scope = new GlobalScope(mp, fam);
        de.monticore.lang.monticar.Utils.addBuiltInTypes(scope);
        return scope;
    }

    //A method to extract the parameters of the custom layers from their respective .py files
    private static ArrayList<String> getParameters(String pathToCustomPyFiles){
        ArrayList<String> attributesDict = new ArrayList<>();
        boolean osCheckWindows = SystemUtils.IS_OS_WINDOWS;
        String[] pyFiles;
        File path = new File(pathToCustomPyFiles + "custom_layers/");
        pyFiles = path.list();

        String printParametersOfLayer = "";
        try {

            for(int index = 0; index < pyFiles.length; index++){
                if(pyFiles[index].equals("__init__.py") || !(pyFiles[index].substring(pyFiles[index].length() - 3, pyFiles[index].length())).equals(".py") ) continue;
                String nameWithoutEnding = pyFiles[index].substring(0, pyFiles[index].length() - 3);

                String callingPrintMethod = "import " + nameWithoutEnding + "; temp=" + nameWithoutEnding + "." + nameWithoutEnding + "(); temp.print_parameters_with_type()";
                if(osCheckWindows == false){
                    printParametersOfLayer = "python3 -c '" + callingPrintMethod + "'";
                } else {
                    printParametersOfLayer = "python -c \"" + callingPrintMethod + "\"";
                }
                Process process = Runtime.getRuntime().exec(new String [] {"/bin/bash", "-c", printParametersOfLayer},null, path);

                String line = null;
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    attributesDict.add(line);
                }
                reader.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> attributes = new ArrayList<>();

        // {'ones': 'int', 'alpha': 'int'}
        for(int i = 0; i < attributesDict.size(); i++){
            attributes.addAll(processAttributesDict(attributesDict.get(i)));
        }

        return attributes;
    }

    private static ArrayList<String> processAttributesDict(String attributesDict){
        String dict = attributesDict;
        dict = dict.replaceAll(",", " ");
        dict = dict.replaceAll("'", " ");
        dict = dict.replaceAll(" ", ":");
        String noBrackets = dict.substring(2, dict.length() - 2);
        String[] processed = noBrackets.split("\\::::");

        return new ArrayList<>(Arrays.asList(processed));
    }

}
