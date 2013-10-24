package org.fao.fi.gis.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.metadata.identification.TopicCategory;

/**
 * A metadata Content template class
 * 
 * @author eblondel
 *
 */
public class MetadataContentTemplate {

	private String collection;
	private String collectionURL;
	private String basetitle;
	private String abstractText;
	private String purpose;
	private String methodology;
	private String supplementaryInfo;
	private String license;
	private String disclaimer;
	private Map<String,List<String>> thesaurusList;
	private Set<TopicCategory> topicsList;
	
	public MetadataContentTemplate(){
	}	
	
	public void setCollection(String collection){
		this.collection = collection;
	}
	
	public String getCollection(){
		return this.collection;
	}
	
	public void setCollectionURL(String url){
		this.collectionURL = url;
	}
	
	public String getCollectionURL(){
		return this.collectionURL;
	}
	
	public void setBaseTitle(String basetitle){
		this.basetitle = basetitle;
	}
	
	public String getBaseTitle(){
		return this.basetitle;
	}	
	
	public void setAbstract(String abstractText){
		this.abstractText = abstractText;
	}

	public String getAbstract(){
		return this.abstractText;
	}
	
	
	public void setPurpose(String purpose){
		this.purpose = purpose;
	}

	public String getPurpose(){
		return this.purpose;
	}
	
	public void setMethodology(String methodology){
		this.methodology = methodology;
	}
	
	public String getMethodology(){
		return this.methodology;
	}
	
	public void setLicense(String license){
		this.license = license;
	}

	public String getLicense(){
		return this.license;
	}
	
	public void setDisclaimer(String disclaimer){
		this.disclaimer = disclaimer;
	}
	
	public String getDisclaimer(){
		return this.disclaimer;
	}
	
	public void addThesaurus(String theme, List<String> keywords){
		if(this.thesaurusList == null){
			this.thesaurusList = new HashMap<String, List<String>>();
		}
		this.thesaurusList.put(theme, keywords);
	}

	public Map<String,List<String>> getThesaurusList(){
		return this.thesaurusList;
	}
	
	public void addTopic(TopicCategory topic){
		if(this.topicsList == null){
			this.topicsList = new HashSet<TopicCategory>();
		}
		this.topicsList.add(topic);
	}
	
	public Set<TopicCategory> getTopics(){
		return this.topicsList;
	}
	
	public void setSupplementaryInformation(String info){
		this.supplementaryInfo = info;
	}
	
	public String getSupplementaryInformation(){
		return this.supplementaryInfo;
	}
	
}

