package pucpr.meincheim.master.miner;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.processtree.ProcessTree;

public class InductiveMiner implements Miner {
	
	public Petrinet mineToPetrinet(UIPluginContext context, XLog log) {		
		IMPetriNet miner = new IMPetriNet();
		return (Petrinet)miner.minePetriNet(context, log)[0];		
	}
	
	
	public ProcessTree mineToProcessTree(UIPluginContext context, XLog log) {		
		IMProcessTree miner = new IMProcessTree();
		return miner.mineProcessTree(context, log);		
	}
	
	
}
