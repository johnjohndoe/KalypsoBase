/**
 *
 */
package org.kalypso.kml.export.utils;

import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.Symbolizer;

import de.micromata.opengis.kml.v_2_2_0.Style;

/**
 * @author Dirk Kuch
 */
public class GoogleEarthExportUtils
{

  /**
   * @param m_factory
   * @param symbolizer
   * @return
   * @throws FilterEvaluationException
   */
  public static boolean updateStyle( final Style style, final Symbolizer symbolizer ) throws FilterEvaluationException
  {
    // FIXME

    return true;
// if( symbolizer instanceof PointSymbolizer )
// else if( symbolizer instanceof LineSymbolizer )
// else if( symbolizer instanceof PolygonSymbolizer )
// else if( symbolizer instanceof TextSymbolizer )
  }

}
