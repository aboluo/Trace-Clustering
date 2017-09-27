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
 *         Implementation based on Michael Becker.
 * 
 *         Customized for ProM 6 and Petri net models
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

	public double calculateSimilarity(PetrinetGraph modelA, PetrinetGraph modelB) {

		Set<PetrinetNode> transitionsModelA = getLabeledElements(modelA, true, true);
		Set<PetrinetNode> transitionsModelB = getLabeledElements(modelB, true, true);

		Map<PetrinetNode, Double> activityVectorA = buildActivityVector(transitionsModelA);
		Map<PetrinetNode, Double> activityVectorB = buildActivityVector(transitionsModelB);

		Map<PetrinetNode, Map<PetrinetNode, Integer>> distanceVectorsA = buildDistanceVectors(transitionsModelA);
		Map<PetrinetNode, Map<PetrinetNode, Integer>> distanceVectorsB = buildDistanceVectors(transitionsModelB);

		Map<PetrinetNode, Map<PetrinetNode, Double>> transitionVectorsA = buildTransitionVectors(modelA,
				activityVectorA, distanceVectorsA);
		Map<PetrinetNode, Map<PetrinetNode, Double>> transitionVectorsB = buildTransitionVectors(modelB,
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
		for (PetrinetNode vertex : transitionsModelA) {
			vertexIdentifiers.add(vertex.getLabel());
		}
		for (PetrinetNode vertex : transitionsModelB) {
			vertexIdentifiers.add(vertex.getLabel());
		}

		// now calculate the necessary variables for the similarities
		for (String vertexIdentifier : vertexIdentifiers) {

			PetrinetNode vertexA = getNode(modelA, vertexIdentifier);
			PetrinetNode vertexB = getNode(modelB, vertexIdentifier);

			double executionProbabilityA = null == vertexA ? 0 : activityVectorA.get(vertexA);
			double executionProbabilityB = null == vertexB ? 0 : activityVectorB.get(vertexB);

			sumExecutionProbabilities += executionProbabilityA * executionProbabilityB;
			sumSquaredExecutionProbabilitiesA += executionProbabilityA * executionProbabilityA;
			sumSquaredExecutionProbabilitiesB += executionProbabilityB * executionProbabilityB;

			for (String vertexIdentifer2 : vertexIdentifiers) {
				PetrinetNode vertexA2 = getNode(modelA, vertexIdentifer2);
				PetrinetNode vertexB2 = getNode(modelB, vertexIdentifer2);

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
	private Map<PetrinetNode, Double> buildActivityVector(Set<PetrinetNode> transitionsModel) {
		Map<PetrinetNode, Double> activityVector = new HashMap<PetrinetNode, Double>();

		for (PetrinetNode node : transitionsModel) {
			Transition transition = (Transition) node;

			Collection<Transition> predecessors = transition.getVisiblePredecessors();

			double prob = 0;

			if (predecessors.size() == 0) { // start
				prob = 1;
			} else if (transition.getVisibleSuccessors().size() == 0) { //stop
				prob = 1;
			} else if (predecessors.size() > 0) {
				
				for (Transition pred : predecessors) {
					double localProb = 1.0 / pred.getVisibleSuccessors().size();
					if (localProb > prob)
						prob = localProb;
				}
			}

			activityVector.put(node, prob);
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

		Transition transitionStart = (Transition) start;

		// first collect the distance for all direct successors and add them to
		// the distance vector
		for (Transition successorTransition : transitionStart.getVisibleSuccessors()) {

			if (distance.containsKey(successorTransition)) {
				continue;
			} else {
				distance.put(successorTransition, distanceValue);
				toVisit.add(successorTransition);
			}
		}

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
	private Map<PetrinetNode, Map<PetrinetNode, Integer>> buildDistanceVectors(Set<PetrinetNode> transitionsModel) {
		Map<PetrinetNode, Map<PetrinetNode, Integer>> distances = new HashMap<PetrinetNode, Map<PetrinetNode, Integer>>();

		for (PetrinetNode vertex : transitionsModel) {
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
