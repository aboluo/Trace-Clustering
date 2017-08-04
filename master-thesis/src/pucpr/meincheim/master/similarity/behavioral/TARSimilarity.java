package pucpr.meincheim.master.similarity.behavioral;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.processmining.converting.epc2transitionsystem.EPCMarking;
import org.processmining.converting.epc2transitionsystem.EpcToTransitionSystem;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.models.transitionsystem.TransitionSystemEdge;
import org.processmining.framework.models.transitionsystem.TransitionSystemVertexSet;
import org.processmining.mining.instancemining.ModelGraphResult;

import att.grappa.Edge;
import att.grappa.Node;
import de.caterdev.phd.similarity.measures.AbstractModelGraphSimilarityMeasure;

/**
 * Similarity according to Zha et al.: A workflow new similarity measure based
 * on transition adjacency relations.
 * 
 * A transition adjacency relation (TAR) of a process models represents direct
 * succession-relations of two activities with each other. The TAR is defined
 * such that a pair (a1, a2) of two activities belongs to TAR, iff the model has
 * a trace of the form <... a1, a2, ...>. Let TAR0 be the TAR-relation for model
 * M0 and TAR1 be the TAR-relation for model M1. The TAR similarity between M0
 * and M1 is defined as sim(M0, M1) = |intersection(TAR0,TAR1)| /
 * |union(TAR0,TAR1)|.
 * 
 * @author Michael Becker
 */
public class TARSimilarity extends AbstractModelGraphSimilarityMeasure
{

    public double calculateSimilarity(ModelGraph a, ModelGraph b) throws Exception
    {
        ModelGraph aGraph = convertEPCtoProcessGraph(a);
        ModelGraph bGraph = convertEPCtoProcessGraph(b);
        
        Set<TAR> tarSetA = getTARSet(aGraph);
        Set<TAR> tarSetB = getTARSet(bGraph);
        
        // System.out.println(tarSetA);
        
        Set<TAR> intersection = new HashSet<TAR>(tarSetA);
        intersection.retainAll(tarSetB);
        Set<TAR> union = new HashSet<TAR>(tarSetA);
        union.addAll(tarSetB);
        
        return (double) intersection.size() / (double) union.size();
    }
    
    private void buildStates(Node node, Set<List<TransitionSystemEdge>> states, List<TransitionSystemEdge> state, Set<Edge> visited, int depth, int maxDept)
    {
        if (maxDept < depth)
        {
            return;
        }
        
        if (null != node.getOutEdges())
        {
            for (Edge edge : node.getOutEdges())
            {
                if (visited.contains(edge))
                {
                    depth++;
                }
                
                visited.add(edge);
            }
            if (1 == node.getOutEdges().size())
            {
                for (Edge edge : node.getOutEdges())
                {
                    state.add((TransitionSystemEdge) edge);
                    buildStates(edge.getHead(), states, state, visited, depth, maxDept);
                }
            }
            else if (1 < node.getOutEdges().size())
            {
                for (Edge edge : node.getOutEdges())
                {
                    List<TransitionSystemEdge> newState = new LinkedList<TransitionSystemEdge>(state);
                    newState.add((TransitionSystemEdge) edge);
                    states.add(newState);
                    buildStates(edge.getHead(), states, newState, visited, depth, maxDept);
                }
            }
        }
    }
    
    private Set<TAR> getTarSet(ConfigurableEPC epc)
    {
        Set<TAR> tarSet = new HashSet<TAR>();
        
        ModelGraphResult transSystem = getTransitionSystem(epc);
        TransitionSystem result = (TransitionSystem) transSystem.getProvidedObjects()[0].getObjects()[0];
        
        ModelGraphVertex start = null;
        ModelGraphVertex end = null;
        boolean startfound = false, endfound = false;
        for (ModelGraphVertex vertex : result.getVerticeList())
        {
            if (0 == vertex.getPredecessors().size())
            {
                start = vertex;
                startfound = true;
            }
            else if (0 == vertex.getSuccessors().size())
            {
                end = vertex;
                endfound = true;
            }
            
            if (startfound && endfound)
            {
                break;
            }
        }
        
        // System.out.println(start.getIdentifier() + ", " +
        // end.getIdentifier());
        
        Set<Edge> visited = new HashSet<Edge>();
        Set<List<TransitionSystemEdge>> states = new HashSet<List<TransitionSystemEdge>>();
        List<TransitionSystemEdge> state = new LinkedList<TransitionSystemEdge>();
        states.add(state);
        
        buildStates(start, states, state, visited, 0, 15);
        
        // System.out.println(states);
        
        Set<List<String>> stateSpace = new HashSet<List<String>>();
        for (List<TransitionSystemEdge> actual : states)
        {
            if (actual.get(actual.size() - 1).getHead().equals(end))
            {
                List<String> actualSpace = new LinkedList<String>();
                for (TransitionSystemEdge edge : actual)
                {
                    // System.out.println("checking edge " + edge + ", " +
                    // edge.getIdentifier());
                    if (!edge.getIdentifier().startsWith("xor") && !edge.getIdentifier().startsWith("or") && !edge.getIdentifier().startsWith("and"))
                    {
                        actualSpace.add(edge.getIdentifier());
                    }
                }
                
                stateSpace.add(actualSpace);
            }
        }
        
        for (List<String> actual : stateSpace)
        {
            for (int i = 0; i < actual.size() - 1; i++)
            {
                String startNode = actual.get(i);
                String endNode = actual.get(i + 1);
                
                tarSet.add(new TAR(startNode, endNode));
            }
        }
        
        return tarSet;
    }
    
    private Set<TAR> getTARSet(ModelGraph graph)
    {
        Set<TAR> tarSet = new HashSet<TARSimilarity.TAR>();
        
        for (Object edgeObject : graph.getEdges())
        {
            ModelGraphEdge edge = (ModelGraphEdge) edgeObject;
            
            tarSet.add(new TAR(edge.getSource().getIdentifier(), edge.getDest().getIdentifier()));
        }
        
        return tarSet;
    }
    
    /**
     * This method has been copied from
     * {@link EpcToTransitionSystem#getTransitionSystem(ConfigurableEPC)}.
     * However, we removed the line showWarningDialog(checkSoundness(TS,Epc)) to
     * get rid of the dialog box.
     * 
     * @param Epc
     * @return
     */
    private ModelGraphResult getTransitionSystem(ConfigurableEPC Epc)
    {
        EpcToTransitionSystem transConv = new EpcToTransitionSystem();
        TransitionSystem TS = new TransitionSystem(Epc.getIdentifier());
        Stack<EPCMarking> interMarkings = new Stack<EPCMarking>();
        EPCMarking currentMarking;
        ArrayList<EPCMarking> newMarkings;
        interMarkings.addAll(transConv.getStartMarkings(Epc));
        // Message.add("Set of start markings is " + interMarkings.size());
        int count = 0;
        while (!interMarkings.isEmpty())
        {
            currentMarking = interMarkings.pop();
            // Message.add("Current Marking " + currentMarking.toString());
            newMarkings = currentMarking.nextMarkings(TS);
            for (EPCMarking mark : newMarkings)
            {
                if (TS.containsVertex(new TransitionSystemVertexSet(mark.toString(), TS)) == null)
                {
                    interMarkings.push(mark);
                }
            }
            count++;
        }
        
        return new ModelGraphResult(TS);
    }
    
    private class TAR
    {
        protected String a;
        protected String b;
        
        public TAR(String a, String b)
        {
            this.a = a;
            this.b = b;
        }
        
        @Override
        public boolean equals(Object arg0)
        {
            if (arg0 instanceof TAR)
            {
                return ((TAR) arg0).a.equals(a) && ((TAR) arg0).b.equals(b);
            }
            else
            {
                return false;
            }
        }
        
        @Override
        public int hashCode()
        {
            return a.hashCode() + b.hashCode();
        }
        
        @Override
        public String toString()
        {
            return "(" + a + ", " + b + ")";
        }
    }
}
