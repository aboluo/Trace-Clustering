package pucpr.meincheim.master.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;

import com.google.common.collect.Iterables;

public class LogUtils {

	private static XFactoryBufferedImpl factory = new XFactoryBufferedImpl();

	public static XLog createNewLog(XTrace trace) {
		XLog log = factory.createLog();
		log.add(trace);
		return log;
	}

	public static XLog addTrace(XLog log, XTrace trace) {
		log.add(trace);
		return log;
	}

	public static XLog loadByFile(File source) {
		XLog log = null;
		Collection<XLog> logs = null;
		try {
			XParser parser = new XesXmlParser();
			if (parser.canParse(source)) {
				System.out.println("Using input parser: " + parser.name());
				logs = parser.parse(source);

				if (logs.size() > 0)
					log = Iterables.get(logs, 0);
			}

		} catch (Exception e) {
			System.out.println("Cannot parse log file");
		}

		return log;
	}

	public static void xesExport(XLog log, String path) throws FileNotFoundException, IOException {
		if (!path.contains(".xes"))
			path = path + ".xes";
		XesXmlSerializer out = new XesXmlSerializer();
		out.serialize(log, new FileOutputStream(path));
	}

}
