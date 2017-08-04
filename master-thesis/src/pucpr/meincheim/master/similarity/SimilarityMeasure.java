package pucpr.meincheim.master.similarity;

public interface SimilarityMeasure<T>
{
    public double calculateSimilarity(T a, T b);
}
