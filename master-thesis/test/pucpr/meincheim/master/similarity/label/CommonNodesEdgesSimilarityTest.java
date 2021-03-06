package pucpr.meincheim.master.similarity.label;


import org.junit.Assert;
import org.junit.Test;

import pucpr.meincheim.master.base.BaseTest;

public class CommonNodesEdgesSimilarityTest extends BaseTest {

	private static CommonNodesEdgesSimilarity sim  = new CommonNodesEdgesSimilarity();

	@Test
	public void calculateTest() {
		double result = sim.calculateSimilarity(model0, model0);
		Assert.assertEquals(1, result, 0);
						
		result = sim.calculateSimilarity(complexModel, complexModel);
		Assert.assertEquals(1, result, 0);
			
		result = sim.calculateSimilarity(model0, model1);
		System.out.println(result);
	}

}
