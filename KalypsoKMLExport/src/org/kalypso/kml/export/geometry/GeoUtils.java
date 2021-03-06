/**
 *
 */
package org.kalypso.kml.export.geometry;

import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;

/**
 * @author Dirk Kuch
 */
public class GeoUtils
{
  public enum GEOMETRY_TYPE
  {
    eMultiCurve,
    eCurve,
    eSurface,
    ePoint,
    eMultiSurface
  }

  public static GEOMETRY_TYPE getGeoType( final GM_Object gmo )
  {
    if( gmo instanceof GM_MultiCurve )
      return GEOMETRY_TYPE.eMultiCurve;
    else if( gmo instanceof GM_Curve )
      return GEOMETRY_TYPE.eCurve;
    else if( gmo instanceof GM_MultiSurface )
      return GEOMETRY_TYPE.eMultiSurface;
    else if( gmo instanceof GM_Polygon )
      return GEOMETRY_TYPE.eSurface;
    else if( gmo instanceof GM_Point )
      return GEOMETRY_TYPE.ePoint;

    throw new UnsupportedOperationException();
  }
}