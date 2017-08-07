package pucpr.meincheim.master.quality;

import java.util.Set;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
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
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppParam;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppParamProvider;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppStubbornAlg;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.input.MemoryMapping;
import be.kuleuven.econ.cbf.metrics.simplicity.AdvancedStructuralAppropriateness;
import be.kuleuven.econ.cbf.utils.MappingUtils;
import nl.tue.astar.AStarException;

public class QualityEvaluator {

	private String modelName = "_default";
	private Mapping mapping;
	private TransEvClassMapping transMapping;
	private UIPluginContext context;

	private static AdvancedStructuralAppropriateness simplicity = new AdvancedStructuralAppropriateness();

	public QualityEvaluator(UIPluginContext context, XLog log, Petrinet net) {
		this.mapping = new MemoryMapping(log, net);
		this.transMapping = MappingUtils.getTransEvClassMapping(mapping, net, log);
		this.context = context;

		if (!PetriNetUtils.hasInitialMarkings(context, mapping.getPetrinet())) {
			PetriNetUtils.addInitialMarking(context, mapping.getPetrinet(), getInitialMarking(mapping.getPetrinet()));
		}

	}

	public double calculateRecall() {
		IPNReplayAlgorithm alg = new PetrinetReplayerWithILP();
		IPNReplayParamProvider provider = alg.constructParamProvider(context, mapping.getPetrinet(), mapping.getLog(),
				transMapping);
		JComponent paramUI = provider.constructUI();
		IPNReplayParameter parameters = provider.constructReplayParameter(paramUI);
		parameters.setCreateConn(false);
		parameters.setGUIMode(false);
		PNLogReplayer rep = new PNLogReplayer();

		PNRepResult result = null;

		try {
			result = rep.replayLog(context, mapping.getPetrinet(), mapping.getLog(), transMapping, alg, parameters);
		} catch (AStarException e) {

		}

		String fitnessResult = "0.00";
		if (result != null) {
			fitnessResult = result.getInfo().get(PNRepResult.TRACEFITNESS).toString();
			fitnessResult = fitnessResult.replace(',', '.');
		}

		System.out.println("Recall: " + fitnessResult);

		return Double.parseDouble(fitnessResult);
	}

	public AlignmentPrecGenRes calculateAlignmentPrecGenRes() {
		IPNReplayAlgorithm alg = new BehavAppStubbornAlg();
		IPNReplayParamProvider provider = new BehavAppParamProvider(context, mapping.getPetrinet(), mapping.getLog(),
				transMapping);
		JComponent paramUI = provider.constructUI();
		BehavAppParam parameters = (BehavAppParam) provider.constructReplayParameter(paramUI);
		parameters.setMaxNumStates(5000);
		parameters.setCreateConn(false);
		parameters.setGUIMode(false);
		PNLogReplayer rep = new PNLogReplayer();
		PNRepResult result = null;

		try {
			result = rep.replayLog(context, mapping.getPetrinet(), mapping.getLog(), transMapping, alg, parameters);
		} catch (AStarException e) {

		}
		AlignmentPrecGen aPrecGen = new AlignmentPrecGen();
		return aPrecGen.measureConformanceAssumingCorrectAlignment(context, transMapping, result, mapping.getPetrinet(),
				getInitialMarking(mapping.getPetrinet()), false);
	}

	public double calculateSimplicity() {
		simplicity.load(mapping);
		simplicity.calculate();
		return simplicity.getResult();
	}

	public ModelQuality calculate() {
		ModelQuality q = new ModelQuality();		
		AlignmentPrecGenRes res = calculateAlignmentPrecGenRes();		
		q.setModelName(modelName);
		q.setRecall(calculateRecall());
		q.setPrecision(res.getPrecision());
		q.setGeneralization(res.getGeneralization());
		// q.setSimplicit(calculateSimplicity());
		return q;
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
