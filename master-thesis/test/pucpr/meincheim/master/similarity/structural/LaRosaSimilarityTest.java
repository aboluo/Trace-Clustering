package pucpr.meincheim.master.similarity.structural;

import org.junit.Assert;
import org.junit.Test;

import pucpr.meincheim.master.base.BaseTest;
import pucpr.meincheim.master.similarity.structural.LaRosaSimilarity;

public class LaRosaSimilarityTest extends BaseTest {

	private static LaRosaSimilarity sim = new LaRosaSimilarity();

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
