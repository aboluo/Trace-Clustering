package pucpr.meincheim.master.cluster;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.plugins.petrinet.PetriNetVisualization;

import pucpr.meincheim.master.base.BaseTest;
import pucpr.meincheim.master.miner.InductiveMiner;
import pucpr.meincheim.master.similarity.label.CommonActivityNameSimilarity;
import pucpr.meincheim.master.util.JComponentVisualizationUtils;
import pucpr.meincheim.master.util.LogUtils;

public class ClusterTest extends BaseTest {

	@Test
	public void clusterTest() throws IOException {
		List<Cluster> clusters = generateClusters();
		PetriNetVisualization pv = new PetriNetVisualization();
		System.out.println("vizualizing...");
		JComponentVisualizationUtils
				.visualize(pv.visualize(new UIContext().getMainPluginContext(), clusters.get(0).getModel()));
		System.out.println("finished");
	}

	private List<Cluster> generateClusters() throws IOException {
		// DependencyGraphSimilarity sim = new DependencyGraphSimilarity();
		CommonActivityNameSimilarity sim = new CommonActivityNameSimilarity();
		// CausalBehaviouralProfileSimilarity sim = new
		// CausalBehaviouralProfileSimilarity();
		InductiveMiner miner = new InductiveMiner();
		// HeuristicMiner miner = new HeuristicMiner();

		TraceCluster cluster = new TraceCluster(context, miner, sim, false, 0.50);
		List<Cluster> clusters = cluster.cluster(hospitalLog);
		Metric.createClusterMetrics(clusters);
		return clusters;
	}

	public static void main(String[] args) throws URISyntaxException, IOException {
		ClusterTest t = new ClusterTest();
		t.setup();
		List<Cluster> clusters = t.generateClusters();
		// t.clusterLogExporter(clusters);

		// InductiveVisualMiner iv = new InductiveVisualMiner();
		// JComponentVisualization.visualize(iv.visualise(t.context,
		// clusters.get(0).getLog(), ProMCanceller.NEVER_CANCEL));
	}

	private void clusterLogExporter(List<Cluster> clusters) {

		// String path = "D:\\Dropbox\\Dropbox\\Mestrado em Informática -
		// PUCPR\\Process Mining\\2017 - Process Mining - estudo artigo 4 -
		// Production\\logsClustering\\";
		String path = "C:\\Users\\alexme\\Dropbox\\Mestrado em Informática - PUCPR\\Process Mining\\2017 - Process Mining - estudo artigo 4 - Production\\logsClusteringRecalculate\\";

		for (int index = 0; index < clusters.size(); index++) {
			String name = path + index + "_Size_" + clusters.get(index).getLog().size() + ".xes";
			try {
				LogUtils.xesExport(clusters.get(index).getLog(), name);
			} catch (IOException e) {

			}
		}
	}
}
