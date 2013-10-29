package org.fao.fi.gis.data;

import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.HTTPUtils;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder21;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.authorityurl.GSAuthorityURLInfoEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;
import it.geosolutions.geoserver.rest.encoder.identifier.GSIdentifierInfoEncoder;
import it.geosolutions.geoserver.rest.encoder.metadata.virtualtable.GSVirtualTableEncoder;
import it.geosolutions.geoserver.rest.encoder.metadata.virtualtable.VTGeometryEncoder;
import it.geosolutions.geoserver.rest.encoder.metadatalink.GSMetadataLinkInfoEncoder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.fao.fi.gis.metadata.entity.EntityProperty;
import org.fao.fi.gis.metadata.entity.GeographicEntity;
import org.fao.fi.gis.metadata.util.Utils;

import com.vividsolutions.jts.geom.Envelope;

/**
 * DataPublisher
 * 
 * @author eblondel (FAO)
 * 
 */
public class DataPublisher {

	private static final String crs = "GEOGCS[\"WGS 84\", \n  DATUM[\"World Geodetic System 1984\", \n    SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], \n    AUTHORITY[\"EPSG\",\"6326\"]], \n  PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \n  UNIT[\"degree\", 0.017453292519943295], \n  AXIS[\"Geodetic longitude\", EAST], \n  AXIS[\"Geodetic latitude\", NORTH], \n  AUTHORITY[\"EPSG\",\"4326\"]]";

	String geoserverBaseURL;
	String gsUser;
	String gsPwd;

	public GeoServerRESTReader GSReader;
	public GeoServerRESTPublisher GSPublisher;

	String srcLayer;
	String srcAttribute;
	String trgWorkspace;
	String trgDatastore;
	String trgLayerPrefix;
	String geonetworkBaseURL;

	/**
	 * DataPublisher constructor
	 * 
	 * @param gsBaseURL
	 * @param gsUser
	 * @param gsPassword
	 * @param srcLayer
	 * @param trgWorkspace
	 * @param trgLayerPrefix
	 * @throws MalformedURLException
	 */
	public DataPublisher(String gsBaseURL, String gsUser, String gsPassword,
			String srcLayer, String srcAttribute, String trgWorkspace,
			String trgDatastore, String trgLayerPrefix, String gnBaseURL)
			throws MalformedURLException {

		this.geoserverBaseURL = gsBaseURL;
		this.gsUser = gsUser;
		this.gsPwd = gsPassword;

		this.GSReader = new GeoServerRESTReader(geoserverBaseURL, gsUser,
				gsPassword);
		this.GSPublisher = new GeoServerRESTPublisher(geoserverBaseURL, gsUser,
				gsPassword);

		this.srcLayer = srcLayer;
		this.srcAttribute = srcAttribute;
		this.trgWorkspace = trgWorkspace;
		this.trgDatastore = trgDatastore;
		this.trgLayerPrefix = trgLayerPrefix;
		this.geonetworkBaseURL = gnBaseURL;

	}

	/**
	 * Get related metadata URL (xml)
	 * 
	 * @param code
	 * @return
	 * @throws Exception
	 */
	public String getRelatedMetadataURL(GeographicEntity entity) {

		String layername = entity.getTargetLayerName();

		// using geoserver-manager
		String metadataURL = null;
		List<GSMetadataLinkInfoEncoder> metadataLinks = GSReader.getResource(
				GSReader.getLayer(layername)).getEncodedMetadataLinkInfoList();
		for (GSMetadataLinkInfoEncoder metadatalink : metadataLinks) {
			if (metadatalink.getType().equals("text/xml")) {
				metadataURL = metadatalink.getContent();
			}
		}
		return metadataURL;
	}

	/**
	 * Check layer existence
	 * 
	 * 
	 * @param code
	 * @return
	 * @throws Exception
	 */
	public boolean checkLayerExistence(String code) {
		String layername = this.trgLayerPrefix + "_" + code;

		// using geoserver-manager
		List<String> layers = GSReader.getLayers().getNames();
		return layers.contains(layername);
	}

	/**
	 * Delete the layer
	 * 
	 * @param alphacode
	 * @throws Exception
	 */
	public boolean deleteLayer(GeographicEntity entity) {
		String layername = entity.getTargetLayerName();

		// using geoserver-manager
		return GSPublisher.unpublishFeatureType(trgWorkspace, trgDatastore,
				layername);
	}

	public boolean deleteOnlyFeatureType(GeographicEntity entity)
			throws MalformedURLException {
		URL deleteFtUrl = new URL(this.geoserverBaseURL + "/rest/workspaces/"
				+ this.trgWorkspace + "/datastores/" + this.trgDatastore
				+ "/featuretypes/" + entity.getTargetLayerName());

		boolean ftDeleted = HTTPUtils.delete(deleteFtUrl.toExternalForm(),
				this.gsUser, this.gsPwd);
		return ftDeleted;
	}

	/**
	 * A method to publish the Species Distribution layer (per species) as
	 * Geoserver SQL View layer. The publication also publish the MetadataURL,
	 * to be me made available through the Geoserver getCapabilities document
	 * 
	 * @param entity
	 * @param metadataURL
	 */
	public boolean publishLayer(GeographicEntity entity, String style,
			String metadataID) {

		// target geoserver layer
		String title = entity.getTemplate().getBaseTitle()
				+ entity.getRefName();

		// Using geoserver-manager
		// -----------------------
		final GSFeatureTypeEncoder fte = new GSFeatureTypeEncoder();
		fte.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);
		fte.setNativeName(entity.getTargetLayerName());
		fte.setName(entity.getTargetLayerName());
		fte.setTitle(title);
		fte.setSRS("EPSG:4326");
		fte.setNativeCRS("EPSG:4326");
		fte.setEnabled(true);

		for (Entry<EntityProperty, List<String>> entry : entity
				.getSpecificProperties().entrySet()) {
			if (entry.getKey().isThesaurus()) {
				if (!entry.getKey().containsURIs()) {
					for (String kw : entry.getValue()) {
						String keyword = kw + " ("
								+ entry.getKey().authority().name() + ")";
						fte.addKeyword(keyword);
					}
				}
			}
		}

		Envelope bbox = entity.getBBOX();
		if (bbox != null) {
			fte.setNativeBoundingBox(bbox.getMinX(), bbox.getMinY(),
					bbox.getMaxX(), bbox.getMaxY(), "EPSG:4326");
			fte.setLatLonBoundingBox(bbox.getMinX(), bbox.getMinY(),
					bbox.getMaxX(), bbox.getMaxY(), "EPSG:4326");
		} else {
			fte.setNativeBoundingBox(-180.0, -90.0, 180.0, 90.0, "EPSG:4326");
			fte.setLatLonBoundingBox(-180.0, -90.0, 180.0, 90.0, "EPSG:4326");
		}

		// virtual table (sql view)
		VTGeometryEncoder gte = new VTGeometryEncoder("THE_GEOM",
				"MultiPolygon", "4326");
		String sql = "SELECT * FROM " + this.srcLayer + " WHERE "
				+ this.srcAttribute + " = '" + entity.getCode() + "'";
		GSVirtualTableEncoder vte = new GSVirtualTableEncoder(
				entity.getTargetLayerName(), sql, null, Arrays.asList(gte),
				null);
		fte.setMetadataVirtualTable(vte);

		// metadata
		final GSMetadataLinkInfoEncoder mde1 = new GSMetadataLinkInfoEncoder(
				"text/xml", "ISO19115:2003", Utils.getXMLMetadataURL(
						this.geonetworkBaseURL, metadataID));
		final GSMetadataLinkInfoEncoder mde2 = new GSMetadataLinkInfoEncoder(
				"text/html", "ISO19115:2003", Utils.getHTMLMetadataURL(
						this.geonetworkBaseURL, metadataID));
		fte.addMetadataLinkInfo(mde1);
		fte.addMetadataLinkInfo(mde2);

		// layer
		final GSLayerEncoder layerEncoder = new GSLayerEncoder21();
		layerEncoder.setDefaultStyle(style);

		// add authorityURL & identifiers
		for (Entry<EntityProperty, List<String>> entry : entity
				.getSpecificProperties().entrySet()) {
			if (entry.getKey().isThesaurus()) {
				if (entry.getKey().containsURIs()) {
					layerEncoder.addAuthorityURL(new GSAuthorityURLInfoEncoder(
							entry.getKey().authority().name(), entry.getKey()
									.authority().href()));
					for (String identifier : entry.getValue()) {
						layerEncoder.addIdentifier(new GSIdentifierInfoEncoder(
								entry.getKey().authority().name(), identifier));
					}
				}
			}
		}

		// publication
		return GSPublisher.publishDBLayer(trgWorkspace, trgDatastore, fte,
				layerEncoder);

	}
}
