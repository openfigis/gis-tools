package org.fao.fi.gis.metadata.entity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.fao.fi.gis.metadata.feature.FeatureTypeProperty;
import org.fao.fi.gis.metadata.model.content.MetadataContent;
import org.fao.fi.gis.metadata.model.settings.GeographicServerSettings;
import org.fao.fi.gis.metadata.model.settings.MetadataCatalogueSettings;

import com.vividsolutions.jts.geom.Envelope;

/**
 * GeographicEntityImpl
 * 
 * 
 * @author eblondel
 *
 */
public abstract class GeographicEntityImpl implements GeographicEntity {

	private String gnBaseURL;
	private String gsBaseURL;
	private String srcWorkspace;
	private String srcLayername;
	private String srcAttribute;
	private String trgWorkspace;
	private String trgLayerPrefix;

	private String code;
	private MetadataContent template;
	private String metaId;
	
	private String targetLayername;
	private Map<FeatureTypeProperty, Object> geoproperties;
	private String domain;
	private String viewerid;
	private URI graphicOverview;
	private URI viewerResource;
	private Map<EntityAddin,String> addins;
 
	public GeographicEntityImpl(String code, MetadataContent template,
								Map<FeatureTypeProperty, Object> geoproperties, Map<EntityAddin,String> addins,
								GeographicServerSettings gsSettings, MetadataCatalogueSettings metaSettings,
								String domain, String vieweridentifier) throws URISyntaxException {

		this.gsBaseURL = gsSettings.getUrl();
		this.gnBaseURL = metaSettings.getUrl();
		this.srcWorkspace = gsSettings.getSourceWorkspace();
		this.srcLayername = gsSettings.getSourceLayer();
		this.srcAttribute = gsSettings.getSourceAttribute();
		this.trgWorkspace = gsSettings.getTargetWorkspace();
		this.trgLayerPrefix = gsSettings.getTargetLayerPrefix();

		this.code = code;
		this.domain = domain;
		this.template = template;
		this.setMetaIdentifier(code);
		
		this.setTargetLayername(trgLayerPrefix);
		this.geoproperties = geoproperties;
		this.addins = addins;
		this.viewerid = vieweridentifier;
		

		this.setLayerGraphicOverview();
		this.setViewerResource();

	}

	public String getGSBaseURL() {
		return this.gsBaseURL;
	}

	public String getGNBaseURL() {
		return this.gnBaseURL;
	}

	public String getSRCWorkspace() {
		return this.srcWorkspace;
	}

	public String getSRCLayername() {
		return this.srcLayername;
	}

	public String getSRCAttribute() {
		return this.srcAttribute;
	}

	public String getTRGWorkspace() {
		return this.trgWorkspace;
	}

	public String getTRGLayerprefix() {
		return this.trgLayerPrefix;
	}

	public String getCode() {
		return this.code;
	}

	public MetadataContent getTemplate() {
		return this.template;
	}

	public Map<FeatureTypeProperty, Object> getGeoProperties() {
		return this.geoproperties;
	}

	public String getTargetLayerName() {
		return this.targetLayername;
	}

	private void setTargetLayername(String trgLayerPrefix) {
		if (trgLayerPrefix == null | trgLayerPrefix == "") {
			this.targetLayername = this.getCode();
		} else {
			this.targetLayername = trgLayerPrefix + "_" + this.getCode();
		}
	}
	
	public Map<EntityAddin,String> getAddins(){
		return this.addins;
	}

	
	public String getMetaIdentifier(){
		return this.metaId;
	}
	
	private void setMetaIdentifier(String code){
		this.metaId = this.template.getOrganizationContact().getAcronym().toLowerCase() +"-"+
					  this.domain + "-map-"+
					  code.toLowerCase();
	}
	
	/**
	 * Method that will handle the layer graphic overview to be appended to the
	 * layer metadata as layer preview
	 * 
	 * @return URI
	 * @throws URISyntaxException
	 */
	private void setLayerGraphicOverview() throws URISyntaxException {

		// compute the image size
		double minX = -180.0;
		double maxX = 180.0;
		double minY = -90.0;
		double maxY = 90.0;
		int width = 600;
		int height = 300;

		Envelope bbox = this.getBBOX();
		if (bbox != null) {
			minX = bbox.getMinX();
			maxX = bbox.getMaxX();
			minY = bbox.getMinY();
			maxY = bbox.getMaxY();

			// adjust width &height
			double rangeX = maxX - minX;
			double rangeY = maxY - minY;
			double newheight = width * rangeY / rangeX;
			height = (int) Math.round(newheight);

			// check height size and adjust width size if necessary
			if (height > width) {
				width = (int) Math.round(Math.pow(width, 2) / height);
			}

		}

		// build the layer preview URI
		String completeLayerName = this.trgWorkspace + ":"
				+ this.targetLayername;
		String graphicLink = this.gsBaseURL
				+ "/wms?service=WMS&version=1.1.0&request=GetMap" + "&layers="
				+ "fifao:UN_CONTINENT," + completeLayerName + "&bbox=" + minX
				+ "," + minY + "," + maxX + "," + maxY + "&width=" + width
				+ "&height=" + height + "&srs=EPSG:4326" + // for now only
															// EPSG:4326 as
															// projection
				"&format=image%2Fpng";

		this.graphicOverview = new URI(graphicLink);
	}

	/**
	 * Get the Species Distribution Viewer Resource
	 * 
	 * @return the URI for the FAO Species distribution map viewer
	 * @throws URISyntaxException
	 */
	private void setViewerResource() throws URISyntaxException {
		Envelope bbox = this.getBBOX();

		String app = null;
		if(this.getDomainName() != null){
			if (!this.getDomainName().endsWith("s")) {
				app = this.domain.concat("s");
			} else {
				app = this.domain;
			}
		}	
		
		String resource = null;
		if (bbox != null) {
			resource = "http://www.fao.org/figis/geoserver/factsheets/" + app
					+ ".html?" + this.domain + "=" + this.viewerid
					+ "&extent=" + bbox.getMinX() + "," + bbox.getMinY() + ","
					+ bbox.getMaxX() + "," + bbox.getMaxY() + "&prj=4326"; // for
																			// now
																			// only
																			// EPSG:4326
																			// managed
																			// as
																			// projection
		}

		this.viewerResource = null;
		if (this.getDomainName() != null && bbox != null) {
			this.viewerResource = new URI(resource);
		}
	}

	public URI getLayerGraphicOverview() {
		return this.graphicOverview;
	}

	public URI getViewerResource() {
		return this.viewerResource;
	}

	public String getViewerIdentifier() {
		return this.viewerid;
	}

	public String getDomainName() {
		return this.domain;
	}

	public Envelope getBBOX() {
		Envelope bbox = null;
		if (this.geoproperties != null) {
			bbox = (Envelope) this.geoproperties.get(FeatureTypeProperty.BBOX);
		}
		return bbox;
	}

	public int getFeaturesCount() {
		Integer count = 0;
		if (this.geoproperties != null) {
			count = (Integer) this.geoproperties.get(FeatureTypeProperty.COUNT);
		}
		return count;
	}
	
}
