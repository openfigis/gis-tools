package org.fao.fi.gis;

import java.net.MalformedURLException;
import java.util.Map;

import org.fao.fi.gis.data.DataPublisher;
import org.fao.fi.gis.metadata.MetadataPublisher;
import org.fao.fi.gis.metadata.entity.EntityAddin;
import org.fao.fi.gis.metadata.entity.GeographicEntity;
import org.fao.fi.gis.metadata.model.MetadataConfig;
import org.fao.fi.gis.metadata.model.settings.Settings;
import org.fao.fi.gis.metadata.util.Utils;

/**
 * Data & Metadata Publisher
 * 
 * @author eblondel (FAO)
 *
 */
public class Publisher {
	
	private Settings settings;
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
	public void publish(GeographicEntity entity, Map<EntityAddin, String> addins, boolean exist) throws Exception {

		String metadataId = entity.getIdentifier();

		if (exist) {

			/* === UTILS === */
			// ***figisid
			// figislist.put(entity.getAphiaID(),"http://www.fao.org/fishery/species/"+entity.getFigisID());

			// ***create srv metadata record reference
			// metalist.add("<srv:operatesOn uuidref=\""+metadataId+"\"/>");

			// force data publication
			if (this.settings.getPublicationSettings().isForceMetadata()) {
				try {
					this.getMetadataPublisher().deleteMetadata(
							Utils.getXMLMetadataURL(this.settings.getMetadataCatalogueSettings().getUrl(), metadataId));
				} catch (Exception e) {
					System.out.println("No metadata for id = "+metadataId);
				}

				this.getMetadataPublisher().publishFullMetadata(
						metadataId, entity);

			}

			if (this.settings.getPublicationSettings().isForceData() == true) {
				this.getDataPublisher().deleteLayer(entity);
				this.getDataPublisher().publishLayer(entity,
						addins.get(EntityAddin.Style),
						metadataId);
			}

		} else {
			System.out.println("Publish new layer");
			this.getMetadataPublisher().publishFullMetadata(metadataId,
					entity);
			this.getDataPublisher().publishLayer(entity,
					addins.get(EntityAddin.Style),
					metadataId);

		}

		System.out.println("Sleeping 3 seconds");
		Thread.sleep(3000);
	}

	/**
	 * Method to unpublish
	 * 
	 * @param publisher
	 * @param entity
	 * @throws Exception
	 */
	public void unpublish(GeographicEntity entity, boolean exist) throws Exception {

		String metadataId = entity.getIdentifier();

		if (this.settings.getPublicationSettings().isUnpublishData()) {
			if (!exist) {
				this.getDataPublisher().deleteOnlyFeatureType(entity);

			} else {
				this.getDataPublisher().deleteLayer(entity);
			}
		}

		if (this.settings.getPublicationSettings().isUnpublishMetadata()) {
			try {
				this.getMetadataPublisher().deleteMetadata(
						Utils.getXMLMetadataURL(this.settings.getMetadataCatalogueSettings().getUrl(), metadataId));
			} catch (Exception e) {
				System.out.println("metadata was yet deleted");
			}

		}

		System.out.println("Sleeping 2 seconds");
		Thread.sleep(2000);
	}

}
