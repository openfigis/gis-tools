package org.fao.fi.gis.metadata.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fao.fi.gis.metadata.feature.FeatureTypeProperty;
import org.fao.fi.gis.metadata.model.settings.GeographicServerSettings;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.shapefile.ShapefileDataStoreFactory;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.storage.DataStoreException;
import org.opengis.feature.Feature;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;
import org.opengis.parameter.ParameterValueGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Envelope;

public final class FeatureTypeUtils {

	private static Logger LOGGER = LoggerFactory.getLogger(FeatureTypeUtils.class);
	
	/**
	 * 
	 * @param settings
	 * @param code
	 * @param buffer
	 * @return
	 */
	public static Map<FeatureTypeProperty, Object> computeFeatureTypeProperties(
			GeographicServerSettings settings,
			String code, double buffer) {

		Map<FeatureTypeProperty, Object> map = null;

		// bbox coordinates
		double bboxMinX = 0;
		double bboxMinY = 0;
		double bboxMaxX = 0;
		double bboxMaxY = 0;
		
		double maxNegX = -180;
		double maxPosX = 180;

		
		try{

			String wfsRequest= settings.getUrl() + "/" + settings.getSourceWorkspace()
					+ "/ows?service=wfs&version=1.0.0&request=GetFeature"
					+ "&typeName=" + settings.getSourceLayer() + "&cql_filter=" + settings.getSourceAttribute()
					+ "='" + code + "'";
			LOGGER.info("== WFS GetFeature Request ==");
			LOGGER.info(wfsRequest);
			URL url = new URL(wfsRequest);
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(url.openStream());
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("gml:featureMember");
			if (nList != null) {
				map = new HashMap<FeatureTypeProperty, Object>();
				if (nList.getLength() > 0) {
					LOGGER.info(nList.getLength() + " features");
					
					// ADD COUNT
					map.put(FeatureTypeProperty.COUNT, nList.getLength());

					for (int temp = 0; temp < nList.getLength(); temp++) {
						Node nNode = nList.item(temp);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {

							Element featureMember = (Element) nNode;
							Node bbox = (Node) featureMember
									.getElementsByTagName("gml:Box").item(0);

							// coords
							Element coords = (Element) bbox.getFirstChild();
							String[] gmlBounds = coords.getTextContent().split(" ");

							String[] min = gmlBounds[0].split(",");
							String[] max = gmlBounds[1].split(",");

							double minX = Double.parseDouble(min[0]);
							double minY = Double.parseDouble(min[1]);
							double maxX = Double.parseDouble(max[0]);
							double maxY = Double.parseDouble(max[1]);

							// first bbox recalculation
							if (temp == 0) {
								bboxMinX = minX;
								bboxMinY = minY;
								bboxMaxX = maxX;
								bboxMaxY = maxY;

							} else {
								// classic bbox expansion rule
								if (minX < bboxMinX)
									bboxMinX = minX;
								if (minY < bboxMinY)
									bboxMinY = minY;
								if (maxX > bboxMaxX)
									bboxMaxX = maxX;
								if (maxY > bboxMaxY)
									bboxMaxY = maxY;

							}

							if (maxX > maxNegX & maxX < 0)
								maxNegX = maxX;
							if (minX < maxPosX & minX > 0)
								maxPosX = minX;
						}

					}

					// final adjustment
					// ***************
					// in case maxNegX & maxPosX unchanged
					if (maxNegX == -180)
						maxNegX = -90;
					if (maxPosX == 180)
						maxPosX = 90;

					// for date-limit geographic distributions
					if (maxNegX < -90 & maxPosX > 90) {
						bboxMinX = maxPosX;
						bboxMaxX = 360 - Math.abs(maxNegX);
					}

					// control for globally distributed layers
					if (bboxMinX < -175.0 && bboxMaxX > 175.0) {
						bboxMinX = -180.0;
						bboxMaxX = 180.0;
						bboxMinY = -90.0;
						bboxMaxY = 90.0;
					}

					// NEW: control for overlimit latitude
					if (bboxMinY < -90)
						bboxMinY = -90;
					if (bboxMaxY > 90)
						bboxMaxY = 90;

					// set null if the connection was lost (coords = 0)
					// the main app will have to recalculate the bbox until
					// while bbox == null
					Envelope bounds = null;
					if (!(bboxMinX == 0 & bboxMaxX == 0 & bboxMinY == 0 & bboxMaxY == 0)) {

						// apply buffer
						if (!(bboxMinX < -180 + buffer)
								&& !(bboxMaxX > 180 - buffer)) {
							bboxMinX = bboxMinX - buffer;
							bboxMaxX = bboxMaxX + buffer;
						}

						if (!(bboxMinY < -90 + buffer)) {
							bboxMinY = bboxMinY - buffer;
						}
						if (!(bboxMaxY > 90 - buffer)) {
							bboxMaxY = bboxMaxY + buffer;

						}

						// build envelope
						bounds = new Envelope(bboxMinX, bboxMaxX,
								bboxMinY, bboxMaxY);
						map.put(FeatureTypeProperty.BBOX, bounds);
						LOGGER.info("Calculated Bounding Box");
						LOGGER.info("min X = "+String.valueOf(bboxMinX));
						LOGGER.info("max X = "+String.valueOf(bboxMaxX));
						LOGGER.info("min Y = "+String.valueOf(bboxMinY));
						LOGGER.info("max Y = "+String.valueOf(bboxMaxY));
						
						map.put(FeatureTypeProperty.CRS, DefaultGeographicCRS.WGS84); //TODO in the future we should manage the crs....
					}
				}else{
					//no feature members
					map.put(FeatureTypeProperty.COUNT, 0);
					
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Error trying to perform WFS GetFeature request");
		}

		return map;
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws DataStoreException 
	 * @throws MalformedURLException 
	 */
	public static Map<FeatureTypeProperty, Object> computeFeatureTypeProperties(String url, double buffer) throws DataStoreException, MalformedURLException {
	
		Map<FeatureTypeProperty, Object> map = new HashMap<FeatureTypeProperty, Object>();
		
		URL pURL = new URL(url);
		final ParameterValueGroup params = ShapefileDataStoreFactory.PARAMETERS_DESCRIPTOR.createValue();
	    Parameters.getOrCreate(ShapefileDataStoreFactory.URLP, params).setValue(pURL);
	    final FeatureStore shpStore = FeatureStoreFinder.open(params);
		Name pName = shpStore.getNames().iterator().next();
		FeatureCollection<Feature> pFC = shpStore.createSession(true).getFeatureCollection(QueryBuilder.all(pName));
		
		//optimize bbox
		// bbox coordinates
		double bboxMinX = 0;
		double bboxMinY = 0;
		double bboxMaxX = 0;
		double bboxMaxY = 0;
		
		double maxNegX = -180;
		double maxPosX = 180;
		
		//first bbox calculation
		Feature f1 = pFC.iterator().next();
		bboxMinX = f1.getBounds().getMinX();
		bboxMinY = f1.getBounds().getMinY();
		bboxMaxX = f1.getBounds().getMaxX();
		bboxMaxY = f1.getBounds().getMaxY();
		
		FeatureIterator<Feature> it = pFC.iterator();
		try{
			while(it.hasNext()){
				Feature f = it.next();
				BoundingBox env = f.getBounds();
				
				double minX = env.getMinX();
				double minY = env.getMinY();
				double maxX = env.getMaxX();
				double maxY = env.getMaxY();

				// classic bbox expansion rule
				if (minX < bboxMinX)
					bboxMinX = minX;
				if (minY < bboxMinY)
					bboxMinY = minY;
				if (maxX > bboxMaxX)
					bboxMaxX = maxX;
				if (maxY > bboxMaxY)
					bboxMaxY = maxY;

				if (maxX > maxNegX & maxX < 0)
					maxNegX = maxX;
				if (minX < maxPosX & minX > 0)
					maxPosX = minX;	
			}
		}finally{
			it.close();
			shpStore.dispose();
		}
		
		// final adjustment
		// ***************
		// in case maxNegX & maxPosX unchanged
		if (maxNegX == -180)
			maxNegX = -90;
		if (maxPosX == 180)
			maxPosX = 90;

		// for date-limit geographic distributions
		if (maxNegX < -90 & maxPosX > 90) {
			bboxMinX = maxPosX;
			bboxMaxX = 360 - Math.abs(maxNegX);
		}

		// control for globally distributed layers
		if (bboxMinX < -175.0 && bboxMaxX > 175.0) {
			bboxMinX = -180.0;
			bboxMaxX = 180.0;
			bboxMinY = -90.0;
			bboxMaxY = 90.0;
		}

		// control for overlimit latitude
		if (bboxMinY < -90)
			bboxMinY = -90;
		if (bboxMaxY > 90)
			bboxMaxY = 90;

		// set null if the connection was lost (coords = 0)
		// the main app will have to recalculate the bbox until
		// while bbox == null
		Envelope bounds = null;
		if (!(bboxMinX == 0 & bboxMaxX == 0 & bboxMinY == 0 & bboxMaxY == 0)) {

			// apply buffer
			if (!(bboxMinX < -180 + buffer)
					&& !(bboxMaxX > 180 - buffer)) {
				bboxMinX = bboxMinX - buffer;
				bboxMaxX = bboxMaxX + buffer;
			}

			if (!(bboxMinY < -90 + buffer)) {
				bboxMinY = bboxMinY - buffer;
			}
			if (!(bboxMaxY > 90 - buffer)) {
				bboxMaxY = bboxMaxY + buffer;

			}

			// build envelope
			bounds = new Envelope(bboxMinX, bboxMaxX,
					bboxMinY, bboxMaxY);
			LOGGER.info("Calculated Bounding Box");
			LOGGER.info("min X = "+String.valueOf(bboxMinX));
			LOGGER.info("max X = "+String.valueOf(bboxMaxX));
			LOGGER.info("min Y = "+String.valueOf(bboxMinY));
			LOGGER.info("max Y = "+String.valueOf(bboxMaxY));

		}
		
		map.put(FeatureTypeProperty.CRS, pFC.getFeatureType().getCoordinateReferenceSystem());
		map.put(FeatureTypeProperty.COUNT, pFC.size());
		map.put(FeatureTypeProperty.BBOX, bounds);

		return map;
	}

}
