package pucpr.meincheim.master.quality;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.projectedrecallandprecision.helperclasses.AutomatonFailedException;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult.ProjectedMeasuresFailedException;

import be.kuleuven.econ.cbf.metrics.simplicity.WeighedPlaceTransitionArcDegree;
import pucpr.meincheim.master.miner.InductiveMiner;

public class FinalQualityEvaluator extends QualityEvaluator {

	private XLog log;
	private PPCProcessTreeQualityEvaluator ppcProcessTreeQualityEvaluator;
	private AyraQualityEvaluator ayraQualityEvaluator;

	public FinalQualityEvaluator(UIPluginContext context, InductiveMiner miner) {
		this.ppcProcessTreeQualityEvaluator = new PPCProcessTreeQualityEvaluator(context, miner);
		this.ayraQualityEvaluator = new AyraQualityEvaluator(context, miner);
	}
	
	@Override
	public void loadMapping(XLog log) {
		this.log = log;
		ppcProcessTreeQualityEvaluator.loadMapping(log);
		ayraQualityEvaluator.loadMapping(log);
	}
	
	public double calculateSimplicity(){
		WeighedPlaceTransitionArcDegree metric = new WeighedPlaceTransitionArcDegree();
		metric.load(ayraQualityEvaluator.getMapping());
		metric.calculate();
		return metric.getResult();
	}
	
	public ModelQuality calculate() {
		ModelQuality q = new ModelQuality();
		q.setModelName("" + log.size());
		try {
			//q.setRecall(ayraQualityEvaluator.calculateRecall());
			q.setPrecision(ppcProcessTreeQualityEvaluator.pccCalculate().getPrecision());
			//q.setSimplicit(calculateSimplicity());
		} catch (InterruptedException | AutomatonFailedException | ProjectedMeasuresFailedException e) {
			System.out.println("Error on calculate metrics " + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("Recall: " + q.getRecall());
		System.out.println("Precision: " + q.getPrecision());
		System.out.println("Simplicity: " + q.getSimplicit());
		return q;
	}
}
