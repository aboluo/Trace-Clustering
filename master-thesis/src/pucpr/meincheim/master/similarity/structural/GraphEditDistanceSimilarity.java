package pucpr.meincheim.master.similarity.structural;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;

import de.caterdev.phd.similarity.measures.AbstractModelGraphSimilarityMeasure;
import de.caterdev.phd.similarity.measures.ISimilarityMeasure;
import de.caterdev.phd.similarity.measures.modelgraphvertex.LevenshteinSimilarity;
import de.caterdev.phd.similarity.measures.modelgraphvertex.LevenshteinVertexSimilarity;

/**
 * Graph Edit Distance Similarity according to Dijkman et al.: Graph Matching
 * Algorithms for Business Process Similarity Search
 * 
 * The measure is based on the graph-representation of business process models
 * where connector/gateway nodes are removed. Similarity is calculated based on
 * the amount of necessary transformation operations:
 * <ul>
 * <li>Node Substitution</li>
 * <li>Node Insertion / Deletion</li>
 * <li>Edge Insertion / Deletion</li>
 * </ul>
 * 
 * There is a cost function for every transformation. The cost of substituting
 * node n1 with node n2 is calculated by 1 - sim(n1, n2). The default similarity
 * metric for node similarity is {@link LevenshteinSimilarity}.
 * 
 * Similarity of models M1, M2 with activities A1, A2 and edges E1, E2 is then
 * calculated based on the set of substituted nodes subn, inserted/deleted nodes
 * skipn, and inserted/deleted edges skipe as follows: fskipn = |skipn|/(|A1| +
 * |A2|), fskipe = |skipe|/(|E1| + |E2|), fsubn = 2 * sum(1 - sim(a1,a2))
 * 
 * sim(M1, M2) = (wskipn*fskipn + wskipe*fskipe + wsubn*fsubn) / (wskipn +
 * wskipe + wsubn)
 * 
 * @author Michael Becker
 * 
 */
public class GraphEditDistanceSimilarity extends AbstractModelGraphSimilarityMeasure
{
    protected static final ISimilarityMeasure<ModelGraphVertex> DEFAULT_SIM_STRATEGY = new LevenshteinVertexSimilarity();
    
    protected static final double DEFAULT_WSKIPN = 1;
    protected static final double DEFAULT_WSKIPE = 1;
    protected static final double DEFAULT_WSUBN = 1;
    
    protected double wskipn;
    protected double wskipe;
    protected double wsubn;
    protected ISimilarityMeasure<ModelGraphVertex> similarityStrategy;
    
    public GraphEditDistanceSimilarity()
    {
        this(DEFAULT_SIM_STRATEGY, DEFAULT_WSKIPN, DEFAULT_WSKIPE, DEFAULT_WSUBN);
    }
    
    public GraphEditDistanceSimilarity(ISimilarityMeasure<ModelGraphVertex> similarityStrategy, double wskipn, double wskipe, double wsubn)
    {
        this.similarityStrategy = similarityStrategy;
        this.wskipn = wskipn;
        this.wskipe = wskipe;
        this.wsubn = wsubn;
    }
    

    public double calculateSimilarity(ModelGraph a, ModelGraph b) throws Exception
    {
        ModelGraph aGraph = convertEPCtoProcessGraph(a);
        ModelGraph bGraph = convertEPCtoProcessGraph(b);
        
        Map<ModelGraphVertex, ModelGraphVertex> mappingsAB = new HashMap<ModelGraphVertex, ModelGraphVertex>();
        Map<ModelGraphVertex, ModelGraphVertex> mappingsBA = new HashMap<ModelGraphVertex, ModelGraphVertex>();
        Map<ModelGraphVertex, Map<ModelGraphVertex, Double>> similarities = new HashMap<ModelGraphVertex, Map<ModelGraphVertex, Double>>();
        
        for (ModelGraphVertex vertexA : aGraph.getVerticeList())
        {
            double maxSim = 0;
            ModelGraphVertex match = null;
            for (ModelGraphVertex vertexB : bGraph.getVerticeList())
            {
                double simAB = similarityStrategy.calculateSimilarity(vertexA, vertexB);
                
                if (simAB > maxSim)
                {
                    if (null != similarities.get(vertexA))
                    {
                        similarities.get(vertexA).clear();
                    }
                    else
                    {
                        similarities.put(vertexA, new HashMap<ModelGraphVertex, Double>());
                    }
                    
                    similarities.get(vertexA).put(vertexB, simAB);
                    mappingsAB.put(vertexA, vertexB);
                    
                    maxSim = simAB;
                    match = vertexB;
                }
            }
            
            if (null != match)
            {
                mappingsBA.put(match, vertexA);
            }
        }
        
        Set<ModelGraphVertex> deletedVertices = new HashSet<ModelGraphVertex>();
        Set<ModelGraphVertex> addedVertices = new HashSet<ModelGraphVertex>();
        
        // calculate the set of deleted vertices
        for (ModelGraphVertex vertex : aGraph.getVerticeList())
        {
            if (!mappingsAB.containsKey(vertex))
            {
                deletedVertices.add(vertex);
            }
        }
        
        // calculate the set of added vertices
        for (ModelGraphVertex vertex : bGraph.getVerticeList())
        {
            if (!mappingsAB.containsValue(vertex))
            {
                addedVertices.add(vertex);
            }
        }
        
        // calculate the set of deleted and added edges
        Set<ModelGraphEdge> deletedEdges = getEdgesOnlyInOneModel(aGraph, bGraph, mappingsAB);
        Set<ModelGraphEdge> addedEdges = getEdgesOnlyInOneModel(bGraph, aGraph, mappingsBA);
        
        double simDist = 0.0;
        for (ModelGraphVertex vertexA : similarities.keySet())
        {
            for (ModelGraphVertex vertexB : similarities.get(vertexA).keySet())
            {
                simDist += 1.0 - similarities.get(vertexA).get(vertexB);
            }
        }
        
        double fskipn = 0;
        double fskipe = 0;
        double fsubn = 0;
        if (aGraph.getVerticeList().size() > 0 && bGraph.getVerticeList().size() > 0)
        {
            fskipn = (double) (deletedVertices.size() + addedVertices.size()) / (double) (aGraph.getVerticeList().size() + bGraph.getVerticeList().size());
        }
        if (aGraph.getEdges().size() > 0 && bGraph.getEdges().size() > 0)
        {
            fskipe = (double) (deletedEdges.size() + addedEdges.size()) / (double) (aGraph.getEdges().size() + bGraph.getEdges().size());
        }
        if (aGraph.getVerticeList().size() > 0 && bGraph.getVerticeList().size() > 0)
        {
            fsubn = 2.0 * simDist / (aGraph.getVerticeList().size() + bGraph.getVerticeList().size() - deletedVertices.size() - addedVertices.size());
        }
        
        return 1.0 - (wskipn * fskipn + wskipe * fskipe + wsubn * fsubn) / (wskipn + wskipe + wsubn);
    }
}
