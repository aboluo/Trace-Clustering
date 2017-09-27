package pucpr.meincheim.master.quality;

public class ModelQuality {

	private String modelName;

	private double precision;
	private double recall;
	private double Simplicit;
	private double generalization;
	private double fScore;

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getSimplicit() {
		return Simplicit;
	}

	public void setSimplicit(double simplicit) {
		Simplicit = simplicit;
	}

	public double getGeneralization() {
		return generalization;
	}

	public void setGeneralization(double generalization) {
		this.generalization = generalization;
	}

	public double getfScore() {
		return fScore;
	}

	public void setfScore(double fScore) {
		this.fScore = fScore;
	}
}
