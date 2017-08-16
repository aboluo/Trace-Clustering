package pucpr.meincheim.master.base;

import java.io.File;
import java.net.URISyntaxException;

import org.deckfour.xes.model.XLog;
import org.junit.Before;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;

import pucpr.meincheim.master.miner.InductiveMiner;
import pucpr.meincheim.master.miner.Miner;
import pucpr.meincheim.master.similarity.AbstractModelGraphSimilarityMeasure;
import pucpr.meincheim.master.util.LogUtils;

public abstract class BaseTest extends AbstractModelGraphSimilarityMeasure{

	protected UIPluginContext context;
	protected XLog log;
	protected XLog logmodel0;
	protected XLog logmodel1;
	protected XLog logmodel2;
	protected XLog hospitalLogCluster0;
	protected XLog hospitalLog;
	protected XLog complexLog;
	protected Miner miner;
	
	protected Petrinet model0;
	protected Petrinet model1;
	protected Petrinet model2;
	protected Petrinet hospitalmodel;
	protected Petrinet hospitalmodelCluster0;
	protected Petrinet complexModel;

	@Before
	public void setup() throws URISyntaxException {
		context = new FakePluginContext();
		PackageManager.getInstance();
		PluginManagerImpl.initialize(FakePluginContext.class);
		PluginManagerImpl.getInstance();
		miner = new InductiveMiner();

		//String filePathBase = "C:\\Users\\alexme\\Dropbox\\Mestrado em Informática - PUCPR\\Process Mining\\2017 - Process Mining - Dissertação\\Dataset\\SimilarityValidation\\";
		
		String filePathBase = "D:\\Dropbox\\Dropbox\\Mestrado em Informática - PUCPR\\Process Mining\\2017 - Process Mining - Dissertação\\Dataset\\SimilarityValidation\\";

		File file = new File(filePathBase + "model0.xes");
		logmodel0 = LogUtils.loadByFile(file);
		model0 = miner.mineToPetrinet(context, logmodel0);

		file = new File(filePathBase + "model1.xes");
		logmodel1 = LogUtils.loadByFile(file);
		model1 = miner.mineToPetrinet(context, logmodel1);

		file = new File(filePathBase + "model2.xes");
		logmodel2 = LogUtils.loadByFile(file);
		model2 = miner.mineToPetrinet(context, logmodel2);
		
		file = new File(filePathBase + "ComplexModel.xes");
		complexLog = LogUtils.loadByFile(file);
		complexModel = miner.mineToPetrinet(context, complexLog);
		
		file = new File(filePathBase + "Hospital_log.xes");
		hospitalLog = LogUtils.loadByFile(file);
		hospitalmodel = miner.mineToPetrinet(context, hospitalLog);
		
		file = new File(filePathBase + "hospital0.xes");
		hospitalLogCluster0 = LogUtils.loadByFile(file);
		hospitalmodelCluster0 = miner.mineToPetrinet(context, hospitalLogCluster0);


	}

}
