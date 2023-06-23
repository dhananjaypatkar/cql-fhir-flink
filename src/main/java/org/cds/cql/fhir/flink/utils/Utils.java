package org.cds.cql.fhir.flink.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class Utils {

	private static List<String> readAllAsList(final String path) {
		final List<String> out = new ArrayList<String>();
		Collection<File> files = FileUtils.listFiles(new File(path), new String[] { "cql" ,"json"},
				false);
		if (!files.isEmpty()) {
			files.forEach(f -> {
				try {
					out.add(FileUtils.readFileToString(f,Charset.defaultCharset()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

		}
		return out;
	}

	public static List<String> loadValuesets(final String path) {
		return readAllAsList(path);
	}

	public static List<String> loadMeasure(final String path) {
		return readAllAsList(path);
	}
	
	
	/*public static void main(String[] args) {
		List<String> cqlLib =   Utils.loadValuesets("C:\\work\\workspace\\cql\\cql.fhir\\cql\\");
		List<String> valueSets  =   Utils.loadValuesets("C:\\work\\workspace\\cql\\cql.fhir\\valuesets\\");
		
		List<String> pats  =   Utils.loadValuesets("C:\\work\\workspace\\cql\\cql.fhir\\input\\");
		System.out.println("Loaded valuesets "+valueSets.size() +" loaded measures "+cqlLib.size());
		
		Engine engine = new Engine("EXM124_FHIR4", "8.2.000",valueSets, cqlLib); //version '8.2.000'
		try {
			engine.run(pats.get(0));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/
}
