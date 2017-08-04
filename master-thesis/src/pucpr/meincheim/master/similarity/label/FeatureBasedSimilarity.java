package pucpr.meincheim.master.similarity.label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;

import de.caterdev.phd.similarity.measures.AbstractModelGraphSimilarityMeasure;
import de.caterdev.phd.similarity.measures.ISimilarityMeasure;
import de.caterdev.phd.similarity.measures.modelgraphvertex.LevenshteinSimilarity;
import de.caterdev.phd.similarity.measures.modelgraphvertex.RoleFeatureSimilarity;

/**
 * Similarity according to Yan et al.: Fast Business Process Similarity Search
 * with Feature-Based Similarity Estimation.
 * 
 * The similarity is calculated using the following features
 * <ul>
 * <li>Label Feature: String Edit Distance Similarity between element labels</li>
 * <li>Role Feature: Similarity based on the role of element (start, stop,
 * split, join, regular (sequential elements)</li>
 * </ul>
 * 
 * The similarity is only applicable in the graph representation of models,
 * where activities are used as connectors, too. Therefore, a given process
 * model is converted into its graphical representation removing non-task
 * elements like gateways/connectors or events in EPCs.
 * 
 * @author Michael Becker
 * 
 */
public class FeatureBasedSimilarity extends AbstractModelGraphSimilarityMeasure
{
    public static final ISimilarityMeasure<String> DEFAULT_LABEL_SIM = new LevenshteinSimilarity();
    public static final ISimilarityMeasure<ModelGraphVertex> DEFAULT_ROLE_SIM = new RoleFeatureSimilarity();
    
    public static final double DEFAULT_LCUTOFF_HIGH = 0.8;
    public static final double DEFAULT_RCUTOFF = 1.0;
    public static final double DEFAULT_LCUTOFF_MED = 0.2;
    
    protected ISimilarityMeasure<String> labelFeatureSimilarity;
    protected ISimilarityMeasure<ModelGraphVertex> roleFeatureSimilarity;
    
    protected double lCutoffHigh;
    protected double rCutoff;
    protected double lCutoffMed;
    
    public FeatureBasedSimilarity()
    {
        this(DEFAULT_LABEL_SIM, DEFAULT_ROLE_SIM, DEFAULT_LCUTOFF_HIGH, DEFAULT_RCUTOFF, DEFAULT_LCUTOFF_MED);
    }
    
    public FeatureBasedSimilarity(ISimilarityMeasure<String> labelFeatureSimilarity, ISimilarityMeasure<ModelGraphVertex> roleFeatureSimilarity, double lCutoffHigh, double rCutoff, double lCutoffMed)
    {
        this.labelFeatureSimilarity = labelFeatureSimilarity;
        this.roleFeatureSimilarity = roleFeatureSimilarity;
        
        this.lCutoffHigh = lCutoffHigh;
        this.rCutoff = rCutoff;
        this.lCutoffMed = lCutoffMed;
    }
    
    
    public double calculateSimilarity(ModelGraph a, ModelGraph b) throws Exception
    {
        ModelGraph aGraph = convertEPCtoProcessGraph(a);
        ModelGraph bGraph = convertEPCtoProcessGraph(b);
        
        Map<ModelGraphVertex, Set<ModelGraphVertex>> matches = new HashMap<ModelGraphVertex, Set<ModelGraphVertex>>();
        
        for (ModelGraphVertex vertexA : aGraph.getVerticeList())
        {
            Set<ModelGraphVertex> matchesA = new HashSet<ModelGraphVertex>();
            boolean hasMatch = false;
            
            for (ModelGraphVertex vertexB : bGraph.getVerticeList())
            {
                double lsimAB = labelFeatureSimilarity.calculateSimilarity(vertexA.getIdentifier(), vertexB.getIdentifier());
                double rsimAB = roleFeatureSimilarity.calculateSimilarity(vertexA, vertexB);
                
                // two nodes are matched, if their labels are similar to a high
                // degree or if their role and label are similar to a medium
                // degree
                if ((lsimAB >= lCutoffHigh) || ((rsimAB >= rCutoff) && (lsimAB >= lCutoffMed)))
                {
                    matchesA.add(vertexB);
                    hasMatch = true;
                }
            }
            
            if (hasMatch)
            {
                matches.put(vertexA, matchesA);
            }
        }
        
        int matchedInA = matches.keySet().size();
        int matchedInB = matches.values().size();
        int total = aGraph.getVerticeList().size() + bGraph.getVerticeList().size();
        
        return (double) (matchedInA + matchedInB) / (double) total;
    }
    
}
