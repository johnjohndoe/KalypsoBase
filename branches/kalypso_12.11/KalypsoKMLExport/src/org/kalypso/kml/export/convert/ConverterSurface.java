/**
 *
 */
package org.kalypso.kml.export.convert;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.kml.export.utils.GoogleEarthUtils;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_PolygonPatch;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Polygon;

/**
 * @author Dirk Kuch
 */
public class ConverterSurface
{
  public static Polygon convert( final GM_Polygon gmo ) throws Exception
  {
    /* handling of multigeometries not implemented at the moment */
    final GM_PolygonPatch polygon = gmo.getSurfacePatch();
    if( polygon == null )
      return null;

    final IGeoTransformer transformer = GeoTransformerFactory.getGeoTransformer( GoogleEarthUtils.GOOGLE_EARTH_CS );

    final Polygon polygoneType = new Polygon();

    /* set outer boundary */

    final LinearRing outerRing = new LinearRing();
    final GM_Position[] exteriorRing = polygon.getExteriorRing();

    for( final GM_Position position : exteriorRing )
    {
      final GM_Point point = GeometryFactory.createGM_Point( position, gmo.getCoordinateSystem() );
      final GM_Point kmlPoint = transformer.transform( point );

      outerRing.addToCoordinates( kmlPoint.getX(), kmlPoint.getY() );
    }

    final Boundary outerBoundary = new Boundary();
    outerBoundary.setLinearRing( outerRing );
    polygoneType.setOuterBoundaryIs( outerBoundary );

    // get inner boundaries
    final List<Boundary> innerBoundaries = polygoneType.getInnerBoundaryIs();

    final GM_Position[][] interiorRings = polygon.getInteriorRings();
    if( ArrayUtils.isEmpty( interiorRings ) )
      return polygoneType;

    for( final GM_Position[] innerRing : interiorRings )
    {

      final LinearRing innerLinearRing = new LinearRing();

      for( final GM_Position position : innerRing )
      {
        final GM_Point point = GeometryFactory.createGM_Point( position, gmo.getCoordinateSystem() );
        final GM_Point kmlPoint = transformer.transform( point );

        innerLinearRing.addToCoordinates( kmlPoint.getX(), kmlPoint.getY() );
      }

      final Boundary innerBoundary = new Boundary();
      innerBoundary.setLinearRing( innerLinearRing );
      innerBoundaries.add( innerBoundary );
    }

    return polygoneType;
  }
}
