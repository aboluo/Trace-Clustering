package pucpr.meincheim.master.similarity.label;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;

import pucpr.meincheim.master.similarity.AbstractModelGraphSimilarityMeasure;
import pucpr.meincheim.master.similarity.SimilarityMeasure;

/**
 * Similarity between workflows according to Minor et al.: Representation and
 * Structure-Based Similarity Assessment for Agile Workflows.
 * 
 * Currently, EPCs are supported and are converted into their process graph
 * representation. Information about connectors is lost during this
 * transformation. Therefore, XOR, AND, and OR connectors are all transformed
 * into the same edge.
 * 
 * TODO: allow for comparison based on connector types, too
 * 
 * @author Alex Meincheim
 * 
 *         Implementation based on Michael Becker.
 * 
 *         Customized for ProM 6 and Petri net models
 */
public class CommonNodesEdgesSimilarity extends AbstractModelGraphSimilarityMeasure
		implements SimilarityMeasure<PetrinetGraph> {

	public double calculateSimilarity(PetrinetGraph aGraph, PetrinetGraph bGraph) {

		Map<PetrinetNode, PetrinetNode> mappingsAB = getMapping(aGraph, bGraph);
		Map<PetrinetNode, PetrinetNode> mappingsBA = getMapping(bGraph, aGraph);

		Set<PetrinetNode> transitionsModelA = getLabeledElements(aGraph, true, true);
		Set<PetrinetNode> transitionsModelB = getLabeledElements(bGraph, true, true);

		// create the set of vertices contained only in one model based on the
		// mapping
		Set<PetrinetNode> verticesOnlyInA = new HashSet<PetrinetNode>(transitionsModelA);
		Set<PetrinetNode> verticesOnlyInB = new HashSet<PetrinetNode>(transitionsModelB);

		Set<Arc> edgesModelA = getTransitionEdges(transitionsModelA);
		Set<Arc> edgesModelB = getTransitionEdges(transitionsModelB);

		verticesOnlyInA.removeAll(mappingsAB.keySet());
		verticesOnlyInB.removeAll(mappingsBA.keySet());

		// search for edges within a but not in b
		Set<Arc> edgesOnlyInA = getArcsOnlyInOneModel(edgesModelA, edgesModelB, mappingsAB);
		Set<Arc> edgesOnlyInB = getArcsOnlyInOneModel(edgesModelB, edgesModelA, mappingsBA);

		double distance = verticesOnlyInA.size() + verticesOnlyInB.size() + edgesOnlyInA.size() + edgesOnlyInB.size();

		return 1.0 - distance
				/ (transitionsModelA.size() + transitionsModelB.size() + edgesModelA.size() + edgesModelB.size());
	}

}
