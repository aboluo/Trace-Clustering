package pucpr.meincheim.master.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;

import pucpr.meincheim.master.cluster.Cluster;
import pucpr.meincheim.master.cluster.TraceCluster;
import pucpr.meincheim.master.miner.InductiveMiner;
import pucpr.meincheim.master.miner.Miner;
import pucpr.meincheim.master.quality.ModelQuality;
import pucpr.meincheim.master.quality.PPCPetrinetQualityEvaluator;
import pucpr.meincheim.master.quality.PPCProcessTreeQualityEvaluator;
import pucpr.meincheim.master.quality.QualityEvaluator;
import pucpr.meincheim.master.similarity.SimilarityMeasure;
import pucpr.meincheim.master.similarity.behavioral.DependencyGraphComparisonSimilarity;
import pucpr.meincheim.master.similarity.behavioral.DependencyGraphSimilarity;
import pucpr.meincheim.master.similarity.behavioral.TARSimilarity;
import pucpr.meincheim.master.similarity.label.CommonActivityNameSimilarity;
import pucpr.meincheim.master.similarity.label.CommonNodesEdgesSimilarity;
import pucpr.meincheim.master.similarity.label.FeatureBasedSimilarity;
import pucpr.meincheim.master.similarity.label.NodeLinkBasedSimilarity;
import pucpr.meincheim.master.similarity.structural.GraphEditDistanceSimilarity;
import pucpr.meincheim.master.similarity.structural.LaRosaSimilarity;
import pucpr.meincheim.master.util.CsvWriter;
import pucpr.meincheim.master.util.LogUtils;

public class ExperimentLoader {

	protected UIPluginContext context;
	protected XLog hospitalLog;
	protected Miner miner;

	protected CommonActivityNameSimilarity semanticSimilarity;
	protected LaRosaSimilarity laRosaSimilarity;
	protected DependencyGraphComparisonSimilarity dependencyGraphComparisonSimilarity;

	protected String filePathBase;
	protected String datesetPathBase;
	protected String experimentPathBase;

	protected List<SimilarityMeasure> similaritiesMeasures;

	public ExperimentLoader() {
		context = new FakePluginContext();
		PackageManager.getInstance();
		PluginManagerImpl.initialize(UIPluginContext.class);
		PluginManagerImpl.getInstance();
		miner = new InductiveMiner();

		similaritiesMeasures = new ArrayList<SimilarityMeasure>();

		// Label
//		similaritiesMeasures.add(new CommonActivityNameSimilarity());
//		similaritiesMeasures.add(new CommonNodesEdgesSimilarity());
//		similaritiesMeasures.add(new FeatureBasedSimilarity());
//		similaritiesMeasures.add(new NodeLinkBasedSimilarity());
		//
		// // Behavioral
//		similaritiesMeasures.add(new DependencyGraphComparisonSimilarity());
//		similaritiesMeasures.add(new DependencyGraphSimilarity());
		similaritiesMeasures.add(new TARSimilarity());

		// // Structural
//		similaritiesMeasures.add(new GraphEditDistanceSimilarity());
//		similaritiesMeasures.add(new LaRosaSimilarity());

		filePathBase = "C:\\Users\\alexme\\Dropbox\\Mestrado em Informática - PUCPR\\Process Mining\\2017 - Process Mining - Dissertação";
		// filePathBase = "D:\\Dropbox\\Dropbox\\Mestrado em Informática -
		// PUCPR\\Process Mining\\2017 - Process Mining - Dissertação";

		experimentPathBase = filePathBase + "\\Experiment\\Results";
		datesetPathBase =  filePathBase + "\\Experiment\\Dataset";

		File file = new File(datesetPathBase + "Hospital_log.xes");
		hospitalLog = LogUtils.loadByFile(file);
	}

	@Test
	public void ProcessAll() {
		List<String> datasets = getFilePaths(datesetPathBase);

		for (String dataset : datasets) {
			File file = new File(dataset);
			XLog log = LogUtils.loadByFile(file);

			for (SimilarityMeasure sim : similaritiesMeasures) {

				try {

					String filename = file.getName().replace(".xes", "");
					process(sim, log, filename, true, false, true, 0.4);
					System.gc();
					process(sim, log, filename, true, false, true, 0.6);
					System.gc();
					process(sim, log, filename, true, false, true, 0.8);
					System.gc();
				} catch (IOException e) {

				}
			}
		}
	}

	// @Test
	public void SemanticSimilarityHospitalLog() throws IOException {
		String folderExporter = experimentPathBase + "\\" + "SemanticSimilarityHospitalLog";
		process(semanticSimilarity, hospitalLog, folderExporter, false, false, true, 0.6);
	}

	// @Test
	public void DependencyGraphSimilarityHospitalLog() throws IOException {
		String folderExporter = experimentPathBase + "\\" + "DependencyGraphSimilaritySimilarityHospitalLog";
		process(dependencyGraphComparisonSimilarity, hospitalLog, folderExporter, false, false, true, 0.6);
	}

	// @Test
	public void LaRosaHospitalLog() throws IOException {
		String folderExporter = experimentPathBase + "\\" + "LaRosaHospitalLog";
		process(laRosaSimilarity, hospitalLog, folderExporter, false, false, true, 0.6);
	}

	private void process(SimilarityMeasure sim, XLog log, String fileName, boolean cluster, boolean recalculateCentroid,
			boolean evaluate, double simThresold) throws IOException {

		String folderExport = experimentPathBase + "\\" + String.format("%s %s %s %s", fileName,
				sim.getClass().getSimpleName(), simThresold, recalculateCentroid);
		;

		if (cluster) {
			TraceCluster traceCluster = new TraceCluster(context, miner, sim, recalculateCentroid, simThresold);
			List<Cluster> clusters = traceCluster.cluster(log);
			exportLogs(clusters, folderExport);
		}
		if (evaluate) {
			List<ModelQuality> qualities = evaluateClusterQuality(folderExport);
			CsvWriter csv = new CsvWriter();
			csv.qualityExportCsv(qualities, folderExport + ".csv");
		}
		System.out.println("Process complete for " + folderExport);
	}

	private List<ModelQuality> evaluateClusterQuality(String logsDirectory) {
		List<ModelQuality> qualities = new ArrayList<ModelQuality>();
		for (String logPath : getFilePaths(logsDirectory)) {
			XLog log = LogUtils.loadByFile(new File(logPath));
			Petrinet model = miner.mineToPetrinet(context, log);
			// QualityEvaluator qe = new AyraQualityEvaluator(context, log, model);
			// QualityEvaluator qe = new PPCPetrinetQualityEvaluator(context, log);
			QualityEvaluator qe = new PPCProcessTreeQualityEvaluator(context, log);
			qualities.add(qe.calculate());
		}
		return qualities;
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
