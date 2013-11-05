package org.fao.fi.gis.metadata.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.fao.fi.gis.metadata.entity.EntityAddin;
import org.geotoolkit.xml.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class Utils {

	/**
	 * Get the metadataURL
	 * 
	 * @param gnBaseURL
	 * @param mdIdentifier
	 * @return the metadata URL
	 */
	public static String getXMLMetadataURL(String gnBaseURL, String mdIdentifier) {

		String metadataURL = gnBaseURL + "/srv/en/" + "csw?service=CSW"
				+ "&request=GetRecordById" + "&Version=2.0.2"
				+ "&elementSetName=full" + "&outputSchema=" + Namespaces.GMD
				+ "&id=" + mdIdentifier;

		return metadataURL;
	}

	/**
	 * Get the metadataURL
	 * 
	 * @param gnBaseURL
	 * @param mdIdentifier
	 * @return the metadata URL
	 */
	public static String getHTMLMetadataURL(String gnBaseURL,
			String mdIdentifier) {

		String metadataURL = gnBaseURL + "/srv/en/main.home?uuid="
				+ mdIdentifier;
		return metadataURL;
	}

	/**
	 * Get Feature Catalogue
	 * 
	 * @param gnBaseURL
	 * @param mdIdentifier
	 * @return the metadata URL
	 */
	public static String getXMLFeatureCatalogueURL(String gnBaseURL,
			String mdIdentifier) {

		String metadataURL = gnBaseURL + "/srv/en/" + "csw?service=CSW"
				+ "&request=GetRecordById" + "&Version=2.0.2"
				+ "&elementSetName=full" + "&outputSchema=" + Namespaces.GFC
				+ "&id=" + mdIdentifier;

		return metadataURL;
	}

}
