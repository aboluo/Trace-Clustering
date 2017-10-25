package pucpr.meincheim.master.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pucpr.meincheim.master.util.CsvUtils;

public class ResultCompiler {

	private String resultsDirectory;

	public static void main(String[] args) {

		// SYNTETIC

		String resultsDirectory = "C:\\Users\\alexme\\Dropbox\\Mestrado em Informática - PUCPR\\Process Mining\\2017 - Process Mining - Dissertação\\Experiment\\Quality Results\\Syntetic";

		// Complete
		complete(resultsDirectory);

		// By Thresholds
		byThresholds("0.4", resultsDirectory);
		byThresholds("0.6", resultsDirectory);
		byThresholds("0.8", resultsDirectory);

		// By database
		byDatabase("log-0-percent-noise", resultsDirectory);
		byDatabase("log-30-percent-noise", resultsDirectory);

		// By database and threshold
		byDatabaseandThresholds("log-0-percent-noise", resultsDirectory);
		byDatabaseandThresholds("log-30-percent-noise", resultsDirectory);
		
		// REAL LIFE
		resultsDirectory = "C:\\Users\\alexme\\Dropbox\\Mestrado em Informática - PUCPR\\Process Mining\\2017 - Process Mining - Dissertação\\Experiment\\Quality Results\\RealLife";

		// Complete
		complete(resultsDirectory);

		// By Thresholds
		byThresholds("0.4", resultsDirectory);
		byThresholds("0.6", resultsDirectory);
		byThresholds("0.8", resultsDirectory);

		// By database
		byDatabase("01Hospital_log", resultsDirectory);
		byDatabase("02Hospital Billing - Event Log", resultsDirectory);
		byDatabase("03CoSeLoG", resultsDirectory);
		byDatabase("04Sepsis Cases", resultsDirectory);
		byDatabase("05BPI_Challenge2012", resultsDirectory);
		byDatabase("06Road_Traffic_Fine_Management_Process", resultsDirectory);

		// By database and threshold
		byDatabaseandThresholds("01Hospital_log", resultsDirectory);
		byDatabaseandThresholds("02Hospital Billing - Event Log", resultsDirectory);
		byDatabaseandThresholds("03CoSeLoG", resultsDirectory);
		byDatabaseandThresholds("04Sepsis Cases", resultsDirectory);
		byDatabaseandThresholds("05BPI_Challenge2012", resultsDirectory);
		byDatabaseandThresholds("06Road_Traffic_Fine_Management_Process", resultsDirectory);

	}

	private static void byDatabaseAndThreshold(String databaseName, String resultsDirectory) {
		compileFile(resultsDirectory, databaseName, "Precision", "0.4");
		compileFile(resultsDirectory, databaseName, "Recall", "0.4");
		compileFile(resultsDirectory, databaseName, "FScore", "0.4");
		compileFile(resultsDirectory, databaseName, "Simplicity", "0.4");

		compileFile(resultsDirectory, databaseName, "Precision", "0.6");
		compileFile(resultsDirectory, databaseName, "Recall", "0.6");
		compileFile(resultsDirectory, databaseName, "FScore", "0.6");
		compileFile(resultsDirectory, databaseName, "Simplicity", "0.6");

		compileFile(resultsDirectory, databaseName, "Precision", "0.8");
		compileFile(resultsDirectory, databaseName, "Recall", "0.8");
		compileFile(resultsDirectory, databaseName, "FScore", "0.8");
		compileFile(resultsDirectory, databaseName, "Simplicity", "0.8");
	}

	private static void complete(String resultsDirectory) {
		compileFile(resultsDirectory, "", "Precision", "");
		compileFile(resultsDirectory, "", "Recall", "");
		compileFile(resultsDirectory, "", "FScore", "");
		compileFile(resultsDirectory, "", "Simplicity", "");
	}

	private static void byDatabase(String databaseName, String resultsDirectory) {
		compileFile(resultsDirectory, databaseName, "Precision", "");
		compileFile(resultsDirectory, databaseName, "Recall", "");
		compileFile(resultsDirectory, databaseName, "FScore", "");
		compileFile(resultsDirectory, databaseName, "Simplicity", "");
	}

	private static void byDatabaseandThresholds(String databaseName, String resultsDirectory) {
		compileFile(resultsDirectory, databaseName, "Precision", "0.4");
		compileFile(resultsDirectory, databaseName, "Recall", "0.4");
		compileFile(resultsDirectory, databaseName, "FScore", "0.4");
		compileFile(resultsDirectory, databaseName, "Simplicity", "0.4");

		compileFile(resultsDirectory, databaseName, "Precision", "0.6");
		compileFile(resultsDirectory, databaseName, "Recall", "0.6");
		compileFile(resultsDirectory, databaseName, "FScore", "0.6");
		compileFile(resultsDirectory, databaseName, "Simplicity", "0.6");

		compileFile(resultsDirectory, databaseName, "Precision", "0.8");
		compileFile(resultsDirectory, databaseName, "Recall", "0.8");
		compileFile(resultsDirectory, databaseName, "FScore", "0.8");
		compileFile(resultsDirectory, databaseName, "Simplicity", "0.8");
	}

	private static void byThresholds(String threshold, String resultsDirectory) {
		compileFile(resultsDirectory, "", "Precision", threshold);
		compileFile(resultsDirectory, "", "Recall", threshold);
		compileFile(resultsDirectory, "", "FScore", threshold);
		compileFile(resultsDirectory, "", "Simplicity", threshold);
	}

	private static void compileFile(String resultsDirectory, String database, String qualityMetric, String threshold) {
		List<String[]> all = new ArrayList<String[]>();

		for (String filePath : getFilePaths(resultsDirectory, database, qualityMetric, threshold)) {
			List<String[]> lines = CsvUtils.readCSV(filePath);
			for (String[] csvLine : lines) {
				String[] value = new String[2];
				value[0] = filePath.split(";")[1];
				value[1] = csvLine[0];
				all.add(value);
			}
		}

		String fileName = String.format("\\Compiled %s %s", database, qualityMetric);
		CsvUtils.exportCompiledFile(all, resultsDirectory + fileName);

	}

	private static List<String> getFilePaths(String directory, String pattern, String qualityMetric, String threshold) {
		List<String> paths = new ArrayList<String>();
		File[] files = new File(directory).listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName().contains(pattern) && file.getName().contains(qualityMetric)
					&& file.getName().contains(threshold)) {
				paths.add(file.getAbsolutePath());
			}
		}
		return paths;
	}
}
