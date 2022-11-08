package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.types2._ast.ASTDimension;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.automl.helper.ArchitectureSymbolHelper;
import de.monticore.mlpipelines.automl.helper.MathNumberExpressionWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EfficientNetEmadlBuilder {
    private final ArchitectureSymbol architecture;
    private final EfficientNetConfig config;
    private final String classesVariableName = "classes";
    private final String channelsVariableName = "channels";

    public EfficientNetEmadlBuilder(ArchitectureSymbol architecture, EfficientNetConfig config) {
        this.architecture = architecture;
        this.config = config;
    }

    public List<String> getEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add(createHeaderEmadl());
        lines.addAll(createPortsEmadl());
        lines.addAll(createCNN());
        lines.add("}");
        return lines;
    }

    private String createHeaderEmadl() {
        String name = "EfficientNetB" + config.getPhi();
        String parameters = getHeaderParameters();
        return "component " + name + parameters + "{";
    }

    private List<String> createPortsEmadl() {
        ASTDimension dimensions = ArchitectureSymbolHelper.getImageDimension(architecture);
        int imageSize = getImageSize(dimensions);
        String line1 = "    ports in Z(0:255)^{1, " + imageSize + ", " + imageSize + "} image,";
        String line2 = "        out Q(0:1)^{" + config.getNumberClasses() + "} predictions;";
        List<String> lines = new ArrayList<>();
        lines.add(line1);
        lines.add(line2);
        return lines;
    }

    private Collection<String> createCNN() {
        //create string for cnn
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add("    implementation CNN {");
        lines.add("");
        lines.addAll(createStemEmadl());
        lines.add("");
        lines.addAll(createConvEmadl());
        lines.add("");
        lines.addAll(createReductionConvEmadl());
        lines.add("");
        lines.addAll(createResidualBlockEmadl());
        lines.add("");
        lines.addAll(createReductionBlockEmadl());
        lines.add("");
        lines.addAll(createElementLayers());
        lines.add("    }");
        return lines;
    }

    private Collection<String> createStemEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add("        def stem(" + channelsVariableName + "){");
        lines.add(
                "            Convolution(kernel=(3,3), channels=" + channelsVariableName + ", stride=(2, 2), padding=\"same\") ->");
        lines.add("            BatchNorm() ->");
        lines.add("            LeakyRelu()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createConvEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add("        def conv(" + channelsVariableName + "){");
        lines.add(
                "            Convolution(kernel=(3,3), channels=" + channelsVariableName + ", stride=(1, 1), padding=\"same\") ->");
        lines.add("            BatchNorm() ->");
        lines.add("            LeakyRelu()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createReductionConvEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add("        def reductionConv(" + channelsVariableName + "){");
        lines.add(
                "            Convolution(kernel=(3,3), channels=" + channelsVariableName + ", stride=(2, 2), padding=\"same\") ->");
        lines.add("            BatchNorm() ->");
        lines.add("            LeakyRelu()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createResidualBlockEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add("        def residualBlock(" + channelsVariableName + "){");
        lines.add("            (");
        lines.add("                conv(channels=" + channelsVariableName + ") ->");
        lines.add("                conv(channels=" + channelsVariableName + ") ");
        lines.add("            |");
        lines.add("                LeakyRelu()");
        lines.add("            ) ->");
        lines.add("            Add()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createReductionBlockEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add("        def reductionBlock(" + channelsVariableName + "){");
        lines.add("            (");
        lines.add("                conv(channels=" + channelsVariableName + ") ->");
        lines.add("                conv(channels=" + channelsVariableName + ") ->");
        lines.add("                reductionConv(channels=" + channelsVariableName + ")");
        lines.add("            |");
        lines.add("                reductionConv(channels=" + channelsVariableName + ")");
        lines.add("            ) ->");
        lines.add("            Add()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createElementLayers() {
        List<String> lines = new ArrayList<>();
        String stemChannels = "8";
        String firstResidualBlockChannels = "48";
        String firstReductionBlockChannels = "48";
        String secondResidualBlockChannels = "96";
        String secondReductionBlockChannels = "96";

        lines.add("        image ->");
        lines.add("        stem(channels=" + stemChannels + ") ->");
        lines.add("        residualBlock(-> = 4, channels=" + firstResidualBlockChannels + ") ->");
        lines.add("        reductionBlock(channels=" + firstReductionBlockChannels + ") ->");
        lines.add("        residualBlock(-> = 4, channels=" + secondResidualBlockChannels + ") ->");
        lines.add("        reductionBlock(channels=" + secondReductionBlockChannels + ") ->");
        lines.add("        FullyConnected(units=" + classesVariableName + ") ->");
        lines.add("        prediction");
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
