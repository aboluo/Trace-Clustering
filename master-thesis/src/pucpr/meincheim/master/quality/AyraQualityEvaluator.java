package pucpr.meincheim.master.quality;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.petrinets.utils.PetriNetUtils;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParamProvider;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppNaiveAlg;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppParam;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppParamProvider;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.input.MemoryMapping;
import be.kuleuven.econ.cbf.utils.MappingUtils;
import nl.tue.astar.AStarException;

public class AyraQualityEvaluator extends QualityEvaluator {

	private Mapping mapping;
	private TransEvClassMapping transMapping;
	private UIPluginContext context;

	public AyraQualityEvaluator(UIPluginContext context, XLog log, Petrinet net) {
		this.mapping = new MemoryMapping(log, net);
		MappingUtils.setInvisiblesInPetrinet(mapping, mapping.getPetrinet());
		this.transMapping = MappingUtils.getTransEvClassMapping(mapping, net, log);
		this.context = context;
	}

	public double calculateRecall() throws AStarException {
		IPNReplayAlgorithm alg = new PetrinetReplayerWithILP();
		IPNReplayParamProvider provider = alg.constructParamProvider(context, mapping.getPetrinet(), mapping.getLog(),
				transMapping);
		JComponent paramUI = provider.constructUI();
		IPNReplayParameter parameters = provider.constructReplayParameter(paramUI);
		parameterBuilder(parameters);
		PNLogReplayer rep = new PNLogReplayer();
		PNRepResult result = null;
		result = rep.replayLog(context, mapping.getPetrinet(), mapping.getLog(), transMapping, alg, parameters);
		String fitnessResult = "0.00";
		if (result != null) {
			fitnessResult = result.getInfo().get(PNRepResult.TRACEFITNESS).toString();
			fitnessResult = fitnessResult.replace(',', '.');
		}
		return Double.parseDouble(fitnessResult);
	}

	public PNRepResult calculateBehavioralApp() throws AStarException {
		IPNReplayAlgorithm alg = new BehavAppNaiveAlg();
		IPNReplayParamProvider provider = new BehavAppParamProvider(context, mapping.getPetrinet(), mapping.getLog(),
				transMapping);
		JComponent paramUI = provider.constructUI();
		BehavAppParam parameters = (BehavAppParam) provider.constructReplayParameter(paramUI);
		parameterBuilder(parameters);
		PNLogReplayer rep = new PNLogReplayer();
		return rep.replayLog(context, mapping.getPetrinet(), mapping.getLog(), transMapping, alg, parameters);
	}

	public AlignmentPrecGenRes calculateAlignmentPrecGen(PNRepResult pnResult) throws AStarException {
		AlignmentPrecGen aPrecGen = new AlignmentPrecGen();
		return aPrecGen.measureConformanceAssumingCorrectAlignment(context, transMapping, pnResult,
				mapping.getPetrinet(), getInitialMarking(mapping.getPetrinet()), false);
	}

	private void parameterBuilder(IPNReplayParameter parameters) {
		parameters.setCreateConn(false);
		parameters.setGUIMode(false);
		Marking finalMarking = PetrinetUtils.getFinalMarking(mapping.getPetrinet());
		if (finalMarking != null) {
			parameters.setFinalMarkings(new Marking[] { finalMarking });
		}
		if (!PetriNetUtils.hasInitialMarkings(context, mapping.getPetrinet())) {
			PetriNetUtils.addInitialMarking(context, mapping.getPetrinet(), getInitialMarking(mapping.getPetrinet()));
			parameters.setInitialMarking(getInitialMarking(mapping.getPetrinet()));
		}
	}

	public ModelQuality calculate() {
		ModelQuality q = new ModelQuality();
		q.setModelName("" + mapping.getLog().size());
		try {
			PNRepResult pnResult = calculateBehavioralApp();
			AlignmentPrecGenRes res = calculateAlignmentPrecGen(pnResult);
			q.setRecall(calculateRecall());
			q.setPrecision(res.getPrecision());
			q.setGeneralization(res.getGeneralization());
		} catch (AStarException e) {
		}
		System.out.println("Recall: " + q.getRecall());
		System.out.println("Precision: " + q.getPrecision());
		System.out.println("Generalization: " + q.getGeneralization());
		return q;
	}
}
