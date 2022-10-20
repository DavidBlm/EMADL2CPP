package de.monticore.mlpipelines.helper;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;

public class ArchitectureWrapper implements Cloneable {
    private ArchitectureSymbol architecture;

    public ArchitectureWrapper(ArchitectureSymbol architecture) {
        this.architecture = architecture;
    }

    public ArchitectureSymbol getArchitecture() {
        return architecture;
    }

    public void setArchitecture(ArchitectureSymbol architecture) {
        this.architecture = architecture;
    }

    @Override
    public ArchitectureWrapper clone() {
        try {
            ArchitectureWrapper clone = (ArchitectureWrapper) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
