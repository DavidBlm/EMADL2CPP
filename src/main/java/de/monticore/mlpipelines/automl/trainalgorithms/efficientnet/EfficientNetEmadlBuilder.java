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

    public EfficientNetEmadlBuilder(ArchitectureSymbol architecture, EfficientNetConfig config) {
        this.architecture = architecture;
        this.config = config;
    }

    public List<String> getEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add(createHeader());
        lines.addAll(createPorts());
        lines.addAll(createCNN());
        lines.add("}");
        return lines;
    }

    private Collection<String> createCNN() {
        //create string for cnn
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add("    implementation CNN {");
        lines.add("");
        lines.addAll(createStemDefinition("channels"));
        lines.add("");
        lines.addAll(createConvDefinition("channels"));
        lines.add("");
        lines.addAll(createReductionConvDefinition("channels"));
        lines.add("");
        lines.addAll(createResidualBlockDefinition("channels"));
        lines.add("");
        lines.addAll(createReductionBlockDefinition("channels"));
        lines.add("");
        lines.addAll(createElementLayers("classes"));
        lines.add("    }");
        return lines;
    }

    private Collection<String>createElementLayers(String argClasses){
        List<String> lines = new ArrayList<>();
        String stemChannels = "8";
        String firstResidualBlockChannels = "48";
        String firstReductionBlockChannels = "48";
        String secondResidualBlockChannels = "96";
        String secondReductionBlockChannels = "96";
        String numLayers = "4";

        lines.add("        image ->");
        lines.add("        stem(channels="+ stemChannels +") ->");
        lines.add("        residualBlock(-> = " + numLayers+ ", channels="+ firstResidualBlockChannels +") ->");
        lines.add("        reductionBlock(channels="+ firstReductionBlockChannels +") ->");
        lines.add("        residualBlock(-> = " + numLayers+ ", channels="+ secondResidualBlockChannels +") ->");
        lines.add("        reductionBlock(channels="+ secondReductionBlockChannels +") ->");
        lines.add("        FullyConnected(units="+ argClasses +") ->");
        lines.add("        prediction");
        return lines;
    }
    private Collection<String> createReductionBlockDefinition(String argChannels) {
        List<String> lines = new ArrayList<>();
        lines.add("        def reductionBlock(" + argChannels + "){");
        lines.add("            (");
        lines.add("                conv(channels=" + argChannels + ") ->");
        lines.add("                conv(channels=" + argChannels + ") ->");
        lines.add("                reductionConv(channels=" + argChannels + ")");
        lines.add("            |");
        lines.add("                reductionConv(channels=" + argChannels + ")");
        lines.add("            ) ->");
        lines.add("            Add()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createResidualBlockDefinition(String argChannels) {
        List<String> lines = new ArrayList<>();
        lines.add("        def residualBlock(" + argChannels + "){");
        lines.add("            (");
        lines.add("                conv(channels=" + argChannels + ") ->");
        lines.add("                conv(channels=" + argChannels + ") ");
        lines.add("            |");
        lines.add("                LeakyRelu()");
        lines.add("            ) ->");
        lines.add("            Add()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createConvDefinition(String argChannels) {
        List<String> lines = new ArrayList<>();
        lines.add("        def conv(" + argChannels + "){");
        lines.add("            Convolution(kernel=(3,3), channels="+argChannels+", stride=(1, 1), padding=\"same\") ->");
        lines.add("            BatchNorm() ->");
        lines.add("            LeakyRelu()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createReductionConvDefinition(String argChannels) {
        List<String> lines = new ArrayList<>();
        lines.add("        def reductionConv(" + argChannels + "){");
        lines.add("            Convolution(kernel=(3,3), channels="+argChannels+", stride=(2, 2), padding=\"same\") ->");
        lines.add("            BatchNorm() ->");
        lines.add("            LeakyRelu()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createStemDefinition(String argChannels) {
        List<String> lines = new ArrayList<>();
        lines.add("        def stem(" + argChannels + "){");
        lines.add("            Convolution(kernel=(3,3), channels="+argChannels+", stride=(2, 2), padding=\"same\") ->");
        lines.add("            BatchNorm() ->");
        lines.add("            LeakyRelu()");
        lines.add("        }");
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
        String line2 = "        out Q(0:1)^{" + config.getNumberClasses() + "} predictions;";
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
