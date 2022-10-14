package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
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
            scaleNetworkElementDepth(architectureElement);
        }
    }

    private void scaleWidth() {
        List<ArchitectureElementSymbol> architectureElements = findArchitectureElements();

        //for residualBlock width scaling
        for (ArchitectureElementSymbol architectureElement : architectureElements) {
            if (!architectureElement.getName().equals("residualBlock"))
                continue;

            ArrayList expressions = getExpressions(architectureElement);
            System.out.println(expressions);
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

    private void scaleNetworkElementDepth(ArchitectureElementSymbol architectureElement) {
        ArrayList expressions = getExpressions(architectureElement);
        int depthIndex = 3;
        setValueInExpressions(expressions, depthIndex, this.depthFactor);
    }

    private void setValueInExpressions(ArrayList expressions, int index, float scalingFactor) {
        MathNumberExpressionSymbol expression = (MathNumberExpressionSymbol) expressions.get(index); //directly fetching the residualBlock
        long oldDividend = expression.getValue().getRealNumber().getDividend().longValue();
        long newDivisor = 20; // Because a minimal step size of 0.05
        long newDividend = oldDividend * (long)(scalingFactor * newDivisor);
        Rational newValue = Rational.valueOf(newDividend, newDivisor);
        expression.getValue().setRealNumber(newValue);
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
