package org.fao.fi.gis.metadata.rfb;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fao.fi.gis.data.FeatureTypeProperty;
import org.fao.fi.gis.entity.EntityAddin;
import org.fao.fi.gis.entity.EntityProperty;
import org.fao.fi.gis.entity.GeographicEntity;
import org.fao.fi.gis.entity.GeographicEntityImpl;
import org.fao.fi.gis.entity.GisProperty;
import org.fao.fi.gis.metadata.MetadataContentTemplate;
import org.fao.fi.gis.metadata.authority.AuthorityEntity;
import org.fao.fi.gis.metadata.species.SpeciesEntity.SpeciesProperty;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * RFB Entity
 * 
 * @author eblondel (FAO)
 *
 */
public class RfbEntity extends GeographicEntityImpl implements GeographicEntity{

	public enum RfbProperty implements EntityProperty{
		
		FAO (AuthorityEntity.FAO, true, true),
		FLOD (AuthorityEntity.FLOD, true, true),
		
		FIGIS(AuthorityEntity.FIGIS, true, false);
		
		private final AuthorityEntity authority;
		private final boolean thesaurus;
		private final boolean containsURIs;
		
		RfbProperty(AuthorityEntity authority, boolean thesaurus, boolean containsURIs){
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
	
	private FLODRfbEntity FLODRfbEntity;
	private String refName;
	private String style;
	private Map<EntityProperty, List<String>> properties;
	private Map<GisProperty, String> gisProperties;
	
	
	public RfbEntity(String code, MetadataContentTemplate template,
			String gsBaseURL, String gnBaseURL, String srcWorkspace,
			String srcLayer, String srcAttribute, String trgWorkspace,String trgLayerPrefix,
			Map<FeatureTypeProperty, Object> geoproperties,
			Map<EntityAddin,String> addins) throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
		
		super(code, template, gsBaseURL, gnBaseURL, srcWorkspace, srcLayer,
				srcAttribute, trgWorkspace, trgLayerPrefix,
				geoproperties,
				"rfb", code, addins);
		
		this.FLODRfbEntity = new FLODRfbEntity(code);
		this.setRefName();
		
		this.setSpecificProperties();
		
		this.style = addins.get(EntityAddin.Style);
		this.setGisProperties();
		this.setRfbAbstract();
	}

	
	public String getViewerProj() {
		return "4326";
	}

	public CoordinateReferenceSystem getCRS() {
		return DefaultGeographicCRS.WGS84;
	}

	private void setRefName(){
		this.refName = this.FLODRfbEntity.getName(); //here we don't want to acronym between parentesis as it is in FIGIS
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
		properties.put(RfbProperty.FIGIS, Arrays.asList(this.getCode(),
													    this.FLODRfbEntity.getName()));
		
		properties.put(RfbProperty.FLOD, Arrays.asList(this.FLODRfbEntity.getRfbCodedEntity()));
		properties.put(SpeciesProperty.FAO, Arrays.asList("fao-rfb-map-"+this.getCode().toLowerCase()));
	}
	
	public Map<EntityProperty, List<String>> getSpecificProperties() {
		return this.properties;
	}

	public String getFigisId() {
		return this.getCode();
	}
	
	
	private void setGisProperties(){
		this.gisProperties  = new HashMap<GisProperty,String>();
		gisProperties.put(GisProperty.STYLE, this.style);
		gisProperties.put(GisProperty.PROJECTION, "EPSG:4326");
		
	}
	
	
	public Map<GisProperty, String> getGisProperties() {
		return this.gisProperties;
	}
	
	private void setRfbAbstract() throws ParserConfigurationException, SAXException, IOException{
		
		String figisID = this.getAddins().get(EntityAddin.FigisID);
		
		System.out.println(figisID);
		URL fsURL = new URL("http://www.fao.org/fishery/xml/organization/"+figisID);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fsURL.openStream());	

		Element geoCoverage = (Element) doc.getDocumentElement().getElementsByTagName("fi:GeoCoverage").item(0);
		NodeList nodeList = geoCoverage.getElementsByTagName("fi:Text");
		if(nodeList.getLength() > 0){
			String abstractText = nodeList.item(0).getTextContent().replaceAll("<p>", "").replaceAll("</p>", "");
			this.getTemplate().setAbstract(abstractText);
		}
	
	}

}
