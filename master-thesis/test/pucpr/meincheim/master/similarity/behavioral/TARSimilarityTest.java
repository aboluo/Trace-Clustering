package pucpr.meincheim.master.similarity.behavioral;

import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Test;

import pucpr.meincheim.master.base.BaseTest;

public class TARSimilarityTest extends BaseTest {

	private static TARSimilarity sim = new TARSimilarity();

	@Test
	public void calculateTest() {
		double result = sim.calculateSimilarity(model0, model0);
		Assert.assertEquals(10 / 10, result, 0);

		result = sim.calculateSimilarity(model0, model1);
		Assert.assertEquals(6.0 / 13.0, result, 0);

		result = sim.calculateSimilarity(model0, model2);
		Assert.assertEquals(6.0 / 12.0, Precision.round(result, 4), 0);

		result = sim.calculateSimilarity(complexModel, complexModel);
		Assert.assertEquals(1, result, 0);
	}

}
