package org.fao.fi.gis.metadata.association;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.fao.fi.gis.metadata.entity.EntityProperty;
import org.fao.fi.gis.metadata.entity.GeographicEntity;
import org.fao.fi.gis.metadata.entity.GisProperty;
import org.fao.fi.gis.metadata.model.content.MetadataContent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public interface GeographicAssociation {

	List<GeographicEntity> getEntities();
	
	String getCode();
	
	String getDomainName();
	
	String getMetaIdentifier();
	
	MetadataContent getTemplate();

	String getGSBaseURL();

	String getGNBaseURL();

	String getSRCWorkspace();

	String getSRCLayername();

	String getSRCAttribute();

	String getTRGWorkspace();

	String getTRGLayerprefix();

	String getTargetLayerName();
	
	String getBaseLayerWorkspace();
	
	String getBaseLayerName();

	Envelope getBBOX();

	int getFeaturesCount();

	URI getLayerGraphicOverview();

	URI getViewerResource();

	String getViewerIdentifier();

	CoordinateReferenceSystem getCRS();

	String getRefName();

	Map<EntityProperty, List<String>> getSpecificProperties();

	String getFigisId();
	
	String getFactsheet();


}