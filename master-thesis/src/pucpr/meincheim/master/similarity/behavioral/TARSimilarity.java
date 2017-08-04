package pucpr.meincheim.master.similarity.behavioral;

import java.util.HashSet;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

import pucpr.meincheim.master.similarity.AbstractModelGraphSimilarityMeasure;
import pucpr.meincheim.master.similarity.SimilarityMeasure;

/**
 * Similarity according to Zha et al.: A workflow new similarity measure based
 * on transition adjacency relations.
 * 
 * A transition adjacency relation (TAR) of a process models represents direct
 * succession-relations of two activities with each other. The TAR is defined
 * such that a pair (a1, a2) of two activities belongs to TAR, iff the model has
 * a trace of the form <... a1, a2, ...>. Let TAR0 be the TAR-relation for model
 * M0 and TAR1 be the TAR-relation for model M1. The TAR similarity between M0
 * and M1 is defined as sim(M0, M1) = |intersection(TAR0,TAR1)| /
 * |union(TAR0,TAR1)|.
 * 
 * @author Alex Meincheim
 * 
 *         Implementation based on Michael Becker.
 * 
 *         Customized for ProM 6 and Petri net models
 * 
 */

public class TARSimilarity extends AbstractModelGraphSimilarityMeasure implements SimilarityMeasure<PetrinetGraph> {

	public double calculateSimilarity(PetrinetGraph a, PetrinetGraph b) {
		Set<TAR> tarSetA = getTARSet(a);
		Set<TAR> tarSetB = getTARSet(b);

		Set<TAR> intersection = new HashSet<TAR>(tarSetA);
		intersection.retainAll(tarSetB);
		Set<TAR> union = new HashSet<TAR>(tarSetA);
		union.addAll(tarSetB);

		return (double) intersection.size() / (double) union.size();
	}

	private Set<TAR> getTARSet(PetrinetGraph graph) {
		Set<TAR> tarSet = new HashSet<TARSimilarity.TAR>();

		for (Object edgeObject : graph.getEdges()) {
			PetrinetEdge edge = (PetrinetEdge) edgeObject;
			PetrinetNode source = (PetrinetNode) edge.getSource();
			PetrinetNode target = (PetrinetNode) edge.getTarget();
			tarSet.add(new TAR(source.getLabel(), target.getLabel()));
		}

		return tarSet;
	}

	private class TAR {
		protected String a;
		protected String b;

		public TAR(String a, String b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof TAR) {
				return ((TAR) arg0).a.equals(a) && ((TAR) arg0).b.equals(b);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return a.hashCode() + b.hashCode();
		}

		@Override
		public String toString() {
			return "(" + a + ", " + b + ")";
		}
	}
}
