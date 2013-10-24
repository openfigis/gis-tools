package org.fao.fi.gis;

import java.net.MalformedURLException;

import org.fao.fi.gis.data.DataPublisher;
import org.fao.fi.gis.metadata.MetadataPublisher;

/**
 * Data & Metadata Publisher
 * 
 * @author eblondel (FAO)
 *
 */
public class Publisher {
	
	DataPublisher dataPublisher;
	MetadataPublisher metadataPublisher;
	
	/**
	 * Publisher
	 * 
	 * @param geographicEntityType
	 * @param revisionDate
	 * @param version
	 * @param gsBaseURL
	 * @param gsUser
	 * @param gsPassword
	 * @param gnBaseURL
	 * @param gnUser
	 * @param gnPassword
	 * @param srcGSWorkspace
	 * @param srcGSLayer
	 * @param srcAttribute
	 * @param trgGSWorkspace
	 * @param trgGSDatastore
	 * @param trgGSLayerPrefix
	 * @param fcURL
	 * @throws MalformedURLException
	 */
	public Publisher(
			String revisionDate, String version,
			String gsBaseURL, String gsUser, String gsPassword,
			String gnBaseURL, String gnUser, String gnPassword,
			String srcGSWorkspace, String srcGSLayer, String srcAttribute,
			String trgGSWorkspace, String trgGSDatastore, String trgGSLayerPrefix,
			String fcURL
			
			) throws MalformedURLException{
	
		dataPublisher = new DataPublisher(
									gsBaseURL, gsUser, gsPassword,
									srcGSLayer, srcAttribute,
									trgGSWorkspace, trgGSDatastore, trgGSLayerPrefix,
									gnBaseURL);
		
		metadataPublisher = new MetadataPublisher(
									revisionDate, version,
									gnBaseURL, gnUser, gnPassword);
	}
	
	
	public DataPublisher getDataPublisher(){
		return this.dataPublisher;
	}
	
	public MetadataPublisher getMetadataPublisher(){
		return this.metadataPublisher;
	}

}
