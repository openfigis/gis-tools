package org.fao.fi.gis.metadata.model.content;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata Thesaurus
 * 
 * @author eblondel
 *
 */
public class MetadataThesaurus {

	private String name;
	private List<String> keywords = new ArrayList<String>();
	
	/**
	 * Constructs a Metadata Thesaurus
	 * 
	 */
	public MetadataThesaurus(){
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @param keyword
	 */
	public void addKeyword(String keyword){
		this.keywords.add(keyword);
	}
	
	/**
	 * 
	 * @return the list of keywords
	 */
	public List<String> getKeywords(){
		return this.keywords;
	}
	
	
}
