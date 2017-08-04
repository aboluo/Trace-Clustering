package pucpr.meincheim.master.similarity.label;

import java.util.HashMap;
import java.util.Map;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;

import de.caterdev.phd.similarity.measures.AbstractModelGraphSimilarityMeasure;
import de.caterdev.phd.similarity.measures.ISimilarityMeasure;
import de.caterdev.phd.similarity.measures.modelgraphvertex.LevenshteinVertexSimilarity;

/**
 * Similarity between process models according to Huang et al.: An algorithm for
 * calculating process similarity to cluster open-source process designs.
 * 
 * In the first iteration, similarity between nodes is establish based on
 * similarity between web services. In our implementation we use
 * {@link LevenshteinVertexSimilarity} as a default. However, every other vertex
 * based similarity measure can be used, too. Based on this, similarity between
 * two edges (A1,A2) and (B1,B2) is calculated as follows:
 * <code>sim((A1,A2),(B1,B2)) = (sim(A1,A2) + sim(B1,B2))/2</code>.
 * 
 * Overall similarity between nodes of a model M0 and nodes of a model M1 is
 * calculated by summing up the individual node similarities and dividing this
 * sum by the overall amount of nodes in both process models. To calculate
 * similarity between edges, edges are assigned with weights. In our
 * implementation, we give an XOR-edge a weight of 1/n where n is the amount of
 * outgoing nodes. All over weights are left untouched, since the paper of Huang
 * et al. does not concretise the approach.
 * 
 * @author Michael Becker
 * 
 */
public class NodeLinkBasedSimilarity extends AbstractModelGraphSimilarityMeasure
{
    public static final ISimilarityMeasure<ModelGraphVertex> DEFAULT_NODE_SIMILARITY = new LevenshteinVertexSimilarity();
    
    protected ISimilarityMeasure<ModelGraphVertex> nodeSimilarity;
    
    public NodeLinkBasedSimilarity()
    {
        this(DEFAULT_NODE_SIMILARITY);
    }
    
    public NodeLinkBasedSimilarity(ISimilarityMeasure<ModelGraphVertex> nodeSimilarity)
    {
        this.nodeSimilarity = nodeSimilarity;
    }
    

    public double calculateSimilarity(ModelGraph a, ModelGraph b) throws Exception
    {
        ModelGraph modelA = convertEPCtoProcessGraph(a);
        ModelGraph modelB = convertEPCtoProcessGraph(b);
        
        Map<ModelGraphVertex, Map<ModelGraphVertex, Double>> nodeSimilaritiesAB = new HashMap<ModelGraphVertex, Map<ModelGraphVertex, Double>>();
        Map<ModelGraphVertex, Map<ModelGraphVertex, Double>> nodeSimilaritiesBA = new HashMap<ModelGraphVertex, Map<ModelGraphVertex, Double>>();
        
        Map<ModelGraphVertex, Map<ModelGraphVertex, Double>> maxNodeSimilaritiesAB = calculateMaxNodeSimilarities(modelA, modelB, nodeSimilaritiesAB);
        Map<ModelGraphVertex, Map<ModelGraphVertex, Double>> maxNodeSimilaritiesBA = calculateMaxNodeSimilarities(modelB, modelA, nodeSimilaritiesBA);
        
        // System.out.println(maxNodeSimilaritiesAB);
        // System.out.println(maxNodeSimilaritiesBA);
        
        Map<ModelGraphEdge, Map<ModelGraphEdge, Double>> maxEdgeSimilaritiesAB = calculateMaxEdgeSimilarities(modelA, modelB, nodeSimilaritiesAB);
        Map<ModelGraphEdge, Map<ModelGraphEdge, Double>> maxEdgeSimilaritiesBA = calculateMaxEdgeSimilarities(modelB, modelA, nodeSimilaritiesBA);
        
        Map<ModelGraphEdge, Double> edgeWeightsA = calculateEdgeWeights(modelA);
        Map<ModelGraphEdge, Double> edgeWeightsB = calculateEdgeWeights(modelB);
        
        // System.out.println(edgeWeightsA);
        // System.out.println(edgeWeightsB);
        
        double simNodeSum = calculateSimNodeSum(maxNodeSimilaritiesAB) + calculateSimNodeSum(maxNodeSimilaritiesBA);
        double simEdgeSum = calculateSimEdgeSum(maxEdgeSimilaritiesAB, edgeWeightsA, edgeWeightsB) + calculateSimEdgeSum(maxEdgeSimilaritiesBA, edgeWeightsB, edgeWeightsA);
        
        double nodeSim = simNodeSum / ((double) modelA.getVerticeList().size() + (double) modelB.getVerticeList().size());
        double edgeSim = simEdgeSum / ((double) modelA.getEdges().size() + (double) modelB.getEdges().size());
        
        // System.out.println(edgeWeightsA);
        // System.out.println(edgeWeightsB);
        //
        // System.out.println(maxNodeSimilaritiesAB);
        // System.out.println(maxNodeSimilaritiesBA);
        //
        // System.out.println(maxEdgeSimilaritiesAB);
        //
        // System.out.println(simNodeSum);
        // System.out.println(nodeSim);
        // System.out.println(simEdgeSum);
        
        return 1.0 / 2.0 * nodeSim + 1.0 / 2.0 * edgeSim;
    }
    
    /**
     * Calculates the weight for all edges in the given model. An xor-edge (with
     * style "X") gets a weight of 1 / amount of outgoing cases.
     * 
     * @param model
     *            the model to calculate the edge weights for
     * @return the weights of edges of the given model
     */
    private Map<ModelGraphEdge, Double> calculateEdgeWeights(ModelGraph model)
    {
        Map<ModelGraphEdge, Double> edgeWeights = new HashMap<ModelGraphEdge, Double>();
        
        for (Object edgeObject : model.getEdges())
        {
            ModelGraphEdge edge = (ModelGraphEdge) edgeObject;
            if ((null != edge.getStyle()) && edge.getStyle().equals("X"))
            {
                int cases;
                if (1 < edge.getSource().getOutEdges().size())
                {
                    cases = edge.getSource().getOutEdges().size();
                }
                else
                {
                    cases = edge.getDest().getInEdges().size();
                }
                
                edgeWeights.put(edge, 1.0 / cases);
            }
            else
            {
                edgeWeights.put(edge, 1.0);
            }
        }
        
        return edgeWeights;
    }
    
    private Map<ModelGraphEdge, Map<ModelGraphEdge, Double>> calculateMaxEdgeSimilarities(ModelGraph a, ModelGraph b, Map<ModelGraphVertex, Map<ModelGraphVertex, Double>> nodeSimilarities) throws Exception
    {
        Map<ModelGraphEdge, Map<ModelGraphEdge, Double>> edgeSimilarities = new HashMap<ModelGraphEdge, Map<ModelGraphEdge, Double>>();
        Map<ModelGraphEdge, Map<ModelGraphEdge, Double>> maxEdgeSimilarities = new HashMap<ModelGraphEdge, Map<ModelGraphEdge, Double>>();
        
        for (Object edgeObjectA : a.getEdges())
        {
            ModelGraphEdge edgeA = (ModelGraphEdge) edgeObjectA;
            
            double maxSim = 0;
            ModelGraphEdge mappedEdge = null;
            Map<ModelGraphEdge, Double> similaritiesA = new HashMap<ModelGraphEdge, Double>();
            
            for (Object edgeObjectB : b.getEdges())
            {
                ModelGraphEdge edgeB = (ModelGraphEdge) edgeObjectB;
                
                double simSrc = nodeSimilarities.get(edgeA.getSource()).get(edgeB.getSource());
                double simDest = nodeSimilarities.get(edgeA.getDest()).get(edgeB.getDest());
                double simEdge = (simSrc + simDest) / 2.0;
                
                similaritiesA.put(edgeB, simEdge);
                
                if (simEdge > maxSim)
                {
                    maxSim = simEdge;
                    mappedEdge = edgeB;
                }
            }
            edgeSimilarities.put(edgeA, similaritiesA);
            
            if (null != mappedEdge)
            {
                Map<ModelGraphEdge, Double> maxEdgeSimilarity = new HashMap<ModelGraphEdge, Double>();
                maxEdgeSimilarity.put(mappedEdge, maxSim);
                maxEdgeSimilarities.put(edgeA, maxEdgeSimilarity);
            }
        }
        
        return maxEdgeSimilarities;
    }
    
    private Map<ModelGraphVertex, Map<ModelGraphVertex, Double>> calculateMaxNodeSimilarities(ModelGraph a, ModelGraph b, Map<ModelGraphVertex, Map<ModelGraphVertex, Double>> nodeSimilarities) throws Exception
    {
        Map<ModelGraphVertex, Map<ModelGraphVertex, Double>> maxNodeSimilarities = new HashMap<ModelGraphVertex, Map<ModelGraphVertex, Double>>();
        
        for (ModelGraphVertex vertexA : a.getVerticeList())
        {
            ModelGraphVertex mappedVertex = null;
            Map<ModelGraphVertex, Double> similaritiesA = new HashMap<ModelGraphVertex, Double>();
            
            double maxSim = 0;
            for (ModelGraphVertex vertexB : b.getVerticeList())
            {
                double simAB = nodeSimilarity.calculateSimilarity(vertexA, vertexB);
                
                similaritiesA.put(vertexB, simAB);
                
                if (simAB > maxSim)
                {
                    maxSim = simAB;
                    mappedVertex = vertexB;
                }
            }
            nodeSimilarities.put(vertexA, similaritiesA);
            
            if (null != mappedVertex)
            {
                Map<ModelGraphVertex, Double> maxNodeSimilarity = new HashMap<ModelGraphVertex, Double>();
                maxNodeSimilarity.put(mappedVertex, maxSim);
                maxNodeSimilarities.put(vertexA, maxNodeSimilarity);
            }
        }
        
        return maxNodeSimilarities;
    }
    
    private double calculateSimEdgeSum(Map<ModelGraphEdge, Map<ModelGraphEdge, Double>> edgeSimilarities, Map<ModelGraphEdge, Double> edgeWeightsA, Map<ModelGraphEdge, Double> edgeWeightsB)
    {
        double simSum = 0;
        
        for (ModelGraphEdge edgeA : edgeSimilarities.keySet())
        {
            double weightA = edgeWeightsA.get(edgeA);
            
            for (ModelGraphEdge edgeB : edgeSimilarities.get(edgeA).keySet())
            {
                double weightB = edgeWeightsB.get(edgeB);
                
                double sim = edgeSimilarities.get(edgeA).get(edgeB);
                
                simSum += weightA * weightB * sim;
            }
        }
        
        // System.out.println("es1: " + simSum);
        
        return simSum;
    }
    
    private double calculateSimNodeSum(Map<ModelGraphVertex, Map<ModelGraphVertex, Double>> nodeSimilarities)
    {
        double simSum = 0;
        
        for (ModelGraphVertex vertexA : nodeSimilarities.keySet())
        {
            for (ModelGraphVertex vertexB : nodeSimilarities.get(vertexA).keySet())
            {
                simSum += nodeSimilarities.get(vertexA).get(vertexB);
            }
        }
        
        return simSum;
    }
}