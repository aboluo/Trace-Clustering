package pucpr.meincheim.master.miner;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.plugins.heuristicsnet.miner.heuristics.converter.HeuristicsNetToPetriNetConverter;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.FlexibleHeuristicsMiner;
import org.processmining.plugins.heuristicsnet.visualizer.HeuristicsNetVisualizationWithSemanticsSplitJoinPoints;

import pucpr.meincheim.master.util.JComponentVisualizationUtils;

public class HeuristicMiner implements Miner {

	public HeuristicsNet mine(UIPluginContext context, XLog log) {
		FlexibleHeuristicsMiner miner = new FlexibleHeuristicsMiner(context, log);
		return miner.mine();
	}

	public void visualize(UIPluginContext context, HeuristicsNet net) {
		JComponentVisualizationUtils
				.visualize(HeuristicsNetVisualizationWithSemanticsSplitJoinPoints.visualize(context, net));
	}

	public Petrinet mineToPetrinet(UIPluginContext context, XLog log) {
		FlexibleHeuristicsMiner miner = new FlexibleHeuristicsMiner(context, log);
		HeuristicsNet hNet = miner.mine();
		return (Petrinet) HeuristicsNetToPetriNetConverter.converter(context, hNet)[0];
	}

}
