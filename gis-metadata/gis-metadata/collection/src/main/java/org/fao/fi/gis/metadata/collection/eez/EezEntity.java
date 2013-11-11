package org.fao.fi.gis.metadata.collection.eez;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.fao.fi.gis.metadata.feature.FeatureTypeProperty;
import org.fao.fi.gis.metadata.authority.AuthorityEntity;
import org.fao.fi.gis.metadata.entity.EntityAddin;
import org.fao.fi.gis.metadata.entity.EntityProperty;
import org.fao.fi.gis.metadata.entity.GeographicEntity;
import org.fao.fi.gis.metadata.entity.GeographicEntityImpl;
import org.fao.fi.gis.metadata.entity.GisProperty;
import org.fao.fi.gis.metadata.model.content.MetadataContent;
import org.fao.fi.gis.metadata.model.settings.GeographicServerSettings;
import org.fao.fi.gis.metadata.model.settings.MetadataCatalogueSettings;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;


/**
 * EEZ Entity.
 * Wrapper for EEZ publication by VLIZ.
 * 
 * @author eblondel (FAO)
 *
 */
public class EezEntity extends GeographicEntityImpl implements GeographicEntity{

	public enum EezProperty implements EntityProperty{
		
		VLIZ (AuthorityEntity.VLIZ, true, true),
		FLOD (AuthorityEntity.FLOD, true, true);
		
		private final AuthorityEntity authority;
		private final boolean thesaurus;
		private final boolean containsURIs;
		
		EezProperty(AuthorityEntity authority, boolean thesaurus, boolean containsURIs){
			this.authority = authority;
			this.thesaurus = thesaurus;
			this.containsURIs = containsURIs;
		}
		
		public AuthorityEntity authority(){
			return this.authority;
		}

		public boolean isThesaurus() {
			return this.thesaurus;
		}

		public boolean containsURIs() {
			return this.containsURIs;
		}
		
	}
	
	private FLODEezEntity FLODEezEntity;
	private String refName;
	private String style;
	private Map<EntityProperty, List<String>> properties;
	private Map<GisProperty, String> gisProperties;
	
	
	public EezEntity(String code, MetadataContent template,
			Map<FeatureTypeProperty, Object> geoproperties, Map<EntityAddin,String> addins,
			GeographicServerSettings gsSettings, MetadataCatalogueSettings metaSettings
			) throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
		
		super(code, template,
				geoproperties, addins,
				gsSettings, metaSettings,
				"eez", code);
		
		this.FLODEezEntity = new FLODEezEntity(code);
		this.setRefName();
		
		this.setSpecificProperties();
		
		this.style = addins.get(EntityAddin.Style);
		this.setGisProperties();
	}

	
	public String getViewerProj() {
		return null;
	}
	
	public String getViewerIdentifier() {
		return null;
	}

	public CoordinateReferenceSystem getCRS() {
		return DefaultGeographicCRS.WGS84;
	}

	private void setRefName(){
		this.refName = this.FLODEezEntity.getName();
	}
		
	/**
	 * Get the Ref name (name that will be used in metadata title)
	 * 
	 */
	public String getRefName() {
		return this.refName;
	}

	public void setSpecificProperties(){
		properties = new HashMap<EntityProperty, List<String>>();		
		properties.put(EezProperty.FLOD, Arrays.asList(this.FLODEezEntity.getCodedEntity()));
		properties.put(EezProperty.VLIZ, Arrays.asList(this.getMetaIdentifier()));
	}
	
	public Map<EntityProperty, List<String>> getSpecificProperties() {
		return this.properties;
	}

	public String getDomainName(){
		return "eez";
	}
	
	public String getFigisId() {
		return null;
	}
	
	
	private void setGisProperties(){
		this.gisProperties  = new HashMap<GisProperty,String>();
		gisProperties.put(GisProperty.STYLE, this.style);
		gisProperties.put(GisProperty.PROJECTION, "EPSG:4326");
		
	}
	
	
	public Map<GisProperty, String> getGisProperties() {
		return this.gisProperties;
	}
	
	public String getFactsheet(){
		return null; //TODO see if there is VLIZ factsheets based on EEZ MRGID 
	}
	
	

}
