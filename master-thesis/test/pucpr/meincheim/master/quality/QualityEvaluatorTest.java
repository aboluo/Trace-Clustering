package pucpr.meincheim.master.quality;

import java.util.Set;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.petrinets.utils.PetriNetUtils;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParamProvider;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppNaiveAlg;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;
import org.processmining.processtree.ProcessTree;
import org.processmining.projectedrecallandprecision.framework.CompareParameters;
import org.processmining.projectedrecallandprecision.framework.CompareParameters.RecallName;
import org.processmining.projectedrecallandprecision.helperclasses.AutomatonFailedException;
import org.processmining.projectedrecallandprecision.plugins.CompareLog2PetriNetPluginScaledLogPrecision;
import org.processmining.projectedrecallandprecision.plugins.CompareLog2ProcessTreePluginScaledLogPrecision;
import org.processmining.projectedrecallandprecision.plugins.CompareParametersDialog;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult.ProjectedMeasuresFailedException;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.input.MemoryMapping;
import be.kuleuven.econ.cbf.utils.MappingUtils;
import gnu.trove.set.hash.THashSet;
import nl.tue.astar.AStarException;
import pucpr.meincheim.master.base.BaseTest;
import pucpr.meincheim.master.miner.InductiveMiner;

public class QualityEvaluatorTest extends BaseTest {

	// @Test
	public void test() throws ConnectionCannotBeObtained, AStarException {
		replayCalc(hospitalLogCluster0, hospitalmodelCluster0, new PNLogReplayer(), new PetrinetReplayerWithILP());
		precCalc(hospitalLogCluster0, hospitalmodelCluster0);
	}

	// @Test
	public void test1() throws ConnectionCannotBeObtained, AStarException {
		replayCalc(logmodel0, model0, new PNLogReplayer(), new PetrinetReplayerWithILP());
		precCalc(logmodel0, model0);
	}

	@Test
	public void test2() throws AutomatonFailedException, InterruptedException, ProjectedMeasuresFailedException {
		pccCals(hospitalLogCluster0, hospitalmodelCluster0);
	}

	@Test
	public void testProcessTree()
			throws AutomatonFailedException, InterruptedException, ProjectedMeasuresFailedException {

		InductiveMiner miner = new InductiveMiner();
		ProcessTree tModel = miner.mineToProcessTree(context, hospitalLogCluster0);
		CompareLog2ProcessTreePluginScaledLogPrecision calc = new CompareLog2ProcessTreePluginScaledLogPrecision();
		int num = new EfficientTree(tModel).getInt2activity().length;

		CompareParametersDialog dialog = new CompareParametersDialog(hospitalLogCluster0, num, RecallName.fitness);
		CompareParameters parameters = dialog.getMiningParameters();
		parameters.setK(2);
		ProjectedRecallPrecisionResult result = calc.measure(hospitalLogCluster0, tModel, parameters,
				ProMCanceller.NEVER_CANCEL);
		System.out.println("Precision " + result.getPrecision());
		System.out.println("Fitness " + result.getRecall());
	}

	private void pccCals(XLog log, Petrinet model)
			throws AutomatonFailedException, InterruptedException, ProjectedMeasuresFailedException {
		AcceptingPetriNet aModel = AcceptingPetriNetFactory.createAcceptingPetriNet(model, getInitialMarking(model),
				PetrinetUtils.getFinalMarking(model));
		CompareLog2PetriNetPluginScaledLogPrecision calc = new CompareLog2PetriNetPluginScaledLogPrecision();

		Set<String> labels = new THashSet<>();
		for (Transition t : aModel.getNet().getTransitions()) {
			if (!t.isInvisible()) {
				labels.add(t.getLabel());
			}
		}
		CompareParametersDialog dialog = new CompareParametersDialog(log, labels.size(), RecallName.fitness);
		CompareParameters parameters = dialog.getMiningParameters();
		parameters.setK(1);
		ProjectedRecallPrecisionResult result = calc.measure(log, aModel, parameters, ProMCanceller.NEVER_CANCEL);
		System.out.println("Precision " + result.getPrecision());
		System.out.println("Fitness " + result.getRecall());

	}

	private void precCalc(XLog log, Petrinet model) throws AStarException, ConnectionCannotBeObtained {
		Mapping mapping = new MemoryMapping(log, model);
		MappingUtils.setInvisiblesInPetrinet(mapping, model);
		TransEvClassMapping trasnEv = MappingUtils.getTransEvClassMapping(mapping, model, log);

		IPNReplayAlgorithm alg = new BehavAppNaiveAlg();
		IPNReplayParamProvider provider = alg.constructParamProvider(context, model, log, trasnEv);
		JComponent paramUI = provider.constructUI();
		BehavAppParam parameters = (BehavAppParam) provider.constructReplayParameter(paramUI);
		parameters.setGUIMode(false);

		Marking finalMarking = PetrinetUtils.getFinalMarking(model);

		if (finalMarking != null) {
			parameters.setFinalMarkings(new Marking[] { finalMarking });
		}

		if (!PetriNetUtils.hasInitialMarkings(context, model)) {
			PetriNetUtils.addInitialMarking(context, model, getInitialMarking(model));
			parameters.setInitialMarking(getInitialMarking(model));
		}

		PNLogReplayer rep = new PNLogReplayer();
		PNRepResult result = null;
		result = rep.replayLog(context, mapping.getPetrinet(), mapping.getLog(), trasnEv, alg, parameters);

		System.out.println("Behavioral app " + result.getInfo().get(PNRepResult.BEHAVIORAPPROPRIATENESS));

		AlignmentPrecGen aPrecGen = new AlignmentPrecGen();
		AlignmentPrecGenRes aresult = aPrecGen.measureConformanceAssumingCorrectAlignment(context, trasnEv, result,
				model, getInitialMarking(model), false);

		double precision = aresult.getPrecision();
		double generalization = aresult.getGeneralization();

		System.out.println("Precision: " + precision);
		System.out.println("Generalization: " + generalization);
	}

	private void replayCalc(XLog log, Petrinet model, PNLogReplayer rep, IPNReplayAlgorithm alg)
			throws AStarException, ConnectionCannotBeObtained {
		Mapping mapping = new MemoryMapping(log, model);
		MappingUtils.setInvisiblesInPetrinet(mapping, model);
		TransEvClassMapping trasnEv = MappingUtils.getTransEvClassMapping(mapping, model, log);
		IPNReplayParamProvider provider = alg.constructParamProvider(context, model, log, trasnEv);
		JComponent paramUI = provider.constructUI();
		IPNReplayParameter parameters = provider.constructReplayParameter(paramUI);
		parameters.setCreateConn(false);
		parameters.setGUIMode(false);

		Marking finalMarking = PetrinetUtils.getFinalMarking(model);

		if (finalMarking != null) {
			parameters.setFinalMarkings(new Marking[] { finalMarking });
		}

		if (!PetriNetUtils.hasInitialMarkings(context, model)) {
			PetriNetUtils.addInitialMarking(context, model, getInitialMarking(model));
			parameters.setInitialMarking(getInitialMarking(model));
		}

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
