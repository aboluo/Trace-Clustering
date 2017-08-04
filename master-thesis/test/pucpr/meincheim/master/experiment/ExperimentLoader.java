package pucpr.meincheim.master.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.junit.Before;
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
import pucpr.meincheim.master.quality.QualityEvaluator;
import pucpr.meincheim.master.similarity.SimilarityMeasure;
import pucpr.meincheim.master.similarity.behavioral.DependencyGraphComparisonSimilarity;
import pucpr.meincheim.master.similarity.label.CommonActivityNameSimilarity;
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
	protected String validationDatesetPathBase;
	protected String experimentPathBase;

	@Before
	public void setup() throws URISyntaxException {
		context = new FakePluginContext();
		PackageManager.getInstance();
		PluginManagerImpl.initialize(UIPluginContext.class);
		PluginManagerImpl.getInstance();
		miner = new InductiveMiner();

		semanticSimilarity = new CommonActivityNameSimilarity();
		laRosaSimilarity = new LaRosaSimilarity();
		dependencyGraphComparisonSimilarity = new DependencyGraphComparisonSimilarity();

		// filePathBase = "C:\\Users\\alexme\\Dropbox\\Mestrado em Informática -
		// PUCPR\\Process Mining\\2017 - Process Mining - Dissertação";
		filePathBase = "D:\\Dropbox\\Dropbox\\Mestrado em Informática - PUCPR\\Process Mining\\2017 - Process Mining - Dissertação";

		validationDatesetPathBase = filePathBase + "\\Dataset\\SimilarityValidation\\";
		experimentPathBase = filePathBase + "\\Experiment";

		File file = new File(validationDatesetPathBase + "Hospital_log.xes");
		hospitalLog = LogUtils.loadByFile(file);
	}

	@Test
	public void SemanticSimilarityHospitalLog() throws IOException {
		String folderExporter = experimentPathBase + "\\" + "SemanticSimilarityHospitalLog";
		process(semanticSimilarity, hospitalLog, folderExporter, true, true);
	}

	@Test
	public void DependencyGraphSimilarityHospitalLog() throws IOException {
		String folderExporter = experimentPathBase + "\\" + "DependencyGraphSimilaritySimilarityHospitalLog";
		process(dependencyGraphComparisonSimilarity, hospitalLog, folderExporter, true, true);
	}

	@Test
	public void LaRosaHospitalLog() throws IOException {
		String folderExporter = experimentPathBase + "\\" + "LaRosaHospitalLog";
		process(laRosaSimilarity, hospitalLog, folderExporter, true, true);
	}

	private void process(SimilarityMeasure sim, XLog log, String folderExporter, boolean cluster, boolean evaluate)
			throws IOException {
		if (cluster) {
			TraceCluster traceCluster = new TraceCluster(context, miner, sim, false, 0.6);
			List<Cluster> clusters = traceCluster.cluster(log);
			exportLogs(clusters, folderExporter);
		}
		if (evaluate) {
			List<ModelQuality> qualities = evaluateClusterQuality(folderExporter);
			CsvWriter csv = new CsvWriter();
			csv.qualityExportCsv(qualities, folderExporter + ".csv");
		}
		System.out.println("Process complete for " + folderExporter);
	}

	private List<ModelQuality> evaluateClusterQuality(String logsDirectory) {
		List<ModelQuality> qualities = new ArrayList<ModelQuality>();
		for (String logPath : getFilePaths(logsDirectory)) {
			XLog log = LogUtils.loadByFile(new File(logPath));
			Petrinet model = miner.mineToPetrinet(context, log);
			QualityEvaluator qe = new QualityEvaluator(context, log, model);
			qualities.add(qe.calculate());
			System.gc();
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
