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
        setArchitectureSymbol(originalArchitecture);
        setDepth(scalingFactors.alpha);
        setWidth(scalingFactors.beta);
        setResolution(scalingFactors.gamma);

        if(this.architecture.getNetworkInstructions().isEmpty())
            return originalArchitecture;

        scaleDepth();
        scaleWidth();
        scaleResolution();
        return getArchitectureSymbol();
    }


    private void scaleDepth() {
        NetworkInstructionSymbol networkInstruction = this.architecture.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol networkInstructionBody = networkInstruction.getBody();
        List<ArchitectureElementSymbol> architectureElements = networkInstructionBody.getElements();

        //for residualBlock depth scaling
        for(ArchitectureElementSymbol architectureElement : architectureElements){
            if(!architectureElement.getName().equals("residualBlock"))
                continue;

            ArchitectureElementScope spannedScope  =  architectureElement.getSpannedScope();
            ArrayList expressions = (ArrayList) spannedScope.getLocalSymbols().get(""); //the MathNumberExpressionSymbol is in the key ""

            MathNumberExpressionSymbol expression = (MathNumberExpressionSymbol) expressions.get(3); //directly fetching the residualBlock
            Rational oldValue = expression.getValue().getRealNumber();
            Rational newValue = oldValue.times((long) this.depthFactor);
            expression.getValue().setRealNumber(newValue);
        }
    }

    private void scaleWidth() {
        NetworkInstructionSymbol networkInstruction = this.architecture.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol networkInstructionBody = networkInstruction.getBody();
        List<ArchitectureElementSymbol> architectureElements = networkInstructionBody.getElements();

        //for residualBlock width scaling
        for(ArchitectureElementSymbol architectureElement : architectureElements){
            if(!architectureElement.getName().equals("residualBlock"))
                continue;

            ArchitectureElementScope spannedScope  =  architectureElement.getSpannedScope();
            ArrayList expressions = (ArrayList) spannedScope.getLocalSymbols().get(""); //the MathNumberExpressionSymbol is in the key ""
        }
    }


    private void scaleResolution() {

    }

    public float getDepth() {
        return depthFactor;
    }

    private void setDepth(float alpha) {
        this.depthFactor = (float)Math.pow(alpha, EfficientNetConfig.phi);
    }

    public float getWidth() {
        return widthFactor;
    }

    private void setWidth(float beta) {
        this.widthFactor = (float)Math.pow(beta, EfficientNetConfig.phi);
    }

    public float getResolution() {
        return resolutionFactor;
    }

    private void setResolution(float gamma) {
        this.resolutionFactor = (float)Math.pow(gamma, EfficientNetConfig.phi);
    }

    public ArchitectureSymbol getArchitectureSymbol() {
        return architecture;
    }

    private void setArchitectureSymbol(ArchitectureSymbol architectureSymbol) {
        this.architecture = architectureSymbol;
    }
}
