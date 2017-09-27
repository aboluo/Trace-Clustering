package pucpr.meincheim.master.similarity.label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import pucpr.meincheim.master.similarity.AbstractModelGraphSimilarityMeasure;
import pucpr.meincheim.master.similarity.LevenshteinNodeSimilarity;
import pucpr.meincheim.master.similarity.SimilarityMeasure;

/**
 * Similarity between process models according to Huang et al.: An algorithm for calculating process similarity to cluster open-source
 * process designs. In the first iteration, similarity between nodes is establish based on similarity between web services. In our
 * implementation we use {@link LevenshteinVertexSimilarity} as a default. However, every other vertex based similarity measure can be used,
 * too. Based on this, similarity between two edges (A1,A2) and (B1,B2) is calculated as follows:
 * <code>sim((A1,A2),(B1,B2)) = (sim(A1,A2) + sim(B1,B2))/2</code>. Overall similarity between nodes of a model M0 and nodes of a model M1
 * is calculated by summing up the individual node similarities and dividing this sum by the overall amount of nodes in both process models.
 * To calculate similarity between edges, edges are assigned with weights. In our implementation, we give an XOR-edge a weight of 1/n where
 * n is the amount of outgoing nodes. All over weights are left untouched, since the paper of Huang et al. does not concretise the approach.
 * @author Alex Meincheim Implementation based on Michael Becker. Customized for ProM 6 and Petri net models
 */

public class NodeLinkBasedSimilarity extends AbstractModelGraphSimilarityMeasure implements SimilarityMeasure<PetrinetGraph> {

	protected static final SimilarityMeasure<PetrinetNode> DEFAULT_NODE_SIMILARITY = new LevenshteinNodeSimilarity();

	protected SimilarityMeasure<PetrinetNode> nodeSimilarity;

	public NodeLinkBasedSimilarity() {
		this(DEFAULT_NODE_SIMILARITY);
	}

	public NodeLinkBasedSimilarity(SimilarityMeasure<PetrinetNode> nodeSimilarity) {
		this.nodeSimilarity = nodeSimilarity;
	}

	public double calculateSimilarity(PetrinetGraph modelA, PetrinetGraph modelB) {
		Map<PetrinetNode, Map<PetrinetNode, Double>> nodeSimilaritiesAB = new HashMap<PetrinetNode, Map<PetrinetNode, Double>>();
		Map<PetrinetNode, Map<PetrinetNode, Double>> nodeSimilaritiesBA = new HashMap<PetrinetNode, Map<PetrinetNode, Double>>();

		Set<PetrinetNode> transitionsModelA = getLabeledElements(modelA, true, true);
		Set<PetrinetNode> transitionsModelB = getLabeledElements(modelB, true, true);

		Set<Arc> edgesModelA = getTransitionEdges(transitionsModelA);
		Set<Arc> edgesModelB = getTransitionEdges(transitionsModelB);

		Map<PetrinetNode, Map<PetrinetNode, Double>> maxNodeSimilaritiesAB = calculateMaxNodeSimilarities(transitionsModelA,
				transitionsModelB, nodeSimilaritiesAB);
		Map<PetrinetNode, Map<PetrinetNode, Double>> maxNodeSimilaritiesBA = calculateMaxNodeSimilarities(transitionsModelB,
				transitionsModelA, nodeSimilaritiesBA);

		Map<Arc, Map<Arc, Double>> maxEdgeSimilaritiesAB = calculateMaxEdgeSimilarities(edgesModelA, edgesModelB, nodeSimilaritiesAB);
		Map<Arc, Map<Arc, Double>> maxEdgeSimilaritiesBA = calculateMaxEdgeSimilarities(edgesModelB, edgesModelA, nodeSimilaritiesBA);

		Map<Arc, Double> edgeWeightsA = calculateEdgeWeights(edgesModelA);
		Map<Arc, Double> edgeWeightsB = calculateEdgeWeights(edgesModelB);

		double simNodeSum = calculateSimNodeSum(maxNodeSimilaritiesAB) + calculateSimNodeSum(maxNodeSimilaritiesBA);

		double simEdgeSum = calculateSimEdgeSum(maxEdgeSimilaritiesAB, edgeWeightsA, edgeWeightsB)
				+ calculateSimEdgeSum(maxEdgeSimilaritiesBA, edgeWeightsB, edgeWeightsA);

		double nodeSim = simNodeSum / ((double) transitionsModelA.size() + (double) transitionsModelB.size());
		double edgeSim = simEdgeSum / ((double) edgesModelA.size() + (double) edgesModelB.size());
		
		 System.out.println(edgeWeightsA);
		 System.out.println(edgeWeightsB);
		
		 System.out.println(maxNodeSimilaritiesAB);
		 System.out.println(maxNodeSimilaritiesBA);
		
		 System.out.println(maxEdgeSimilaritiesAB);
		
		 System.out.println(simNodeSum);
		 System.out.println(nodeSim);
		 
		 System.out.println(simEdgeSum);

		return 1.0 / 2.0 * nodeSim + 1.0 / 2.0 * edgeSim;
	}

	/**
	 * Calculates the weight for all edges in the given model. An xor-edge (with style "X") gets a weight of 1 / amount of outgoing cases.
	 * @param model the model to calculate the edge weights for
	 * @return the weights of edges of the given model
	 */
	// TODO: Rever
	private Map<Arc, Double> calculateEdgeWeights(Set<Arc> edgesModel) {
		Map<Arc, Double> edgeWeights = new HashMap<Arc, Double>();

		for (Arc edge : edgesModel) {

			int cases = 1;

			Transition source = (Transition) edge.getSource();
			int edgeSourceVisibleSucessorsSize = source.getVisibleSuccessors().size();

			if (edgeSourceVisibleSucessorsSize > 1) {
				cases = edgeSourceVisibleSucessorsSize;
			} else {
				Transition target = (Transition) edge.getTarget();
				cases = target.getVisiblePredecessors().size();
			}

			edgeWeights.put(edge, 1.0 / cases);
		}

		return edgeWeights;
	}

	private Map<Arc, Map<Arc, Double>> calculateMaxEdgeSimilarities(Set<Arc> edgesModelA, Set<Arc> edgesModelB,
			Map<PetrinetNode, Map<PetrinetNode, Double>> nodeSimilarities) {

		Map<Arc, Map<Arc, Double>> edgeSimilarities = new HashMap<Arc, Map<Arc, Double>>();
		Map<Arc, Map<Arc, Double>> maxEdgeSimilarities = new HashMap<Arc, Map<Arc, Double>>();

		for (Object edgeObjectA : edgesModelA) {
			Arc edgeA = (Arc) edgeObjectA;

			double maxSim = 0;
			Arc mappedEdge = null;
			Map<Arc, Double> similaritiesA = new HashMap<Arc, Double>();

			for (Object edgeObjectB : edgesModelB) {
				Arc edgeB = (Arc) edgeObjectB;

				double simSrc = nodeSimilarities.get(edgeA.getSource()).get(edgeB.getSource());
				double simDest = nodeSimilarities.get(edgeA.getTarget()).get(edgeB.getTarget());

				double simEdge = (simSrc + simDest) / 2.0;

				similaritiesA.put(edgeB, simEdge);

				if (simEdge > maxSim) {
					maxSim = simEdge;
					mappedEdge = edgeB;
				}
			}

			edgeSimilarities.put(edgeA, similaritiesA);
			if (null != mappedEdge) {
				Map<Arc, Double> maxEdgeSimilarity = new HashMap<Arc, Double>();
				maxEdgeSimilarity.put(mappedEdge, maxSim);
				maxEdgeSimilarities.put(edgeA, maxEdgeSimilarity);
			}
		}

		return maxEdgeSimilarities;
	}

	private Map<PetrinetNode, Map<PetrinetNode, Double>> calculateMaxNodeSimilarities(Set<PetrinetNode> transitionsModelA,
			Set<PetrinetNode> transitionsModelB, Map<PetrinetNode, Map<PetrinetNode, Double>> nodeSimilarities) {
		Map<PetrinetNode, Map<PetrinetNode, Double>> maxNodeSimilarities = new HashMap<PetrinetNode, Map<PetrinetNode, Double>>();

		for (PetrinetNode vertexA : transitionsModelA) {
			PetrinetNode mappedVertex = null;
			Map<PetrinetNode, Double> similaritiesA = new HashMap<PetrinetNode, Double>();

			double maxSim = 0;
			for (PetrinetNode vertexB : transitionsModelB) {
				double simAB = nodeSimilarity.calculateSimilarity(vertexA, vertexB);

				similaritiesA.put(vertexB, simAB);

				if (simAB > maxSim) {
					maxSim = simAB;
					mappedVertex = vertexB;
				}
			}
			nodeSimilarities.put(vertexA, similaritiesA);

			if (null != mappedVertex) {
				Map<PetrinetNode, Double> maxNodeSimilarity = new HashMap<PetrinetNode, Double>();
				maxNodeSimilarity.put(mappedVertex, maxSim);
				maxNodeSimilarities.put(vertexA, maxNodeSimilarity);
			}
		}

		return maxNodeSimilarities;
	}

	private double calculateSimEdgeSum(Map<Arc, Map<Arc, Double>> edgeSimilarities, Map<Arc, Double> edgeWeightsA,
			Map<Arc, Double> edgeWeightsB) {
		double simSum = 0;

		for (Arc edgeA : edgeSimilarities.keySet()) {
			double weightA = edgeWeightsA.get(edgeA);

			for (Arc edgeB : edgeSimilarities.get(edgeA).keySet()) {
				double weightB = edgeWeightsB.get(edgeB);

				double sim = edgeSimilarities.get(edgeA).get(edgeB);

				simSum += weightA * weightB * sim;
			}
		}

		return simSum;
	}

	private double calculateSimNodeSum(Map<PetrinetNode, Map<PetrinetNode, Double>> nodeSimilarities) {
		double simSum = 0;

		for (PetrinetNode vertexA : nodeSimilarities.keySet()) {
			for (PetrinetNode vertexB : nodeSimilarities.get(vertexA).keySet()) {
				simSum += nodeSimilarities.get(vertexA).get(vertexB);
			}
		}

		return simSum;
	}
}