package pucpr.meincheim.master.similarity.label;


import org.junit.Test;

import pucpr.meincheim.master.base.BaseTest;

public class NodeLinkBasedSimilarityTest extends BaseTest {

	private static NodeLinkBasedSimilarity sim  = new NodeLinkBasedSimilarity();

	@Test
	public void calculateTest() {
		double result = sim.calculateSimilarity(model0, model0);
		System.out.println(result);

		result = sim.calculateSimilarity(model0, model1);
		System.out.println(result);

		result = sim.calculateSimilarity(model0, model2);
		System.out.println(result);

		result = sim.calculateSimilarity(complexModel, complexModel);
		System.out.println(result);
	}
}
