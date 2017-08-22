package pucpr.meincheim.master.quality;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.processtree.ProcessTree;
import org.processmining.projectedrecallandprecision.framework.CompareParameters;
import org.processmining.projectedrecallandprecision.framework.CompareParameters.RecallName;
import org.processmining.projectedrecallandprecision.helperclasses.AutomatonFailedException;
import org.processmining.projectedrecallandprecision.plugins.CompareLog2ProcessTreePluginScaledLogPrecision;
import org.processmining.projectedrecallandprecision.plugins.CompareParametersDialog;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult.ProjectedMeasuresFailedException;

import pucpr.meincheim.master.miner.InductiveMiner;

public class PPCProcessTreeQualityEvaluator extends QualityEvaluator {

	private XLog log;
	private ProcessTree model;
	private InductiveMiner miner;
	private UIPluginContext context;

	public PPCProcessTreeQualityEvaluator(UIPluginContext context, InductiveMiner miner) {
		this.context = context;
		this.miner = miner;
	}

	public void loadMapping(XLog log) {
		this.log = log;
		model = miner.mineToProcessTree(context, log);
	}

	public ProjectedRecallPrecisionResult pccCalculate()
			throws AutomatonFailedException, InterruptedException, ProjectedMeasuresFailedException {
		int num = new EfficientTree(model).getInt2activity().length;
		CompareParametersDialog dialog = new CompareParametersDialog(log, num, RecallName.fitness);
		CompareParameters parameters = dialog.getMiningParameters();
		parameters.setK(1);
		return CompareLog2ProcessTreePluginScaledLogPrecision.measure(log, model, parameters,
				ProMCanceller.NEVER_CANCEL);
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
