package org.fao.fi.gis.metadata.association;

import org.fao.fi.gis.metadata.authority.AuthorityEntity;

public interface GeographicMetaObjectProperty {

	AuthorityEntity authority();

	boolean isThesaurus();

	boolean containsURIs();
}
