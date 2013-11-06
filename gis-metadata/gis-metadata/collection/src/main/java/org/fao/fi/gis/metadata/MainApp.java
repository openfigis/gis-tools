package org.fao.fi.gis.metadata;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fao.fi.gis.metadata.collection.species.FLODSpeciesEntity;
import org.fao.fi.gis.metadata.collection.species.SpeciesEntity;
import org.fao.fi.gis.metadata.feature.FeatureTypeProperty;
import org.fao.fi.gis.metadata.entity.EntityAddin;
import org.fao.fi.gis.metadata.entity.GeographicEntity;
import org.fao.fi.gis.metadata.model.MetadataConfig;
import org.fao.fi.gis.metadata.publisher.Publisher;
import org.fao.fi.gis.metadata.util.FeatureTypeUtils;
import org.fao.fi.gis.metadata.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main App to launch the batch data/metadata publication
 * 
 */
public class MainApp {

	private static Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

	static Map<String, Map<EntityAddin, String>> set = null;
	
	
	/**
	 * Main
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		//Read the configuration
		LOGGER.info("(1) Loading the configuration file");
		MetadataConfig config = MetadataConfig.fromXML(new File("c:/gis/metadata/config/species.xml"));
		
		//read the 
		LOGGER.info("(2) Loading the reference list");
		set = CollectionUtils.parseSpeciesList(config.getSettings()
				.getPublicationSettings().getCodelistURL());
		// specific cases (for testing)
				set.clear();
				Map<EntityAddin, String> addins = new HashMap<EntityAddin, String>();
				addins.put(EntityAddin.Habitat, "m");
				set.put("GRN", addins);
		// configure the publisher
		Publisher publisher = new Publisher(config);

		int size = 0;

		// UTILS
		Set<String> metalist = new HashSet<String>();

		List<String> existingLayers = publisher.getDataPublisher().GSReader
				.getLayers().getNames(); // read once, improve performance

		// iteration on the entities
		LOGGER.info("(3) Start metadata creation & publication");
		Iterator<String> entityIterator = set.keySet().iterator();
		while (entityIterator.hasNext()) {

			String code = entityIterator.next();
			LOGGER.info("==============");
			LOGGER.info("Publishing single layer & metadata for: "+code);

			Map<FeatureTypeProperty, Object> geoproperties = null;
			GeographicEntity entity = null;

			boolean exist = existingLayers.contains(config.getSettings().getGeographicServerSettings().getTargetLayerPrefix() + "_" + code);
			
			String action = config.getSettings().getPublicationSettings().getAction();
			LOGGER.info("== ACTION: "+action+" == ");
			
			//CHECK ACTION
			if (action.matches("CHECK")) {	
				if (!exist) {
					FLODSpeciesEntity flodEntity = new FLODSpeciesEntity(code);
					if (flodEntity.getFlodContent() != null) {
						metalist.add(code);
					}
				}
				
			// PUBLISH ACTION
			} else if (action.matches("PUBLISH")) {
		
				// calculate geoproperties
				while (geoproperties == null) {
					geoproperties = FeatureTypeUtils
							.computeFeatureTypeProperties(config.getSettings().getGeographicServerSettings(),
														code, config.getSettings().getPublicationSettings().getBuffer());
				}
				// configure entity & publish
				FLODSpeciesEntity flodEntity = new FLODSpeciesEntity(code);
				if (flodEntity.getFlodContent() != null) {
					entity = new SpeciesEntity(code, config.getContent(), geoproperties, set.get(code),
											   config.getSettings().getGeographicServerSettings(),
											   config.getSettings().getMetadataCatalogueSettings());
					
					publisher.publish(entity, exist);
					size = size + 1;
					
					LOGGER.info(size + " published metalayers");
				}

			// UNPUBLISH ACTION
			} else if (action.matches("UNPUBLISH")) {
				// configure entity & publish
				FLODSpeciesEntity flodEntity = new FLODSpeciesEntity(code);
				if (flodEntity.getFlodContent() != null) {
					entity = new SpeciesEntity(code, config.getContent(), geoproperties, set.get(code),
							   config.getSettings().getGeographicServerSettings(),
							   config.getSettings().getMetadataCatalogueSettings());
				}
			
				publisher.unpublish(entity, exist);
			}
		}

		// list all items for which the metadata production was not performed
		if(metalist.size() > 0){
			LOGGER.info("=======================================");
			LOGGER.info("=========== NOT PRODUCED ==============");
			LOGGER.info("==================|====================");
			LOGGER.info("==================V====================");
			LOGGER.info("=======================================");
			Iterator<String> it2 = metalist.iterator();
			while (it2.hasNext()) {
				LOGGER.info(it2.next());
			}
		}
	}
}
