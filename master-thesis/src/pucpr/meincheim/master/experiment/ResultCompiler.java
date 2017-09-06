package pucpr.meincheim.master.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pucpr.meincheim.master.util.CsvUtils;

public class ResultCompiler {

	private String resultsDirectory;

	public static void main(String[] args) {
		String resultsDirectory = "C:\\Users\\alexme\\Dropbox\\Mestrado em Informática - PUCPR\\Process Mining\\2017 - Process Mining - Dissertação\\Experiment\\Results";

		// resultsDirectory = "D:\\Dropbox\\Dropbox\\Mestrado em Informática -
		// PUCPR\\Process Mining\\2017 - Process Mining - Dissertação";

		// Complete
		compileFile(resultsDirectory, "", "Precision");
		compileFile(resultsDirectory, "", "Precision");
		compileFile(resultsDirectory, "", "Precision");

		// By Thresholds
		compileFile(resultsDirectory, "0.4", "Precision");
		compileFile(resultsDirectory, "0.6", "Precision");
		compileFile(resultsDirectory, "0.8", "Precision");

		// By database
		compileFile(resultsDirectory, "HospitalLog", "Precision");
		compileFile(resultsDirectory, "RoadTraffic", "Precision");
		compileFile(resultsDirectory, "Financial", "Precision");

	}

	private static void compileFile(String resultsDirectory, String view, String qualityMetric) {
		List<String[]> all = new ArrayList<String[]>();

		for (String filePath : getFilePaths(resultsDirectory, view, qualityMetric)) {
			List<String[]> lines = CsvUtils.readCSV(filePath);
			for (String[] csvLine : lines) {
				String[] value = new String[2];
				value[0] = filePath.split(";")[1];
				value[1] = csvLine[0];
				all.add(value);
			}
		}

		String fileName = String.format("\\Compiled %s %s", view, qualityMetric);
		CsvUtils.exportCompiledFile(all, resultsDirectory + fileName);

	}

	private static List<String> getFilePaths(String directory, String pattern, String qualityMetric) {
		List<String> paths = new ArrayList<String>();
		File[] files = new File(directory).listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName().contains(pattern) && file.getName().contains(qualityMetric)) {
				paths.add(file.getAbsolutePath());
			}
		}
		return paths;
	}
}
