package pucpr.meincheim.master.cluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

import pucpr.meincheim.master.miner.Miner;
import pucpr.meincheim.master.similarity.SimilarityMeasure;
import pucpr.meincheim.master.util.LogUtils;

public class TraceCluster {

	private double threshold = 0.5;
	private boolean recalculateModel = true;
	private UIPluginContext uiContext;
	private Miner miner;
	private SimilarityMeasure simMeasure;

	public TraceCluster(UIPluginContext uiContext, Miner miner, SimilarityMeasure simMeasure, boolean recalculateModel,
			double threshold) {
		this.uiContext = uiContext;
		this.miner = miner;
		this.simMeasure = simMeasure;
		this.recalculateModel = recalculateModel;
		this.threshold = threshold;
	}

	public List<Cluster> cluster(XLog logs) {

		List<Cluster> clusters = new ArrayList<Cluster>();

		int instanceCount = 0;

		for (XTrace trace : logs) {
			XLog nLog = LogUtils.createNewLog(trace);
			Petrinet net = miner.mineToPetrinet(uiContext, nLog);

			if (clusters.isEmpty()) {
				clusters.add(new Cluster(net, nLog, instanceCount));

			} else {

				Cluster cluster = getSimilarCluster(simMeasure, clusters, net);

				if (cluster != null) {
					recalculateCentroide(trace, cluster);

				} else {
					clusters.add(new Cluster(net, nLog, instanceCount));
				}
			}

			System.out.println("instances ----> " + instanceCount);
			System.out.println("clusters ----> " + clusters.size());
			instanceCount++;
		}

		System.out.println("Cluster size " + clusters.size());

		for (Cluster cluster : clusters)
			cluster.setModel(miner.mineToPetrinet(uiContext, cluster.getLog()));

		return clusters;

	}

	private void recalculateCentroide(XTrace trace, Cluster cluster) {
		XLog log = cluster.getLog();
		log = LogUtils.addTrace(log, trace);
		cluster.setLog(log);

		if (this.recalculateModel)
			cluster.setModel(miner.mineToPetrinet(uiContext, log));
	}

	private Cluster getSimilarCluster(SimilarityMeasure simMeasure, List<Cluster> clusters, Petrinet net) {
		List<Candidate> candidates = new ArrayList<Candidate>();
		for (Cluster ct : clusters) {
			double similaridade = simMeasure.calculateSimilarity(net, ct.getModel());
			// double similaridade = 1 / (1 + dissimilaridade);
			if (similaridade >= threshold)
				candidates.add(new Candidate(ct, similaridade));
		}

		Optional<Candidate> selected = candidates.stream().max(Comparator.comparing(Candidate::getSimilarity));

		return selected.isPresent() ? selected.get().getCentroide() : null;
	}
}
