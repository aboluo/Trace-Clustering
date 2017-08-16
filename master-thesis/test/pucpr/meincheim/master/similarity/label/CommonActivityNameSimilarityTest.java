package pucpr.meincheim.master.similarity.label;


import org.junit.Assert;
import org.junit.Test;

import pucpr.meincheim.master.base.BaseTest;

public class CommonActivityNameSimilarityTest extends BaseTest {

	private static CommonActivityNameSimilarity sim  = new CommonActivityNameSimilarity();

	@Test
	public void calculateTest() {
		double result = sim.calculateSimilarity(model0, model0);
		Assert.assertEquals(1, result, 0);
						
		result = sim.calculateSimilarity(complexModel, complexModel);
		Assert.assertEquals(1, result, 0);
			
		result = sim.calculateSimilarity(model0, model1);
	
	}

}
