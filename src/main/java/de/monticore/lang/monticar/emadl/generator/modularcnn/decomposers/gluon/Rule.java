package de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.gluon;

public class Rule {
    public String op = null;
    public String name = null;
    public String inputs = null;
    public boolean condition = false;

    public Rule(String op, String name, String inputs){
        this.op = op;
        this.name = name;
        this.inputs = inputs;
    }


}
