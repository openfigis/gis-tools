package org.fao.fi.gis.metadata.association;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.fao.fi.gis.metadata.entity.GeographicEntity;
import org.fao.fi.gis.metadata.model.content.MetadataContent;
import org.fao.fi.gis.metadata.model.settings.GeographicServerSettings;
import org.fao.fi.gis.metadata.model.settings.MetadataCatalogueSettings;
import org.fao.fi.gis.metadata.model.settings.PublicationSettings;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public interface GeographicMetaObject {

	//Identification
	
	List<GeographicEntity> getEntities();
	
	String getCode();
	
	String getRefName();
	
	String getMetaIdentifier();
	
	String getMetaTitle();
	
	MetadataContent getTemplate();
	
	Map<GeographicMetaObjectProperty, List<String>> getSpecificProperties();
	
	//Settings

	GeographicServerSettings getGeographicServerSettings();
	
	MetadataCatalogueSettings getMetadataCatalogueSettings();
	
	PublicationSettings getPublicationSettings();
	
	String getTargetLayerName();

	
	//GIS

	CoordinateReferenceSystem getCRS();
	
	Envelope getBBOX();

	int getFeaturesCount();

	URI getLayerGraphicOverview();
	
	
	//SPECIFIC to FIGIS
	
	boolean isFromFigis();
	
	URI getFigisViewerResource();
	
	String getFigisFactsheet();




}