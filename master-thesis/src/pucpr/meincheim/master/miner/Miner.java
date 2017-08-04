package pucpr.meincheim.master.miner;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public interface Miner {

	Petrinet mineToPetrinet(UIPluginContext context, XLog log);

}
