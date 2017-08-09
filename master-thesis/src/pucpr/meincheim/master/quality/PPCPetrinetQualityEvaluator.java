package pucpr.meincheim.master.quality;

import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.projectedrecallandprecision.framework.CompareParameters;
import org.processmining.projectedrecallandprecision.framework.CompareParameters.RecallName;
import org.processmining.projectedrecallandprecision.helperclasses.AutomatonFailedException;
import org.processmining.projectedrecallandprecision.plugins.CompareLog2PetriNetPluginScaledLogPrecision;
import org.processmining.projectedrecallandprecision.plugins.CompareParametersDialog;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult.ProjectedMeasuresFailedException;

import gnu.trove.set.hash.THashSet;
import pucpr.meincheim.master.miner.InductiveMiner;

public class PPCPetrinetQualityEvaluator extends QualityEvaluator {

	private XLog log;
	private Petrinet model;

	public PPCPetrinetQualityEvaluator(UIPluginContext context, XLog log) {
		this.log = log;
		model = new InductiveMiner().mineToPetrinet(context, log);
	}

	private ProjectedRecallPrecisionResult pccCalculate()
			throws AutomatonFailedException, InterruptedException, ProjectedMeasuresFailedException {
		AcceptingPetriNet aModel = AcceptingPetriNetFactory.createAcceptingPetriNet(model, getInitialMarking(model),
				PetrinetUtils.getFinalMarking(model));
		Set<String> labels = new THashSet<>();
		for (Transition t : aModel.getNet().getTransitions()) {
			if (!t.isInvisible()) {
				labels.add(t.getLabel());
			}
		}
		CompareParametersDialog dialog = new CompareParametersDialog(log, labels.size(), RecallName.fitness);
		CompareParameters parameters = dialog.getMiningParameters();
		parameters.setK(1);
		return CompareLog2PetriNetPluginScaledLogPrecision.measure(log, aModel, parameters, ProMCanceller.NEVER_CANCEL);
	}

	public ModelQuality calculate() {
		ModelQuality q = new ModelQuality();
		q.setModelName("" + log.size());
		ProjectedRecallPrecisionResult result;
		try {
			result = pccCalculate();
			q.setRecall(result.getRecall());
			q.setPrecision(result.getPrecision());
		} catch (AutomatonFailedException | InterruptedException | ProjectedMeasuresFailedException e) {

		}

		System.out.println("Recall: " + q.getRecall());
		System.out.println("Precision: " + q.getPrecision());
		return q;
	}
}
