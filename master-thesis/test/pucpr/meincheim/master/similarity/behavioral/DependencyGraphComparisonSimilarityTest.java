package pucpr.meincheim.master.similarity.behavioral;


import org.junit.Assert;
import org.junit.Test;

import pucpr.meincheim.master.base.BaseTest;
import pucpr.meincheim.master.similarity.behavioral.DependencyGraphComparisonSimilarity;

public class DependencyGraphComparisonSimilarityTest extends BaseTest {

	private static DependencyGraphComparisonSimilarity sim  = new DependencyGraphComparisonSimilarity();

	@Test
	public void calculateTest() {
				
		double result = sim.calculateSimilarity(model0, model0);
		Assert.assertEquals(1, result, 0);
						
		result = sim.calculateSimilarity(complexModel, complexModel);
		Assert.assertEquals(1, result, 0);
			
		result = sim.calculateSimilarity(model0, model1);
	
	}

}
