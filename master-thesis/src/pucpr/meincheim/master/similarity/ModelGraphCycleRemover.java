package pucpr.meincheim.master.similarity;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;

public class ModelGraphCycleRemover {

	/**
	 * Returns a cycle-free version of the given graph
	 * 
	 * @param graph
	 *            the graph to remove cycles from
	 * @param start
	 *            the start element of the model
	 */
	public static PetrinetGraph removeCycles(PetrinetGraph graph, PetrinetNode start) {
		Collection<PetrinetEdge> cycles = new HashSet<PetrinetEdge>();
		// identify cylces and store them in the set of edges
		identifyCycles(graph, start, new HashSet<PetrinetNode>(), cycles);
		
		for(PetrinetEdge edge : cycles){
			graph.removeEdge(edge);
		}
		
		return graph;
	}

	public static void identifyCycles(PetrinetGraph graph, PetrinetNode actual, Collection<PetrinetNode> visited,
			Collection<PetrinetEdge> cycles) {
		visited.add(actual);

		if (actual.getGraph().getOutEdges(actual) != null) {
			for (PetrinetEdge edge : actual.getGraph().getOutEdges(actual)) {
				PetrinetNode node = (PetrinetNode) edge.getTarget();
				Collection<PetrinetNode> visitedActual = new HashSet<PetrinetNode>(visited);
				if (visitedActual.contains(node)) {
					cycles.add((PetrinetEdge) edge);
				} else {
					identifyCycles(graph, (PetrinetNode) node, visitedActual, cycles);
				}
			}
		}
	}
}
