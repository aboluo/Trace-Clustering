package pucpr.meincheim.master.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import pucpr.meincheim.master.quality.ModelQuality;

public class CsvWriter {

	private final String CRLF = "\r\n";
	private String delimiter = ",";

	public void qualityExportCsv(List<ModelQuality> clusterQualities, String filename) {
		try {

			FileWriter writer = new FileWriter(filename);
			writer.append(headerBuilder());
			writer.append(delimiter + CRLF);

			for (ModelQuality quality : clusterQualities) {
				writer.append(quality.getModelName())
					  .append(delimiter)
					  .append(String.valueOf(quality.getPrecision()))
					  .append(delimiter)
					  .append(String.valueOf(quality.getRecall()))
					  .append(delimiter)
					  .append(String.valueOf(quality.getSimplicit()))
					  .append(delimiter)
					  .append(String.valueOf(quality.getGeneralization()));
				writer.append(delimiter + CRLF);
				
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String headerBuilder() {
		StringBuilder header = new StringBuilder();
		return header.append("Id")
				     .append(delimiter)
				     .append("Precision")
				     .append(delimiter)
				     .append("Recall")
				     .append(delimiter)
				     .append("Simplicity")
				     .append(delimiter)
				     .append("Generalization").toString();
	}

}
