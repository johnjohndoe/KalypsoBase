/**
 *
 */
package org.kalypso.kml.export.convert;

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

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;

/**
 * @author Dirk Kuch
 */
public class ConvertFacade
{
  public static void convert( final IKMLAdapter[] providers, final Folder folder, final GM_Object[] geometries, final Style style, final Feature feature ) throws Exception
  {
    for( final GM_Object gmo : geometries )
    {
      final GEOMETRY_TYPE gt = GeoUtils.getGeoType( gmo );
      if( GEOMETRY_TYPE.eMultiCurve.equals( gt ) )
      {
        final Placemark placemark = folder.createAndAddPlacemark();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );

        final MultiGeometry multiGeometry = ConverterMultiCurve.convert( (GM_MultiCurve) gmo );
        placemark.setGeometry( multiGeometry );

        if( style != null )
          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$
      }
      else if( GEOMETRY_TYPE.eCurve.equals( gt ) )
      {
        final Placemark placemark = folder.createAndAddPlacemark();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );

        final LineString lineString = ConverterCurve.convert( (GM_Curve) gmo );
        placemark.setGeometry( lineString );

        if( style != null )
          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$
      }
      else if( GEOMETRY_TYPE.eMultiSurface.equals( gt ) )
      {
        final Placemark placemark = folder.createAndAddPlacemark();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );

        final MultiGeometry multiGeometry = ConverterMultiSurface.convert( (GM_MultiSurface) gmo );
        placemark.setGeometry( multiGeometry );

        if( style != null )
          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$
      }
      else if( GEOMETRY_TYPE.eSurface.equals( gt ) )
      {
        final Placemark placemark = folder.createAndAddPlacemark();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );

        final Polygon geometry = ConverterSurface.convert( (GM_Surface< ? >) gmo );
        placemark.setGeometry( geometry );

        if( style != null )
          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$
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
  }
}
