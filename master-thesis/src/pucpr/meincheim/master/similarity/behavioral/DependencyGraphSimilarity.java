package pucpr.meincheim.master.similarity.behavioral;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

import pucpr.meincheim.master.similarity.AbstractModelGraphSimilarityMeasure;
import pucpr.meincheim.master.similarity.SimilarityMeasure;

/**
 * Similarity calculation based on dependency graph comparison as proposed by
 * Bae et al.: Process mining, discovery, and integration using distance
 * measures.
 * 
 * @author Alex Meincheim 
 * 
 * Implementation based on Michael Becker.
 * 
 * Customized for ProM 6 and Petri net models
 * 
 */
public class DependencyGraphSimilarity extends AbstractModelGraphSimilarityMeasure
		implements SimilarityMeasure<PetrinetGraph> {

	protected static final double DEFAULT_DELTA = 0.5;

	protected double delta;

	public DependencyGraphSimilarity() {
		this(DEFAULT_DELTA);
	}

	public DependencyGraphSimilarity(double delta) {
		this.delta = delta;
	}

	public double calculateSimilarity(PetrinetGraph aGraph, PetrinetGraph bGraph) {

		// verify, if both models fulfil the delta-comparability defined as
		// |nodes1 intersection nodes2|/|nodes1 union nodes2| >= delta where
		// node equality is obtained by equal labels
		Set<String> elementNamesA = new HashSet<String>();
		Set<String> elementNamesB = new HashSet<String>();

		for (PetrinetNode vertex : aGraph.getNodes()) {
			elementNamesA.add(vertex.getLabel());
		}
		for (PetrinetNode vertex : aGraph.getNodes()) {
			elementNamesB.add(vertex.getLabel());
		}

		Set<String> namesIntersection = new HashSet<String>(elementNamesA);
		namesIntersection.retainAll(elementNamesB);
		Set<String> namesUnion = new HashSet<String>(elementNamesA);
		namesUnion.addAll(elementNamesB);

		double deltaCondition = (double) namesIntersection.size() / (double) namesUnion.size();
		if (deltaCondition < delta) {
			return -1;
		}

		// create the mapping between elements based on equal names
		Map<PetrinetNode, PetrinetNode> mappingsAB = getMapping(aGraph, bGraph);
		Map<PetrinetNode, PetrinetNode> mappingsBA = getMapping(bGraph, aGraph);

		Set<PetrinetEdge> edgesOnlyInA = getEdgesOnlyInOneModel(aGraph, bGraph, mappingsAB);
		Set<PetrinetEdge> edgesOnlyInB = getEdgesOnlyInOneModel(bGraph, aGraph, mappingsBA);
	
		return 1.0 / (1.0 + edgesOnlyInA.size() + edgesOnlyInB.size());
	}
}
