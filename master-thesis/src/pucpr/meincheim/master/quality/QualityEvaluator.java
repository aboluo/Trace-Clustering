package pucpr.meincheim.master.quality;

import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;

public abstract class QualityEvaluator {

	public ModelQuality calculate() {
		throw new NotImplementedException();
	};

	public Marking getInitialMarking(Petrinet net) {
		Set<Place> places = PetrinetUtils.getStartPlaces(net);
		if (places.isEmpty()) {
			for (Place vertexA : net.getPlaces()) {
				if (vertexA.getLabel().startsWith("source")) {
					places.add(vertexA);
				}
			}
		}
		Marking initialMarking = new Marking();
		initialMarking.addAll(places);
		return initialMarking;
	}

}
