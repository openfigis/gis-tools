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
	 * Method to parse the specieslist.xml and return a set of alpha3code
	 * 
	 * @param file
	 * @return
	 */
	public static Map<String, Map<EntityAddin,String>> parseSpeciesList(String specieslist) {

		Map<String, Map<EntityAddin,String>> speciesList = new HashMap<String, Map<EntityAddin,String>>();
		try {

			URL url = new URL(specieslist);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(url.openStream());
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("item");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				// for (int temp = 0; temp < 20; temp++) { //test with the first
				// 10 species
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
					Map<EntityAddin, String> addins = new HashMap<EntityAddin,String>();
					
					Element eElement = (Element) nNode;
					String alphacode = eElement.getAttribute("a3c");

					String mar = eElement.getAttribute("mar");
					String inl = eElement.getAttribute("inl");
					String hab = null;
					if (mar.equals("1")) {
						if (inl.equals("1")) {
							hab = "mi";
						} else {
							hab = "m";
						}
					} else if (mar.equals("0")) {
						if (inl.equals("1")) {
							hab = "i";
						}

					}
					
					addins.put(EntityAddin.Habitat, hab);
					addins.put(EntityAddin.Style, "species_style");
					speciesList.put(alphacode, addins);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return speciesList;
	}

	/**
	 * Method to parse the unique source of RFB settings and return a set of rfb
	 * + style (for now)
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Map<EntityAddin,String>> parseRfbList(String file)
			throws IOException {

		Map<String, Map<EntityAddin,String>> rfbList = new HashMap<String, Map<EntityAddin,String>>();
		InputStream is = null;
		try {

			URL url = new URL(file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			is = url.openStream();
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("rfb");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Map<EntityAddin,String> addins = new HashMap<EntityAddin,String>();
				
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					String rfb = eElement.getAttribute("name");
					
					String style = eElement.getAttribute("style");
					String fid = eElement.getAttribute("fid");
					addins.put(EntityAddin.Style, style);
					addins.put(EntityAddin.FigisID, fid);
					
					rfbList.put(rfb, addins);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			is.close();
		}

		return rfbList;
	}

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
