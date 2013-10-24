package org.fao.fi.gis.metadata.species;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.fi.gis.data.FeatureTypeProperty;
import org.fao.fi.gis.entity.EntityAddin;
import org.fao.fi.gis.entity.EntityProperty;
import org.fao.fi.gis.entity.GeographicEntity;
import org.fao.fi.gis.entity.GeographicEntityImpl;
import org.fao.fi.gis.entity.GisProperty;
import org.fao.fi.gis.metadata.MetadataContentTemplate;
import org.fao.fi.gis.metadata.authority.AuthorityEntity;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Species entity
 * 
 * @author eblondel (FAO)
 *
 */
public class SpeciesEntity extends GeographicEntityImpl implements GeographicEntity{

	
	public enum SpeciesProperty implements EntityProperty{
		
		FAO(AuthorityEntity.FAO, true, true),
		FLOD (AuthorityEntity.FLOD, true, true),
		
		HABITAT (AuthorityEntity.HABITAT, false, false),
		FIGIS (AuthorityEntity.FIGIS, true, false),
		ASFIS (AuthorityEntity.ASFIS, true, false),
		WORMS (AuthorityEntity.WORMS, true, false);		
	
		private final AuthorityEntity authority;
		private final boolean thesaurus;
		private final boolean containsURIs;
		
		SpeciesProperty(AuthorityEntity authority, boolean thesaurus, boolean containsURIs){
			this.authority = authority;
			this.thesaurus = thesaurus;
			this.containsURIs = containsURIs;
		}
		
		public AuthorityEntity authority(){
			return this.authority;
		}
		
		public boolean isThesaurus(){
			return this.thesaurus;
		}

		public boolean containsURIs() {
			return this.containsURIs;
		}
		
		
	}
	
	private FLODSpeciesEntity FLODSpeciesEntity;
	private String refName;
	String habitat;
	private Map<EntityProperty, List<String>> properties;
	private Map<GisProperty, String> gisProperties;
	
	public SpeciesEntity(String code, MetadataContentTemplate template,
						String gsBaseURL, String gnBaseURL,
						String srcWorkspace, String srcLayer, String srcAttribute,
						String trgWorkspace, String trgLayerPrefix,
						Map<FeatureTypeProperty, Object> geoproperties,
						Map<EntityAddin,String> addins) throws URISyntaxException{
		
		super(code, template,
			gsBaseURL, gnBaseURL,
			srcWorkspace, srcLayer, srcAttribute,
			trgWorkspace, trgLayerPrefix,
			geoproperties,
			"species", code+"-"+addins.get(EntityAddin.Habitat), addins);
		
		this.FLODSpeciesEntity = new FLODSpeciesEntity(code);
		this.setRefName();
		
		this.habitat = addins.get(EntityAddin.Habitat);
		this.setSpecificProperties();
		
		this.setGisProperties();
		
	}
	
	
	private void setRefName(){
		this.refName = this.FLODSpeciesEntity.getAsfisScientificName();
	}
	
	/**
	 * Get the Ref name (name that will be used in metadata title)
	 * 
	 */
	public String getRefName() {
		return this.refName;
	}

	
	private void setSpecificProperties(){
		properties = new HashMap<EntityProperty, List<String>>();
		properties.put(SpeciesProperty.HABITAT, Arrays.asList(this.habitat));
		properties.put(SpeciesProperty.FIGIS, Arrays.asList(this.FLODSpeciesEntity.getFigisID()));
		properties.put(SpeciesProperty.ASFIS, Arrays.asList(this.FLODSpeciesEntity.getAlphacode(),
															this.FLODSpeciesEntity.getAsfisScientificName(),
															this.FLODSpeciesEntity.getAsfisEnglishName()));
		if(this.FLODSpeciesEntity.getAphiaID() != null){//control because not all species in FLOD have worms info
			properties.put(SpeciesProperty.WORMS, Arrays.asList(
													this.FLODSpeciesEntity.getAphiaID(),
													this.FLODSpeciesEntity.getWormsScientificName()));
		}
		properties.put(SpeciesProperty.FAO, Arrays.asList("fao-species-map-"+this.getCode().toLowerCase()));
		properties.put(SpeciesProperty.FLOD, Arrays.asList(this.FLODSpeciesEntity.getASFISCodedEntity()));
		
	}	
	
	public Map<EntityProperty, List<String>> getSpecificProperties() {
		return properties;
	}

	
	public CoordinateReferenceSystem getCRS() {
		CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
		return crs;
	}

	public String getFigisId() {
		return this.FLODSpeciesEntity.getFigisID();
	}

	public String getViewerIdentifier() {
		return this.getCode()+"-"+this.getSpecificProperties().get(SpeciesProperty.HABITAT).get(0);
	}


	public String getViewerProj() {
		return "4326";
	}


	private void setGisProperties(){
		this.gisProperties  = new HashMap<GisProperty,String>();
		gisProperties.put(GisProperty.STYLE, "species_style");
		gisProperties.put(GisProperty.PROJECTION, "EPSG:4326");
		
	}
	
	
	public Map<GisProperty, String> getGisProperties() {
		return this.gisProperties;
	}

}
