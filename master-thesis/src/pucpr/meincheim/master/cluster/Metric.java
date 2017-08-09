package pucpr.meincheim.master.cluster;

import java.io.IOException;
import java.util.List;

import pucpr.meincheim.master.quality.AyraQualityEvaluator;

public class Metric {

	public static void createClusterMetrics(List<Cluster> cluster) throws IOException {

		int count = 0;
		System.out.println("-----------");
		System.out.println("Cluster" + ";Log size ");
		
//		for (Cluster centroide : cluster) {
//			
//			System.out.println(count + ";" + centroide.getLog().size());
//
//			QualityEvaluator evaluator = new QualityEvaluator(centroide.getLog(), centroide.getModel());
//			System.out.println("Fitness " + evaluator.calculateRecall());
//			System.out.println("Generalization " + evaluator.calculateGeneralization());
//			System.out.println("Simplicity " + evaluator.calculateSimplicity());
//			System.out.println("Precision " + evaluator.calculatePrecision());
//
//			count++;
//		}

	}

}
