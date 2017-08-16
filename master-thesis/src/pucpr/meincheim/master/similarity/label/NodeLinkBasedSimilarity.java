package pucpr.meincheim.master.similarity.label;

import java.util.HashMap;
import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

import pucpr.meincheim.master.similarity.AbstractModelGraphSimilarityMeasure;
import pucpr.meincheim.master.similarity.LevenshteinNodeSimilarity;
import pucpr.meincheim.master.similarity.SimilarityMeasure;

/**
 * Similarity between process models according to Huang et al.: An algorithm for
 * calculating process similarity to cluster open-source process designs.
 * 
 * In the first iteration, similarity between nodes is establish based on
 * similarity between web services. In our implementation we use
 * {@link LevenshteinVertexSimilarity} as a default. However, every other vertex
 * based similarity measure can be used, too. Based on this, similarity between
 * two edges (A1,A2) and (B1,B2) is calculated as follows:
 * <code>sim((A1,A2),(B1,B2)) = (sim(A1,A2) + sim(B1,B2))/2</code>.
 * 
 * Overall similarity between nodes of a model M0 and nodes of a model M1 is
 * calculated by summing up the individual node similarities and dividing this
 * sum by the overall amount of nodes in both process models. To calculate
 * similarity between edges, edges are assigned with weights. In our
 * implementation, we give an XOR-edge a weight of 1/n where n is the amount of
 * outgoing nodes. All over weights are left untouched, since the paper of Huang
 * et al. does not concretise the approach.
 * 
 * 
 * @author Alex Meincheim 
 * 
 * Implementation based on Michael Becker.
 * 
 * Customized for ProM 6 and Petri net models
 * 
 */

//TODO: Rever questão dos nodes
public class NodeLinkBasedSimilarity extends AbstractModelGraphSimilarityMeasure
		implements SimilarityMeasure<PetrinetGraph> {

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

		Map<PetrinetNode, Map<PetrinetNode, Double>> maxNodeSimilaritiesAB = calculateMaxNodeSimilarities(modelA,
				modelB, nodeSimilaritiesAB);
		Map<PetrinetNode, Map<PetrinetNode, Double>> maxNodeSimilaritiesBA = calculateMaxNodeSimilarities(modelB,
				modelA, nodeSimilaritiesBA);

		Map<PetrinetEdge, Map<PetrinetEdge, Double>> maxEdgeSimilaritiesAB = calculateMaxEdgeSimilarities(modelA,
				modelB, nodeSimilaritiesAB);
		Map<PetrinetEdge, Map<PetrinetEdge, Double>> maxEdgeSimilaritiesBA = calculateMaxEdgeSimilarities(modelB,
				modelA, nodeSimilaritiesBA);

		Map<PetrinetEdge, Double> edgeWeightsA = calculateEdgeWeights(modelA);
		Map<PetrinetEdge, Double> edgeWeightsB = calculateEdgeWeights(modelB);

		double simNodeSum = calculateSimNodeSum(maxNodeSimilaritiesAB) + calculateSimNodeSum(maxNodeSimilaritiesBA);
		double simEdgeSum = calculateSimEdgeSum(maxEdgeSimilaritiesAB, edgeWeightsA, edgeWeightsB)
				+ calculateSimEdgeSum(maxEdgeSimilaritiesBA, edgeWeightsB, edgeWeightsA);

		double nodeSim = simNodeSum
				/ ((double) modelA.getTransitions().size() + (double) modelB.getTransitions().size());
		double edgeSim = simEdgeSum / ((double) modelA.getEdges().size() + (double) modelB.getEdges().size());

		return 1.0 / 2.0 * nodeSim + 1.0 / 2.0 * edgeSim;
	}

	/**
	 * Calculates the weight for all edges in the given model. An xor-edge (with
	 * style "X") gets a weight of 1 / amount of outgoing cases.
	 * 
	 * @param model
	 *            the model to calculate the edge weights for
	 * @return the weights of edges of the given model
	 */
	//TODO: Rever 
	private Map<PetrinetEdge, Double> calculateEdgeWeights(PetrinetGraph model) {
		Map<PetrinetEdge, Double> edgeWeights = new HashMap<PetrinetEdge, Double>();

		for (Object edgeObject : model.getEdges()) {
			PetrinetEdge edge = (PetrinetEdge) edgeObject;
//			if ((null != edge.getStyle()) && edge.getStyle().equals("X")) {
//				int cases;
//				if (1 < edge.getSource().getOutEdges().size()) {
//					cases = edge.getSource().getOutEdges().size();
//				} else {
//					cases = edge.getDest().getInEdges().size();
//				}
//
//				edgeWeights.put(edge, 1.0 / cases);
//			} else {
				edgeWeights.put(edge, 1.0);
//			}
		}

		return edgeWeights;
	}

	private Map<PetrinetEdge, Map<PetrinetEdge, Double>> calculateMaxEdgeSimilarities(PetrinetGraph a, PetrinetGraph b,
			Map<PetrinetNode, Map<PetrinetNode, Double>> nodeSimilarities) {

		Map<PetrinetEdge, Map<PetrinetEdge, Double>> edgeSimilarities = new HashMap<PetrinetEdge, Map<PetrinetEdge, Double>>();
		Map<PetrinetEdge, Map<PetrinetEdge, Double>> maxEdgeSimilarities = new HashMap<PetrinetEdge, Map<PetrinetEdge, Double>>();

		for (Object edgeObjectA : a.getEdges()) {
			PetrinetEdge edgeA = (PetrinetEdge) edgeObjectA;

			double maxSim = 0;
			PetrinetEdge mappedEdge = null;
			Map<PetrinetEdge, Double> similaritiesA = new HashMap<PetrinetEdge, Double>();

			for (Object edgeObjectB : b.getEdges()) {
				PetrinetEdge edgeB = (PetrinetEdge) edgeObjectB;

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
				Map<PetrinetEdge, Double> maxEdgeSimilarity = new HashMap<PetrinetEdge, Double>();
				maxEdgeSimilarity.put(mappedEdge, maxSim);
				maxEdgeSimilarities.put(edgeA, maxEdgeSimilarity);
			}
		}

		return maxEdgeSimilarities;
	}

	private Map<PetrinetNode, Map<PetrinetNode, Double>> calculateMaxNodeSimilarities(PetrinetGraph a, PetrinetGraph b,
			Map<PetrinetNode, Map<PetrinetNode, Double>> nodeSimilarities) {
		Map<PetrinetNode, Map<PetrinetNode, Double>> maxNodeSimilarities = new HashMap<PetrinetNode, Map<PetrinetNode, Double>>();

		for (PetrinetNode vertexA : getLabeledElements(a, true, true)) {
			PetrinetNode mappedVertex = null;
			Map<PetrinetNode, Double> similaritiesA = new HashMap<PetrinetNode, Double>();

			double maxSim = 0;
			for (PetrinetNode vertexB : getLabeledElements(b, true, true)) {
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

	private double calculateSimEdgeSum(Map<PetrinetEdge, Map<PetrinetEdge, Double>> edgeSimilarities,
			Map<PetrinetEdge, Double> edgeWeightsA, Map<PetrinetEdge, Double> edgeWeightsB) {
		double simSum = 0;

		for (PetrinetEdge edgeA : edgeSimilarities.keySet()) {
			double weightA = edgeWeightsA.get(edgeA);

			for (PetrinetEdge edgeB : edgeSimilarities.get(edgeA).keySet()) {
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