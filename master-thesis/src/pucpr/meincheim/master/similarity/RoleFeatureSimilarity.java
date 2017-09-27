package pucpr.meincheim.master.similarity;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Similarity of two process graph nodes according to Yan et al.: Fast Business
 * Process Similarity Search with Feature-Based Similarity Estimation.
 * 
 * A node in a process graph may have different roles:
 * <ul>
 * <li>start: no incoming edges</li>
 * <li>stop: no outgoing edges</li>
 * <li>split: more than one outgoing edge</li>
 * <li>join: more than one incoming edge</li>
 * <li>regular: exactely one incoming and one outgoing edge</li>
 * </ul>
 * 
 * Based on their roles, similarity between role similarity (rsim) between two
 * nodes is established as follows where |n.| and |.n| is the amount of outgoing
 * and incoming edges respectively:
 * 
 * rsim(n, m) =
 * <ul>
 * <li>1, if n and m both have start and stop role</li>
 * <li>1 - 1/2 * (abs(|n.| - |m.|)/(|n.| + |m.|)), if n and m both have start
 * but not stop role</li>
 * <li>1 - 1/2 * (abs(|.n| - |.m|)/(|.n| + |.m|)), if n and m both have stop but
 * not start role</li>
 * <li>1 - 1/2 * (abs(|n.| - |m.|)/(|n.| + |m.|)) - 1/2 * (abs(|.n| -
 * |.m|)/(|.n| + |.m|)), otherwise
 * </ul>
 * 
 * @author Michael Becker
 * 
 */
public class RoleFeatureSimilarity extends AbstractModelGraphSimilarityMeasure
		implements SimilarityMeasure<Transition> {

	public double calculateSimilarity(Transition a, Transition b) {
		// determine amount of incoming and outgoing edges of transitions directed
		// connected into transitions

		int predA = a.getVisiblePredecessors().size();
		int succA = a.getVisibleSuccessors().size();
		int predB = b.getVisiblePredecessors().size();
		int succB = b.getVisibleSuccessors().size();

		boolean[] rolesA = new boolean[5];
		boolean[] rolesB = new boolean[5];

		// identify the different roles of element a
		rolesA[0] = 0 == predA ? true : false; // start
		rolesA[1] = 0 == succA ? true : false; // stop
		rolesA[2] = 2 <= succA ? true : false; // split
		rolesA[3] = 2 <= predA ? true : false; // join
		rolesA[4] = ((1 == predA) && (1 == succA)) ? true : false; // regular

		// identifiy the different roles of element b
		rolesB[0] = 0 == predB ? true : false; // start
		rolesB[1] = 0 == succB ? true : false; // stop
		rolesB[2] = 2 <= succB ? true : false; // split
		rolesB[3] = 2 <= predB ? true : false; // join
		rolesB[4] = ((1 == predB) && (1 == succB)) ? true : false; // regular

		// some precalculations to shorten writing
		int diffPred = Math.abs(predA - predB);
		int diffSucc = Math.abs(succA - succB);
		int sumPred = predA + predB;
		int sumSucc = succA + succB;

		double similarity;
		if (rolesA[0] && rolesA[1] && rolesB[0] && rolesB[1]) {
			// both elements have role start and stop
			similarity = 1.0;
		} else if (rolesA[0] && !rolesA[1] && rolesB[0] && !rolesB[1]) {
			// both elements have role start but not stop
			similarity = 1.0 - diffSucc / (2.0 * sumSucc);
		} else if (!rolesA[0] && rolesA[1] && !rolesB[0] && rolesB[1]) {
			// both elements have role stop but not start
			similarity = 1.0 - diffPred / (2.0 * sumPred);
		} else {
			// both elements have either split, join, or regular roles
			double val1 = 1.0 - (double) diffSucc / (double) sumSucc;
			double val2 = 1.0 - (double) diffPred / (double) sumPred;

			similarity = (val1 + val2) / 2.0;
		}

		return similarity;
	}
}
