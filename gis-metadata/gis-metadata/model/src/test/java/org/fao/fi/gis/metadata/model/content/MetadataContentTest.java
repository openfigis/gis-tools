package org.fao.fi.gis.metadata.model.content;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.fao.fi.gis.metadata.model.content.MetadataContact;
import org.fao.fi.gis.metadata.model.content.MetadataContent;
import org.fao.fi.gis.metadata.model.content.MetadataThesaurus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * Test MetadataContent decoder
 * 
 * @author eblondel
 *
 */
public class MetadataContentTest {

	MetadataContent content;
	
	
	@Before
	public void setUp() throws URISyntaxException{
		File fileName = this.getResourceFile("content.xml");
		content = (MetadataContent) MetadataContent.fromXML(fileName);
	}
	
	@Test
	public void testTextualContent(){
		
		Assert.assertNotNull(content);
		Assert.assertEquals("collection1", content.getCollection());
		Assert.assertEquals("url1", content.getCollectionURL());
		Assert.assertTrue(content.getHasBaseTitle());
		Assert.assertEquals("basetitle", content.getBaseTitle());
		Assert.assertEquals("abstract", content.getAbstract());
		Assert.assertEquals("purpose", content.getPurpose());
		Assert.assertEquals("methodology", content.getMethodology());
		Assert.assertEquals("suppInfo", content.getSupplementaryInformation());
		Assert.assertEquals("license", content.getLicense());
		Assert.assertEquals("disclaimer", content.getDisclaimer());
	}
	
	@Test
	public void testThesaurusList(){
		List<MetadataThesaurus> thesaurusList = content.getThesaurusList();
		Assert.assertNotNull(thesaurusList);
		Assert.assertEquals(2, thesaurusList.size());
		
		Assert.assertEquals("General", thesaurusList.get(0).getName());
		List<String> keywords1 = thesaurusList.get(0).getKeywords();
		Assert.assertNotNull(keywords1);
		Assert.assertEquals(2,keywords1.size());
		Assert.assertEquals("keyword1", keywords1.get(0));
		Assert.assertEquals("keyword2", keywords1.get(1));
		
		Assert.assertEquals("INSPIRE", thesaurusList.get(1).getName());
		List<String> keywords2 = thesaurusList.get(1).getKeywords();
		Assert.assertNotNull(keywords2);
		Assert.assertEquals(2,keywords2.size());
		Assert.assertEquals("theme1", keywords2.get(0));
		Assert.assertEquals("theme2", keywords2.get(1));
		
	}

	@Test
	public void testOrganizationContact(){
		
		MetadataContact org = content.getOrganizationContact();
		Assert.assertNotNull(org);
		Assert.assertEquals("ORG", org.getAcronym());
		Assert.assertEquals("ORGANIZATION", org.getName());
		Assert.assertEquals("ORGANIZATION", org.getOrgName());
		Assert.assertEquals("http://www.organization.org", org.getUrl());
		Assert.assertEquals("address", org.getAddress());
		Assert.assertEquals("TheCity", org.getCity());
		Assert.assertEquals("TheCountry", org.getCountry());
		Assert.assertEquals("78", org.getPostalCode());
		
	}
	
	@Test
	public void testIndividualContacts(){
		
		List<MetadataContact> contacts = content.getIndividualContacts();
		Assert.assertNotNull(contacts);
		
		Assert.assertEquals("ORGANIZATION", contacts.get(0).getName());
		Assert.assertEquals("ORGANIZATION", contacts.get(0).getOrgName());
		Assert.assertEquals("http://www.organization.org", contacts.get(0).getUrl());
		Assert.assertEquals("address", contacts.get(0).getAddress());
		Assert.assertEquals("TheCity", contacts.get(0).getCity());
		Assert.assertEquals("TheCountry", contacts.get(0).getCountry());
		Assert.assertEquals("78", contacts.get(0).getPostalCode());
		
		Assert.assertEquals("someone@organization.org", contacts.get(0).getMainEmail());
		Assert.assertEquals("someone2@organization.org", contacts.get(1).getMainEmail());
		Assert.assertEquals("999", contacts.get(0).getMainPhone());
		Assert.assertEquals("999-2", contacts.get(1).getMainPhone());
		Assert.assertEquals("777", contacts.get(0).getFax());
		Assert.assertEquals("777-2", contacts.get(1).getFax());
		Assert.assertEquals("Some One", contacts.get(0).getIndividualName());
		Assert.assertEquals("Some One2", contacts.get(1).getIndividualName());
		Assert.assertEquals("His Position", contacts.get(0).getPositionName());
		Assert.assertEquals("His Position2", contacts.get(1).getPositionName());
		
	}
	
	@Test
	public void testTopicCategories(){
		List<String> categories = content.getTopicCategories();
		Assert.assertNotNull(categories);
		Assert.assertEquals(3, categories.size());
		Assert.assertEquals("BOUNDARIES", categories.get(0));
		Assert.assertEquals("OCEAN", categories.get(1));
		Assert.assertEquals("BIOTA", categories.get(2));
	}
	
	
	
	private File getResourceFile(String resource) throws URISyntaxException {
		return new File(this.getClass().getResource("/test-data/model/"+resource).toURI());
	}
	
}
