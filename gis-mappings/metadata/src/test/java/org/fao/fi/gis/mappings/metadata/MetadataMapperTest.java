package org.fao.fi.gis.mappings.metadata;

import java.util.Map;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * MetadataMapperTest
 * 
 * @author Emmanuel Blondel <emmanuel.blondel@fao.org><emmanuel.blondel1@gmail.com>
 *
 */
public class MetadataMapperTest {

	private static String baseUrl = "http://www.fao.org/figis/geoserver";
	private static String workspace = "species";
	private static String authority = "FLOD";
	private static String entity = "http://www.fao.org/figis/flod/entities/codedentity/4bad4ac2-6e9b-49df-9fb2-25504aafbf42";
	private MetadataMapper mapper;
	
	@Before
	public void setUp() throws JAXBException{
		mapper = new MetadataMapper(baseUrl, workspace);
	}
	
	@Test
	public void testMappings() {
		Map<String, Map<LayerProperty, String>> mappings = mapper
				.getMappings(authority);
		Assert.assertNotNull(mappings);
		Assert.assertNotSame(0, mappings.size());
		Assert.assertEquals("species:SPECIES_DIST_BLC", mappings.get(entity)
				.get(LayerProperty.NAME));
		Assert.assertEquals(
				"FAO aquatic species distribution map of Anadara granosa",
				mappings.get(entity).get(LayerProperty.TITLE));
		Assert.assertEquals(
				"http://www.fao.org/geonetwork/srv/en/csw?service=CSW&request=GetRecordById&Version=2.0.2&elementSetName=full&outputSchema=http://www.isotc211.org/2005/gmd&id=fao-species-map-blc",
				mappings.get(entity).get(LayerProperty.METADATAURL));
	}
	
}
