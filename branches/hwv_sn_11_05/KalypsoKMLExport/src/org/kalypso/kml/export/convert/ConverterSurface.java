/**
 *
 */
package org.kalypso.kml.export.convert;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.kalypso.kml.export.utils.GoogleEarthUtils;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Polygon;

/**
 * @author Dirk Kuch
 */
public class ConverterSurface
{

  /**
   * @param factory
   * @param gmo
   * @param style
   * @throws Exception
   */
  public static Polygon convert( final GM_Surface< ? > gmo ) throws Exception
  {
    /* handling of multigeometries not implemented at the moment */
    if( gmo.size() > 1 )
      throw new NotImplementedException();

    final IGeoTransformer transformer = GeoTransformerFactory.getGeoTransformer( GoogleEarthUtils.GOOGLE_EARTH_CS );

    for( int i = 0; i < gmo.size(); i++ )
    {
      final Polygon polygoneType = new Polygon();

      final Object object = gmo.get( i );
      if( !(object instanceof GM_Polygon) )
        continue;

      final GM_Polygon polygon = (GM_Polygon) object;

      /* set outer boundary */

      final LinearRing outerRing = new LinearRing();
      final GM_Position[] exteriorRing = polygon.getExteriorRing();

      for( final GM_Position position : exteriorRing )
      {
        final GM_Point point = GeometryFactory.createGM_Point( position, gmo.getCoordinateSystem() );
        final GM_Point kmlPoint = (GM_Point) transformer.transform( point );

        outerRing.addToCoordinates( kmlPoint.getX(), kmlPoint.getY() );
      }

      final Boundary outerBoundary = new Boundary();
      outerBoundary.setLinearRing( outerRing );
      polygoneType.setOuterBoundaryIs( outerBoundary );

      // get inner boundaries
      final List<Boundary> innerBoundaries = polygoneType.getInnerBoundaryIs();

      final GM_Position[][] interiorRings = polygon.getInteriorRings();
      for( final GM_Position[] innerRing : interiorRings )
      {

        final LinearRing innerLinearRing = new LinearRing();

        for( final GM_Position position : innerRing )
        {
          final GM_Point point = GeometryFactory.createGM_Point( position, gmo.getCoordinateSystem() );
          final GM_Point kmlPoint = (GM_Point) transformer.transform( point );

          innerLinearRing.addToCoordinates( kmlPoint.getX(), kmlPoint.getY() );
        }

        final Boundary innerBoundary = new Boundary();
        innerBoundary.setLinearRing( innerLinearRing );
        innerBoundaries.add( innerBoundary );
      }

      return polygoneType;
    }

    throw new NotImplementedException();
  }
}
