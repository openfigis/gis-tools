package org.fao.fi.gis.mappings.metadata;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import net.opengis.wms.v_1_3_0.Identifier;
import net.opengis.wms.v_1_3_0.Layer;
import net.opengis.wms.v_1_3_0.MetadataURL;
import net.opengis.wms.v_1_3_0.WMSCapabilities;


/**
 * This class aims to give the 1st business logic to query the mapping between
 * FLOD codedentity URI & GIS OGC metadata URI.
 * 
 * @author Emmanuel Blondel <emmanuel.blondel@fao.org><emmanuel.blondel1@gmail.com>
 * 
 */
public class MetadataMapper {

	private String baseUrl;
	private String workspace;
	private WMSCapabilities wmsCapabilities;

	/**
	 * Constructs the Metadata mapper
	 * 
	 * @param baseUrl
	 * @param ws
	 * @throws JAXBException 
	 * @throws MalformedURLException
	 */
	public MetadataMapper(String baseUrl, String ws) throws JAXBException{
		this.baseUrl = baseUrl;
		this.workspace = ws;
		this.setUp();
	}

	private void setUp() throws JAXBException{
		JAXBContext context = JAXBContext
				.newInstance("net.opengis.wms.v_1_3_0");
		Unmarshaller unmarshaller = context.createUnmarshaller();
		
		String url = this.baseUrl + "/" + this.workspace
				+ "/ows?service=WMS&version=1.3.0&request=GetCapabilities";

		JAXBElement<WMSCapabilities> wmsCapabilitiesElement = unmarshaller
				.unmarshal(new StreamSource(url), WMSCapabilities.class);
		this.wmsCapabilities = (WMSCapabilities) wmsCapabilitiesElement
				.getValue();
	}
	
	
	/**
	 * Get the mappings between coded entity URI & GIS metadata URI
	 * 
	 * @return
	 */
	public Map<String, Map<LayerProperty,String>> getMappings(String authority) {
		Map<String, Map<LayerProperty,String>> results = null;

		List<Layer> layers = wmsCapabilities.getCapability().getLayer().getLayer();
		if(layers != null){
			results = new HashMap<String, Map<LayerProperty,String>>();
			for(Layer layer : layers){
				
				String codedEntity = null;
				
				// Add both metadataURI & title
				Map<LayerProperty,String> layerProperties = new HashMap<LayerProperty,String>(); 
				
				// get reference codedentity added as authority-based Identifiers
				for(Identifier identifier : layer.getIdentifier()){
					if (identifier.getAuthority().matches(authority)) {
						codedEntity = identifier.getValue();
	
						// get xml Metadata		
						List<MetadataURL> metadataList = layer.getMetadataURL();
						if (metadataList.size() > 0) {
							for(MetadataURL metadata : metadataList){
								if(metadata.getFormat().matches("text/xml")){
									layerProperties.put(LayerProperty.NAME, layer.getName());
									layerProperties.put(LayerProperty.TITLE, layer.getTitle());
									layerProperties.put(LayerProperty.METADATAURL, metadata.getOnlineResource().getHref());
								}
							}
						}
						break;
					}
				}
				
				if (codedEntity != null) {
					results.put(codedEntity, layerProperties);
				}
			}
		}

		return results;
	}

	/**
	 * Main class
	 * 
	 * @param args
	 * @throws JAXBException 
	 */
	public static void main(String[] args) throws JAXBException {

		String baseUrl = "http://www.fao.org/figis/geoserver";
		String workspace = "species";

		MetadataMapper mapper = new MetadataMapper(baseUrl, workspace);
		Map<String, Map<LayerProperty, String>> results = mapper
				.getMappings("FLOD");

		for (Entry<String, Map<LayerProperty, String>> entry : results
				.entrySet()) {
			System.out.println(entry.getKey() + " | "
					+ entry.getValue().get(LayerProperty.NAME) + " | "
					+ entry.getValue().get(LayerProperty.TITLE) + " | "
					+ entry.getValue().get(LayerProperty.METADATAURL));
		}
	}
}
