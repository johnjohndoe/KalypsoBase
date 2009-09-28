/**
 *
 */
package org.kalypso.kml.export.convert;

import java.util.List;

import javax.xml.bind.JAXBElement;

import net.opengis.kml.AbstractGeometryType;
import net.opengis.kml.MultiGeometryType;
import net.opengis.kml.ObjectFactory;

import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Surface;

/**
 * @author Dirk Kuch
 */
public class ConverterMultiSurface
{

  /**
   * @param factory
   * @param gmo
   * @return
   * @throws Exception
   */
  public static MultiGeometryType convert( final ObjectFactory factory, final GM_MultiSurface gmo ) throws Exception
  {
    final MultiGeometryType multiGeometryType = factory.createMultiGeometryType();
    final List<JAXBElement< ? extends AbstractGeometryType>> geometries = multiGeometryType.getAbstractGeometryGroup();

    final GM_Surface< ? >[] surfaces = gmo.getAllSurfaces();
    for( final GM_Surface< ? > surface : surfaces )
      geometries.add( factory.createPolygon( ConverterSurface.convert( factory, surface ) ) );

    return multiGeometryType;
  }

}
