package org.kalypso.kml.export.utils;

import org.apache.commons.lang.NotImplementedException;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * @author Dirk Kuch
 */
public class GoogleEarthUtils
{
  /**
   * target coordinate system of google earth
   */
  public static final String GOOGLE_EARTH_CS = "EPSG:4326"; //$NON-NLS-1$

  /**
   * @param boundingBox
   * @param coordinatesSystem
   * @param factory
   * @param documentType
   * @throws Exception
   */
  public static void setLookAt( final GM_Envelope boundingBox, final String coordinatesSystem, final Kml kml ) throws Exception
  {
    // set look at to the middle of bounding box
    final IGeoTransformer transformer = GeoTransformerFactory.getGeoTransformer( GoogleEarthUtils.GOOGLE_EARTH_CS );

    final GM_Position min = boundingBox.getMin();

    // point
    final GM_Position pos = GeometryFactory.createGM_Position( min.getX() + boundingBox.getWidth() / 2, min.getY() + boundingBox.getHeight() / 2 );
    final GM_Point middle = (GM_Point) transformer.transform( GeometryFactory.createGM_Point( pos, coordinatesSystem ) );

    // FIXME
// final LookAtType lookAtType = factory.createLookAtType();
// lookAtType.setAltitude( 12000.0 );
// lookAtType.setLatitude( middle.getY() );
// lookAtType.setLongitude( middle.getX() );

    // FIXME changed in kml2.2
// documentType.setLookAt( lookAtType );
  }

  /**
   * @param boundingBox
   * @param srcCRS
   * @param factory
   * @param documentType
   * @throws Exception
   */
  public static void setMapBoundary( final GM_Envelope boundingBox, final String srcCRS, final Kml kml, final Document document ) throws Exception
  {
    final IGeoTransformer transformer = GeoTransformerFactory.getGeoTransformer( GoogleEarthUtils.GOOGLE_EARTH_CS );

    final GM_Position min = boundingBox.getMin();
    final GM_Position max = boundingBox.getMax();

    // north = min.getX
    // west = min.gety
    final GM_Point northWest = (GM_Point) transformer.transform( GeometryFactory.createGM_Point( min, srcCRS ) );

    // south = max.getX
    // east = max.getY
    final GM_Point southEast = (GM_Point) transformer.transform( GeometryFactory.createGM_Point( max, srcCRS ) );

    throw new NotImplementedException();

// // set google earth boundaries
// final LatLonAltBoxType latLonBox = factory.createLatLonAltBoxType();
// latLonBox.setNorth( northWest.getY() );
// latLonBox.setWest( northWest.getX() );
// latLonBox.setSouth( southEast.getY() );
// latLonBox.setEast( southEast.getX() );
//
// final RegionType region = factory.createRegionType();
// region.setLatLonAltBox( latLonBox );
// documentType.setRegion( region );

  }
}
