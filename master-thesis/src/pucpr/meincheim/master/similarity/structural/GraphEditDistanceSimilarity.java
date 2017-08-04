package pucpr.meincheim.master.similarity.structural;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

import pucpr.meincheim.master.similarity.AbstractModelGraphSimilarityMeasure;
import pucpr.meincheim.master.similarity.LevenshteinNodeSimilarity;
import pucpr.meincheim.master.similarity.SimilarityMeasure;

/**
 * Graph Edit Distance Similarity according to Dijkman et al.: Graph Matching
 * Algorithms for Business Process Similarity Search
 * 
 * The measure is based on the graph-representation of business process models
 * where connector/gateway nodes are removed. Similarity is calculated based on
 * the amount of necessary transformation operations:
 * <ul>
 * <li>Node Substitution</li>
 * <li>Node Insertion / Deletion</li>
 * <li>Edge Insertion / Deletion</li>
 * </ul>
 * 
 * There is a cost function for every transformation. The cost of substituting
 * node n1 with node n2 is calculated by 1 - sim(n1, n2). The default similarity
 * metric for node similarity is {@link LevenshteinSimilarity}.
 * 
 * Similarity of models M1, M2 with activities A1, A2 and edges E1, E2 is then
 * calculated based on the set of substituted nodes subn, inserted/deleted nodes
 * skipn, and inserted/deleted edges skipe as follows: fskipn = |skipn|/(|A1| +
 * |A2|), fskipe = |skipe|/(|E1| + |E2|), fsubn = 2 * sum(1 - sim(a1,a2))
 * 
 * sim(M1, M2) = (wskipn*fskipn + wskipe*fskipe + wsubn*fsubn) / (wskipn +
 * wskipe + wsubn)
 * 
 * @author Alex Meincheim
 * 
 *         Implementation based on Michael Becker.
 * 
 *         Customized for ProM 6 and Petri net models
 * 
 */
public class GraphEditDistanceSimilarity extends AbstractModelGraphSimilarityMeasure
		implements SimilarityMeasure<PetrinetGraph> {

	protected static final SimilarityMeasure<PetrinetNode> DEFAULT_SIM_STRATEGY = new LevenshteinNodeSimilarity();

	protected static final double DEFAULT_WSKIPN = 1;
	protected static final double DEFAULT_WSKIPE = 1;
	protected static final double DEFAULT_WSUBN = 1;

	protected double wskipn;
	protected double wskipe;
	protected double wsubn;
	protected SimilarityMeasure<PetrinetNode> similarityStrategy;

	public GraphEditDistanceSimilarity() {
		this(DEFAULT_SIM_STRATEGY, DEFAULT_WSKIPN, DEFAULT_WSKIPE, DEFAULT_WSUBN);
	}

	public GraphEditDistanceSimilarity(SimilarityMeasure<PetrinetNode> similarityStrategy, double wskipn, double wskipe,
			double wsubn) {
		this.similarityStrategy = similarityStrategy;
		this.wskipn = wskipn;
		this.wskipe = wskipe;
		this.wsubn = wsubn;
	}

	public double calculateSimilarity(PetrinetGraph modelA, PetrinetGraph modelB) {

		Map<PetrinetNode, PetrinetNode> mappingsAB = new HashMap<PetrinetNode, PetrinetNode>();
		Map<PetrinetNode, PetrinetNode> mappingsBA = new HashMap<PetrinetNode, PetrinetNode>();
		Map<PetrinetNode, Map<PetrinetNode, Double>> similarities = new HashMap<PetrinetNode, Map<PetrinetNode, Double>>();

		for (PetrinetNode vertexA : getLabeledElements(modelA, true, true)) {
			double maxSim = 0;
			PetrinetNode match = null;
			for (PetrinetNode vertexB : getLabeledElements(modelB, true, true)) {
				double simAB = similarityStrategy.calculateSimilarity(vertexA, vertexB);

				if (simAB > maxSim) {
					if (null != similarities.get(vertexA)) {
						similarities.get(vertexA).clear();
					} else {
						similarities.put(vertexA, new HashMap<PetrinetNode, Double>());
					}

					similarities.get(vertexA).put(vertexB, simAB);
					mappingsAB.put(vertexA, vertexB);

					maxSim = simAB;
					match = vertexB;
				}
			}

			if (null != match) {
				mappingsBA.put(match, vertexA);
			}
		}

		Set<PetrinetNode> deletedVertices = new HashSet<PetrinetNode>();
		Set<PetrinetNode> addedVertices = new HashSet<PetrinetNode>();

		// calculate the set of deleted vertices
		for (PetrinetNode vertex : getLabeledElements(modelA, true, true)) {
			if (!mappingsAB.containsKey(vertex)) {
				deletedVertices.add(vertex);
			}
		}

		// calculate the set of added vertices
		for (PetrinetNode vertex : getLabeledElements(modelB, true, true)) {
			if (!mappingsAB.containsValue(vertex)) {
				addedVertices.add(vertex);
			}
		}

		// calculate the set of deleted and added edges
		Set<PetrinetEdge> deletedEdges = getEdgesOnlyInOneModel(modelA, modelB, mappingsAB);
		Set<PetrinetEdge> addedEdges = getEdgesOnlyInOneModel(modelB, modelA, mappingsBA);

		double simDist = 0.0;
		for (PetrinetNode vertexA : similarities.keySet()) {
			for (PetrinetNode vertexB : similarities.get(vertexA).keySet()) {
				simDist += 1.0 - similarities.get(vertexA).get(vertexB);
			}
		}

		double fskipn = 0;
		double fskipe = 0;
		double fsubn = 0;

		fskipn = (double) (deletedVertices.size() + addedVertices.size())
				/ (double) (getLabeledElements(modelA, true, true).size()
						+ getLabeledElements(modelB, true, true).size());

		fskipe = (double) (deletedEdges.size() + addedEdges.size())
				/ (double) (modelA.getEdges().size() + modelB.getEdges().size());

		fsubn = 2.0 * simDist / (getLabeledElements(modelA, true, true).size()
				+ getLabeledElements(modelB, true, true).size() - deletedVertices.size() - addedVertices.size());

		return 1.0 - (wskipn * fskipn + wskipe * fskipe + wsubn * fsubn) / (wskipn + wskipe + wsubn);
	}
}
