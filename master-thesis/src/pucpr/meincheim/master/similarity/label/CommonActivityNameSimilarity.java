package pucpr.meincheim.master.similarity.label;

import java.util.HashSet;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;

import pucpr.meincheim.master.similarity.AbstractModelGraphSimilarityMeasure;
import pucpr.meincheim.master.similarity.SimilarityMeasure;

/**
 * Similarity of models based on the method proposed by Akkiraju2010.
 * 
 * sim(A, B) = 2 * n_AB / (n_A + n_B) with
 * <ul>
 * <li>n_A, n_B ... amount of labels in models A and B</li>
 * <li>n_AB ... amount of common labels in models A an B</li>
 * </ul>
 * 
 * Based on Michael Becker
 */

/**
 * Customized by Alex
 * We consider only the petrinets transitions in the mapping. 
 * Places are ignored to calculate the commons labels * 
 */
public class CommonActivityNameSimilarity extends AbstractModelGraphSimilarityMeasure
		implements SimilarityMeasure<PetrinetGraph> {

	public double calculateSimilarity(PetrinetGraph modelA, PetrinetGraph modelB) {
		// get all labels from both models
		
		Set<String> labelsModelA = getLabels(modelA, true, true);
		Set<String> labelsModelB = getLabels(modelB, true, true);

		// create a set having only the labels occurring in both models
		Set<String> commonLabels = new HashSet<String>(labelsModelA);
		commonLabels.retainAll(labelsModelB);

		return 2.0 * commonLabels.size() / (labelsModelA.size() + labelsModelB.size());
	}
}
