/**
 *
 */
package org.kalypso.kml.export.convert;

import org.kalypso.kml.export.utils.GoogleEarthUtils;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

import de.micromata.opengis.kml.v_2_2_0.LineString;

/**
 * @author Dirk Kuch
 */
public class ConverterCurve
{
  public static LineString convert( final GM_Curve curve ) throws Exception
  {
    final LineString kmlLineString = new LineString();

    final IGeoTransformer transformer = GeoTransformerFactory.getGeoTransformer( GoogleEarthUtils.GOOGLE_EARTH_CS );

    final GM_LineString lineString = curve.getAsLineString();
    final GM_Position[] positions = lineString.getPositions();
    for( final GM_Position position : positions )
    {
      final GM_Point point = GeometryFactory.createGM_Point( position, curve.getCoordinateSystem() );
      final GM_Point kmlPoint = (GM_Point) transformer.transform( point );

      kmlLineString.addToCoordinates( kmlPoint.getX(), kmlPoint.getY() );
    }

    return kmlLineString;
  }
}