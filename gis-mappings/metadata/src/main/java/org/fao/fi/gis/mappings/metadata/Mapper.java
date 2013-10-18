package org.fao.fi.gis.mappings.metadata;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.decoder.RESTLayerList;
import it.geosolutions.geoserver.rest.decoder.utils.NameLinkElem;
import it.geosolutions.geoserver.rest.encoder.metadatalink.GSMetadataLinkInfoEncoder;

/**
 * This main class aims to give the 1st business logic to query the mapping
 * between FLOD codedentity URI & GIS OGC metadata URI
 * 
 * At now, it's specific to GeoServer implementation and uses the Java REST
 * client library "geoserver-manager".
 * 
 */
public class Mapper {

	public static void main(String[] args) throws MalformedURLException {
		// prepare output
		// a map giving codedentity / metadataURI pairs
		Map<String, List<String>> results = new HashMap<String, List<String>>();

		// Settings for GeoServer
		// ---------------------
		String gsBaseURL = "gsBaseUrl";
		String gsUser = "gsUser";
		String gsPassword = "gsPassword";

		// workspace
		// ---------
		String workspace = "ws";

		GeoServerRESTReader reader = new GeoServerRESTReader(gsBaseURL, gsUser,
				gsPassword);

		RESTLayerList layers = reader.getLayers();
		if (layers != null) {
			Iterator<NameLinkElem> it = layers.iterator();
			while (it.hasNext()) {

				// get the GS layer
				String layerName = it.next().getName();
				RESTLayer layer = reader.getLayer(workspace, layerName);

				if (layer != null) {

					String codedEntity = null;
					List<String> info = new ArrayList<String>();

					// get FeatureType where properties (keywords, metadata,
					// etc) are configured
					RESTFeatureType ft = reader.getFeatureType(layer);

					info.add(ft.getTitle()); // add title to infolist

					// get reference codedentity added as featuretype keyword
					List<String> keywords = ft.getKeywords();
					for (String keyword : keywords) {
						if (keyword.contains("flod")) {
							codedEntity = keyword;

							// get xml Metadata
							List<GSMetadataLinkInfoEncoder> metadataList = ft
									.getEncodedMetadataLinkInfoList();
							if (metadataList.size() > 0) {
								for (GSMetadataLinkInfoEncoder metadata : metadataList) {
									if (metadata.getType().matches("text/xml")) {
										info.add(metadata.getContent()); 
										break;
									}
								}
							}
							break;
						}
					}

					if (codedEntity != null) {
						results.put(codedEntity, info);
					}
				}
			}
		}

		// list the results
		for (Entry<String, List<String>> entry : results.entrySet()) {
			System.out.println(entry.getKey() + " | " + entry.getValue().get(0)
					+ " | " + entry.getValue().get(1));
		}

	}

}
