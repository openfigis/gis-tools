package org.fao.fi.gis.mappings.eez;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * Processor to establish sovereignty & exploitation rights EEZ relationships.
 * 
 * Notes:
 * - Sovereignty relationship: refers to concept of Country
 * - Exploitation rights relationship: refers to concept of Flagstate
 * 
 * Limitations
 * - The processor relies on JSP FLOD services to get current
 * content. A Java FLOD API should be use instead in future
 * 
 * - The processor provides an adhoc way to produce relationships deltas (ie those FLOD
 *  has to update because they changed). Future enhancements should consider top-level
 * interface to give such kind of update
 * 
 * @author eblondel
 * 
 */
public class EezMappingsProcessor {
	
	
	private static String FLOD_FLAGSTATE_SERVICE = "http://www.fao.org/figis/flod/askflod/json/isflagstate.jsp?code=";
	private static String wfsGetCap = "http://geo.vliz.be/geoserver/ows?service=WFS&version=1.0.0&request=GetCapabilities";
	
	private static String FLOD_SOV_SERVICE = "http://www.fao.org/figis/flod/askflod/json/eez4c.jsp?code=";
	private static String FLOD_EXPL_SERVICE = "http://www.fao.org/figis/flod/askflod/json/eez4fs.jsp?code=";
	
	
	private static String EEZ_NAME = "country";
	private static String EEZ_LONGNAME = "eez";
	private static String EEZ_ISO3 = "iso_3digit";
	private static String EEZ_ID = "eez_id";
	private static String SOV_NAME = "sovereign";
	
	private static String FLOD_FLAGSTATE_BASEURI = "http://www.fao.org/figis/flod/entities/flagstatecode";
	private static String FLOD_EEZ_BASEURI = "http://www.fao.org/figis/flod/entities/eezcode";
	
	
	private SimpleFeatureSource featureSource;
	private Map<String,List<String>> codelist;
	
	/**
	 * Constructs a MappingsProcessor
	 * 
	 * @throws IOException
	 */
	public EezMappingsProcessor() throws IOException{
		this.setFeatureSource();
		this.setCountryCodeList();
	};
	
	
	/**
	 * Set the feature source
	 * 
	 * @throws IOException
	 */
	private void setFeatureSource() throws IOException{
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(WFSDataStoreFactory.URL.key, wfsGetCap);
		params.put(WFSDataStoreFactory.TIMEOUT.key, new Integer(60000));
		params.put(WFSDataStoreFactory.FILTER_COMPLIANCE.key, new Integer(0));
		
		DataStore datastore = DataStoreFinder.getDataStore(params);
		SimpleFeatureSource featureSource = datastore.getFeatureSource("MarineRegions:eez");
		this.featureSource = featureSource;
	}
	
	/**
	 * Get the feature source
	 * 
	 * @return
	 */
	public SimpleFeatureSource getFeatureSource(){
		return this.featureSource;
	}
	
	/**
	 * Get adhoc codelist (iso3code, name) built from the VLIZ EEZ layer
	 * The iso3code is set as value, the name as key, as for some EEZs
	 * the iso3code was used for several entries.
	 * 
	 * This codelist is only an intermediary bridge to build the EEZs
	 * mappings.
	 * 
	 * @return
	 * @throws IOException
	 */
	private void setCountryCodeList() throws IOException{
		Map<String,List<String>> codelist = new HashMap<String,List<String>>();
		
		SimpleFeatureIterator it = this.getFeatureSource().getFeatures().features();
		try{
			while(it.hasNext()){
				SimpleFeature sf = it.next();
				String eezIso = (String) sf.getAttribute(EEZ_ISO3);
				String eezName = (String) sf.getAttribute(EEZ_NAME);
				if(eezIso != null){
					if(!codelist.keySet().contains(eezIso)){
						List<String> names = new ArrayList<String>();
						names.add(eezName);
						codelist.put(eezIso, names);
					}else{
						List<String> names = codelist.get(eezIso);
						if(!names.contains(eezName)){
							names.add(eezName);
							codelist.put(eezIso, names);
						}
					}
				}
			}
		}finally{
			if(it != null){
				it.close();
			}
		}
		
		this.codelist = codelist;
	}
	
	/**
	 * Get the adhoc EEZ codelist
	 * 
	 * @return
	 */
	public Map<String,List<String>> getCodeList(){
		return this.codelist;
	}
	
	/**
	 * execute
	 * 
	 * @return
	 * @throws Exception
	 */
	public JsonObject execute(Boolean flodLookup, Boolean includeErrors) throws Exception{
		
		//prepare output
		final JsonObject root = new JsonObject();
		final JsonObject response = new JsonObject();
		final JsonObject value = new JsonObject();
		final JsonArray items = new JsonArray();
		value.add("items", items);
		response.add("value", value);		
		root.add("response", response);
		
		Iterator<String> iterator = this.getCodeList().keySet().iterator();
		while(iterator.hasNext()){
			Country cnt = new Country(iterator.next(), featureSource, codelist);
			JsonObject output = cnt.buildJsonOutput(flodLookup, includeErrors);
			if(output != null){
				items.add(output);	
			}
		}

		return root;
	}
	
	
	
	
	/**
	 * Country object
	 * The "country" refers here to the ISO3 code definition.
	 * 
	 *
	 */
	static class Country {

		
		private String code;
		private SimpleFeatureSource featureSource;
		private Map<String, List<String>> codelist;

		
		FilterFactory ff = CommonFactoryFinder.getFilterFactory();
		
		
		/**
		 * Constructs a country object to build EEZs mappings
		 * 
		 * @param iso3code
		 * @param featureSource
		 */
		public Country(String code, SimpleFeatureSource source, Map<String,List<String>> codelist){
			this.code = code;
			this.featureSource = source;
			this.codelist = codelist;
		}
		
		/**
		 * Get the ISO3 code
		 * 
		 * @return
		 */
		public String getCode(){
			return this.code;
		}
		
		/**
		 * Get the adhoc VLIZ country codelist
		 * 
		 * @return
		 */
		public Map<String,List<String>> getCodeList(){
			return this.codelist;
		}
		
		/**
		 * Get source sub feature collection
		 * 
		 * @return
		 * @throws IOException
		 */
		public SimpleFeatureCollection getSourceCollection() throws IOException{
			
			List<Filter> filters = new ArrayList<Filter>();
			
			//add eez filter
			Filter eezFilter = ff.equal(ff.property(EEZ_ISO3),
					ff.literal(this.getCode()), true);
			filters.add(eezFilter);
			
			//add filters from codelist keys (possible names)
			Iterator<String> it = this.getCodeList().keySet().iterator();
			while(it.hasNext()){
				String iso3code = it.next();
				if(iso3code.equals(this.getCode())){
					List<String> names = this.getCodeList().get(iso3code);
					for(String name : names){
						filters.add(ff.equal(ff.property(SOV_NAME), ff.literal(name), true));
					}
				}
			}
			Filter orFilter = ff.or(filters);
			return this.featureSource.getFeatures(orFilter);
		}

		
		/**
		 * Get the list of EEZs (identifiers) on which the country (as flagstate) has
		 * FISHERY EXPLOITATION rights
		 * 
		 * @return
		 * @throws Exception 
		 */
		public Map<String, String> getEEZExploitationRights() throws Exception{
			Map<String, String> result = new HashMap<String,String>();
			
			if(this.getSourceCollection().size() > 0){
				SimpleFeatureIterator it = this.getSourceCollection().features();	
				try{
					while(it.hasNext()){
						SimpleFeature sf = it.next();
						
						String eezIso = (String) sf.getAttribute(EEZ_ISO3);
						if(eezIso !=null){
							if(eezIso.equals(this.getCode())){
								//main EEZ
								if(isFlagState(this.getCode())){
									String eezId = sf.getAttribute(EEZ_ID).toString();
									String eez = (String) sf.getAttribute(EEZ_LONGNAME);
									
									if(!result.keySet().contains(eezId)){
										result.put(eezId, eez);
									}
								}
							}else{
								//other EEZs
								String sovName = (String) sf.getAttribute(SOV_NAME);
								if(this.getCodeList().get(this.getCode()).contains(sovName) && !isFlagState(eezIso)){
									String eezId = sf.getAttribute(EEZ_ID).toString();
									String eez = (String) sf.getAttribute(EEZ_LONGNAME);
									if(!result.keySet().contains(eezId)){
										result.put(eezId, eez);
									}
								}		
							}
						}
	
					}
				}finally{
					if(it != null){
						it.close();
					}
				}
			}

			return result;
		}
		
		
		/**
		 * Get the list of EEZs (identifiers) on which the country has
		 * SOVEREIGNTY rights
		 * 
		 * @return
		 * @throws IOException 
		 */
		public Map<String, String> getEEZSovereigntyRights() throws IOException{
			Map<String, String> result = new HashMap<String,String>();
			
			if(this.getSourceCollection().size() > 0){
				SimpleFeatureIterator it = this.getSourceCollection().features();
				try{
					while(it.hasNext()){
						SimpleFeature sf = it.next();
						
						String sovName = (String) sf.getAttribute(SOV_NAME);
	
						if(this.getCodeList().get(this.getCode()).contains(sovName)){
							String eezId = sf.getAttribute(EEZ_ID).toString();
							String eez = (String) sf.getAttribute(EEZ_LONGNAME);
							if(!result.keySet().contains(eezId)){
								result.put(eezId, eez);
							}
						}	
					}
				}finally{
					if(it != null){
						it.close();
					}
				}
			}
			return result;
		}
		
		
		/**
		 * Builds the overall result mapping as JSON output
		 * 
		 * @return
		 * @throws Exception 
		 */
		public JsonObject buildJsonOutput(Boolean flodLookup, Boolean includeErrors) throws Exception{
			
			JsonObject mapping = null;
			String error = null;
			boolean hasError = false;
			
			//add messages
			if (this.getCode().length() < 3) {
				error = this.getCode()+" is not an ISO3 code. Action required at source level";
				hasError = true;
				
			} else if (this.getCode().length() > 3 || this.getCode().contains("-")) {
				error = this.getCode()+ " is not an ISO3 code. Composite EEZ detected. Action required at source level";
				hasError = true;
				
			}
			
			if(!hasError){
	
				mapping = new JsonObject();
				
				List<String> sovCurrentList = getCurrentSovereigntyRights(this.getCode());
				Map<String, String> sovEEZs = this.getEEZSovereigntyRights();
				List<String> sovNewList = getURIs(FLOD_EEZ_BASEURI, sovEEZs);
				boolean addSovMapping = (sovCurrentList.size() != sovNewList.size() && !sovCurrentList.containsAll(sovNewList));
				
				List<String> expCurrentList = getCurrentExploitationRights(this.getCode());
				Map<String, String> expEEZs = this.getEEZExploitationRights();
				List<String> expNewList = getURIs(FLOD_EEZ_BASEURI, expEEZs);
				boolean addExpMapping = (expCurrentList.size() != expNewList.size() && !expCurrentList.containsAll(sovNewList));
				
				//add flagstate iso3 URI
				if(flodLookup){
					if(addSovMapping || addExpMapping){
						String flagstateURI = FLOD_FLAGSTATE_BASEURI +"/"+ this.getCode().toLowerCase();
						mapping.addProperty("uri", flagstateURI);
					}
				}else{
					String flagstateURI = FLOD_FLAGSTATE_BASEURI +"/"+ this.getCode().toLowerCase();
					mapping.addProperty("uri", flagstateURI);
				}
				
				//add SOVEREIGNTY mapping			
				if(flodLookup){
					if(addSovMapping){		
						JsonArray eezList = new JsonArray();
						for(String uri : sovNewList){
							JsonObject eezObj = new JsonObject();
							eezObj.addProperty("uri", uri);
							eezObj.addProperty("name", sovEEZs.get(uri.split(FLOD_EEZ_BASEURI+"/")[1]));
							eezList.add(eezObj);
						}
						mapping.add("sovereigntyRights", eezList);
					}
					
					//add EXPLOITATION RIGHTS mapping
					if(addExpMapping){
						JsonArray eezList = new JsonArray();
						for(String uri : expNewList){
							JsonObject eezObj = new JsonObject();
							eezObj.addProperty("uri", uri);
							eezObj.addProperty("name", expEEZs.get(uri.split(FLOD_EEZ_BASEURI+"/")[1]));
							eezList.add(eezObj);

						}
						mapping.add("exploitationRights", eezList);
					}
				}else{
					if(sovEEZs.size() > 0){		
						JsonArray eezList = new JsonArray();
						for(String uri : sovNewList){
							JsonObject eezObj = new JsonObject();
							eezObj.addProperty("uri", uri);
							eezObj.addProperty("name", sovEEZs.get(uri.split(FLOD_EEZ_BASEURI+"/")[1]));
							eezList.add(eezObj);
						}
						mapping.add("sovereigntyRights", eezList);
					}else{
						mapping.add("sovereigntyRights", new JsonArray());
					}
					
					//add EXPLOITATION RIGHTS mapping
					if(expEEZs.size() > 0){
						JsonArray eezList = new JsonArray();
						for(String uri : expNewList){
							JsonObject eezObj = new JsonObject();
							eezObj.addProperty("uri", uri);
							eezObj.addProperty("name", expEEZs.get(uri.split(FLOD_EEZ_BASEURI+"/")[1]));
							eezList.add(eezObj);

						}
						mapping.add("exploitationRights", eezList);
					}else{
						mapping.add("exploitationRights", new JsonArray());
					}
				}
				
				
			}else{
				if(includeErrors){
					mapping = new JsonObject();
					mapping.addProperty("warning", error);
				}
			}
			
			return mapping;
		}
		
		

	}
	

	/**
	 * Returns true if the country is considered as flagstate from a
	 * statistical/reporting POV
	 * 
	 * @return true if flagstate, otherwise false
	 * @throws Exception
	 */
	public static Boolean isFlagState(String iso3code) throws Exception{
		
		Boolean result = null;
		JsonReader reader = null;

		//read Json data
		URL dataURL = new URL(FLOD_FLAGSTATE_SERVICE + iso3code);
		reader = new JsonReader(new InputStreamReader(dataURL.openStream()));
		JsonParser parser = new JsonParser();
		JsonObject flodJsonObject = parser.parse(reader).getAsJsonObject();
			
		result = flodJsonObject.get("value").getAsBoolean();
		reader.close();

		
		return result;
	}
	
	
	/**
	 * Look to the current FLOD EEZ relationships content
	 * 
	 * @param code
	 * @return
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private static List<String> getCurrentEEZRelationships(String service, String code) throws IOException, InterruptedException{
		List<String> result = new ArrayList<String>();
		
		//read Json data
		URL dataURL = new URL(service + code);
		JsonReader reader = new JsonReader(new InputStreamReader(dataURL.openStream()));
		JsonParser parser = new JsonParser();
		JsonObject flodJsonObject = parser.parse(reader).getAsJsonObject();
		
		JsonArray bindings = flodJsonObject
				.get("results").getAsJsonObject()
				.get("bindings").getAsJsonArray();
		
		Iterator<JsonElement> it = bindings.iterator();
		while(it.hasNext()){
			JsonObject obj = (JsonObject) it.next();
			result.add(obj.get("eezCode").getAsJsonObject().get("value").toString());
		}
		
		reader.close();
		Thread.sleep(2000);
		
		return result;
	}
	
	
	/**
	 * Provide the current Exploitation Rights
	 * 
	 * @param code (
	 * @return the list of exploited EEZs
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static List<String> getCurrentExploitationRights(String code) throws IOException, InterruptedException{
		return getCurrentEEZRelationships(FLOD_EXPL_SERVICE, code);
	}
	
	
	/**
	 * 
	 * 
	 * @param code
	 * @return the list of EEZs
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static List<String> getCurrentSovereigntyRights(String code) throws IOException, InterruptedException{
		return getCurrentEEZRelationships(FLOD_SOV_SERVICE, code);
	}
	
	
	/**
	 * Get list of URIs from mapping
	 * 
	 * @param baseuri
	 * @param mapping
	 * @return
	 */
	public static List<String> getURIs(String baseuri, Map<String,String> mapping){
		List<String> result = new ArrayList<String>();
		Iterator<String> it = mapping.keySet().iterator();
		while(it.hasNext()){
			result.add(baseuri +"/"+ it.next());
		}
		return result;
	}
	
}
