package pucpr.meincheim.master.similarity.label;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

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
 * Implementation based on Michael Becker.
 * 
 * Customized for ProM 6 and Petri net models
 */
public class CommonNodesEdgesSimilarity extends AbstractModelGraphSimilarityMeasure
		implements SimilarityMeasure<PetrinetGraph> {

	public double calculateSimilarity(PetrinetGraph aGraph, PetrinetGraph bGraph)  {

		Map<PetrinetNode, PetrinetNode> mappingsAB = getMapping(aGraph, bGraph);
		Map<PetrinetNode, PetrinetNode> mappingsBA = getMapping(bGraph, aGraph);

		// create the set of vertices contained only in one model based on the mapping
		Set<PetrinetNode> verticesOnlyInA = new HashSet<PetrinetNode>(getLabeledElements(aGraph,true,true));
		Set<PetrinetNode> verticesOnlyInB = new HashSet<PetrinetNode>(getLabeledElements(bGraph,true,true));
		
		verticesOnlyInA.removeAll(mappingsAB.keySet());
		verticesOnlyInB.removeAll(mappingsBA.keySet());
		
		mappingsAB.putAll(getPlacesMapping(aGraph, bGraph));
		mappingsBA.putAll(getPlacesMapping(bGraph, aGraph));

		// search for edges within a but not in b
		Set<PetrinetEdge> edgesOnlyInA = getEdgesOnlyInOneModel(aGraph, bGraph, mappingsAB);
		Set<PetrinetEdge> edgesOnlyInB = getEdgesOnlyInOneModel(bGraph, aGraph, mappingsBA);

		double distance = verticesOnlyInA.size() + verticesOnlyInB.size() + edgesOnlyInA.size() + edgesOnlyInB.size();

		return 1.0 - distance / (aGraph.getNodes().size() + bGraph.getNodes().size()
				+ aGraph.getEdges().size() + bGraph.getEdges().size());
	}

}
