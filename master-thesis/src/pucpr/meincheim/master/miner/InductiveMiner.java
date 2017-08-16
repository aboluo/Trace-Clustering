package pucpr.meincheim.master.miner;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.processtree.ProcessTree;

public class InductiveMiner implements Miner {

	private static IMProcessTree processTreeMiner;
	private static IMPetriNet petrinetMiner;

	public InductiveMiner() {
		processTreeMiner = new IMProcessTree();
		petrinetMiner = new IMPetriNet();
	}

	public Petrinet mineToPetrinet(UIPluginContext context, XLog log) {
		return (Petrinet) petrinetMiner.minePetriNet(context, log)[0];
	}

	public ProcessTree mineToProcessTree(UIPluginContext context, XLog log) {
		return processTreeMiner.mineProcessTree(context, log);
	}

}
