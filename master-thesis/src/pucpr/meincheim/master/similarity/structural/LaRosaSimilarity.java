package pucpr.meincheim.master.similarity.structural;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;

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

		Set<PetrinetNode> transitionsModelA = getLabeledElements(modelA, true, true);
		Set<PetrinetNode> transitionsModelB = getLabeledElements(modelB, true, true);

		// calculate similarity between activities of transitions
		calculateActivitiesSimilarity(transitionsModelA, transitionsModelB, mappingsAB, mappingsBA, similarities);

		Set<PetrinetNode> deletedVertices = calculateDeletedVertices(mappingsAB, transitionsModelA);
		Set<PetrinetNode> addedVertices = calculateAddedVertices(mappingsAB, transitionsModelB);

		Set<Arc> edgesModelA = getTransitionEdges(transitionsModelA);
		Set<Arc> edgesModelB = getTransitionEdges(transitionsModelB);

		// search for edges within a but not in b
		Set<Arc> deletedEdges = getArcsOnlyInOneModel(edgesModelA, edgesModelB, mappingsAB);
		Set<Arc> addedEdges = getArcsOnlyInOneModel(edgesModelB, edgesModelA, mappingsBA);

		double simSum = 0.0;
		for (PetrinetNode vertexA : similarities.keySet()) {
			for (PetrinetNode vertexB : similarities.get(vertexA).keySet()) {
				simSum += 1.0 - similarities.get(vertexA).get(vertexB);
			}
		}

		double fskipn = (double) (deletedVertices.size() + addedVertices.size()) / (double) (transitionsModelA.size()+ transitionsModelB.size());
		double fskipe = (double) (deletedEdges.size() + addedEdges.size()) / (double) (edgesModelA.size() + edgesModelB.size());
		double fsubn = 2.0 * simSum / (transitionsModelA.size() + transitionsModelB.size() - deletedVertices.size() - addedVertices.size());

		return 1.0 - (wskipn * fskipn + wskipe * fskipe + wsubn * fsubn) / (wskipn + wskipe + wsubn);

	}

	private Set<PetrinetNode> calculateAddedVertices(Map<PetrinetNode, PetrinetNode> mappingsAB,
			Set<PetrinetNode> transitionsModelB) {
		// calculate the set of added vertices
		Set<PetrinetNode> addedVertices = new HashSet<PetrinetNode>();
		{
			for (PetrinetNode vertex : transitionsModelB) {
				if (!mappingsAB.containsValue(vertex)) {
					addedVertices.add(vertex);
				}
			}
		}
		return addedVertices;
	}

	private Set<PetrinetNode> calculateDeletedVertices(Map<PetrinetNode, PetrinetNode> mappingsAB,
			Set<PetrinetNode> transitionsModelA) {
		// calculate the set of deleted vertices
		Set<PetrinetNode> deletedVertices = new HashSet<PetrinetNode>();
		for (PetrinetNode vertex : transitionsModelA) {
			if (!mappingsAB.containsKey(vertex)) {
				deletedVertices.add(vertex);
			}
		}
		return deletedVertices;
	}

	private void calculateActivitiesSimilarity(Set<PetrinetNode> transitionsModelA, Set<PetrinetNode> transitionsModelB,
			Map<PetrinetNode, PetrinetNode> mappingsAB, Map<PetrinetNode, PetrinetNode> mappingsBA,
			Map<PetrinetNode, Map<PetrinetNode, Double>> similarities) {

		for (PetrinetNode nodeA : transitionsModelA) {

			double maxSim = 0.80;
			PetrinetNode match = null;

			for (PetrinetNode nodeB : transitionsModelB) {

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
					// break;
				}
			}
		}
	}

}