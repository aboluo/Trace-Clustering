package pucpr.meincheim.master.cluster;

public class Candidate {
	
	private Cluster centroide;
	private Double similarity;
	
	public Candidate(Cluster centroide, Double similarity) {
		super();
		this.centroide = centroide;
		this.similarity = similarity;
	}
	
	public Cluster getCentroide() {
		return centroide;
	}
	
	public void setCentroide(Cluster centroide) {
		this.centroide = centroide;
	}
	
	public Double getSimilarity() {
		return similarity;
	}
	
	public void setSimilarity(Double similarity) {
		this.similarity = similarity;
	}

}
