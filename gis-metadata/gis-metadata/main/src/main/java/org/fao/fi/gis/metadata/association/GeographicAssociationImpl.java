package org.fao.fi.gis.metadata.association;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.fao.fi.gis.metadata.entity.EntityAddin;
import org.fao.fi.gis.metadata.entity.EntityProperty;
import org.fao.fi.gis.metadata.entity.GeographicEntity;
import org.fao.fi.gis.metadata.entity.GisProperty;
import org.fao.fi.gis.metadata.feature.FeatureTypeProperty;
import org.fao.fi.gis.metadata.model.content.MetadataContent;
import org.fao.fi.gis.metadata.model.settings.GeographicServerSettings;
import org.fao.fi.gis.metadata.model.settings.MetadataCatalogueSettings;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * GeographicAssociationImpl
 * 
 * 
 * @author eblondel
 *
 */
public class GeographicAssociationImpl implements GeographicAssociation {

	private String gnBaseURL;
	private String gsBaseURL;
	private String srcWorkspace;
	private String srcLayername;
	private String srcAttribute;
	private String trgWorkspace;
	private String trgLayerPrefix;
	private String baseLayerWorkspace;
	private String baseLayerName;

	private List<GeographicEntity> entities;
	private String code;
	private String refName;
	private MetadataContent template;
	private String metaId;
	private String targetLayername;
	
	
	private Map<FeatureTypeProperty, Object> geoproperties;
	private String domain;
	private String viewerid;
	private URI graphicOverview;
	private URI viewerResource;
	private Map<EntityAddin,String> addins;
 
	/**
	 * Constructs a geographic Association
	 * 
	 * @param entities
	 * @param template
	 * @param geoproperties
	 * @param addins
	 * @param gsSettings
	 * @param metaSettings
	 * @param domain
	 * @param vieweridentifier
	 * @throws URISyntaxException
	 */
	public GeographicAssociationImpl(List<GeographicEntity> entities, MetadataContent template,
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
		this.baseLayerWorkspace = gsSettings.getBaseLayerWorkspace();
		this.baseLayerName = gsSettings.getBaseLayerName();

		this.setEntities(entities);
		this.setCode(entities);
		this.template = template;
		this.setRefname(entities);
		this.domain = domain;
		this.setMetaIdentifier(entities);
		
		this.setTargetLayername(trgLayerPrefix);
		this.geoproperties = geoproperties;
		this.addins = addins;
		this.viewerid = vieweridentifier;
		

		this.setLayerGraphicOverview();
		this.setViewerResource();

	}
	
	/** ==============================================
	 *  		    IDENTIFICATION METHODS
	 *  ==============================================
	 */ 
	
	/**
	 * @return the entities
	 */
	public List<GeographicEntity> getEntities() {
		return entities;
	}

	/**
	 * @param entities the entities to set
	 */
	public void setEntities(List<GeographicEntity> entities) {
		this.entities = entities;
	}

	
	/**
	 * Set Code
	 * @param entities
	 */
	public void setCode(List<GeographicEntity> entities){
		
		String codes ="";
		for(int i = 0; i <entities.size();i++){
			if(i==0){
				codes = entities.get(i).getCode();
			}else{
				codes = "_x_"+entities.get(i).getCode();
			}
		}
		this.code = codes;
		
	}
	
	/**
	 * Get Code
	 * 
	 */
	public String getCode(){
		return this.code;
	}

	/**
	 * Set Ref Name
	 * 
	 * @param entities
	 */
	public void setRefname(List<GeographicEntity> entities){
		
		String refName = "";
		if(this.template.getHasBaseTitle()){
			refName += this.template.getBaseTitle();
		}
		
		if(entities.size() == 1){
			refName += entities.get(0).getRefName();
		}else if(entities.size() > 1){
		
			for(int i=0;i<entities.size();i++){
				if(i==0){
					refName += entities.get(i).getRefName();
				}else{
					refName += " | "+entities.get(i).getRefName();
				}
			}
		}
		this.refName = refName;
		
	}
	
	
	/**
	 * Get Ref Name
	 * 
	 */
	public String getRefName() {
		return this.refName;
	}
	
	
	/**
	 * Get Target layer name
	 * 
	 */
	public String getTargetLayerName() {
		return this.targetLayername;
	}
	
	/**
	 * Set Target layer name
	 * 
	 * @param trgLayerPrefix
	 */
	private void setTargetLayername(String trgLayerPrefix) {
		if (trgLayerPrefix == null | trgLayerPrefix == "") {
			this.targetLayername = this.getCode();
		} else {
			this.targetLayername = trgLayerPrefix + "_" + this.getCode();
		}
	}
	
	
	/**
	 * Get Domain name
	 * 
	 */
	public String getDomainName() {
		return this.domain;
	}
	
	/**
	 * Get Meta Identifier
	 * 
	 */
	public String getMetaIdentifier(){
		return this.metaId;
	}
	
	/**
	 * Set Meta Identifier
	 * 
	 * @param entities
	 */
	private void setMetaIdentifier(List<GeographicEntity> entities){	
		this.metaId = this.template.getOrganizationContact().getAcronym().toLowerCase() +"-"+
					  this.domain + "-map-"+
					  this.getCode().toLowerCase();
	}

	/**
	 * Get Object Addins
	 * 
	 * @return
	 */
	public Map<EntityAddin,String> getAddins(){
		return this.addins;
	}
	
	/** ==============================================
	 *  		    SETTINGS METHODS
	 *  ==============================================
	 */
	
	
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
	
	public String getBaseLayerWorkspace(){
		return this.baseLayerWorkspace;
	}
	
	public String getBaseLayerName(){
		return this.baseLayerName;
	}

	public MetadataContent getTemplate() {
		return this.template;
	}

	/** ==============================================
	 *  		    GIS PROPERTIES METHODS
	 *  ==============================================
	 */
	
	/**
	 * Get Geo properties
	 * 
	 * @return
	 */
	public Map<FeatureTypeProperty, Object> getGeoProperties() {
		return this.geoproperties;
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
		String completeBaseLayerName = this.baseLayerWorkspace +":"+this.baseLayerName;
		String completeLayerName = this.trgWorkspace + ":"
				+ this.targetLayername;
		String graphicLink = this.gsBaseURL
				+ "/wms?service=WMS&version=1.1.0&request=GetMap" + "&layers="
				+ completeBaseLayerName+"," + completeLayerName + "&bbox=" + minX
				+ "," + minY + "," + maxX + "," + maxY + "&width=" + width
				+ "&height=" + height + "&srs=EPSG:4326" + // for now only
															// EPSG:4326 as
															// projection
				"&format=image%2Fpng";

		this.graphicOverview = new URI(graphicLink);
	}

	public URI getLayerGraphicOverview() {
		return this.graphicOverview;
	}
	
	/** ==============================================
	 *  		    FIGIS SPECIFIC METHODS
	 *  ==============================================
	 */
	
	/**
	 * Get Viewer object identifier
	 * 
	 */
	public String getViewerIdentifier() {
		return this.viewerid;
	}
	
	/**
	 * Get FIGIS Viewer Resource (specific to FAO)
	 * 
	 * @return the URI for the FIGIS map viewer
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

	public URI getViewerResource() {
		return this.viewerResource;
	}

	public CoordinateReferenceSystem getCRS() {
		// TODO Auto-generated method stub
		return null;
	}



	public Map<EntityProperty, List<String>> getSpecificProperties() {
		// TODO Auto-generated method stub
		return null;
	}



	public String getFigisId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFactsheet() {
		// TODO Auto-generated method stub
		return null;
	}	
	
}
