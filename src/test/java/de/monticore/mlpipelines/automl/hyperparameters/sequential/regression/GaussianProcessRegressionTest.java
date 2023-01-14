package de.monticore.mlpipelines.automl.hyperparameters.sequential.regression;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class GaussianProcessRegressionTest extends TestCase {

    private double[] func(double[][] x) {
        double[] y = new double[x.length];
        for (int i=0; i < x.length; i++) {
            y[i] = Math.pow(x[i][0], 2);
        }
        return y;
    }

    private double[][] getXTrain() {
        double[][] xTrain = new double[10][1];
        for (int i=0; i < xTrain.length; i++) {
            xTrain[i][0] = i - 5;
        }
        return xTrain;
    }

    @Before
    public void setup() throws IOException {
        double[][] xTrain = this.getXTrain();
        double[] yTrain = this.func(xTrain);

        GaussianProcessRegression gpr = new GaussianProcessRegression();
        gpr.fit(xTrain, yTrain);

        double[][] xTest = {
                {2.5}
        };
        gpr.predict(xTest);

        System.out.println("test");
    }

    @Test
    public void testGetInitialNumEpoch() {
        System.out.println("test");
    }
}
