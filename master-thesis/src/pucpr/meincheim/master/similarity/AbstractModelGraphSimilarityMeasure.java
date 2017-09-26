package pucpr.meincheim.master.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public abstract class AbstractModelGraphSimilarityMeasure {

	/**
	 * Creates a mapping of places in one model to nodes in another model based
	 * on edges successors and predecessors. This method can only generate 1:1
	 * mappings.
	 * 
	 * @param from
	 *            the from-model
	 * @param to
	 *            the to-model
	 * @return the mapping of nodes from model from to model to
	 * @throws Exception
	 */
	protected Map<PetrinetNode, PetrinetNode> getPlacesMapping(PetrinetGraph from, PetrinetGraph to) {
		Map<PetrinetNode, PetrinetNode> mapping = new HashMap<PetrinetNode, PetrinetNode>();

		for (PetrinetNode vertexA : from.getPlaces()) {
			for (PetrinetNode vertexB : to.getPlaces()) {
				boolean matchInEdges = checkMatchInEdges(from, to, vertexA, vertexB);
				if (matchInEdges) {
					boolean matchOutEdges = checkMatchOutEdges(from, to, vertexA, vertexB);
					if (matchOutEdges) {
						mapping.put(vertexA, vertexB);
					}
				}
			}
		}

		return mapping;
	}

	protected PetrinetNode getFirstNodeFromGraph(PetrinetGraph graph) {
		PetrinetNode node = null;
		for (PetrinetNode vertexA : graph.getNodes()) {
			if (vertexA.getGraph().getInEdges(vertexA).size() == 0) {
				node = vertexA;
				break;
			}
		}

		if (node == null) {
			for (PetrinetNode vertexA : graph.getNodes()) {
				if (vertexA.getLabel().contains("source")) {
					node = vertexA;
					break;
				}
			}
		}

		return node;
	}

	protected boolean checkMatchInEdges(PetrinetGraph from, PetrinetGraph to, PetrinetNode vertexA,
			PetrinetNode vertexB) {
		Collection<PetrinetEdge<?, ?>> edgesInVertexA = from.getInEdges(vertexA);
		Collection<PetrinetEdge<?, ?>> edgesInVertexB = to.getInEdges(vertexB);
		boolean matchInEdges = true;
		if (!isModelSource(edgesInVertexA, edgesInVertexB)) {
			matchInEdges = false;
			if (edgesInVertexA.size() == edgesInVertexB.size()) {
				for (PetrinetEdge edgeA : edgesInVertexA) {
					for (PetrinetEdge edgeB : edgesInVertexB) {
						if (edgeA.getSource().toString().equals(edgeB.getSource().toString())) {
							matchInEdges = true;
							break;
						} else {
							matchInEdges = false;
						}
					}

					if (!matchInEdges) {
						break;
					}
				}
			}
		}
		return matchInEdges;
	}

	protected boolean checkMatchOutEdges(PetrinetGraph from, PetrinetGraph to, PetrinetNode vertexA,
			PetrinetNode vertexB) {
		Collection<PetrinetEdge<?, ?>> edgesOutVertexA = from.getOutEdges(vertexA);
		Collection<PetrinetEdge<?, ?>> edgesOutVertexB = to.getOutEdges(vertexB);
		boolean matchEdges = true;
		if (!isModelEnd(edgesOutVertexA, edgesOutVertexB)) {
			matchEdges = false;
			if (edgesOutVertexA.size() == edgesOutVertexB.size()) {
				for (PetrinetEdge edgeA : edgesOutVertexA) {
					for (PetrinetEdge edgeB : edgesOutVertexB) {
						if (edgeA.getTarget().toString().equals(edgeB.getTarget().toString())) {
							matchEdges = true;
							break;
						} else {
							matchEdges = false;
						}
					}

					if (!matchEdges) {
						break;
					}
				}
			}
		}
		return matchEdges;
	}

	protected boolean isModelSource(Collection<PetrinetEdge<?, ?>> edgesInVertexA,
			Collection<PetrinetEdge<?, ?>> edgesInVertexB) {
		return edgesInVertexA.size() == 0 && edgesInVertexB.size() == 0;
	}

	protected boolean isModelEnd(Collection<PetrinetEdge<?, ?>> edgesOutVertexA,
			Collection<PetrinetEdge<?, ?>> edgesOutVertexB) {
		return edgesOutVertexA.size() == 0 && edgesOutVertexB.size() == 0;
	}

	protected Set<Arc> getTransitionEdges(Set<PetrinetNode> modelTransitions) {
		Set<Arc> edges = new HashSet<Arc>();
		for (PetrinetNode node : modelTransitions) {
			Transition source = (Transition) node;
			for (Transition target : source.getVisibleSuccessors()) {
				Arc arc = new Arc(source, target, 1);
				edges.add(arc);
			}
		}
		return edges;
	}

	/**
	 * Returns the set of edges that are only contained in model a but not in
	 * model b.
	 * 
	 * @param a
	 *            model a
	 * @param b
	 *            model b
	 * @param mappingsAB
	 *            mapping of elements from a to elements from b
	 * 
	 * @return set of edges that are only contained in model a but not in b
	 */
	protected Set<PetrinetEdge> getEdgesOnlyInOneModel(PetrinetGraph a, PetrinetGraph b,
			Map<PetrinetNode, PetrinetNode> mappingsAB) {

		Set<PetrinetEdge> edgesOnlyInOneModel = new HashSet<PetrinetEdge>();

		// search for edges within a but not in b
		for (PetrinetEdge edgeA : a.getEdges()) {

			PetrinetNode source = (PetrinetNode) edgeA.getSource();
			PetrinetNode dest = (PetrinetNode) edgeA.getTarget();

			PetrinetNode mappedSource = mappingsAB.get(source);
			PetrinetNode mappedDest = mappingsAB.get(dest);

			if (mappedSource != null || mappedDest != null) {

				boolean found = false;

				for (PetrinetEdge edgeB : b.getEdges()) {

					if (edgeB.getSource().equals(mappedSource) && edgeB.getTarget().equals(mappedDest)) {
						found = true;
						break;
					}
				}

				if (!found && !isInvisibleTransition((PetrinetNode) edgeA.getSource())
						&& !isInvisibleTransition((PetrinetNode) edgeA.getTarget())) {
					edgesOnlyInOneModel.add(edgeA);
				}
			}
		}

		return edgesOnlyInOneModel;
	}

	protected Set<Arc> getArcsOnlyInOneModel(Set<Arc> arcsModela, Set<Arc> arcsModelB,
			Map<PetrinetNode, PetrinetNode> mappingsAB) {

		Set<Arc> edgesOnlyInOneModel = new HashSet<Arc>();

		// search for edges within a but not in b
		for (Arc edgeA : arcsModela) {

			PetrinetNode source = (PetrinetNode) edgeA.getSource();
			PetrinetNode dest = (PetrinetNode) edgeA.getTarget();

			PetrinetNode mappedSource = mappingsAB.get(source);
			PetrinetNode mappedDest = mappingsAB.get(dest);

			if (mappedSource != null || mappedDest != null) {
				boolean found = false;

				for (Arc edgeB : arcsModelB) {
					if (edgeB.getSource().equals(mappedSource) && edgeB.getTarget().equals(mappedDest)) {
						found = true;
						break;
					}
				}

				if (!found) {
					edgesOnlyInOneModel.add(edgeA);
				}
			}
		}

		return edgesOnlyInOneModel;
	}

	/**
	 * Creates a mapping of nodes in one model to nodes in another model based
	 * on identical names. This method can only generate 1:1 mappings.
	 * 
	 * @param from
	 *            the from-model
	 * @param to
	 *            the to-model
	 * @return the mapping of nodes from model from to model to
	 * @throws Exception
	 */
	protected Map<PetrinetNode, PetrinetNode> getMapping(PetrinetGraph from, PetrinetGraph to) {
		Map<PetrinetNode, PetrinetNode> mapping = new HashMap<PetrinetNode, PetrinetNode>();

		for (PetrinetNode vertexA : from.getNodes()) {
			if (vertexA.getLabel() != null) {
				for (PetrinetNode vertexB : to.getNodes()) {
					if (vertexB.getLabel() != null) {
						if (vertexA.getLabel().equals(vertexB.getLabel())) {
							mapping.put(vertexA, vertexB);
							break;
						}
					}
				}
			}
		}
		return mapping;
	}

	/**
	 * Return all labels used in the given model graph.
	 * 
	 * @param graph
	 *            the model-representing graph *
	 * 
	 * @return all labels used in the given model graph
	 */
	protected Set<String> getLabels(PetrinetGraph graph) {
		return getLabels(graph, true, true);
	}

	/**
	 * Return all labels used in the given model graph.
	 * 
	 * @param graph
	 *            the model-representing graph
	 * @param exclusions
	 *            a collection of element types that must not be used as labeled
	 *            elements
	 * 
	 * @return all labels used in the given model graph
	 */
	protected Set<String> getLabels(PetrinetGraph graph, boolean onlyConsiderTransitions,
			boolean onlyConsiderVisibleTasks) {
		Set<String> labels = new HashSet<String>();

		for (PetrinetNode vertex : getLabeledElements(graph, onlyConsiderTransitions, onlyConsiderVisibleTasks)) {
			labels.add(vertex.getLabel());
		}

		return labels;
	}

	/**
	 * Returns the vertex with the given label. If no such vertex exists
	 * <code>null</code> is returned.
	 * 
	 * @param graph
	 *            the graph to search in
	 * @param label
	 *            the label to search for
	 * 
	 * @return the vertex with the given label or null
	 */
	protected PetrinetNode getNode(PetrinetGraph graph, String label) {
		for (PetrinetNode vertex : graph.getNodes()) {
			if (label.equals(vertex.getLabel())) {
				return vertex;
			}
		}

		return null;
	}

	/**
	 * Return all elements of the model that have a non-empty label.
	 * 
	 * @param graph
	 *            the model-representing graph
	 * @param exclusions
	 *            a collection of element types that must not be used as labeled
	 *            elements
	 * 
	 * @return elements with a non-empty label
	 */
	protected Set<PetrinetNode> getLabeledElements(PetrinetGraph graph, boolean onlyConsiderTransitions,
			boolean onlyConsiderVisibleTransitions) {
		Set<PetrinetNode> getNodesLabeled = new HashSet<PetrinetNode>();
		for (PetrinetNode node : graph.getNodes()) {
			boolean excluded = false;
			if (onlyConsiderTransitions) {
				if (!(node instanceof Transition))
					excluded = true;
				else if (onlyConsiderVisibleTransitions) {
					Transition trans = (Transition) node;
					if (trans.isInvisible())
						excluded = true;
				}

			}

			if (!excluded) {
				if ((null == node.getLabel()) || node.getLabel().equals("")) {
					continue;
				} else {
					getNodesLabeled.add(node);
				}
			}
		}

		return getNodesLabeled;
	}

	protected boolean isInvisibleTransition(PetrinetNode node) {
		return node instanceof Transition && ((Transition) node).isInvisible();
	}

	/**
	 * Iterates over all elements of a model graph and returns true, if the
	 * graph contains an element with the specified label. Additionally, it is
	 * possible to define certain types of element that have to be searched
	 * (e.g. only {@link EPCFunction}s). possible to
	 * 
	 * @param model
	 * @param label
	 * @param elementTypes
	 * @return
	 */
	protected boolean containsElement(PetrinetGraph model, String label, Class<? extends PetrinetNode> elementType) {
		if (null == elementType) {
			elementType = PetrinetNode.class;
		}

		boolean found = false;

		for (PetrinetNode vertex : model.getNodes()) {
			if (elementType.isAssignableFrom(vertex.getClass())) {
				if (null != vertex && vertex.getLabel().equals(label)) {
					found = true;
					break;
				}
			}

		}

		return found;
	}

	// protected Set<PetrinetNode> getNextTransitions(Set<PetrinetNode> nodes,
	// PetrinetNode actual,
	// boolean considerOnlyVisible) {
	//
	// for (PetrinetEdge edge : actual.getGraph().getOutEdges(actual)) {
	// PetrinetNode target = (PetrinetNode) edge.getTarget();
	//
	// boolean toAdd = false;
	//
	// if (target instanceof Transition) {
	//
	// if (considerOnlyVisible && isInvisibleTransition(target)) {
	// toAdd = false;
	// } else {
	// toAdd = true;
	// }
	//
	// }
	//
	// if (toAdd) {
	// nodes.add(target);
	// } else {
	//
	// if (target.getGraph().getOutEdges(target).size() > 0)
	// getNextTransitions(nodes, target, considerOnlyVisible);
	// }
	// }
	// return nodes;
	// }

	// protected Set<PetrinetNode> getNextTransitions(Set<PetrinetNode> nodes,
	// PetrinetNode actual,
	// boolean considerOnlyVisible) {
	//
	// for (PetrinetEdge edge : actual.getGraph().getOutEdges(actual)) {
	// PetrinetNode target = (PetrinetNode) edge.getTarget();
	//
	// if (target instanceof Transition) {
	// nodes.add(target);
	// } else {
	// if (target.getGraph().getOutEdges(target).size() > 0)
	// getNextTransitions(nodes, target, considerOnlyVisible);
	// }
	// }
	// return nodes;
	// }

	/**
	 * Returns the transitive closure of the predecessors of a given vertex
	 * along the line of the node type given by the cont-class.
	 * 
	 * @param vertex
	 *            starting vertex
	 * @param cont
	 *            class to continue with search, all nodes of other classes will
	 *            be added to the set of predecessors
	 * 
	 * @return the transitive closure of predecessors of the given vertex
	 */
	protected Set<PetrinetNode> getTransitiveClosurePredecessors(PetrinetNode vertex) {
		return getTransitiveClosurePredecessors(vertex, new HashSet<PetrinetNode>(), 0, 10);
	}

	/**
	 * Returns the transitive closure of the successors of a given vertex along
	 * the line of the node type given by the cont-class.
	 * 
	 * @param vertex
	 *            starting vertex
	 * @param cont
	 *            class to continue with search, all nodes of other classes will
	 *            be added to the set of predecessors
	 * 
	 * @return the transitivie closure of successors of the given vertex
	 */
	protected Set<PetrinetNode> getTransitiveClosureSuccessors(PetrinetNode vertex) {
		return getTransitiveClosureSuccessors(vertex, new HashSet<PetrinetNode>(), 0, 10);
	}

	protected Set<PetrinetNode> getPredecessors(PetrinetNode vertex) {
		Set<PetrinetNode> predecessors = new HashSet<PetrinetNode>();
		for (Object edgeObject : vertex.getGraph().getInEdges(vertex)) {
			PetrinetEdge<PetrinetNode, PetrinetNode> inEdge = (PetrinetEdge) edgeObject;
			predecessors.add(inEdge.getSource());
		}

		return predecessors;
	}

	protected Set<PetrinetNode> getSuccessors(PetrinetNode vertex) {
		Set<PetrinetNode> successors = new HashSet<PetrinetNode>();
		for (Object edgeObject : vertex.getGraph().getOutEdges(vertex)) {
			PetrinetEdge<PetrinetNode, PetrinetNode> outEdge = (PetrinetEdge) edgeObject;
			successors.add(outEdge.getTarget());
		}
		return successors;
	}

	private Set<PetrinetNode> getTransitiveClosurePredecessors(PetrinetNode vertex, Set<PetrinetNode> predecessors,
			int depth, int maxDepth) {
		if (depth < maxDepth) {
			for (Object edgeObject : vertex.getGraph().getInEdges(vertex)) {
				PetrinetEdge<PetrinetNode, PetrinetNode> inEdge = (PetrinetEdge) edgeObject;
				predecessors.add(inEdge.getSource());
			}
		}

		return predecessors;
	}

	private Set<PetrinetNode> getTransitiveClosureSuccessors(PetrinetNode vertex, Set<PetrinetNode> successors,
			int dept, int maxDepth) {
		if (dept < maxDepth) {
			for (Object edgeObject : vertex.getGraph().getOutEdges(vertex)) {
				PetrinetEdge<PetrinetNode, PetrinetNode> outEdge = (PetrinetEdge) edgeObject;
				successors.add(outEdge.getTarget());
			}
		}
		return successors;
	}

}
