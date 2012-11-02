/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.kml.export.convert;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.kalypso.kml.export.geometry.GeoUtils;
import org.kalypso.kml.export.geometry.GeoUtils.GEOMETRY_TYPE;
import org.kalypso.kml.export.interfaces.IKMLAdapter;
import org.kalypso.kml.export.utils.KMLAdapterUtils;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.Font;
import org.kalypsodeegree.graphics.sld.PointSymbolizer;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Stroke;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.graphics.sld.TextSymbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree_impl.graphics.displayelements.DisplayElementFactory;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.LabelStyle;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;

/**
 * @author Dirk Kuch
 */
public class StyleConverter
{
  private final GraphicImageExporter m_iconExporter;

  public StyleConverter( final File imageDir, final String baseName, final GeoTransform transform )
  {
    m_iconExporter = new GraphicImageExporter( imageDir, baseName, transform );
  }

  public void convert( final IKMLAdapter[] providers, final Folder folder, final Symbolizer symbolizer, final Feature feature ) throws Exception
  {
    final GM_Object[] geometries = DisplayElementFactory.findGeometries( feature, symbolizer );

    for( final GM_Object gmo : geometries )
    {
      final GEOMETRY_TYPE gt = GeoUtils.getGeoType( gmo );
      if( GEOMETRY_TYPE.eMultiCurve.equals( gt ) )
      {
        final Placemark placemark = folder.createAndAddPlacemark();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );

        final MultiGeometry multiGeometry = ConverterMultiCurve.convert( (GM_MultiCurve)gmo );
        placemark.setGeometry( multiGeometry );

// if( style != null )
//          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$
      }
      else if( GEOMETRY_TYPE.eCurve.equals( gt ) )
      {
        final Placemark placemark = folder.createAndAddPlacemark();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );

        final LineString lineString = ConverterCurve.convert( (GM_Curve)gmo );
        placemark.setGeometry( lineString );

// if( style != null )
//          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$
      }
      else if( GEOMETRY_TYPE.eMultiSurface.equals( gt ) )
      {
        final Placemark placemark = folder.createAndAddPlacemark();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );

        final MultiGeometry multiGeometry = ConverterMultiSurface.convert( (GM_MultiSurface)gmo );
        placemark.setGeometry( multiGeometry );

// if( style != null )
//          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$
      }
      else if( GEOMETRY_TYPE.eSurface.equals( gt ) )
      {
        final Placemark placemark = folder.createAndAddPlacemark();
        placemark.setName( KMLAdapterUtils.getFeatureName( feature, providers ) );

        final Polygon geometry = ConverterSurface.convert( (GM_Polygon)gmo );
        placemark.setGeometry( geometry );

        final Style style = placemark.createAndAddStyle();
        convert( style, symbolizer, feature );

// if( style != null )
//          placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$
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
        final Placemark placemark = folder.createAndAddPlacemark();
        placemark.setName( feature.getId() );

        final Point point = ConverterPoint.convert( (GM_Point)gmo );
        placemark.setGeometry( point );

        final Style style = placemark.createAndAddStyle();
        convert( style, symbolizer, feature );

// if( style != null )
// placemark.setStyleUrl( "#" + style.getId() ); //$NON-NLS-1$

// featureTypes.add( placemark );
// }
      }
      else
        throw new UnsupportedOperationException();

    }
  }

  private void convert( final Style style, final Symbolizer symbolizer, final Feature feature ) throws FilterEvaluationException, IOException
  {
    if( symbolizer instanceof PolygonSymbolizer )
      convert( (PolygonSymbolizer) symbolizer, feature, style );
    else if( symbolizer instanceof PointSymbolizer )
      convert( (PointSymbolizer)symbolizer, feature, style );
    else if( symbolizer instanceof TextSymbolizer )
      convert( (TextSymbolizer) symbolizer, feature, style );
    else
      throw new UnsupportedOperationException();
  }

  private void convert( final PointSymbolizer symbolizer, final Feature feature, final Style style ) throws FilterEvaluationException, IOException
  {
    // FIXME: we could save the image relative to the kml?
    final String iconPath = m_iconExporter.getImagePath( symbolizer, feature );

    final Icon icon = new Icon();
    icon.setHref( iconPath );

    final IconStyle iconStyle = new IconStyle();
    iconStyle.setIcon( icon );

    // final double size = graphic.getSize( feature );
    // iconStyle.setScale( size );

    style.setIconStyle( iconStyle );
  }

  private void convert( final TextSymbolizer symbolizer, final Feature feature, final Style style ) throws FilterEvaluationException
  {
    final LabelStyle labelStyle = new LabelStyle();

    final Font font = symbolizer.getFont();
    labelStyle.setColor( toKmlColor( font.getColor( feature ), 1 ) );

    style.setLabelStyle( labelStyle );
  }

  private void convert( final PolygonSymbolizer symbolizer, final Feature feature, final Style style ) throws FilterEvaluationException
  {
    final Stroke stroke = symbolizer.getStroke();
    final LineStyle targetStroke = new LineStyle();
    targetStroke.setColor( toKmlColor( stroke.getStroke( feature ), stroke.getOpacity( feature ) ) );

    final PolyStyle targetFill = new PolyStyle();
    final Fill fill = symbolizer.getFill();
    targetFill.setColor( toKmlColor( fill.getFill( feature ), fill.getOpacity( feature ) ) );

    style.setPolyStyle( targetFill );
    style.setLineStyle( targetStroke );
  }

  private static String toKmlColor( final Color color, final double opacity )
  {
    if( opacity > 1.0 )
      throw new IllegalStateException( "invalid opacity" ); //$NON-NLS-1$

    final Double kmlOpacity = opacity * 255 + 40;
    // alpha=0x7f, blue=0xff, green=0x00, and red=0x00.

    String result = ""; //$NON-NLS-1$
    result += String.format( "%02x", Math.min( kmlOpacity.intValue(), 230 ) ); //$NON-NLS-1$
    result += String.format( "%02x", color.getBlue() ); //$NON-NLS-1$
    result += String.format( "%02x", color.getGreen() ); //$NON-NLS-1$
    result += String.format( "%02x", color.getRed() ); //$NON-NLS-1$

    return result;
  }
}