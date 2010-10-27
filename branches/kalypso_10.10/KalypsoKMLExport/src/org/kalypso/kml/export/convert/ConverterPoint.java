/**
 *
 */
package org.kalypso.kml.export.convert;

import org.kalypso.kml.export.utils.GoogleEarthUtils;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Point;

import de.micromata.opengis.kml.v_2_2_0.Point;

/**
 * @author Dirk Kuch
 */
public class ConverterPoint
{
  public static Point convert( final GM_Point gmo ) throws Exception
  {
    final Point point = new Point();

    final IGeoTransformer transformer = GeoTransformerFactory.getGeoTransformer( GoogleEarthUtils.GOOGLE_EARTH_CS );
    final GM_Point kmlPoint = (GM_Point) transformer.transform( gmo );

    point.addToCoordinates( kmlPoint.getX(), kmlPoint.getY() );

    return point;
  }

}
