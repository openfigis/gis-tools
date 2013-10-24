package org.fao.fi.gis.entity;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.fao.fi.gis.metadata.MetadataContentTemplate;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public interface GeographicEntity {

	MetadataContentTemplate getTemplate();

	String getGSBaseURL();

	String getGNBaseURL();

	String getSRCWorkspace();

	String getSRCLayername();

	String getSRCAttribute();

	String getTRGWorkspace();

	String getTRGLayerprefix();

	String getCode();

	String getTargetLayerName();

	Envelope getBBOX();

	int getFeaturesCount();

	URI getLayerGraphicOverview();

	URI getViewerResource();

	String getViewerIdentifier();

	String getViewerProj();

	CoordinateReferenceSystem getCRS();

	String getRefName();

	Map<EntityProperty, List<String>> getSpecificProperties();

	Map<GisProperty, String> getGisProperties();

	String getFigisId();

	String getFigisDomain();

}
