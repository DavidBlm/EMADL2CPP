package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import org.jscience.mathematics.number.Rational;

public class MathNumberExpressionWrapper {
    private MathNumberExpressionSymbol number;

    public MathNumberExpressionWrapper(MathNumberExpressionSymbol numberSymbol) {
        this.number = numberSymbol;
    }

    public void setValue(int value){
        Rational newValue = Rational.valueOf(value, 1);
        number.getValue().setRealNumber(newValue);
    }

    public void setValue(float value){
        int precision = 10000;
        setValue(value, precision);
    }

    public void setValue(float value, int precision){
        int dividend = Math.round(value * precision);
        Rational newValue = Rational.valueOf(dividend, precision);
        number.getValue().setRealNumber(newValue);
    }

    public void scale(float scalingFactor) {
        float oldValue = getFloatValue();
        float newValue = oldValue * scalingFactor;
        setValue(newValue);
    }

    public void scaleRound(float scalingFactor) {
        float oldValue = getFloatValue();
        int newValue = Math.round(oldValue * scalingFactor);
        setValue(newValue);
    }

    public float getFloatValue() {
        float dividend = number.getValue().getRealNumber().getDividend().floatValue();
        float divisor = number.getValue().getRealNumber().getDivisor().floatValue();
        return dividend / divisor;
    }
}
