package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.lang.monticar.common2._ast.ASTCommonMatrixType;
import de.monticore.lang.monticar.ts.references.MCASTTypeSymbolReference;
import de.monticore.lang.monticar.types2._ast.ASTDimension;
import de.monticore.symboltable.Symbol;
import org.jscience.mathematics.number.Rational;

import java.util.*;

public class NetworkScaler {

    private float depthFactor = 0;
    private float widthFactor = 0;
    private float resolutionFactor = 0;
    private ArchitectureSymbol architecture = null;

    public ArchitectureSymbol scale(ArchitectureSymbol originalArchitecture, ScalingFactors scalingFactors, int phi) {
        this.architecture = originalArchitecture;
        calculateFactors(scalingFactors, phi);

        if (this.architecture.getNetworkInstructions().isEmpty())
            return originalArchitecture;

        scaleUsingFactors();
        return getArchitectureSymbol();
    }

    private void calculateFactors(ScalingFactors scalingFactors, int phi) {
        this.depthFactor = (float) Math.pow(scalingFactors.alpha, phi);
        this.widthFactor = (float) Math.pow(scalingFactors.beta, phi);
        this.resolutionFactor = (float) Math.pow(scalingFactors.gamma, phi);
    }

    private void scaleUsingFactors() {
        scaleDepth();
        scaleWidth();
        scaleResolution();
    }

    private void scaleDepth() {
        List<ArchitectureElementSymbol> architectureElements = findArchitectureElements();

        for (ArchitectureElementSymbol architectureElement : architectureElements) {
            if (!architectureElement.getName().equals("residualBlock"))
                continue;
            scaleNetworkElementDepth(architectureElement);
        }
    }

    // TODO: add channels parameter to stem and scale it
    private void scaleWidth() {
        List<ArchitectureElementSymbol> architectureElements = findArchitectureElements();

        for (ArchitectureElementSymbol architectureElement : architectureElements) {
            if (!architectureElement.getName().equals("residualBlock"))
                continue;

            scaleArchitectureElementWidth(architectureElement);
        }
    }

    private void scaleArchitectureElementWidth(ArchitectureElementSymbol architectureElement) {
        ArrayList expressions = getExpressions(architectureElement);
        int channelsIndex = getChannelsIndex(architectureElement.getName());
        setValueInExpressions(expressions, channelsIndex, widthFactor);
    }

    private static int getChannelsIndex(String architectureElementName) {
        switch (architectureElementName) {
            case "residualBlock":
                return 5;
            case "reductionBlock":
                return 1;
        }
        throw new IllegalArgumentException("Block type not supported");
    }


    private void scaleResolution() {
        Map<String, Collection<Symbol>> enclosedSymbols = this.architecture.getSpannedScope().getEnclosingScope().get().getLocalSymbols();
        ArrayList image = (ArrayList) enclosedSymbols.get("image");
        EMAPortInstanceSymbol instanceSymbols = (EMAPortInstanceSymbol) image.get(0);
        MCASTTypeSymbolReference typeReference = (MCASTTypeSymbolReference) instanceSymbols.getTypeReference();
        ASTCommonMatrixType astType = (ASTCommonMatrixType)typeReference.getAstType();
        ASTDimension dimensions = astType.getDimension();
        changeImageDimension(dimensions);
    }

    private void changeImageDimension(ASTDimension dimensions){
        //ASTNumberExpression channelDim = (ASTNumberExpression)dimensions.getMatrixDim(0)
        MathNumberExpressionSymbol heightDim = (MathNumberExpressionSymbol)dimensions.getMatrixDim(1).getSymbol();
        MathNumberExpressionSymbol widthDim = (MathNumberExpressionSymbol)dimensions.getMatrixDim(2).getSymbol();

        MathNumberExpressionWrapper imageHeight = new MathNumberExpressionWrapper(heightDim);
        MathNumberExpressionWrapper imageWidth = new MathNumberExpressionWrapper(widthDim);

        int newDimension = calculateNewImageDimension(imageHeight);

        imageHeight.setValue(newDimension);
        imageWidth.setValue(newDimension);
    }

    private int calculateNewImageDimension(MathNumberExpressionWrapper imageDim) {
        float oldDimension = imageDim.getFloatValue();
        int newDimension = Math.round(this.resolutionFactor*oldDimension);
        return newDimension;
    }

    private List<ArchitectureElementSymbol> findArchitectureElements() {
        NetworkInstructionSymbol networkInstruction = this.architecture.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol networkInstructionBody = networkInstruction.getBody();
        List<ArchitectureElementSymbol> architectureElements = networkInstructionBody.getElements();
        return architectureElements;
    }

    private void scaleNetworkElementDepth(ArchitectureElementSymbol architectureElement) {
        ArrayList expressions = getExpressions(architectureElement);
        int depthIndex = 3;
        setValueInExpressions(expressions, depthIndex, this.depthFactor);
    }

    private void setValueInExpressions(ArrayList expressions, int index, float scalingFactor) {
        MathNumberExpressionSymbol expressionSymbol = (MathNumberExpressionSymbol) expressions.get(index);
        MathNumberExpressionWrapper expression = new MathNumberExpressionWrapper(expressionSymbol);
        float oldValue = expression.getFloatValue();
        float newValue = oldValue * scalingFactor;
        expression.setValue(newValue);
    }

    private static ArrayList getExpressions(ArchitectureElementSymbol architectureElement) {
        ArchitectureElementScope spannedScope = architectureElement.getSpannedScope();
        ArrayList expressions = (ArrayList) spannedScope.getLocalSymbols()
                .get(""); //the MathNumberExpressionSymbol is in the key ""
        return expressions;
    }

    public ArchitectureSymbol getArchitectureSymbol() {
        return architecture;
    }

    public float getDepthFactor() {
        return depthFactor;
    }

    public float getWidthFactor() {
        return widthFactor;
    }

    public float getResolutionFactor() {
        return resolutionFactor;
    }

}
