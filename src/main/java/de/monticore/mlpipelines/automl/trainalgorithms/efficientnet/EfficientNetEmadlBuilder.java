package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.types2._ast.ASTDimension;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.automl.helper.ArchitectureSymbolHelper;
import de.monticore.mlpipelines.automl.helper.MathNumberExpressionWrapper;

import java.util.ArrayList;
import java.util.List;

public class EfficientNetEmadlBuilder {
    private final ArchitectureSymbol architecture;
    private final EfficientNetConfig config;

    public EfficientNetEmadlBuilder(ArchitectureSymbol architecture, EfficientNetConfig config) {
        this.architecture = architecture;
        this.config = config;
    }

    public List<String> getEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add(createHeader());
        lines.addAll(createPorts());
        lines.add("}");
        return lines;
    }

    private String createHeader() {
        String name = "EfficientNetB" + config.getPhi();
        String parameters = getHeaderParameters();
        return "component " + name + parameters + "{";
    }

    private List<String> createPorts() {
        ASTDimension dimensions = ArchitectureSymbolHelper.getImageDimension(architecture);
        int imageSize = getImageSize(dimensions);
        String line1 = "    ports in Z(0:255)^{1, " + imageSize + ", " + imageSize + "} image,";
        String line2 = "          out Z(0:1)^{" + config.getNumberClasses() + "} prediction;";
        List<String> lines = new ArrayList<>();
        lines.add(line1);
        lines.add(line2);
        return lines;
    }

    private String getHeaderParameters() {
        return "<classes=" + config.getNumberClasses() + ">";
    }

    private int getImageSize(ASTDimension dimensions) {
        MathNumberExpressionSymbol widthDim = (MathNumberExpressionSymbol) dimensions.getMatrixDim(2).getSymbol();
        MathNumberExpressionWrapper imageWidth = new MathNumberExpressionWrapper(widthDim);
        return imageWidth.getIntValue();
    }
}
