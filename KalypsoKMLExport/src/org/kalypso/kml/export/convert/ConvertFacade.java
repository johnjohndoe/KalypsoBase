/**
 *
 */
package org.kalypso.kml.export.convert;

import java.util.ArrayList;
import java.util.List;

import net.opengis.kml.AbstractFeatureType;
import net.opengis.kml.ObjectFactory;
import net.opengis.kml.PlacemarkType;
import net.opengis.kml.StyleType;

import org.apache.commons.lang.NotImplementedException;
import org.kalypso.kml.export.geometry.GeoUtils;
import org.kalypso.kml.export.geometry.GeoUtils.GEOMETRY_TYPE;
import org.kalypso.kml.export.interfaces.IKMLAdapter;
import org.kalypso.kml.export.utils.KMLAdapterUtils;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Surface;

/**
 * @author Dirk Kuch
 */
public class ConvertFacade
{
  public static AbstractFeatureType[] convert( final IKMLAdapter[] providers, final ObjectFactory factory, final GM_Object[] geometries, final StyleType style, final Feature feature ) throws Exception
  {
    final List<AbstractFeatureType> featureTypes = new ArrayList<AbstractFeatureType>();

    for( final GM_Object gmo : geometries )
    {
      final GEOMETRY_TYPE gt = GeoUtils.getGeoType( gmo );
      if( GEOMETRY_TYPE.eMultiCurve.equals( gt ) )
      {
        final PlacemarkType placemark = factory.createPlacemarkType();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );
        placemark.setAbstractGeometryGroup( factory.createMultiGeometry( ConverterMultiCurve.convert( factory, (GM_MultiCurve) gmo ) ) );

        if( style != null )
          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$

        featureTypes.add( placemark );
      }
      else if( GEOMETRY_TYPE.eCurve.equals( gt ) )
      {
        final PlacemarkType placemark = factory.createPlacemarkType();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );
        placemark.setAbstractGeometryGroup( factory.createLineString( ConverterCurve.convert( factory, (GM_Curve) gmo ) ) );

        if( style != null )
          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$

        featureTypes.add( placemark );
      }
      else if( GEOMETRY_TYPE.eMultiSurface.equals( gt ) )
      {
        final PlacemarkType placemark = factory.createPlacemarkType();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );
        placemark.setAbstractGeometryGroup( factory.createMultiGeometry( ConverterMultiSurface.convert( factory, (GM_MultiSurface) gmo ) ) );

        if( style != null )
          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$

        featureTypes.add( placemark );
      }
      else if( GEOMETRY_TYPE.eSurface.equals( gt ) )
      {
        final PlacemarkType placemark = factory.createPlacemarkType();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );
        placemark.setAbstractGeometryGroup( factory.createPolygon( ConverterSurface.convert( factory, (GM_Surface< ? >) gmo ) ) );

        if( style != null )
          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$

        featureTypes.add( placemark );
      }
      else if( GEOMETRY_TYPE.ePoint.equals( gt ) )
      {

        // FIXME implement
// IPlacemarkIcon myPlacemark = null;
//
// for( final IGoogleEarthAdapter adapter : providers )
// {
// myPlacemark = adapter.getPlacemarkIcon( feature );
// if( myPlacemark != null )
// break;
// }
//
// if( myPlacemark != null )
// {
// final PlacemarkType placemark = factory.createPlacemarkType();
// placemark.setName( feature.getId() );
//
// final StyleTypeFactory styleTypeFactory = StyleTypeFactory.getStyleFactory( factory );
//
// final StyleType iconStyle = styleTypeFactory.createIconStyle( "http://www.heise.de/icons/ho/heise.gif" );
// placemark.setStyleUrl( "#" + iconStyle.getId() );
//
// featureTypes.add( placemark );
// }
// else
// {
// final PlacemarkType placemark = factory.createPlacemarkType();
// placemark.setName( feature.getId() );
//
// placemark.setGeometry( factory.createPoint( ConverterPoint.convert( factory, (GM_Point) gmo ) ) );
// if( style != null )
// placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$
//
// featureTypes.add( placemark );
// }
      }
      else
        throw new NotImplementedException();

    }

    return featureTypes.toArray( new AbstractFeatureType[] {} );
  }
}
