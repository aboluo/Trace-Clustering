package pucpr.meincheim.master.similarity.structural;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import pucpr.meincheim.master.similarity.AbstractModelGraphSimilarityMeasure;
import pucpr.meincheim.master.similarity.LevenshteinNodeSimilarity;
import pucpr.meincheim.master.similarity.SimilarityMeasure;

public class LaRosaSimilarity extends AbstractModelGraphSimilarityMeasure implements SimilarityMeasure<PetrinetGraph> {

	protected static final SimilarityMeasure<PetrinetNode> DEFAULT_NODE_SIMILARITY = new LevenshteinNodeSimilarity();
	protected static final double DEFAULT_WSKIPN = 1.0;
	protected static final double DEFAULT_WSKIPE = 1.0;
	protected static final double DEFAULT_WSUBN = 1.0;

	protected SimilarityMeasure<PetrinetNode> labeledNodeSimilarity;
	protected double wskipn;
	protected double wskipe;
	protected double wsubn;

	public LaRosaSimilarity() {
		this(DEFAULT_NODE_SIMILARITY, DEFAULT_WSKIPN, DEFAULT_WSKIPE, DEFAULT_WSUBN);
	}

	public LaRosaSimilarity(SimilarityMeasure<PetrinetNode> nodeSimilarity, double wskipn, double wskipe,
			double wsubn) {
		this.labeledNodeSimilarity = nodeSimilarity;
		this.wskipn = wskipn;
		this.wskipe = wskipe;
		this.wsubn = wsubn;
	}

	public double calculateSimilarity(PetrinetGraph modelA, PetrinetGraph modelB) {

		Map<PetrinetNode, PetrinetNode> mappingsAB = new HashMap<PetrinetNode, PetrinetNode>();
		Map<PetrinetNode, PetrinetNode> mappingsBA = new HashMap<PetrinetNode, PetrinetNode>();
		Map<PetrinetNode, Map<PetrinetNode, Double>> similarities = new HashMap<PetrinetNode, Map<PetrinetNode, Double>>();

		// calculate similarity between activities of transitions
		calculateActivitiesSimilarity(modelA, modelB, mappingsAB, mappingsBA, similarities);

		mappingsAB.putAll(getPlacesMapping(modelA, modelB));
		mappingsBA.putAll(getPlacesMapping(modelB, modelA));

		Set<PetrinetNode> deletedVertices = calculateDeletedVertices(modelA, mappingsAB);
		Set<PetrinetNode> addedVertices = calculateAddedVertices(modelB, mappingsAB);

		Set<PetrinetEdge> deletedEdges = getEdgesOnlyInOneModel(modelA, modelB, mappingsAB);
		Set<PetrinetEdge> addedEdges = getEdgesOnlyInOneModel(modelB, modelA, mappingsBA);

		double simSum = 0.0;
		for (PetrinetNode vertexA : similarities.keySet()) {
			for (PetrinetNode vertexB : similarities.get(vertexA).keySet()) {
				simSum += 1.0 - similarities.get(vertexA).get(vertexB);
			}
		}

		// TODO: REVISAR
		double fskipn = (double) (deletedVertices.size() + addedVertices.size())
				/ (double) (getLabeledElements(modelA, true, true).size() + getLabeledElements(modelB, true, true).size());
		double fskipe = (double) (deletedEdges.size() + addedEdges.size())
				/ (double) (modelA.getEdges().size() + modelB.getEdges().size());
		double fsubn = 2.0 * simSum
				/ (getLabeledElements(modelA, true, true).size() + getLabeledElements(modelB, true, true).size() - deletedVertices.size() - addedVertices.size());

		return 1.0 - (wskipn * fskipn + wskipe * fskipe + wsubn * fsubn) / (wskipn + wskipe + wsubn);

	}

	private void calculateActivitiesSimilarity(PetrinetGraph modelA, PetrinetGraph modelB,
			Map<PetrinetNode, PetrinetNode> mappingsAB, Map<PetrinetNode, PetrinetNode> mappingsBA,
			Map<PetrinetNode, Map<PetrinetNode, Double>> similarities) {

		for (PetrinetNode nodeA : modelA.getTransitions()) {

			if (!isInvisibleTransition(nodeA)) {

				double maxSim = 0.99;
				PetrinetNode match = null;

				for (PetrinetNode nodeB : modelB.getTransitions()) {

					if (!isInvisibleTransition(nodeB)) {

						double simAB = labeledNodeSimilarity.calculateSimilarity(nodeA, nodeB);

						if (simAB > maxSim) {
							if (null != similarities.get(nodeA)) {
								similarities.get(nodeA).clear();
							} else {
								similarities.put(nodeA, new HashMap<PetrinetNode, Double>());
							}
							
							similarities.get(nodeA).put(nodeB, simAB);
							mappingsAB.put(nodeA, nodeB);
							maxSim = simAB;
							match = nodeB;
						}

						if (null != match) {
							mappingsBA.put(match, nodeA);
							//break;
						}
					}
				}
			}
		}
	}

	private Set<PetrinetNode> calculateAddedVertices(PetrinetGraph modelB, Map<PetrinetNode, PetrinetNode> mappingsAB) {
		Set<PetrinetNode> addedVertices = new HashSet<PetrinetNode>();
		{
			for (PetrinetNode vertex : modelB.getNodes()) {
				if (!isInvisibleTransition(vertex) && !mappingsAB.containsValue(vertex)) {
					addedVertices.add(vertex);
				}
			}
		}
		return addedVertices;
	}

	private Set<PetrinetNode> calculateDeletedVertices(PetrinetGraph modelA,
			Map<PetrinetNode, PetrinetNode> mappingsAB) {
		Set<PetrinetNode> deletedVertices = new HashSet<PetrinetNode>();
		for (PetrinetNode vertex : modelA.getNodes()) {
			if (!isInvisibleTransition(vertex) && !mappingsAB.containsKey(vertex)) {
				deletedVertices.add(vertex);
			}
		}
		return deletedVertices;
	}
}