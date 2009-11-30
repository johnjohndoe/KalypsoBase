/**
 *
 */
package org.kalypso.kml.export.convert;

import java.util.List;

import javax.xml.bind.JAXBElement;

import net.opengis.kml.AbstractGeometryType;
import net.opengis.kml.MultiGeometryType;
import net.opengis.kml.ObjectFactory;

import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;

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
  public static MultiGeometryType convert( final ObjectFactory factory, final GM_MultiCurve multiCurve ) throws Exception
  {
    final MultiGeometryType multiGeometryType = factory.createMultiGeometryType();

    final List<JAXBElement< ? extends AbstractGeometryType>> geometries = multiGeometryType.getAbstractGeometryGroup();
    final GM_Curve[] curves = multiCurve.getAllCurves();
    for( final GM_Curve curve : curves )
      geometries.add( factory.createLineString( ConverterCurve.convert( factory, curve ) ) );

    return multiGeometryType;
  }

}
