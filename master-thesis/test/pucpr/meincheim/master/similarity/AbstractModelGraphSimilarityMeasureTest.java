package pucpr.meincheim.master.similarity;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;

import pucpr.meincheim.master.miner.InductiveMiner;
import pucpr.meincheim.master.util.LogUtils;

public class AbstractModelGraphSimilarityMeasureTest extends AbstractModelGraphSimilarityMeasure  {

	protected UIPluginContext context;
	protected XLog log;
	protected InductiveMiner inductiveMiner;

	protected Petrinet model0;
	protected Petrinet model1;
	protected Petrinet model2;
	protected Petrinet complexModel;

	@Before
	public void setup() throws URISyntaxException {
		context = new FakePluginContext();
		PackageManager.getInstance();
		PluginManagerImpl.initialize(UIPluginContext.class);
		PluginManagerImpl.getInstance();
		inductiveMiner = new InductiveMiner();

		String filePathBase = "C:\\Users\\alexme\\Dropbox\\Mestrado em Informática - PUCPR\\Process Mining\\2017 - Process Mining - Dissertação\\Dataset\\SimilarityValidation\\";
		//String filePathBase = "D:\\Dropbox\\Dropbox\\Mestrado em Informática - PUCPR\\Process Mining\\2017 - Process Mining - Dissertação\\Dataset\\SimilarityValidation\\";

		File file = new File(filePathBase + "model0.xes");
		log = LogUtils.loadByFile(file);
		model0 = inductiveMiner.mineToPetrinet(context, log);

		file = new File(filePathBase + "model1.xes");
		log = LogUtils.loadByFile(file);
		model1 = inductiveMiner.mineToPetrinet(context, log);

		file = new File(filePathBase + "model2.xes");
		log = LogUtils.loadByFile(file);
		model2 = inductiveMiner.mineToPetrinet(context, log);
	
		file = new File(filePathBase + "ComplexModel.xes");
		log = LogUtils.loadByFile(file);
		complexModel = inductiveMiner.mineToPetrinet(context, log);

	}

	@Test
	public void getTransitionsLabelsTest() throws Exception {
		Set<String> labels = getLabels(model0, true, true);
		Assert.assertEquals(12, labels.size());

		labels = getLabels(model1, true, true);
		Assert.assertEquals(14, labels.size());

		labels = getLabels(model2, true, true);
		Assert.assertEquals(8, labels.size());

		labels = getLabels(model1, true, false);
		Assert.assertEquals(15, labels.size());

		labels = getLabels(model1, false, false);
		Assert.assertEquals(31, labels.size());
	}

	@Test
	public void getPlacesMappingTest() {
		Map<PetrinetNode, PetrinetNode> mappingAB = getPlacesMapping(model0, model0);
		Assert.assertEquals(14, mappingAB.size());
		
		mappingAB = getPlacesMapping(model1, model1);
		Assert.assertEquals(16, mappingAB.size());
		
		mappingAB = getPlacesMapping(model0, model1);
		Assert.assertEquals(13, mappingAB.size());
		
		mappingAB = getPlacesMapping(complexModel, complexModel);
		Assert.assertEquals(24, mappingAB.size());
	}

	@Test
	public void getTransitiveClosurePredecessorsTest() {
		for (PetrinetNode node : model0.getNodes()) {		
			Set<PetrinetNode> nodes = getTransitiveClosurePredecessors(node);
		}
	}

	@Test
	public void getTransitiveClosureSucessorsTest() {	
		for (PetrinetNode node : model0.getNodes()) {		
			Set<PetrinetNode> nodes = getTransitiveClosureSuccessors(node);
		}
	}

	@Test
	public void getEdgesOnlyInOneModelTest() {
//		Set<PetrinetEdge> edges = getEdgesOnlyInOneModel(complexModel.getGraph(), complexModel.getGraph());
//		Assert.assertEquals(24, mappingAB.size());
	}

}
