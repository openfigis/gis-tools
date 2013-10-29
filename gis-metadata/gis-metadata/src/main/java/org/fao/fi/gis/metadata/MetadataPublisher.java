package org.fao.fi.gis.metadata;

import java.io.File;
import java.util.EnumSet;

import org.fao.fi.gis.metadata.entity.GeographicEntity;
import org.geotoolkit.xml.XML;
import org.jdom.Element;

import it.geosolutions.geonetwork.GNClient;
import it.geosolutions.geonetwork.util.GNInsertConfiguration;
import it.geosolutions.geonetwork.util.GNPriv;
import it.geosolutions.geonetwork.util.GNPrivConfiguration;

/**
 * Metadata Publisher Allows to publish ISO 19115/19139 compliant metadata in a
 * Geonetwork catalogue.
 * 
 * @author eblondel (FAO)
 * 
 */
public class MetadataPublisher {

	private String revisionDate;
	private String version;

	private String gnBaseURL;
	GNClient client;

	public MetadataPublisher(String revisionDate, String version,
			String gnBaseURL, String gnUser, String gnPassword) {
		this.revisionDate = revisionDate;
		this.version = version;

		// geonetwork connection
		this.gnBaseURL = gnBaseURL;
		client = new GNClient(this.gnBaseURL);
		boolean logged = client.login(gnUser, gnPassword);
		if (!logged) {
			throw new RuntimeException("Could not log in");
		}
	}

	/**
	 * Method to publish a full metadata compliant with ISO 19115/19139 standard
	 * The method use 2 main libraries: - GeoToolKit to generate the metadata
	 * through a GeographicEntityMetadata class - Geonetwork-manager to publish
	 * the metadata in the Geonetwork catalogue
	 * 
	 * @param alphacode
	 * @param scientificname
	 * @param bbox
	 * @return the metadataURL (string)
	 */
	public String publishFullMetadata(String fileIdentifier,
			GeographicEntity entity) {

		String metadataID = null;
		try {

			final GeographicEntityMetadata metadata = new GeographicEntityMetadata(
					entity, fileIdentifier, this.revisionDate, this.version);

			// metadata insert configuration
			GNInsertConfiguration icfg = new GNInsertConfiguration();
			icfg.setCategory("datasets");
			icfg.setGroup("1"); // group 1 is usually "all"
			icfg.setStyleSheet("_none_");
			icfg.setValidate(Boolean.FALSE);

			File tmp = new File("./metadata.xml");
			XML.marshal(metadata, tmp);

			long id = client.insertMetadata(icfg, tmp); // insert metadata
			tmp.delete(); // delete metadata file

			// public privileges configuration
			GNPrivConfiguration pcfg = new GNPrivConfiguration();
			pcfg.addPrivileges(1,
					EnumSet.of(GNPriv.VIEW, GNPriv.DYNAMIC, GNPriv.FEATURED));
			client.setPrivileges(id, pcfg); // set public view privilege

			// metadataURL
			metadataID = metadata.getFileIdentifier();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return metadataID;

	}

	/**
	 * Delete a metadata from Geonetwork Use of gCube Geonetwork Caller rather
	 * than the Geonetwork-manager
	 * 
	 * @param metadataURL
	 * @throws Exception
	 */
	public void deleteMetadata(String metadataURL) throws Exception {
		String uuid = metadataURL.split("&id=")[1];

		// get Geonetwork long id
		Element element = client.get(uuid);
		Element gnInfo = (Element) element.getChildren().get(
				element.getChildren().size() - 1);
		long id = Long.parseLong(gnInfo.getChild("id").getValue());

		// delete
		client.deleteMetadata(id);
	}

}
