/**
 *
 */
package org.kalypso.kml.export.convert;

import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Polygon;

import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Polygon;

/**
 * @author Dirk Kuch
 */
public class ConverterMultiSurface
{
  public static MultiGeometry convert( final GM_MultiSurface gmo ) throws Exception
  {
    final MultiGeometry multGeometry = new MultiGeometry();

    final GM_Polygon[] surfaces = gmo.getAllSurfaces();
    for( final GM_Polygon surface : surfaces )
    {
      final Polygon geometry = ConverterSurface.convert( surface );
      multGeometry.addToGeometry( geometry );
    }

    return multGeometry;
  }
}