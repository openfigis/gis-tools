package org.fao.fi.gis.metadata;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.fao.fi.gis.metadata.entity.EntityProperty;
import org.fao.fi.gis.metadata.entity.GeographicEntity;
import org.fao.fi.gis.metadata.model.content.MetadataContact;
import org.fao.fi.gis.metadata.model.content.MetadataThesaurus;
import org.fao.fi.gis.metadata.util.Utils;
import org.geotoolkit.internal.jaxb.gmx.Anchor;
import org.geotoolkit.metadata.iso.DefaultIdentifier;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.metadata.iso.citation.DefaultAddress;
import org.geotoolkit.metadata.iso.citation.DefaultCitation;
import org.geotoolkit.metadata.iso.citation.DefaultCitationDate;
import org.geotoolkit.metadata.iso.citation.DefaultContact;
import org.geotoolkit.metadata.iso.citation.DefaultOnlineResource;
import org.geotoolkit.metadata.iso.citation.DefaultResponsibleParty;
import org.geotoolkit.metadata.iso.citation.DefaultTelephone;
import org.geotoolkit.metadata.iso.constraint.DefaultLegalConstraints;
import org.geotoolkit.metadata.iso.distribution.DefaultDigitalTransferOptions;
import org.geotoolkit.metadata.iso.distribution.DefaultDistribution;
import org.geotoolkit.metadata.iso.extent.DefaultExtent;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.metadata.iso.identification.DefaultBrowseGraphic;
import org.geotoolkit.metadata.iso.identification.DefaultDataIdentification;
import org.geotoolkit.metadata.iso.identification.DefaultKeywords;
import org.geotoolkit.metadata.iso.lineage.DefaultLineage;
import org.geotoolkit.metadata.iso.maintenance.DefaultMaintenanceInformation;
import org.geotoolkit.metadata.iso.quality.DefaultDataQuality;
import org.geotoolkit.metadata.iso.quality.DefaultScope;
import org.geotoolkit.metadata.iso.spatial.DefaultGeometricObjects;
import org.geotoolkit.metadata.iso.spatial.DefaultVectorSpatialRepresentation;
import org.geotoolkit.util.SimpleInternationalString;

import org.opengis.metadata.citation.Contact;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.metadata.citation.OnlineResource;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.citation.Telephone;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.distribution.DigitalTransferOptions;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.KeywordType;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.MaintenanceFrequency;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.spatial.GeometricObjectType;
import org.opengis.metadata.spatial.TopologyLevel;
import com.vividsolutions.jts.geom.Envelope;

/**
 * A class aimed to handle generic geographicEntityMetadata that can be then
 * used for different collections: species, rfb, etc
 * 
 * We define here a geographic entity a dataset that can be mapped one-to-one to
 * a domain entity such as SPECIES, RFB, VME
 * 
 * 
 * @author eblondel
 * 
 */
public class GeographicEntityMetadata extends DefaultMetadata {

	private static final String INSPIRE_THESAURUS_CITATION = "GEMET - INSPIRE themes, version 1.0";

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -6602734832572502929L;

	protected GeographicEntity entity;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	Calendar c = Calendar.getInstance();

	private Date lastRevisionDate;
	private String lastVersion;
	private String title;

	Collection<? extends Constraints> constraints;
	DefaultResponsibleParty ORGANIZATION;

	/**
	 * Constructs a GeographicEntity metadata
	 * 
	 * @throws URISyntaxException
	 * @throws ParseException
	 * 
	 */
	public GeographicEntityMetadata(GeographicEntity entity, String revisionDate, String version) throws URISyntaxException, ParseException {

		super();
		this.entity = entity;

		this.lastRevisionDate = sdf.parse(revisionDate);
		this.lastVersion = version;
		if(entity.getTemplate().getHasBaseTitle()){
			this.title = entity.getTemplate().getBaseTitle() + entity.getRefName();
		}else{
			this.title = entity.getRefName();
		}

		// build the metadata
		this.setIdentifier(entity.getIdentifier()); // identifier
		this.setDateStamp(this.lastRevisionDate);

		this.setLocales(Arrays.asList(Locale.ENGLISH)); // Locales
		this.setLanguage(Locale.ENGLISH); // Language
		this.setCharacterSet(CharacterSet.UTF_8); // Encoding

		this.setMetadataStandardName("ISO 19115:2003/19139"); // standard
		this.setMetadataStandardVersion("1.0"); // version
		this.getHierarchyLevels().add(ScopeCode.DATASET); // hierarchical level

		this.setOrganization(); // FAO main contact
		this.setContacts(); // contacts
		this.setDataQuality(); // methodology if existing
		this.setSpatialRepresentation(); // spatial representation
		this.setReferenceSystemInfo(Arrays.asList(entity.getCRS())); // ReferenceSystem
		this.setMetadataConstraints(); // constraints
		this.setDistributionInfo();
		this.setIdentificationInfo();

	}

	protected GeographicEntity getEntity() {
		return this.entity;
	}

	protected Date getRevisionDate() {
		return this.lastRevisionDate;
	}

	protected String getVersion() {
		return this.lastVersion;
	}

	protected DefaultResponsibleParty getOrganization() {
		return this.ORGANIZATION;
	}

	/**
	 * Set the metadata identifier
	 * 
	 * @param fileIdentifier
	 */
	private void setIdentifier(String fileIdentifier) {
		if (fileIdentifier != null) {
			this.setFileIdentifier(fileIdentifier);
		} else {
			UUID uuid = UUID.randomUUID();
			String fileId = uuid.toString();
			this.setFileIdentifier(fileId);
		}

	}

	/**
	 * set Organization (FAO)
	 * 
	 * @throws URISyntaxException
	 */
	private void setOrganization() throws URISyntaxException {
		// Main responsible party
		this.ORGANIZATION = new DefaultResponsibleParty();

		// contact info
		final DefaultContact contactORG = new DefaultContact();
		final DefaultOnlineResource resourceORG = new DefaultOnlineResource();
		resourceORG.setName(entity.getTemplate().getOrganizationContact().getName());
		resourceORG.setLinkage(new URI(entity.getTemplate().getOrganizationContact().getUrl()));
		contactORG.setOnlineResource(resourceORG);

		// Address
		final DefaultAddress addressORG = new DefaultAddress();
		addressORG.getDeliveryPoints().add(entity.getTemplate().getOrganizationContact().getAddress()); // deliveryPoint
		addressORG.setCity(new SimpleInternationalString(entity.getTemplate().getOrganizationContact().getCity())); // city
		addressORG.setPostalCode(entity.getTemplate().getOrganizationContact().getPostalCode()); // postal code
		addressORG.setCountry(new SimpleInternationalString(entity.getTemplate().getOrganizationContact().getCountry())); // country
		contactORG.setAddress(addressORG);

		ORGANIZATION.setContactInfo(contactORG);
		ORGANIZATION.setOrganisationName(new SimpleInternationalString(
				entity.getTemplate().getOrganizationContact().getOrgName()));
		ORGANIZATION.setRole(Role.OWNER);
	}

	/**
	 * A method to set the list of contacts
	 * 
	 * @throws URISyntaxException
	 * 
	 * 
	 */
	private void setContacts() throws URISyntaxException {
		
		final List<ResponsibleParty> contacts = new ArrayList<ResponsibleParty>();
		
		for(MetadataContact iContact : entity.getTemplate().getIndividualContacts()){
		
			DefaultResponsibleParty rp = new DefaultResponsibleParty();
			
			// contact info
			final DefaultContact contact = new DefaultContact();
			final DefaultOnlineResource resource = new DefaultOnlineResource();
			resource.setName(iContact.getName());
			resource.setLinkage(new URI(iContact.getUrl()));
			contact.setOnlineResource(resource);
			
			// telephone
			DefaultTelephone tel = null;
			if(iContact.getMainPhone() != null){
				if(tel == null){
					tel = new DefaultTelephone();
				}
				tel.getVoices().add(iContact.getMainPhone());
			}
			if(iContact.getFax() != null){
				if(tel == null){
					tel = new DefaultTelephone();
				}
				tel.getVoices().add(iContact.getFax());
			}
			if(tel != null){
				contact.setPhone((Telephone) tel);
			}

			// Address
			DefaultAddress address = null;
			if(iContact.getAddress() != null){
				if(address == null){
					address = new DefaultAddress();
				}
				address.getDeliveryPoints().add(iContact.getAddress());
				address.setCity(new SimpleInternationalString(iContact.getCity()));
				address.setPostalCode(iContact.getPostalCode());
				address.setCountry(new SimpleInternationalString(iContact.getCountry()));
			}
			if(iContact.getMainEmail() != null){
				if(address == null){
					address = new DefaultAddress();
				}
				address.getElectronicMailAddresses().add(iContact.getMainEmail());
			}
			if(address != null){
				contact.setAddress(address);
			}

			rp.setContactInfo(contact);
			rp.setIndividualName(iContact.getIndividualName());
			rp.setOrganisationName(new SimpleInternationalString(iContact.getOrgName()));
			rp.setPositionName(new SimpleInternationalString(iContact.getPositionName()));
			rp.setRole(Role.POINT_OF_CONTACT);

			contacts.add(rp);
			this.setContacts(contacts);
		}

	}

	/**
	 * Data Quality / Lineage
	 * 
	 * 
	 */
	protected void setDataQuality() {
		DefaultDataQuality quality = new DefaultDataQuality();
		DefaultScope scope = new DefaultScope();
		scope.setLevel(ScopeCode.DATASET);
		quality.setScope(scope);

		DefaultLineage lineage = new DefaultLineage();
		lineage.setStatement(new SimpleInternationalString(entity.getTemplate()
				.getMethodology()));
		quality.setLineage(lineage);
		this.setDataQualityInfo(Arrays.asList(quality));
	}

	/**
	 * spatial representation
	 * 
	 */
	protected void setSpatialRepresentation() {
		DefaultVectorSpatialRepresentation spatialRepresentation = new DefaultVectorSpatialRepresentation();

		// Geometry objects
		DefaultGeometricObjects geomObjects = new DefaultGeometricObjects();
		geomObjects.setGeometricObjectType(GeometricObjectType.SURFACE);

		// count
		int count = this.entity.getFeaturesCount();

		geomObjects.setGeometricObjectCount(count);
		spatialRepresentation.setGeometricObjects(Arrays.asList(geomObjects));

		// topology level
		spatialRepresentation.setTopologyLevel(TopologyLevel.GEOMETRY_ONLY);

		this.setSpatialRepresentationInfo(Arrays.asList(spatialRepresentation));

	}

	/**
	 * A method to set the metadata constraints, e.g. legal use/access
	 * constraints such as license
	 * 
	 * LAST UPDATE: 2013/04/16
	 * 
	 */
	private void setMetadataConstraints() {

		// Legal constraints
		DefaultLegalConstraints legalConstraints = new DefaultLegalConstraints();
		legalConstraints.setUseConstraints(Arrays.asList(Restriction.COPYRIGHT,
				Restriction.LICENSE));
		legalConstraints
				.setUseLimitations(Arrays
						.asList(

						// license to use
						new SimpleInternationalString(entity.getTemplate()
								.getLicense()),

						// Usage for bibliography
								new SimpleInternationalString(
										"Usage subject to mandatory citation: (c) "+entity.getTemplate().getOrganizationContact().getAcronym()+", "
												+ c.get(Calendar.YEAR)+ ". "
												+ entity.getTemplate().getCollection()+ ". "
												+ this.title
												+ " ("+ entity.getCode()+ "). "
												+ "In: "+entity.getTemplate().getOrganizationContact().getName()+" [online]. "
												+ entity.getTemplate().getOrganizationContact().getCity()+". [Cited <DATE>] "
												+ entity.getTemplate()
														.getCollectionURL()),

								// Disclaimer
								new SimpleInternationalString(entity
										.getTemplate().getDisclaimer())));
		legalConstraints.setAccessConstraints(Arrays.asList(
				Restriction.COPYRIGHT, Restriction.LICENSE));

		// set constraints
		this.constraints = Arrays.asList(legalConstraints);
	}

	/**
	 * Species DistributionInfo
	 * 
	 */
	private void setDistributionInfo() {
		try {
			DefaultDistribution distribution = new DefaultDistribution();

			DefaultDigitalTransferOptions option = new DefaultDigitalTransferOptions();
			Set<OnlineResource> resources = new HashSet<OnlineResource>();

			// website main resource
			// ---------------------------
			DefaultOnlineResource fiweb = new DefaultOnlineResource();
			fiweb.setLinkage(new URI(entity.getTemplate().getCollectionURL()));
			fiweb.setProtocol("WWW:LINK-1.0-http--link");
			fiweb.setDescription(new SimpleInternationalString(entity
					.getTemplate().getCollection()));
			fiweb.setFunction(OnLineFunction.INFORMATION);
			resources.add(fiweb);

			// factsheet (if it exists)
			// ---------------------------
			if(entity.getFactsheet() != null){
				DefaultOnlineResource factsheet = new DefaultOnlineResource();
				factsheet.setLinkage(new URI(entity.getFactsheet()));
				factsheet.setProtocol("WWW:LINK-1.0-http--link");
				factsheet.setDescription(new SimpleInternationalString(
						"Factsheet - Summary description"));
				factsheet.setFunction(OnLineFunction.INFORMATION);
				resources.add(factsheet);
			}

			// viewer Resource (if it exists)
			// -------------------------------
			if(entity.getViewerIdentifier() != null){
				DefaultOnlineResource viewerResource = new DefaultOnlineResource();
				viewerResource.setLinkage(entity.getViewerResource());
				viewerResource.setProtocol("WWW:LINK-1.0-http--link");
				viewerResource.setDescription(new SimpleInternationalString(entity
						.getTemplate().getCollection() + " (GIS Viewer)"));
				viewerResource.setFunction(OnLineFunction.INFORMATION);
				resources.add(viewerResource);
			}
			
			// OGC standard data protocols
			// ===========================
			// WMS resource
			// ----------------
			DefaultOnlineResource wmsResource = new DefaultOnlineResource();
			wmsResource.setLinkage(new URI(entity.getGSBaseURL() + "/"
					+ entity.getTRGWorkspace() + "/ows?SERVICE=WMS"));
			// "&srs="+entity.getGisProperties().get(GisProperty.PROJECTION)+
			// "&styles="+entity.getGisProperties().get(GisProperty.STYLE)));
			wmsResource.setProtocol("OGC:WMS-1.3.0-http-get-map");
			wmsResource.setName(entity.getTargetLayerName());
			wmsResource.setDescription(new SimpleInternationalString(this.title));
			resources.add(wmsResource);

			// WFS resource (both GML and SHP)
			// -------------------------------
			// GML
			DefaultOnlineResource wfsResource1 = new DefaultOnlineResource();
			wfsResource1.setLinkage(new URI(entity.getGSBaseURL() + "/"
					+ entity.getTRGWorkspace()
					+ "/ows?service=WFS&request=GetFeature&version=1.0.0"
					+ "&typeName=" + entity.getTargetLayerName()));
			wfsResource1.setProtocol("OGC:WFS-1.0.0-http-get-feature");
			wfsResource1.setName(entity.getTargetLayerName());
			wfsResource1.setDescription(new SimpleInternationalString(
					"GIS data (WFS - GML)"));
			wfsResource1.setFunction(OnLineFunction.DOWNLOAD);
			resources.add(wfsResource1);

			// SHP
			// note: in the future we should see to customize the SHAPE-ZIP so
			// it handles the metadata. This will require Geoserver
			// developements
			String shpFileName = "FAO_" + entity.getTargetLayerName();
			DefaultOnlineResource wfsResource2 = new DefaultOnlineResource();
			wfsResource2.setLinkage(new URI(entity.getGSBaseURL() + "/"
					+ entity.getTRGWorkspace()
					+ "/ows?service=WFS&request=GetFeature&version=1.0.0"
					+ "&typeName=" + entity.getTargetLayerName()
					+ "&outputFormat=SHAPE-ZIP" + "&format_options=filename:"
					+ shpFileName + ".zip"));

			wfsResource2.setProtocol("OGC:WFS-1.0.0-http-get-feature");
			wfsResource2.setName(entity.getTargetLayerName());
			wfsResource2.setDescription(new SimpleInternationalString(
					"GIS data (WFS - Shapefile)"));
			wfsResource2.setFunction(OnLineFunction.DOWNLOAD);
			resources.add(wfsResource2);

			// Metadata formats
			// =================

			// Geonetwork - metadata as XML ISO 19115/19139
			// ----------------
			DefaultOnlineResource xmlResource = new DefaultOnlineResource();
			xmlResource.setLinkage(new URI(Utils.getXMLMetadataURL(
					entity.getGNBaseURL(), this.getFileIdentifier())));
			xmlResource.setProtocol("WWW:LINK-1.0-http--link");
			xmlResource.setName("XML");
			xmlResource.setDescription(new SimpleInternationalString(
					"metadata (XML)"));
			resources.add(xmlResource);

			option.setOnLines(resources);
			Set<DigitalTransferOptions> options = new HashSet<DigitalTransferOptions>();
			options.add(option);
			distribution.setTransferOptions(options);
			this.setDistributionInfo(distribution);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void setIdentificationInfo() throws URISyntaxException,
			ParseException {

		DefaultDataIdentification identification = new DefaultDataIdentification();

		// language
		// --------
		identification.getLanguages().add(Locale.ENGLISH);

		// citation
		// --------
		DefaultCitation citation = new DefaultCitation();
		citation.setCitedResponsibleParties(Arrays.asList(this
				.getOrganization()));

		// Identifier
		DefaultIdentifier identifier = new DefaultIdentifier();
		identifier.setCode(this.getFileIdentifier());
		citation.setIdentifiers(Arrays.asList(identifier));

		// title
		citation.setTitle(new SimpleInternationalString(this.title));
		DefaultCitationDate citationDate = new DefaultCitationDate();
		citationDate.setDate(this.getRevisionDate());
		citationDate.setDateType(DateType.REVISION);
		citation.setDates(Arrays.asList(citationDate));

		// edition
		citation.setEdition(new SimpleInternationalString(this.getVersion()));
		citation.setEditionDate(this.getRevisionDate());

		// presentation form
		citation.getPresentationForms().add(PresentationForm.MAP_DIGITAL);

		identification.setCitation(citation); // add to the identification info

		// point of contact
		identification.setPointOfContacts(this.getContacts());

		// resource constraints
		identification.setResourceConstraints(this.constraints);

		// extent
		// ------
		DefaultExtent extent = new DefaultExtent();
		DefaultGeographicBoundingBox boundingBox = new DefaultGeographicBoundingBox();
		Envelope bbox = entity.getBBOX();
		if (bbox != null) {
			boundingBox.setWestBoundLongitude(bbox.getMinX());
			boundingBox.setEastBoundLongitude(bbox.getMaxX());
			boundingBox.setSouthBoundLatitude(bbox.getMinY());
			boundingBox.setNorthBoundLatitude(bbox.getMaxY());
		} else {
			boundingBox.setWestBoundLongitude(-180);
			boundingBox.setEastBoundLongitude(180);
			boundingBox.setSouthBoundLatitude(-90);
			boundingBox.setNorthBoundLatitude(90);
		}

		extent.getGeographicElements().add(boundingBox);
		identification.getExtents().add(extent);

		// abstract
		// -------
		identification.setAbstract(new SimpleInternationalString(this.title+ ". "+ entity.getTemplate().getAbstract()));

		// purpose
		// -------
		identification.setPurpose(new SimpleInternationalString(entity
				.getTemplate().getPurpose()));
		// maintenance information
		// -----------------------
		DefaultMaintenanceInformation info = new DefaultMaintenanceInformation();
		info.setMaintenanceAndUpdateFrequency(MaintenanceFrequency.AS_NEEDED);
		identification.getResourceMaintenances().add(info);

		// graphic overview
		// ----------------
		DefaultBrowseGraphic graphic = new DefaultBrowseGraphic();
		graphic.setFileDescription(new SimpleInternationalString("Map overview"));
		graphic.setFileName(entity.getLayerGraphicOverview());
		graphic.setFileType("image/png");
		identification.setGraphicOverviews(Arrays.asList(graphic));

		// descriptive keywords
		// --------------------
		// TODO

		List<DefaultKeywords> keywordsList = new ArrayList<DefaultKeywords>();

		// add general thesaurus
		for (MetadataThesaurus thesaurus : entity.getTemplate()
				.getThesaurusList()) {

			DefaultKeywords keywords = new DefaultKeywords();
			keywords.setType(KeywordType.THEME);
			DefaultCitation kwCitation = new DefaultCitation();
			DefaultCitationDate kwCitationDate = new DefaultCitationDate();

			if (thesaurus.getName().matches(INSPIRE_THESAURUS_CITATION)) {
				kwCitationDate.setDate(sdf.parse("2008-06-01"));
				kwCitationDate.setDateType(DateType.PUBLICATION);
			} else {
				kwCitationDate.setDate(this.getRevisionDate());
				kwCitationDate.setDateType(DateType.REVISION);
			}
			kwCitation.setDates(Arrays.asList(kwCitationDate));
			kwCitation.setTitle(new SimpleInternationalString(thesaurus
					.getName()));
			keywords.setThesaurusName(kwCitation);
			for (String kw : thesaurus.getKeywords()) {
				keywords.getKeywords().add(new SimpleInternationalString(kw));
			}

			keywordsList.add(keywords);
		}

		// add entity-based thesaurus
		for (Entry<EntityProperty, List<String>> entityType : entity
				.getSpecificProperties().entrySet()) {
			if (entityType.getKey().isThesaurus()) {

				DefaultKeywords keywords = new DefaultKeywords();
				keywords.setType(KeywordType.THEME);
				DefaultCitation kwCitation = new DefaultCitation();

				DefaultCitationDate kwCitationDate = new DefaultCitationDate();
				kwCitationDate.setDate(this.getRevisionDate());
				kwCitationDate.setDateType(DateType.REVISION);
				kwCitation.setDates(Arrays.asList(kwCitationDate));
				kwCitation.setTitle(new SimpleInternationalString(entityType
						.getKey().authority().name()));
				keywords.setThesaurusName(kwCitation);

				// TODO add more citation for thesaurus (e.g. add authority
				// href)

				if (entityType.getKey().containsURIs()) {
					for (String kw : entityType.getValue()) {
						keywords.getKeywords().add(new Anchor(new URI(kw), kw));
					}
				} else {
					for (String kw : entityType.getValue()) {
						keywords.getKeywords().add(
								new SimpleInternationalString(kw));
					}
				}
				keywordsList.add(keywords);
			}
		}

		// add keywords to identification info
		identification.setDescriptiveKeywords(keywordsList);

		// character set
		// -------------
		identification.getCharacterSets().add(CharacterSet.UTF_8);

		// topic category
		// --------------
		List<TopicCategory> categories = new ArrayList<TopicCategory>();
		for(String cat : entity.getTemplate().getTopicCategories()){
			categories.add(TopicCategory.valueOf(cat));
		}
		identification.setTopicCategories(categories);

		// additional information
		// ----------------------
		identification
				.setSupplementalInformation(new SimpleInternationalString(entity.getTemplate().getSupplementaryInformation()));

		// add identification info
		this.getIdentificationInfo().add(identification);

	}

}
