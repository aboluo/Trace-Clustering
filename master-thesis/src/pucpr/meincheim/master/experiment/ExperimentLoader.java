package pucpr.meincheim.master.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;

import pucpr.meincheim.master.cluster.Cluster;
import pucpr.meincheim.master.cluster.TraceCluster;
import pucpr.meincheim.master.miner.InductiveMiner;
import pucpr.meincheim.master.miner.Miner;
import pucpr.meincheim.master.quality.FinalQualityEvaluator;
import pucpr.meincheim.master.quality.ModelQuality;
import pucpr.meincheim.master.quality.QualityEvaluator;
import pucpr.meincheim.master.similarity.SimilarityMeasure;
import pucpr.meincheim.master.similarity.behavioral.DependencyGraphComparisonSimilarity;
import pucpr.meincheim.master.similarity.behavioral.TARSimilarity;
import pucpr.meincheim.master.similarity.label.CommonActivityNameSimilarity;
import pucpr.meincheim.master.similarity.label.CommonNodesEdgesSimilarity;
import pucpr.meincheim.master.similarity.label.FeatureBasedSimilarity;
import pucpr.meincheim.master.similarity.structural.GraphEditDistanceSimilarity;
import pucpr.meincheim.master.similarity.structural.LaRosaSimilarity;
import pucpr.meincheim.master.util.CsvWriter;
import pucpr.meincheim.master.util.LogUtils;

public class ExperimentLoader {

	private UIPluginContext context;
	private Miner miner;

	private String datesetPathBase;
	private String experimentPathBase;

	private List<SimilarityMeasure<?>> similaritiesMeasures;

	private QualityEvaluator qualityEvaluator;

	private boolean evaluate;
	private boolean cluster;

	public ExperimentLoader(String filePathBase, boolean cluster, boolean evaluate) {
		this.context = new FakePluginContext();
		PackageManager.getInstance();
		PluginManagerImpl.initialize(UIPluginContext.class);
		PluginManagerImpl.getInstance();
		this.miner = new InductiveMiner();

		this.qualityEvaluator = new FinalQualityEvaluator(context, (InductiveMiner) miner);
		this.similaritiesMeasures = new ArrayList<SimilarityMeasure<?>>();

		// Label
		this.similaritiesMeasures.add(new CommonActivityNameSimilarity());
		this.similaritiesMeasures.add(new CommonNodesEdgesSimilarity());
		this.similaritiesMeasures.add(new FeatureBasedSimilarity());
		// Behavioral
		this.similaritiesMeasures.add(new TARSimilarity());
		this.similaritiesMeasures.add(new DependencyGraphComparisonSimilarity());
		// Structural
		this.similaritiesMeasures.add(new GraphEditDistanceSimilarity());
		this.similaritiesMeasures.add(new LaRosaSimilarity());

		this.experimentPathBase = filePathBase + "\\Experiment\\Results";
		this.datesetPathBase = filePathBase + "\\Experiment\\Dataset";
		
		this.cluster = cluster;
		this.evaluate = evaluate;
	}

	public void LoadAll() {
		List<String> datasets = getFilePaths(datesetPathBase);
		for (String dataset : datasets) {
			File file = new File(dataset);
			XLog log = LogUtils.loadByFile(file);
			for (SimilarityMeasure<?> sim : similaritiesMeasures) {
				load(file, log, sim);
			}
		}
	}

	public void load(File file, XLog log, SimilarityMeasure<?> sim) {
		try {
			String filename = file.getName().replace(".xes", "");
			process(sim, log, filename, cluster, false, evaluate, 0.4);
			process(sim, log, filename, cluster, false, evaluate, 0.6);
			process(sim, log, filename, cluster, false, evaluate, 0.8);
		} catch (IOException e) {

		}
	}

	private void process(SimilarityMeasure<?> sim, XLog log, String fileName, boolean cluster,
			boolean recalculateCentroid, boolean evaluate, double simThresold) throws IOException {
		String folderExport = buildFolderExporter(sim.getClass().getSimpleName(), fileName, recalculateCentroid,
				simThresold);
		if (cluster) {
			TraceCluster traceCluster = new TraceCluster(context, miner, sim, recalculateCentroid, simThresold);
			List<Cluster> clusters = traceCluster.cluster(log);
			exportLogs(clusters, folderExport);
		}
		if (evaluate)
			evaluateClusterQuality(folderExport);
		System.out.println("Process complete for " + folderExport);
	}

	private String buildFolderExporter(String simName, String fileName, boolean recalculateCentroid,
			double simThresold) {
		return experimentPathBase + "\\"
				+ String.format("%s %s %s %s", fileName, simName, simThresold, recalculateCentroid);
	}

	private void evaluateClusterQuality(String logsDirectory) {
		List<ModelQuality> qualities = new ArrayList<ModelQuality>();
		for (String logPath : getFilePaths(logsDirectory)) {
			XLog log = LogUtils.loadByFile(new File(logPath));
			qualityEvaluator.loadMapping(log);
			qualities.add(qualityEvaluator.calculate());
		}
		CsvWriter csv = new CsvWriter();
		csv.qualityExportCsv(qualities, logsDirectory + ".csv");
	}

	private void exportLogs(List<Cluster> clusters, String folderExporter) throws FileNotFoundException, IOException {
		new File(folderExporter).mkdirs();
		for (Cluster cluster : clusters) {
			String fileExporter = folderExporter + "\\" + cluster.getId();
			LogUtils.xesExport(cluster.getLog(), fileExporter);
		}
	}

	private List<String> getFilePaths(String directory) {
		List<String> paths = new ArrayList<String>();
		File[] files = new File(directory).listFiles();
		for (File file : files) {
			if (file.isFile()) {
				paths.add(file.getAbsolutePath());
			}
		}
		return paths;
	}
}
