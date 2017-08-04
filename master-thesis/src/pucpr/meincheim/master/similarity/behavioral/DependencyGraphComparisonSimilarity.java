package pucpr.meincheim.master.similarity.behavioral;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import pucpr.meincheim.master.similarity.AbstractModelGraphSimilarityMeasure;
import pucpr.meincheim.master.similarity.SimilarityMeasure;

/**
 * Similarity of process models according to Jung et al.: Hierarchical
 * clustering of Business Process Models.
 * 
 * The idea is to establish two vectors and calculate similarity between process
 * models based on comparing these vectors. The activity vector contains
 * execution probabilities for every activity. Sequential activities and
 * activities in an AND-split have an execution probability of 1, activities
 * following an XOR-split of 1/n where n is the amount of outgoing arcs, and
 * activities following an OR-split 1/2.
 * 
 * Furhtermore, a transition vector is established: t_x = (t_ij,x), t_ij,x =
 * 1/d_ij,x*e_i,x*e_j,x where d_ij is the distance between activities i and j.
 * Directly connected activities have a distance of 1 increasing with the amount
 * of intermediary activities. e_i,x and e_j,x represent the execution
 * probabilities of activities i and j, respectively.
 * 
 * Similary of vectors is calculated using cosine coefficient. Similarity of
 * activtiy vectors is calculated by
 * sim_act=sum(a_ix*a_jx)/sqrt(sum(a_ix^2)*sum(a_jx^2)) and similarity of
 * transition vectors is sum(t_ix*t_jx)/sqrt(sum(t_ix^2)*sum(t_jx)^2).
 * 
 * Overall similarity between process models is the weighted average between
 * activity vector and transition vector similarity: sim(M1,M2) =
 * w*sim_act(M1,M2) + (1-w)*sim_trans(M1,M2).
 * 
 * In our implementation, we can handle EPC models with cycles
 * 
 * @author Alex Meincheim 
 * 
 * Implementation based on Michael Becker.
 * 
 * Customized for ProM 6 and Petri net models
 */
public class DependencyGraphComparisonSimilarity extends AbstractModelGraphSimilarityMeasure
		implements SimilarityMeasure<PetrinetGraph> {

	public static final double DEFAULT_ALPHA = 0.5;

	protected double alpha;

	public DependencyGraphComparisonSimilarity() {
		this(DEFAULT_ALPHA);
	}

	public DependencyGraphComparisonSimilarity(double alpha) {
		this.alpha = alpha;
	}

	public double calculateSimilarity(PetrinetGraph a, PetrinetGraph b) {

		PetrinetGraph aGraph = a;
		PetrinetGraph bGraph = b;

		Map<PetrinetNode, Double> activityVectorA = buildActivityVector(aGraph);
		Map<PetrinetNode, Double> activityVectorB = buildActivityVector(bGraph);

		Map<PetrinetNode, Map<PetrinetNode, Integer>> distanceVectorsA = buildDistanceVectors(aGraph);
		Map<PetrinetNode, Map<PetrinetNode, Integer>> distanceVectorsB = buildDistanceVectors(bGraph);

		Map<PetrinetNode, Map<PetrinetNode, Double>> transitionVectorsA = buildTransitionVectors(aGraph,
				activityVectorA, distanceVectorsA);
		Map<PetrinetNode, Map<PetrinetNode, Double>> transitionVectorsB = buildTransitionVectors(bGraph,
				activityVectorB, distanceVectorsB);

		// calculate similarity of activity and transition vectors
		double sumSquaredExecutionProbabilitiesA = 0;
		double sumSquaredExecutionProbabilitiesB = 0;
		double sumExecutionProbabilities = 0;

		double sumSquaredTransitionProbabilitiesA = 0;
		double sumSquaredTransitionProbabilitiesB = 0;
		double sumTransitionProbabilities = 0;

		// establish the set of all activities occurring in models a and b
		Set<String> vertexIdentifiers = new HashSet<String>();
		for (PetrinetNode vertex : getLabeledElements(aGraph, true, true)) {
			vertexIdentifiers.add(vertex.getLabel());
		}
		for (PetrinetNode vertex : getLabeledElements(bGraph, true, true)) {
			vertexIdentifiers.add(vertex.getLabel());
		}

		// now calculate the necessary variables for the similarities
		for (String vertexIdentifier : vertexIdentifiers) {

			PetrinetNode vertexA = getNode(aGraph, vertexIdentifier);
			PetrinetNode vertexB = getNode(bGraph, vertexIdentifier);

			double executionProbabilityA = null == vertexA ? 0 : activityVectorA.get(vertexA);
			double executionProbabilityB = null == vertexB ? 0 : activityVectorB.get(vertexB);

			sumExecutionProbabilities += executionProbabilityA * executionProbabilityB;
			sumSquaredExecutionProbabilitiesA += executionProbabilityA * executionProbabilityA;
			sumSquaredExecutionProbabilitiesB += executionProbabilityB * executionProbabilityB;

			for (String vertexIdentifer2 : vertexIdentifiers) {
				PetrinetNode vertexA2 = getNode(aGraph, vertexIdentifer2);
				PetrinetNode vertexB2 = getNode(bGraph, vertexIdentifer2);

				double transitionProbabilityA = null == vertexA || null == vertexA2 ? 0
						: transitionVectorsA.get(vertexA).get(vertexA2);
				double transitionProbabilityB = null == vertexB || null == vertexB2 ? 0
						: transitionVectorsB.get(vertexB).get(vertexB2);

				sumTransitionProbabilities += transitionProbabilityA * transitionProbabilityB;
				sumSquaredTransitionProbabilitiesA += transitionProbabilityA * transitionProbabilityA;
				sumSquaredTransitionProbabilitiesB += transitionProbabilityB * transitionProbabilityB;
			}
		}

		double simAct = sumExecutionProbabilities
				/ Math.sqrt(sumSquaredExecutionProbabilitiesA * sumSquaredExecutionProbabilitiesB);
		double simTran = sumTransitionProbabilities
				/ Math.sqrt(sumSquaredTransitionProbabilitiesA * sumSquaredTransitionProbabilitiesB);

		return alpha * simAct + (1.0 - alpha) * simTran;
	}

	/**
	 * Establishes the activity vector for the given model graph. The activity
	 * vector contains the execution probabilities for activities in a process
	 * model.
	 * 
	 * @param graph
	 *            the graph
	 * 
	 * @return the activity vector for the given model graph
	 */
	private Map<PetrinetNode, Double> buildActivityVector(PetrinetGraph graph) {
		// we use a graph without cycles, since branch water cannot handle
		// cycles and the results are the same for both types
//		PetrinetGraph graphWithoutCycles = ModelGraphCycleRemover.removeCycles(graph, getFirstNodeFromGraph(graph));
		Map<PetrinetNode, Double> activityVector = new HashMap<PetrinetNode, Double>();
//
//		// TODO rever
//		Map<PetrinetNode, Double> activityVectorGraphWithoutCycles = branchWater(graphWithoutCycles,
//				getFirstNodeFromGraph(graphWithoutCycles));
//
//		// we need to establish activity vectors that contain the vertices from
//		// the working models not the ones from the model with removed cycles
//		Map<PetrinetNode, PetrinetNode> mappings = getMapping(graphWithoutCycles, graph);
//
//		for (PetrinetNode vertex : activityVectorGraphWithoutCycles.keySet()) {
//			if (vertex instanceof Transition && !isInvisibleTransition(vertex))
//				activityVector.put(mappings.get(vertex), activityVectorGraphWithoutCycles.get(vertex));
//		}
		
		for(PetrinetNode node : graph.getTransitions()){
			if (node instanceof Transition && !isInvisibleTransition(node)){
				activityVector.put(node, 1.0);
			}
		}

		return activityVector;
	}

	/**
	 * Recursive function to build a distance vector.
	 * 
	 * @param start
	 *            actual node
	 * @param distance
	 *            distance vector
	 * @param distanceValue
	 *            actual distance
	 */
	private void buildDistanceVector(PetrinetNode start, Map<PetrinetNode, Integer> distance, int distanceValue) {
		distanceValue++;
		Collection<PetrinetNode> toVisit = new HashSet<PetrinetNode>();

		// first collect the distance for all direct successors and add them to
		// the distance vector
		for (Object successorObject : getTransitiveClosureSuccessors(start)) {
			PetrinetNode successor = (PetrinetNode) successorObject;

			if (distance.containsKey(successor)) {
				continue;
			} else {
				distance.put(successor, distanceValue);
				toVisit.add(successor);
			}
		}

		// no visit the vertices that were not already visited (to handle
		// cycles)
		for (PetrinetNode vertex : toVisit) {
			buildDistanceVector(vertex, distance, distanceValue);
		}
	}

	/**
	 * Returns the distance vectors for all vertices in a graph. If two vertices
	 * are not connected with each other they do not have an entry in the
	 * vector.
	 * 
	 * @param graph
	 *            the graph
	 * @return the map containing the distances
	 */
	private Map<PetrinetNode, Map<PetrinetNode, Integer>> buildDistanceVectors(PetrinetGraph graph) {
		Map<PetrinetNode, Map<PetrinetNode, Integer>> distances = new HashMap<PetrinetNode, Map<PetrinetNode, Integer>>();

		for (PetrinetNode vertex : graph.getNodes()) {
			Map<PetrinetNode, Integer> distance = new HashMap<PetrinetNode, Integer>();
			distances.put(vertex, distance);

			buildDistanceVector(vertex, distance, 0);
		}

		return distances;
	}

	/**
	 * Establishes the transition vectors for a specific graph based on the
	 * given activity and distance vectors. The transition vectors contain the
	 * probability that a transition between two activities in a process
	 * occurrs.
	 * 
	 * @param graph
	 *            the graph that represents the process
	 * @param activityVector
	 *            the activity vector of the process
	 * @param distanceVectors
	 *            the distance vectors of the process
	 * 
	 * @return the transition vectors for the given process
	 */
	private Map<PetrinetNode, Map<PetrinetNode, Double>> buildTransitionVectors(PetrinetGraph graph,
			Map<PetrinetNode, Double> activityVector, Map<PetrinetNode, Map<PetrinetNode, Integer>> distanceVectors) {
		Map<PetrinetNode, Map<PetrinetNode, Double>> transitionVectors = new HashMap<PetrinetNode, Map<PetrinetNode, Double>>();

		for (PetrinetNode vertex : graph.getTransitions()) {

			if (!isInvisibleTransition(vertex)) {

				Map<PetrinetNode, Double> transitionVector = new HashMap<PetrinetNode, Double>();
				transitionVectors.put(vertex, transitionVector);

				for (PetrinetNode vertex2 : graph.getTransitions()) {

					if (!isInvisibleTransition(vertex2)) {

						Integer distance = distanceVectors.get(vertex).get(vertex2);
						double transitionValue;
						if (null == distance) {
							transitionValue = 0;
						} else {
							transitionValue = 1.0 / (double) distance * activityVector.get(vertex)
									* activityVector.get(vertex2);
						}

						transitionVector.put(vertex2, transitionValue);
					}
				}
			}
		}

		return transitionVectors;
	}

}
