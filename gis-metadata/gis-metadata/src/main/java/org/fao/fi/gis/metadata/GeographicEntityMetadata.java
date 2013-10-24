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

import org.fao.fi.gis.entity.EntityProperty;
import org.fao.fi.gis.entity.GeographicEntity;
import org.fao.fi.gis.util.Utils;
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
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.citation.Telephone;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.distribution.DigitalTransferOptions;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.KeywordType;
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

	Collection<? extends Constraints> constraints;
	DefaultResponsibleParty FAO;

	/**
	 * Constructs a GeographicEntity metadata
	 * 
	 * @throws URISyntaxException
	 * @throws ParseException
	 * 
	 */
	public GeographicEntityMetadata(GeographicEntity entity,
			String fileIdentifier, String revisionDate, String version

	) throws URISyntaxException, ParseException {

		super();
		this.entity = entity;

		this.lastRevisionDate = sdf.parse(revisionDate);
		this.lastVersion = version;

		// build the metadata
		this.setIdentifier(fileIdentifier); // identifier
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
		return this.FAO;
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
		// FAO responsible party
		this.FAO = new DefaultResponsibleParty();

		// contact info
		final DefaultContact contactFAO = new DefaultContact();
		final DefaultOnlineResource resourceFAO = new DefaultOnlineResource();
		resourceFAO.setName("FAO Fisheries and Aquaculture Department");
		resourceFAO.setLinkage(new URI("http://www.fao.org/fishery/en"));
		contactFAO.setOnlineResource(resourceFAO);

		// Address
		final DefaultAddress addressFAO = new DefaultAddress();
		addressFAO.getDeliveryPoints().add("Viale delle Terme di Caracalla"); // deliveryPoint
		addressFAO.setCity(new SimpleInternationalString("Rome")); // city
		addressFAO.setPostalCode("00153"); // postal code
		addressFAO.setCountry(new SimpleInternationalString("Italy")); // country
		contactFAO.setAddress(addressFAO);

		FAO.setContactInfo((Contact) contactFAO);
		FAO.setOrganisationName(new SimpleInternationalString(
				"FAO Fisheries and Aquaculture Department"));// organization
																// name
		FAO.setRole(Role.OWNER);
	}

	/**
	 * A method to set the list of contacts
	 * 
	 * @throws URISyntaxException
	 * 
	 * 
	 */
	private void setContacts() throws URISyntaxException {
		// Responsible party
		// -----------------

		// Responsible party 1
		DefaultResponsibleParty responsibleParty1 = new DefaultResponsibleParty();

		// contact info
		final DefaultContact contact = new DefaultContact();
		final DefaultOnlineResource resource = new DefaultOnlineResource();
		resource.setName("Food & Agriculture Organization of the United Nations");
		resource.setLinkage(new URI("http://www.fao.org"));
		contact.setOnlineResource(resource);

		// telephone
		final DefaultTelephone tel = new DefaultTelephone();
		tel.getVoices().add("+39 06 570 55176");// voice
		tel.getFacsimiles().add("+39 06 570 53020");// fax
		contact.setPhone((Telephone) tel);

		// Address
		final DefaultAddress address = new DefaultAddress();
		address.getDeliveryPoints().add("Viale delle Terme di Caracalla"); // deliveryPoint
		address.setCity(new SimpleInternationalString("Rome")); // city
		address.setPostalCode("00153"); // postal code
		address.setCountry(new SimpleInternationalString("Italy")); // country
		address.getElectronicMailAddresses().add("Fabio.Carocci@fao.org"); // email
		contact.setAddress(address);

		responsibleParty1.setContactInfo((Contact) contact);

		responsibleParty1.setIndividualName("Fabio Carocci");// individual name
		responsibleParty1.setOrganisationName(new SimpleInternationalString(
				"FAO - Fisheries Management and Conservation Service"));// organization
																		// name
		responsibleParty1.setPositionName(new SimpleInternationalString(
				"Fishery Resource Officer"));// position name
		responsibleParty1.setRole(Role.POINT_OF_CONTACT);// role

		// Responsible party 2
		// *******************
		DefaultResponsibleParty responsibleParty2 = new DefaultResponsibleParty();

		// contact info
		final DefaultContact contact2 = new DefaultContact();
		final DefaultOnlineResource resource2 = new DefaultOnlineResource();
		resource2
				.setName("Food & Agriculture Organization of the United Nations");
		resource2.setLinkage(new URI("http://www.fao.org"));
		contact2.setOnlineResource(resource);

		// Address
		final DefaultAddress address2 = new DefaultAddress();
		address2.getElectronicMailAddresses().add("Emmanuel.Blondel@fao.org"); // email
		contact2.setAddress(address2);

		responsibleParty2.setContactInfo((Contact) contact2);

		responsibleParty2.setIndividualName("Emmanuel Blondel");// individual
																// name
		responsibleParty2
				.setOrganisationName(new SimpleInternationalString(
						"FAO - Fisheries and Aquaculture Department. Statistics and Information"));// organization
																									// name
		responsibleParty2.setPositionName(new SimpleInternationalString(
				"GIS Consultant"));// position name
		responsibleParty2.setRole(Role.POINT_OF_CONTACT);// role

		this.setContacts(Arrays.asList(responsibleParty1, responsibleParty2));

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
										"Usage subject to mandatory citation: (c) FAO, "
												+ c.get(Calendar.YEAR)
												+ ". "
												+ entity.getTemplate()
														.getCollection()
												+ ". "
												+ entity.getTemplate()
														.getBaseTitle()
												+ entity.getRefName()
												+ " ("
												+ entity.getCode()
												+ "). "
												+ "In: FAO Fisheries and Aquaculture Department [online]. Rome. [Cited <DATE>] "
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

			// resource 1 (FAO/FI website)
			// ---------------------------
			DefaultOnlineResource fiweb = new DefaultOnlineResource();
			fiweb.setLinkage(new URI(entity.getTemplate().getCollectionURL()));
			fiweb.setProtocol("WWW:LINK-1.0-http--link");
			fiweb.setDescription(new SimpleInternationalString(entity
					.getTemplate().getCollection()));
			fiweb.setFunction(OnLineFunction.INFORMATION);
			resources.add(fiweb);

			// resource 1 (FAO/FI website)
			// ---------------------------
			DefaultOnlineResource factsheet = new DefaultOnlineResource();
			factsheet.setLinkage(new URI("http://www.fao.org/fishery/"
					+ entity.getFigisDomain() + "/" + entity.getFigisId()));
			factsheet.setProtocol("WWW:LINK-1.0-http--link");
			factsheet.setDescription(new SimpleInternationalString(
					"Factsheet - Summary description"));
			factsheet.setFunction(OnLineFunction.INFORMATION);
			resources.add(factsheet);

			// viewer Resource (Species Distribution Map Viewer)
			// --------------------------------------------
			DefaultOnlineResource viewerResource = new DefaultOnlineResource();
			viewerResource.setLinkage(entity.getViewerResource());
			viewerResource.setProtocol("WWW:LINK-1.0-http--link");
			viewerResource.setDescription(new SimpleInternationalString(entity
					.getTemplate().getCollection() + " (GIS Viewer)"));
			viewerResource.setFunction(OnLineFunction.INFORMATION);
			resources.add(viewerResource);

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
			wmsResource.setDescription(new SimpleInternationalString(entity
					.getTemplate().getBaseTitle() + entity.getRefName()));
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
		citation.setTitle(new SimpleInternationalString(entity.getTemplate()
				.getBaseTitle() + entity.getRefName()));
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
		identification.setAbstract(new SimpleInternationalString(entity
				.getTemplate().getBaseTitle()
				+ entity.getRefName()
				+ ". "
				+ entity.getTemplate().getAbstract()));

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
		for (Entry<String, List<String>> thesaurus : entity.getTemplate()
				.getThesaurusList().entrySet()) {

			DefaultKeywords keywords = new DefaultKeywords();
			keywords.setType(KeywordType.THEME);
			DefaultCitation kwCitation = new DefaultCitation();
			DefaultCitationDate kwCitationDate = new DefaultCitationDate();

			if (thesaurus.getKey().matches(INSPIRE_THESAURUS_CITATION)) {
				kwCitationDate.setDate(sdf.parse("2008-06-01"));
				kwCitationDate.setDateType(DateType.PUBLICATION);
			} else {
				kwCitationDate.setDate(this.getRevisionDate());
				kwCitationDate.setDateType(DateType.REVISION);
			}
			kwCitation.setDates(Arrays.asList(kwCitationDate));
			kwCitation.setTitle(new SimpleInternationalString(thesaurus
					.getKey()));
			keywords.setThesaurusName(kwCitation);
			for (String kw : thesaurus.getValue()) {
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
		identification.setTopicCategories(entity.getTemplate().getTopics());

		// additional information
		// ----------------------
		identification
				.setSupplementalInformation(new SimpleInternationalString(entity.getTemplate().getSupplementaryInformation()));

		// add identification info
		this.getIdentificationInfo().add(identification);

	}

}
