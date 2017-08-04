package pucpr.meincheim.master.quality;

import java.io.IOException;
import java.util.Set;

import javax.swing.JComponent;
import javax.xml.transform.TransformerException;

import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.petrinets.utils.PetriNetUtils;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParamProvider;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppParamProvider;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppStubbornAlg;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.input.MemoryMapping;
import be.kuleuven.econ.cbf.utils.MappingUtils;
import nl.tue.astar.AStarException;
import pucpr.meincheim.master.base.BaseTest;

public class QualityEvaluatorTest extends BaseTest {

	//@Test
	public void qualityModelTest()
			throws IOException, TransformerException, ConnectionCannotBeObtained, AStarException {
		QualityEvaluator qe = new QualityEvaluator(context, logmodel1, model1);

		ModelQuality modelQuality = qe.calculate();
		System.out.println("Recall " + modelQuality.getRecall());
		System.out.println("Precision " + modelQuality.getPrecision());

		qe = new QualityEvaluator(context, logmodel0, model0);
		modelQuality = qe.calculate();
		System.out.println("Recall " + modelQuality.getRecall());
		System.out.println("Precision " + modelQuality.getPrecision());

		qe = new QualityEvaluator(context, logmodel2, model2);
		modelQuality = qe.calculate();
		System.out.println("Recall " + modelQuality.getRecall());
		System.out.println("Precision " + modelQuality.getPrecision());

		qe = new QualityEvaluator(context, hospitalLogCluster0, hospitalmodelCluster0);
		modelQuality = qe.calculate();
		System.out.println("Recall " + modelQuality.getRecall());
		System.out.println("Precision " + modelQuality.getPrecision());

	}

	@Test
	public void test() throws ConnectionCannotBeObtained, AStarException {
		replayCalc(hospitalLog, hospitalmodelCluster0, new PNLogReplayer(), new PetrinetReplayerWithILP());		
		precCalc(hospitalLog, hospitalmodelCluster0);
	}
	
	//@Test
	public void test1() throws ConnectionCannotBeObtained, AStarException {		
		replayCalc(logmodel0, model0, new PNLogReplayer(), new PetrinetReplayerWithILP());		
		precCalc(logmodel0, model0);
	}
	
	private void precCalc(XLog log, Petrinet model)
			throws AStarException, ConnectionCannotBeObtained {
		Mapping mapping = new MemoryMapping(log, model);
		TransEvClassMapping trasnEv = MappingUtils.getTransEvClassMapping(mapping, model, log);
		MappingUtils.setInvisiblesInPetrinet(mapping, model);
		
		if(!PetriNetUtils.hasInitialMarkings(context, model)){
			PetriNetUtils.addInitialMarking(context, model, getInitialMarking(model));
		}
		
		IPNReplayAlgorithm alg = new BehavAppStubbornAlg();
		IPNReplayParamProvider provider = new BehavAppParamProvider(context, model, log,
				trasnEv);
		JComponent paramUI = provider.constructUI();
		IPNReplayParameter parameters = provider.constructReplayParameter(paramUI);
		parameters.setCreateConn(false);
		parameters.setGUIMode(false);
		parameters.setInitialMarking(getInitialMarking(model));
		PNLogReplayer rep = new PNLogReplayer();
		PNRepResult result = null;
		
	

		try {
			result = rep.replayLog(context, mapping.getPetrinet(), mapping.getLog(), trasnEv, alg, parameters);
		} catch (AStarException e) {

		}
		
		AlignmentPrecGen aPrecGen = new AlignmentPrecGen();
		AlignmentPrecGenRes aresult =  aPrecGen.measureConformanceAssumingCorrectAlignment(
				context, trasnEv, result, model, getInitialMarking(model),
				false);
				
		double precision = aresult.getPrecision();
		double generalization = aresult.getGeneralization();

		System.out.println("Precision: " + precision);
		System.out.println("Generalization: " + generalization);
	}

	private void replayCalc(XLog log, Petrinet model, PNLogReplayer rep, IPNReplayAlgorithm alg)
			throws AStarException, ConnectionCannotBeObtained {
		Mapping mapping = new MemoryMapping(log, model);
		TransEvClassMapping trasnEv = MappingUtils.getTransEvClassMapping(mapping, model, log);

		MappingUtils.setInvisiblesInPetrinet(mapping, model);
				
		if(!PetriNetUtils.hasInitialMarkings(context, model)){
			PetriNetUtils.addInitialMarking(context, model, getInitialMarking(model));
		}
		
		IPNReplayParamProvider provider = alg.constructParamProvider(context, model, log, trasnEv);
		JComponent paramUI = provider.constructUI();
		IPNReplayParameter parameters = provider.constructReplayParameter(paramUI);
		parameters.setCreateConn(false);
		parameters.setGUIMode(false);
		//parameters.setInitialMarking(getInitialMarking(model));
		PNRepResult result = rep.replayLog(context, model, log, trasnEv, alg, parameters);
		System.out.println("Fitness: " + result.getInfo().get(result.TRACEFITNESS));

	}

	private Marking getInitialMarking(Petrinet net) {
		Set<Place> places = PetrinetUtils.getStartPlaces(net);
		if (places.isEmpty()) {
			for (Place vertexA : net.getPlaces()) {
				if (vertexA.getLabel().startsWith("source")) {
					places.add(vertexA);
				}
			}
		}

		Marking initialMarking = new Marking();
		initialMarking.addAll(places);
		return initialMarking;
	}

}
