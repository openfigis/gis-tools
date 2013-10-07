package org.fao.fi.gis.mappings.eez;

import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.JsonObject;


/**
 * Main app to process the EEZ mappings
 * 
 * @author eblondel
 *
 */
public class MappingsApp {
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		EezMappingsProcessor processor = new EezMappingsProcessor();
		JsonObject result = processor.execute();
		writeToJson(result, "c:/gis/eez/mappings_result_20131004.json");
		
	}
	
	/** Method to write a Json record to a Json file (final result)
	 * 
	 * @param data
	 * @param outputFile
	 * @return the Json file name
	 */
	private static String writeToJson(JsonObject data, String outputFile){
		try {

	    	FileWriter writer;
			writer = new FileWriter(outputFile);
			writer.write(data.toString());
			writer.close();

			return outputFile;
		
    	} catch (IOException e) {
			throw new RuntimeException (e);
		}
	}
}
