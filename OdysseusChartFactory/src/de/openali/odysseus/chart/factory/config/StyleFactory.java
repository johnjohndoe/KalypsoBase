/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package de.openali.odysseus.chart.factory.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.factory.OdysseusChartFactory;
import de.openali.odysseus.chart.factory.config.resolver.ChartTypeResolver;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTWEIGHT;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.ColorFill;
import de.openali.odysseus.chart.framework.model.style.impl.ImageFill;
import de.openali.odysseus.chart.framework.model.style.impl.ImageMarker;
import de.openali.odysseus.chart.framework.model.style.impl.OvalMarker;
import de.openali.odysseus.chart.framework.model.style.impl.PolygonMarker;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chartconfig.x020.AbstractStyleType;
import de.openali.odysseus.chartconfig.x020.AreaStyleType;
import de.openali.odysseus.chartconfig.x020.ColorFillType;
import de.openali.odysseus.chartconfig.x020.FillType;
import de.openali.odysseus.chartconfig.x020.ImageFillType;
import de.openali.odysseus.chartconfig.x020.ImageMarkerType;
import de.openali.odysseus.chartconfig.x020.LineStyleType;
import de.openali.odysseus.chartconfig.x020.PointStyleType;
import de.openali.odysseus.chartconfig.x020.PointType;
import de.openali.odysseus.chartconfig.x020.PolygonMarkerType;
import de.openali.odysseus.chartconfig.x020.ReferencableType;
import de.openali.odysseus.chartconfig.x020.StrokeType;
import de.openali.odysseus.chartconfig.x020.StylesDocument.Styles;
import de.openali.odysseus.chartconfig.x020.TextStyleType;

/**
 * @author alibu
 */
public final class StyleFactory
{
  private StyleFactory( )
  {
  }

  public static final String STYLE_KEY = "de.openali.odysseus.chart.factory.style"; //$NON-NLS-1$

  public static Map<String, IStyle> createStyleMap( final Styles styles, final URL context )
  {
    final Map<String, IStyle> styleMap = new HashMap<String, IStyle>();

    // Styles erzeugen
    if( styles != null )
    {
      for( final AreaStyleType ast : styles.getAreaStyleArray() )
      {
        final IAreaStyle as = StyleFactory.createAreaStyle( ast, context );
        as.setData( STYLE_KEY, ast );
        styleMap.put( ast.getRole(), as );
      }
      for( final LineStyleType lst : styles.getLineStyleArray() )
      {
        final ILineStyle as = StyleFactory.createLineStyle( lst );
        as.setData( STYLE_KEY, lst );
        styleMap.put( lst.getRole(), as );
      }
      for( final PointStyleType pst : styles.getPointStyleArray() )
      {
        final IPointStyle ps = StyleFactory.createPointStyle( pst, context );
        ps.setData( STYLE_KEY, pst );
        styleMap.put( pst.getRole(), ps );
      }
      for( final TextStyleType tst : styles.getTextStyleArray() )
      {
        final ITextStyle ps = StyleFactory.createTextStyle( tst );
        ps.setData( STYLE_KEY, tst );
        styleMap.put( tst.getRole(), ps );
      }
    }
    return styleMap;
  }

  public static IStyleSet createStyleSet( final Styles styles )
  {
    return createStyleSet( styles, null, null );
  }

  public static IStyleSet createStyleSet( final Styles styles, final ReferencableType[] baseTypes, final URL context )
  {
    final IStyleSet styleSet = new StyleSet();
    if( styles != null )
    {
      final AreaStyleType[] asArray = styles.getAreaStyleArray();
      for( final AreaStyleType ast : asArray )
      {
        final IAreaStyle as = StyleFactory.createAreaStyle( ast, context );
        // save configuration type so it can be used for saving to chart file
        as.setData( AbstractChartFactory.CONFIGURATION_TYPE_KEY, ast );
        styleSet.addStyle( ast.getRole(), as );
      }
      final PointStyleType[] psArray = styles.getPointStyleArray();
      for( final PointStyleType pst : psArray )
      {
        final IPointStyle ps = StyleFactory.createPointStyle( pst, context );
        // save configuration type so it can be used for saving to chart file
        ps.setData( AbstractChartFactory.CONFIGURATION_TYPE_KEY, pst );
        styleSet.addStyle( pst.getRole(), ps );
      }
      final LineStyleType[] lsArray = styles.getLineStyleArray();
      for( final LineStyleType lst : lsArray )
      {
        final ILineStyle ls = StyleFactory.createLineStyle( lst );
        // save configuration type so it can be used for saving to chart file
        ls.setData( AbstractChartFactory.CONFIGURATION_TYPE_KEY, lst );
        styleSet.addStyle( lst.getRole(), ls );
      }
      final TextStyleType[] tsArray = styles.getTextStyleArray();
      for( final TextStyleType tst : tsArray )
      {
        final ITextStyle ts = StyleFactory.createTextStyle( tst );
        // save configuration type so it can be used for saving to chart file
        ts.setData( AbstractChartFactory.CONFIGURATION_TYPE_KEY, tst );
        styleSet.addStyle( tst.getRole(), ts );
      }

      final String[] references = styles.getStyleReferenceArray();
      final ChartTypeResolver resolver = ChartTypeResolver.getInstance();

      for( final String reference : references )
      {
        try
        {
//          if( reference.startsWith( "#" ) ) // local style reference //$NON-NLS-1$
//          {
//            final RETokenizer tokenizer = new RETokenizer( new Pattern( "#" ), reference ); //$NON-NLS-1$
//            final String identifier = tokenizer.nextToken();
//
//            final AbstractStyleType styleType = StyleHelper.findStyle( baseTypes, identifier );
//            final IStyle style = StyleFactory.createStyle( styleType, context );
//            if( style == null )
//              return null;
//
//            // save configuration type so it can be used for saving to chart file
//            style.setData( AbstractChartFactory.CONFIGURATION_TYPE_KEY, style );
//            styleSet.addStyle( styleType.getRole(), style );
//          }
//          else
//          {
            final AbstractStyleType styleType = resolver.findStyleType( reference, context );
            if( styleType == null )
            {
              System.out.println( String.format( "StyleFactory - Missing style refernce: %s", reference ) );
              continue;
            }

            final IStyle style = StyleFactory.createStyle( styleType, context );

            // save configuration type so it can be used for saving to chart file
            style.setData( AbstractChartFactory.CONFIGURATION_TYPE_KEY, style );
            styleSet.addStyle( styleType.getRole(), style );
         // }

        }
        catch( final CoreException e )
        {
          OdysseusChartFactory.getDefault().getLog().log( new Status( IStatus.ERROR, OdysseusChartFactory.PLUGIN_ID, e.getLocalizedMessage(), e ) );
        }
      }
    }

    return styleSet;
  }

  private static IStyle createStyle( final AbstractStyleType styleType, final URL context )
  {
    if( styleType == null )
      return null;
    else if( styleType instanceof AreaStyleType )
    {
      return createAreaStyle( (AreaStyleType) styleType, context );
    }
    else if( styleType instanceof PointStyleType )
    {
      return createPointStyle( (PointStyleType) styleType, context );
    }
    else if( styleType instanceof LineStyleType )
    {
      return createLineStyle( (LineStyleType) styleType );
    }
    else if( styleType instanceof TextStyleType )
    {
      return createTextStyle( (TextStyleType) styleType );
    }

    throw new UnsupportedOperationException();
  }

  public static IPointStyle createPointStyle( final PointStyleType pst, final URL context )
  {
    final IPointStyle style = StyleUtils.getDefaultPointStyle();

    // style.setTitle( pst.getTitle() );

    // visible
    if( pst.isSetIsVisible() )
      style.setVisible( pst.getIsVisible() );

    // alpha
    if( pst.isSetAlpha() )
      style.setAlpha( StyleHelper.byteToInt( pst.getAlpha()[0] ) );

    // width
    if( pst.isSetWidth() )
      style.setWidth( pst.getWidth() );

    // height
    if( pst.isSetHeight() )
      style.setHeight( pst.getHeight() );

    // fill color
    final ColorFillType fillColor = pst.getFillColor();
    if( fillColor != null )
    {
      if( fillColor.isSetIsVisible() )
        style.setFillVisible( fillColor.getIsVisible() );
      final byte[] color = fillColor.getColor();
      if( color != null )
        style.setInlineColor( StyleHelper.colorByteToRGB( color ) );
    }

    // marker

    if( pst.isSetPolygonMarker() )
    {
      final PolygonMarkerType polygonMarker = pst.getPolygonMarker();
      final PointType[] configPointArray = polygonMarker.getPointArray();

      final Point[] pointArray = new Point[configPointArray.length];
      for( int i = 0; i < pointArray.length; i++ )
      {
        final PointType configPoint = configPointArray[i];
        pointArray[i] = new Point( configPoint.getX(), configPoint.getY() );
      }
      style.setMarker( new PolygonMarker( pointArray ) );
    }
    else if( pst.isSetImageMarker() )
    {
      final ImageMarkerType imageMarker = pst.getImageMarker();
      final String imgPath = imageMarker.getImageFile();
      // ImageData id = ChartFactoryUtilities.loadImageData( context, imgPath, -1, -1 );
      ImageDescriptor id;
      try
      {
        id = ImageDescriptor.createFromURL( new URL( context, imgPath ) );
        style.setMarker( new ImageMarker( id ) );
      }
      catch( final MalformedURLException e )
      {
        Logger.logError( Logger.TOPIC_LOG_STYLE, "Can not load image from '" + context + imgPath + "'" );
        style.setMarker( new OvalMarker() );
      }
    }
    else if( pst.isSetOvalMarker() )
      style.setMarker( new OvalMarker() );

    final StrokeType strokeStyle = pst.getStroke();
    if( strokeStyle != null )
    {
      final ILineStyle ls = StyleUtils.getDefaultLineStyle();
      setStrokeAttributes( ls, strokeStyle );
      style.setStroke( ls );
    }

    return style;
  }

  public static ILineStyle createLineStyle( final LineStyleType lst )
  {
    final ILineStyle style = StyleUtils.getDefaultLineStyle();

    // final String title = lst.getTitle();

    // style.setTitle( title );

    // visible
    if( lst.isSetIsVisible() )
    {
      final boolean isVisible = lst.getIsVisible();
      style.setVisible( isVisible );
    }

    // alpha
    if( lst.isSetAlpha() )
      style.setAlpha( StyleHelper.byteToInt( lst.getAlpha()[0] ) );

    // width
    if( lst.isSetWidth() )
      style.setWidth( lst.getWidth() );

    // dashArray
    int dashOffset = 0;
    float[] dashArray = null;

    if( lst.isSetDashArray1() )
    {
      final List< ? > dashArray1 = lst.getDashArray1();
      dashArray = new float[dashArray1.size()];
      for( int i = 0; i < dashArray.length; i++ )
      {
        final Object elt = dashArray1.get( i );
        if( elt instanceof Integer )
          dashArray[i] = ((Number) elt).floatValue();
      }
    }
    if( lst.isSetDashOffset() )
      dashOffset = lst.getDashOffset();
    style.setDash( dashOffset, dashArray );

    // color
    if( lst.isSetLineColor() )
      style.setColor( StyleHelper.colorByteToRGB( lst.getLineColor() ) );

    return style;
  }

  /**
   * this is needed as separate (and redundant) method: StrokeTypes aren't LineTypes (they don't contain title
   * elements); their equivalent elements had to be declared as groups in XML Schema, so they aren't related to each
   * other
   */
  public static void setStrokeAttributes( final ILineStyle style, final StrokeType st )
  {
    // visible
    if( st.isSetIsVisible() )
    {
      final boolean isVisible = st.getIsVisible();
      style.setVisible( isVisible );
    }

    // alpha
    if( st.isSetAlpha() )
      style.setAlpha( StyleHelper.byteToInt( st.getAlpha()[0] ) );

    // width
    if( st.isSetWidth() )
      style.setWidth( st.getWidth() );

    // dashArray
    float dashOffset = 0;
    float[] dashArray = null;

    if( st.isSetDashArray1() )
    {
      final List< ? > dashArray1 = st.getDashArray1();
      dashArray = new float[dashArray1.size()];
      for( int i = 0; i < dashArray.length; i++ )
      {
        final Object elt = dashArray1.get( i );
        if( elt instanceof Integer )
          dashArray[i] = ((Number) elt).floatValue();
      }
    }
    if( st.isSetDashOffset() )
      dashOffset = st.getDashOffset();
    style.setDash( dashOffset, dashArray );

    // color
    if( st.isSetLineColor() )
      style.setColor( StyleHelper.colorByteToRGB( st.getLineColor() ) );
  }

  public static IAreaStyle createAreaStyle( final AreaStyleType ast, final URL context )
  {
    final IAreaStyle style = StyleUtils.getDefaultAreaStyle();

    // style.setTitle( ast.getTitle() );
    // visible
    if( ast.isSetIsVisible() )
    {
      final boolean isVisible = ast.getIsVisible();
      style.setVisible( isVisible );
    }

    // alpha
    if( ast.isSetAlpha() )
    {
      final int alpha = StyleHelper.byteToInt( ast.getAlpha()[0] );
      style.setAlpha( alpha );
    }

    if( ast.isSetFill() )
    {
      final FillType fill = ast.getFill();
      if( fill.getColorFill() != null )
      {
        final ColorFillType cft = fill.getColorFill();
        final ColorFill colorFill = new ColorFill( StyleHelper.colorByteToRGB( cft.getColor() ) );
        style.setFill( colorFill );
      }
      else if( fill.getImageFill() != null )
      {
        final ImageFillType ift = fill.getImageFill();
        final String imgPath = ift.getImageFile();

        try
        {
          final ImageDescriptor id = ImageDescriptor.createFromURL( new URL( context, imgPath ) );
          // style.setMarker( new ImageMarker( id ) );
          final ImageFill imageFill = new ImageFill( id );
          style.setFill( imageFill );
        }
        catch( final MalformedURLException e )
        {
          Logger.logError( Logger.TOPIC_LOG_STYLE, "Can not load image from '" + context + imgPath + "'" );
          style.setFill( new ColorFill( new RGB( 255, 0, 0 ) ) );
        }

      }
      // else: use default fill
    }

    // outline
    if( ast.isSetStroke() )
    {
      final ILineStyle ls = StyleUtils.getDefaultLineStyle();
      setStrokeAttributes( ls, ast.getStroke() );
      style.setStroke( ls );
    }

    return style;
  }

  public static ITextStyle createTextStyle( final TextStyleType tst )
  {
    final ITextStyle style = StyleUtils.getDefaultTextStyle();

    if( tst == null )
      return null;

    // style.setTitle( tst.getTitle() );

    // visible
    if( tst.isSetIsVisible() )
    {
      final boolean isVisible = tst.getIsVisible();
      style.setVisible( isVisible );
    }

    // alpha
    if( tst.isSetAlpha() )
    {
      final int alpha = StyleHelper.byteToInt( tst.getAlpha()[0] );
      style.setAlpha( alpha );
    }

    // font family
    if( tst.isSetFontFamily() )
      // TODO: check if family exists
      style.setFamily( tst.getFontFamily() );

    // background color
    if( tst.isSetFillColor() )
      style.setFillColor( StyleHelper.colorByteToRGB( tst.getFillColor() ) );

    // text color
    if( tst.isSetTextColor() )
      style.setTextColor( StyleHelper.colorByteToRGB( tst.getTextColor() ) );

    // text size
    if( tst.isSetSize() )
      style.setHeight( tst.getSize() );

    // font style
    if( tst.isSetFontStyle() )
    {
      final de.openali.odysseus.chartconfig.x020.FontStyleType.Enum configfontStyle = tst.getFontStyle();
      if( configfontStyle.toString().equals( FONTSTYLE.ITALIC.toString() ) )
        style.setFontStyle( FONTSTYLE.ITALIC );
      else if( configfontStyle.toString().equals( FONTSTYLE.NORMAL.toString() ) )
        style.setFontStyle( FONTSTYLE.NORMAL );
      else
      {
        // keep default
      }
    }

    // font weight
    if( tst.isSetFontWeight() )
    {
      final de.openali.odysseus.chartconfig.x020.FontWeightType.Enum fontWeight = tst.getFontWeight();
      if( fontWeight.toString().equals( FONTWEIGHT.BOLD.toString() ) )
        style.setWeight( FONTWEIGHT.BOLD );
      if( fontWeight.toString().equals( FONTWEIGHT.NORMAL.toString() ) )
        style.setWeight( FONTWEIGHT.NORMAL );
      else
      {
        // keep default
      }
    }

    return style;
  }

}
