package pucpr.meincheim.master.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pucpr.meincheim.master.quality.ModelQuality;

public class CsvUtils {

	private final static String CRLF = "\r\n";
	private static String delimiter = ",";

	public void qualityExportCsv(List<ModelQuality> clusterQualities, String filename) {
		List<Double> precision = clusterQualities.stream().map(ModelQuality::getPrecision).collect(Collectors.toList());
		exportFile(precision, filename + " Precision");

		List<Double> recall = clusterQualities.stream().map(ModelQuality::getRecall).collect(Collectors.toList());
		exportFile(recall, filename + " Recall");
	}

	public void exportFile(List<Double> values, String filename) {
		filename = filename + ".csv";
		try {
			FileWriter writer = new FileWriter(filename);
			for (Double value : values) {
				writer.append(value.toString());
				writer.append(CRLF);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void exportCompiledFile(List<String[]> values, String filename) {
		filename = filename + ".csv";
		try {
			FileWriter writer = new FileWriter(filename);
			for (String[] columns : values) {
				for (String column : columns) {
					writer.append(column);
					writer.append(delimiter);
				}
				writer.append(CRLF);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String[]> readCSV(String filePath) {
		List<String[]> lines = new ArrayList<String[]>();
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(filePath));
			while ((line = br.readLine()) != null) {
				lines.add(line.split(delimiter));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return lines;
	}
}
