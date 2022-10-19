package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import junit.framework.TestCase;

import java.util.List;

public class CandidateFinderTest extends TestCase {

    public void testConstructor() {
        CandidateFinder candidateFinder = new CandidateFinder();
        assertNotNull(candidateFinder);
    }

    public void testConstructorSetsCandidateBuilder() {
        CandidateFinder candidateFinder = new CandidateFinder();
        assertNotNull(candidateFinder.getCandidateBuilder());
    }

    public void testConstructorSetsComponentFinder() {
        CandidateFinder candidateFinder = new CandidateFinder();
        assertNotNull(candidateFinder.getComponentFinder());
    }

    public void testFindCandidatesReturnsTwoCandidates() {
        CandidateFinder candidateFinder = new CandidateFinder();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        List<ArchitectureSymbol> candidates = candidateFinder.findCandidates(architecture, 1);
        int expected = 2;
        int actual = candidates.size();
        assertEquals(expected, actual);
    }
}