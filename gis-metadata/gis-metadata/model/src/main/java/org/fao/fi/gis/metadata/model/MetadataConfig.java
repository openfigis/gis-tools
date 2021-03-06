package org.fao.fi.gis.metadata.model;

import java.io.File;
import java.io.IOException;

import org.fao.fi.gis.metadata.model.content.MetadataContact;
import org.fao.fi.gis.metadata.model.content.MetadataContent;
import org.fao.fi.gis.metadata.model.content.MetadataThesaurus;
import org.fao.fi.gis.metadata.model.settings.Settings;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;


/**
 * Main Metadata configuration
 * 
 * @author eblondel
 *
 */
public class MetadataConfig {
	
	private Settings settings;
	private MetadataContent content;
	
	/**
	 * Constructs a MetadataConfig
	 * 
	 */
	public MetadataConfig(){
		
	}
	
	/**
	 * @return the settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * @param settings the settings to set
	 */
	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	/**
	 * @return the content
	 */
	public MetadataContent getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(MetadataContent content) {
		this.content = content;
	}

	/**
	 * Parsing from XML
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static MetadataConfig fromXML(File file) {

		XStream xstream = new XStream(new StaxDriver());
		xstream.alias("configuration", MetadataConfig.class);
		xstream.alias("contact", MetadataContact.class);
		xstream.alias("thesaurus", MetadataThesaurus.class);

		MetadataConfig config = (MetadataConfig) xstream
				.fromXML(file);

		return config;
	}
	
}
