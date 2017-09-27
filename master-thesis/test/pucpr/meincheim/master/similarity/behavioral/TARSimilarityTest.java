package pucpr.meincheim.master.similarity.behavioral;


import org.junit.Assert;
import org.junit.Test;

import pucpr.meincheim.master.base.BaseTest;

public class TARSimilarityTest extends BaseTest {

	private static TARSimilarity sim  = new TARSimilarity();

	@Test
	public void calculateTest() {
		double result = sim.calculateSimilarity(model0, model0);
		Assert.assertEquals(1, result, 0);

		result = sim.calculateSimilarity(model0, model1);

		result = sim.calculateSimilarity(model0, model2);
	
		result = sim.calculateSimilarity(complexModel, complexModel);
		Assert.assertEquals(1, result, 0);
	}

}
