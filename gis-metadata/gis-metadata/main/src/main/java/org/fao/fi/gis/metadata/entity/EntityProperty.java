package org.fao.fi.gis.metadata.entity;

import org.fao.fi.gis.metadata.authority.AuthorityEntity;

public interface EntityProperty {

	AuthorityEntity authority();

	boolean isThesaurus();

	boolean containsURIs();
}
