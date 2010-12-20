/**
 *
 */
package org.kalypso.kml.export.convert;

import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;

import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;

/**
 * @author Dirk Kuch
 */
public class ConverterMultiCurve
{

  /**
   * @param factory
   * @param string
   * @param multiCurve
   * @return
   * @throws Exception
   */
  public static MultiGeometry convert( final GM_MultiCurve multiCurve ) throws Exception
  {
    final MultiGeometry multiGeometry = new MultiGeometry();

    final GM_Curve[] curves = multiCurve.getAllCurves();
    for( final GM_Curve curve : curves )
    {
      final LineString lineString = ConverterCurve.convert( curve );
      multiGeometry.addToGeometry( lineString );
    }

    return multiGeometry;
  }
}
