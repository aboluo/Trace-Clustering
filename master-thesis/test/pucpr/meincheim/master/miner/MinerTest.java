package pucpr.meincheim.master.miner;

import java.io.IOException;

import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XLog;
import org.junit.Assert;
import org.junit.Test;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.petrinet.PetriNetVisualization;

import pucpr.meincheim.master.base.BaseTest;
import pucpr.meincheim.master.util.JComponentVisualizationUtils;

public class MinerTest extends BaseTest {

	@Test
	public void minerAllLogWithInductiveTest() throws IOException {
		miner = new InductiveMiner();
		Petrinet p = miner.mineToPetrinet(context, log);
		PetriNetVisualization pv = new PetriNetVisualization();
		System.out.println("vizualizing...");
		JComponentVisualizationUtils.visualize(pv.visualize(new UIContext().getMainPluginContext(), p));
//		BerthelotPlugin reduction = new BerthelotPlugin();
//		p = reduction.runDefault(context, p);
		JComponentVisualizationUtils.visualize(pv.visualize(new UIContext().getMainPluginContext(), p));
		Assert.assertNotNull(p);
		
		
	}

	@Test
	public void minerEachTraceWithInductiveTest() {
		XFactoryBufferedImpl factory = new XFactoryBufferedImpl();
		for (int index = 0; index < log.size(); index++) {
			XLog log2 = factory.createLog();
			log2.add(log.get(index));
			miner = new InductiveMiner();
			Petrinet net = miner.mineToPetrinet(context, log2);
			System.out.println(net.getNodes().size());
		}
	}

}
