package pucpr.meincheim.master.similarity;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.plugins.petrinet.PetriNetVisualization;

import pucpr.meincheim.master.base.BaseTest;
import pucpr.meincheim.master.util.JComponentVisualizationUtils;

public class ModelGraphCycleRemoverTest extends BaseTest {

	@Test
	public void identifyCyclesTest() {
		Petrinet petrinet = model2;
		PetrinetNode firstNode = getFirstNodeFromGraph(petrinet);
		Collection<PetrinetEdge> cycles = new HashSet<PetrinetEdge>();
		ModelGraphCycleRemover.identifyCycles(petrinet, firstNode, new HashSet<PetrinetNode>(), cycles);
		Assert.assertEquals(1, cycles.size());

		cycles = new HashSet<PetrinetEdge>();
		PetrinetGraph petrinetGraph = ModelGraphCycleRemover.removeCycles(petrinet, firstNode);
		ModelGraphCycleRemover.identifyCycles(petrinet, firstNode, new HashSet<PetrinetNode>(), cycles);
		Assert.assertEquals(0, cycles.size());

		PetriNetVisualization pv = new PetriNetVisualization();
		JComponentVisualizationUtils.visualize(pv.visualize(new UIContext().getMainPluginContext(), petrinet));
	}
}
