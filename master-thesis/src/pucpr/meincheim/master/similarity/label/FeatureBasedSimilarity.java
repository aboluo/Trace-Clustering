package pucpr.meincheim.master.similarity.label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

import pucpr.meincheim.master.similarity.AbstractModelGraphSimilarityMeasure;
import pucpr.meincheim.master.similarity.LevenshteinNodeSimilarity;
import pucpr.meincheim.master.similarity.RoleFeatureSimilarity;
import pucpr.meincheim.master.similarity.SimilarityMeasure;

/**
 * Similarity according to Yan et al.: Fast Business Process Similarity Search
 * with Feature-Based Similarity Estimation.
 * 
 * The similarity is calculated using the following features
 * <ul>
 * <li>Label Feature: String Edit Distance Similarity between element
 * labels</li>
 * <li>Role Feature: Similarity based on the role of element (start, stop,
 * split, join, regular (sequential elements)</li>
 * </ul>
 * 
 * The similarity is only applicable in the graph representation of models,
 * where activities are used as connectors, too. Therefore, a given process
 * model is converted into its graphical representation removing non-task
 * elements like gateways/connectors or events in EPCs.
 * 
 * @author Alex Meincheim
 * 
 *         Implementation based on Michael Becker.
 * 
 *         Customized for ProM 6 and Petri net models
 * 
 */
public class FeatureBasedSimilarity extends AbstractModelGraphSimilarityMeasure
		implements SimilarityMeasure<PetrinetGraph> {

	protected static final SimilarityMeasure<PetrinetNode> DEFAULT_LABEL_SIM = new LevenshteinNodeSimilarity();

	public static final SimilarityMeasure<PetrinetNode> DEFAULT_ROLE_SIM = new RoleFeatureSimilarity();

	public static final double DEFAULT_LCUTOFF_HIGH = 0.8;
	public static final double DEFAULT_RCUTOFF = 1.0;
	public static final double DEFAULT_LCUTOFF_MED = 0.2;

	protected SimilarityMeasure<PetrinetNode> labelFeatureSimilarity;
	protected SimilarityMeasure<PetrinetNode> roleFeatureSimilarity;

	protected double lCutoffHigh;
	protected double rCutoff;
	protected double lCutoffMed;

	public FeatureBasedSimilarity() {
		this(DEFAULT_LABEL_SIM, DEFAULT_ROLE_SIM, DEFAULT_LCUTOFF_HIGH, DEFAULT_RCUTOFF, DEFAULT_LCUTOFF_MED);
	}

	public FeatureBasedSimilarity(SimilarityMeasure<PetrinetNode> labelFeatureSimilarity,
			SimilarityMeasure<PetrinetNode> roleFeatureSimilarity, double lCutoffHigh, double rCutoff,
			double lCutoffMed) {
		this.labelFeatureSimilarity = labelFeatureSimilarity;
		this.roleFeatureSimilarity = roleFeatureSimilarity;

		this.lCutoffHigh = lCutoffHigh;
		this.rCutoff = rCutoff;
		this.lCutoffMed = lCutoffMed;
	}

	public double calculateSimilarity(PetrinetGraph a, PetrinetGraph b) {

		Map<PetrinetNode, Set<PetrinetNode>> matches = new HashMap<PetrinetNode, Set<PetrinetNode>>();

		for (PetrinetNode vertexA : getLabeledElements(a, true, true)) {
			Set<PetrinetNode> matchesA = new HashSet<PetrinetNode>();
			boolean hasMatch = false;

			for (PetrinetNode vertexB : getLabeledElements(b, true, true)) {
				double lsimAB = labelFeatureSimilarity.calculateSimilarity(vertexA,
						vertexB);
				double rsimAB = roleFeatureSimilarity.calculateSimilarity(vertexA, vertexB);

				// two nodes are matched, if their labels are similar to a high
				// degree or if their role and label are similar to a medium
				// degree
				if ((lsimAB >= lCutoffHigh) || ((rsimAB >= rCutoff) && (lsimAB >= lCutoffMed))) {
					matchesA.add(vertexB);
					hasMatch = true;
				}
			}

			if (hasMatch) {
				matches.put(vertexA, matchesA);
			}
		}

		int matchedInA = matches.keySet().size();
		int matchedInB = matches.values().size();
		int total = getLabeledElements(a, true, true).size() + getLabeledElements(b, true, true).size();

		return (double) (matchedInA + matchedInB) / (double) total;
	}

}
