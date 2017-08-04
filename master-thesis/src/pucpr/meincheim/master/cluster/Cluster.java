package pucpr.meincheim.master.cluster;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class Cluster {	
	
	private Petrinet model;
	private XLog log;
	private int id;
		
	public Cluster(Petrinet model, XLog log, int id) {
		super();
		this.model = model;
		this.log = log;
		this.id = id;
	}
	
	public Petrinet getModel() {
		return model;
	}
	
	public void setModel(Petrinet model) {
		this.model = model;
	}
	
	public XLog getLog() {
		return log;
	}
	
	public void setLog(XLog log) {
		this.log = log;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
