package pucpr.meincheim.master.miner;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;

public class InductiveMiner implements Miner {
	
	public Petrinet mineToPetrinet(UIPluginContext context, XLog log) {		
		IMPetriNet miner = new IMPetriNet();
		return (Petrinet)miner.minePetriNet(context, log)[0];		
	}
	
}
