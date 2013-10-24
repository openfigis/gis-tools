package org.fao.fi.gis.mappings.metadata;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.decoder.RESTLayer21;
import it.geosolutions.geoserver.rest.decoder.RESTLayerList;
import it.geosolutions.geoserver.rest.decoder.utils.NameLinkElem;
import it.geosolutions.geoserver.rest.encoder.identifier.GSIdentifierInfoEncoder;
import it.geosolutions.geoserver.rest.encoder.metadatalink.GSMetadataLinkInfoEncoder;

/**
 * This class aims to give the 1st business logic to query the mapping between
 * FLOD codedentity URI & GIS OGC metadata URI
 * 
 * At now, it's specific to GeoServer implementation and uses the Java REST
 * client library "geoserver-manager".
 * 
 */
public class MetadataMapper {

	private GeoServerRESTReader reader;
	private String workspace;

	/**
	 * Constructs the Metadata mapper
	 * 
	 * @param gsBaseURL
	 * @param gsUser
	 * @param gsPwd
	 * @param ws
	 * @throws MalformedURLException
	 */
	public MetadataMapper(String gsBaseURL, String gsUser, String gsPwd,
			String ws) throws MalformedURLException {
		this.reader = new GeoServerRESTReader(gsBaseURL, gsUser, gsPwd);
		this.workspace = ws;
	}

	/**
	 * Get the mappings between coded entity URI & GIS metadata URI
	 * 
	 * @return
	 */
	public Map<String, Map<LayerProperty,String>> getMappings(String authority) {
		Map<String, Map<LayerProperty,String>> results = null;

		RESTLayerList layers = reader.getLayers();

		if (layers != null) {
			results = new HashMap<String, Map<LayerProperty,String>>();
			Iterator<NameLinkElem> it = layers.iterator();
			while (it.hasNext()) {

				// get the GS layer
				String layerName = it.next().getName();
				RESTLayer layer = (RESTLayer21) reader.getLayer(workspace, layerName);

				if (layer != null) {

					String codedEntity = null;
					
					// Add both metadataURI & title
					Map<LayerProperty,String> layerProperties = new HashMap<LayerProperty,String>(); 

					// get FeatureType where properties (keywords, metadata,
					// etc) are configured
					RESTFeatureType ft = reader.getFeatureType(layer);

					// get reference codedentity added as authority-based Identifiers
					List<GSIdentifierInfoEncoder> identifiers = layer.getEncodedIdentifierInfoList();
					for (GSIdentifierInfoEncoder identifier : identifiers) {
						if (identifier.getAuthority().matches(authority)) {
							codedEntity = identifier.getIdentifier();

							// get xml Metadata
							List<GSMetadataLinkInfoEncoder> metadataList = ft
									.getEncodedMetadataLinkInfoList();
							if (metadataList.size() > 0) {
								for (GSMetadataLinkInfoEncoder metadata : metadataList) {
									if (metadata.getType().matches("text/xml")) {
										layerProperties.put(LayerProperty.NAME, ft.getName());
										layerProperties.put(LayerProperty.TITLE, ft.getTitle());
										layerProperties.put(LayerProperty.METADATAURL, metadata.getContent());
										break;
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
		}

		return results;
	}

	/**
	 * Main class
	 * 
	 * @param args
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws MalformedURLException {

		String gsBaseURL = "gsBaseURL";
		String gsUser = "gsUser";
		String gsPassword = "gsPwd";
		String workspace = "ws";

		MetadataMapper mapper = new MetadataMapper(gsBaseURL, gsUser,
				gsPassword, workspace);
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
