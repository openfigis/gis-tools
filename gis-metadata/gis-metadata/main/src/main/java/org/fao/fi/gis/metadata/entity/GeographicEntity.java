package org.fao.fi.gis.metadata.entity;

import java.util.List;
import java.util.Map;

import org.fao.fi.gis.metadata.association.GeographicMetaObjectProperty;
import org.fao.fi.gis.metadata.model.MetadataConfig;

/**
 * GeographicEntity interface
 * 
 * @author eblondel
 *
 */
public interface GeographicEntity {

	//identification
	
	String getCode();

	String getRefName();
	
	String getMetaIdentifier();

	Map<GeographicMetaObjectProperty, List<String>> getSpecificProperties();

	MetadataConfig getConfig();
	
	//specific to FIGIS
	
	String getFigisDomain();
	
	String getFigisId();

	String getFigisViewerId();

}
