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
import pucpr.meincheim.master.quality.FinalQualityEvaluator;
import pucpr.meincheim.master.quality.ModelQuality;
import pucpr.meincheim.master.quality.QualityEvaluator;
import pucpr.meincheim.master.similarity.SimilarityMeasure;
import pucpr.meincheim.master.similarity.behavioral.DependencyGraphComparisonSimilarity;
import pucpr.meincheim.master.similarity.behavioral.TARSimilarity;
import pucpr.meincheim.master.similarity.label.CommonActivityNameSimilarity;
import pucpr.meincheim.master.similarity.label.CommonNodesEdgesSimilarity;
import pucpr.meincheim.master.similarity.label.FeatureBasedSimilarity;
import pucpr.meincheim.master.similarity.label.NodeLinkBasedSimilarity;
import pucpr.meincheim.master.similarity.structural.GraphEditDistanceSimilarity;
import pucpr.meincheim.master.similarity.structural.LaRosaSimilarity;
import pucpr.meincheim.master.util.CsvWriter;
import pucpr.meincheim.master.util.LogUtils;

public class ExperimentLoaderTest {

	protected UIPluginContext context;
	protected XLog hospitalLog;
	protected Miner miner;

	protected String filePathBase;
	protected String datesetPathBase;
	protected String experimentPathBase;

	protected List<SimilarityMeasure> similaritiesMeasures;

	protected QualityEvaluator qualityEvaluator;

	private boolean evaluate = true;
	private boolean cluster = false;

	private File currentFile;
	private XLog currentLog;

	public ExperimentLoaderTest() {
		context = new FakePluginContext();
		PackageManager.getInstance();
		PluginManagerImpl.initialize(UIPluginContext.class);
		PluginManagerImpl.getInstance();
		miner = new InductiveMiner();

		// filePathBase = "C:\\Users\\alexme\\Dropbox\\Mestrado em Informática -
		// PUCPR\\Process Mining\\2017 - Process Mining - Dissertação";
		filePathBase = "D:\\Dropbox\\Dropbox\\Mestrado em Informática - PUCPR\\Process Mining\\2017 - Process Mining - Dissertação";

		experimentPathBase = filePathBase + "\\Experiment\\Results";
		datesetPathBase = filePathBase + "\\Experiment\\Dataset";

		currentFile = new File(datesetPathBase + "\\01Hospital_log.xes");
		currentLog = LogUtils.loadByFile(currentFile);
	}

	

	@Test
	public void CommonActivityNameSimilarity() {
		protocol(currentFile, currentLog, new CommonActivityNameSimilarity());
	}

	@Test
	public void CommonNodesEdgesSimilarity() {
		protocol(currentFile, currentLog, new CommonNodesEdgesSimilarity());
	}

	@Test
	public void FeatureBasedSimilarity() {
		protocol(currentFile, currentLog, new FeatureBasedSimilarity());
	}

	// @Test
	public void NodeLinkBasedSimilarity() {
		protocol(currentFile, currentLog, new NodeLinkBasedSimilarity());
	}

	@Test
	public void DependencyGraphComparisonSimilarity() {
		protocol(currentFile, currentLog, new DependencyGraphComparisonSimilarity());
	}

	@Test
	public void TARSimilarity() {
		protocol(currentFile, currentLog, new TARSimilarity());
	}

	@Test
	public void GraphEditDistanceSimilarity() {
		protocol(currentFile, currentLog, new GraphEditDistanceSimilarity());
	}

	@Test
	public void LaRosaSimilarity() {
		protocol(currentFile, currentLog, new LaRosaSimilarity());
	}
}
