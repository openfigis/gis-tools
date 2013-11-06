package org.fao.fi.gis.metadata.publisher;

import java.net.MalformedURLException;
import java.util.Map;

import org.fao.fi.gis.metadata.entity.EntityAddin;
import org.fao.fi.gis.metadata.entity.GeographicEntity;
import org.fao.fi.gis.metadata.model.MetadataConfig;
import org.fao.fi.gis.metadata.model.settings.Settings;
import org.fao.fi.gis.metadata.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data & Metadata Publisher
 * 
 * @author eblondel (FAO)
 *
 */
public class Publisher {
	
	private static Logger LOGGER = LoggerFactory.getLogger(Publisher.class);
	
	private Settings settings;
	DataPublisher dataPublisher;
	MetadataPublisher metadataPublisher;
	

	/**
	 * Publisher
	 * 
	 * @param config
	 * @throws MalformedURLException
	 */
	public Publisher(MetadataConfig config) throws MalformedURLException{
	
		settings = config.getSettings();
		dataPublisher = new DataPublisher(config.getSettings().getGeographicServerSettings(),
										  config.getSettings().getMetadataCatalogueSettings());
		
		metadataPublisher = new MetadataPublisher(config.getSettings().getMetadataCatalogueSettings(),
												  config.getSettings().getPublicationSettings());
	}
	
	
	public DataPublisher getDataPublisher(){
		return this.dataPublisher;
	}
	
	public MetadataPublisher getMetadataPublisher(){
		return this.metadataPublisher;
	}
	
	/**
	 * Method to publish
	 * 
	 * @param publisher
	 * @param entity
	 * @throws Exception
	 */
	public void publish(GeographicEntity entity, boolean exist) throws Exception {

		if (exist) {

			// force data publication
			if (this.settings.getPublicationSettings().isForceMetadata()) {
				try {
					this.getMetadataPublisher().deleteMetadata(entity);
				} catch (Exception e) {
					LOGGER.warn("No metadata for id = "+entity.getMetaIdentifier());
				}

				this.getMetadataPublisher().publishMetadata(entity);

			}

			if (this.settings.getPublicationSettings().isForceData() == true) {
				this.getDataPublisher().deleteLayer(entity);
				this.getDataPublisher().publishLayer(entity);
			}

		} else {
			LOGGER.info("Publish new layer");
			this.getMetadataPublisher().publishMetadata(entity);
			this.getDataPublisher().publishLayer(entity);

		}

		int sleep = 3;
		Thread.sleep(3*1000);
		LOGGER.info("Sleeping "+sleep+" seconds");
		
	}

	/**
	 * Method to unpublish
	 * 
	 * @param entity
	 * @param exist
	 * @throws Exception
	 */
	public void unpublish(GeographicEntity entity, boolean exist) throws Exception {

		if (this.settings.getPublicationSettings().isUnpublishData()) {
			if (!exist) {
				this.getDataPublisher().deleteOnlyFeatureType(entity);

			} else {
				this.getDataPublisher().deleteLayer(entity);
			}
		}

		if (this.settings.getPublicationSettings().isUnpublishMetadata()) {
			try {
				this.getMetadataPublisher().deleteMetadata(entity);
			} catch (Exception e) {
				LOGGER.warn("metadata was yet deleted");
			}

		}

		int sleep = 3;
		Thread.sleep(3*1000);
		LOGGER.info("Sleeping "+sleep+" seconds");
	}

}
