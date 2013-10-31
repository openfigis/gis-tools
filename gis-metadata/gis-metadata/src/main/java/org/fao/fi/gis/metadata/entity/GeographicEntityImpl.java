package org.fao.fi.gis.metadata.entity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.fao.fi.gis.data.FeatureTypeProperty;
import org.fao.fi.gis.metadata.template.ContentTemplate;

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
	private ContentTemplate template;
	private String targetLayername;
	private Map<FeatureTypeProperty, Object> geoproperties;
	private String figisdomain;
	private String viewerid;
	private URI graphicOverview;
	private URI viewerResource;
	private Map<EntityAddin,String> addins;
 
	public GeographicEntityImpl(String code, ContentTemplate template,
			String gsBaseURL, String gnBaseURL, String srcWorkspace,
			String srcLayer, String srcAttribute, String trgWorkspace,
			String trgLayerPrefix,
			Map<FeatureTypeProperty, Object> geoproperties, String figisdomain,
			String vieweridentifier, Map<EntityAddin,String> addins) throws URISyntaxException {

		this.gsBaseURL = gsBaseURL;
		this.gnBaseURL = gnBaseURL;
		this.srcWorkspace = srcWorkspace;
		this.srcLayername = srcLayer;
		this.srcAttribute = srcAttribute;
		this.trgWorkspace = trgWorkspace;
		this.trgLayerPrefix = trgLayerPrefix;

		this.code = code;
		this.template = template;

		this.setTargetLayername(trgLayerPrefix);
		this.geoproperties = geoproperties;
		this.figisdomain = figisdomain;
		this.viewerid = vieweridentifier;
		this.addins = addins;

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

	public ContentTemplate getTemplate() {
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
				app = this.figisdomain.concat("s");
			} else {
				app = this.figisdomain;
			}
		}	
		
		String resource = null;
		if (bbox != null) {
			resource = "http://www.fao.org/figis/geoserver/factsheets/" + app
					+ ".html?" + this.figisdomain + "=" + this.viewerid
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
		return this.figisdomain;
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

	public String getIdentifier(){
		String id = this.getTemplate().getOrganizationContact().getAcronym().toLowerCase()
					+ "-"+this.getDomainName()
					+"-map-"+this.getCode().toLowerCase();
		return id;
	}
	
}
