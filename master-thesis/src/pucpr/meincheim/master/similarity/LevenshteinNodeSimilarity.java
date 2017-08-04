package pucpr.meincheim.master.similarity;

import org.processmining.framework.util.LevenshteinDistance;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

public class LevenshteinNodeSimilarity implements SimilarityMeasure<PetrinetNode> {
		
	private LevenshteinDistance similarity;

	public LevenshteinNodeSimilarity() {
		similarity = new LevenshteinDistance();
	}

	@Override
	public double calculateSimilarity(PetrinetNode a, PetrinetNode b) {
		double distance = similarity.getLevenshteinDistanceLinearSpace(a.getLabel(), b.getLabel());
		return 1.0 - distance / Math.max(a.getLabel().length(), b.getLabel().length());
	}

}
