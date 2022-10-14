package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import org.jscience.mathematics.number.Rational;

import java.util.ArrayList;
import java.util.List;

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
            scaleNetworkElement(architectureElement);
        }
    }

    private void scaleWidth() {
        List<ArchitectureElementSymbol> architectureElements = findArchitectureElements();

        //for residualBlock width scaling
        for (ArchitectureElementSymbol architectureElement : architectureElements) {
            if (!architectureElement.getName().equals("residualBlock"))
                continue;

            ArchitectureElementScope spannedScope = architectureElement.getSpannedScope();
            ArrayList expressions = (ArrayList) spannedScope.getLocalSymbols()
                    .get(""); //the MathNumberExpressionSymbol is in the key ""
        }
    }

    private void scaleResolution() {

    }

    private List<ArchitectureElementSymbol> findArchitectureElements() {
        NetworkInstructionSymbol networkInstruction = this.architecture.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol networkInstructionBody = networkInstruction.getBody();
        List<ArchitectureElementSymbol> architectureElements = networkInstructionBody.getElements();
        return architectureElements;
    }

    private void scaleNetworkElement(ArchitectureElementSymbol architectureElement) {
        ArchitectureElementScope spannedScope = architectureElement.getSpannedScope();
        ArrayList expressions = (ArrayList) spannedScope.getLocalSymbols()
                .get(""); //the MathNumberExpressionSymbol is in the key ""

        MathNumberExpressionSymbol expression = (MathNumberExpressionSymbol) expressions.get(
                3); //directly fetching the residualBlock
        Rational oldValue = expression.getValue().getRealNumber();
        Rational newValue = oldValue.times((long) this.depthFactor);
        expression.getValue().setRealNumber(newValue);
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
