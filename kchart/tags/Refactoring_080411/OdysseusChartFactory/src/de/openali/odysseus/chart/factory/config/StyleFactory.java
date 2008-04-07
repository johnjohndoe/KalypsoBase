/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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

import java.net.URL;
import java.util.List;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.factory.util.ChartFactoryUtilities;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTWEIGHT;
import de.openali.odysseus.chart.framework.model.style.impl.ColorFill;
import de.openali.odysseus.chart.framework.model.style.impl.ImageFill;
import de.openali.odysseus.chart.framework.model.style.impl.ImageMarker;
import de.openali.odysseus.chart.framework.model.style.impl.OvalMarker;
import de.openali.odysseus.chart.framework.model.style.impl.PolygonMarker;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chartconfig.x010.AreaStyleType;
import de.openali.odysseus.chartconfig.x010.ColorFillType;
import de.openali.odysseus.chartconfig.x010.FillType;
import de.openali.odysseus.chartconfig.x010.ImageFillType;
import de.openali.odysseus.chartconfig.x010.ImageMarkerType;
import de.openali.odysseus.chartconfig.x010.LineStyleType;
import de.openali.odysseus.chartconfig.x010.PointStyleType;
import de.openali.odysseus.chartconfig.x010.PointType;
import de.openali.odysseus.chartconfig.x010.PolygonMarkerType;
import de.openali.odysseus.chartconfig.x010.StrokeType;
import de.openali.odysseus.chartconfig.x010.TextStyleType;
import de.openali.odysseus.chartconfig.x010.FontStyleType.Enum;

/**
 * @author alibu
 */
public class StyleFactory
{

  public static IPointStyle createPointStyle( final PointStyleType pst, final URL context )
  {
    IPointStyle style = StyleUtils.getDefaultPointStyle();

    style.setTitle( pst.getTitle() );

    // visible
    if( pst.isSetIsVisible() )
    {
      style.setVisible( pst.getIsVisible() );
    }

    // alpha
    if( pst.isSetAlpha() )
    {
      style.setAlpha( byteToInt( pst.getAlpha()[0] ) );
    }

    // width
    if( pst.isSetWidth() )
    {
      style.setWidth( pst.getWidth() );
    }

    // height
    if( pst.isSetHeight() )
    {
      style.setHeight( pst.getHeight() );
    }

    // fill color
    byte[] fillColor = pst.getFillColor();
    if( fillColor != null )
    {
      style.setInlineColor( colorByteToRGB( fillColor ) );
    }

    // marker

    if( pst.isSetPolygonMarker() )
    {
      PolygonMarkerType polygonMarker = pst.getPolygonMarker();
      PointType[] configPointArray = polygonMarker.getPointArray();

      Point[] pointArray = new Point[configPointArray.length];
      for( int i = 0; i < pointArray.length; i++ )
      {
        PointType configPoint = configPointArray[i];
        pointArray[i] = new Point( configPoint.getX(), configPoint.getY() );
      }
      style.setMarker( new PolygonMarker( pointArray ) );
    }
    else if( pst.isSetImageMarker() )
    {
      ImageMarkerType imageMarker = pst.getImageMarker();
      String imgPath = imageMarker.getImageFile();
      ImageData id = ChartFactoryUtilities.loadImageData( context, imgPath, -1, -1 );
      style.setMarker( new ImageMarker( id ) );
    }
    else if( pst.isSetOvalMarker() )
    {
      style.setMarker( new OvalMarker() );
    }
    // else: do nothing - use default

    StrokeType strokeStyle = pst.getStroke();
    if( strokeStyle != null )
    {
      ILineStyle ls = StyleUtils.getDefaultLineStyle();
      setStrokeAttributes( ls, strokeStyle );
      style.setStroke( ls );
    }

    return style;
  }

  @SuppressWarnings("unchecked")
  public static ILineStyle createLineStyle( LineStyleType lst )
  {
    ILineStyle style = StyleUtils.getDefaultLineStyle();

    style.setTitle( lst.getTitle() );

    // visible
    if( lst.isSetIsVisible() )
    {
      boolean isVisible = lst.getIsVisible();
      style.setVisible( isVisible );
    }

    // alpha
    if( lst.isSetAlpha() )
    {
      style.setAlpha( byteToInt( lst.getAlpha()[0] ) );
    }

    // width
    if( lst.isSetWidth() )
    {
      style.setWidth( lst.getWidth() );
    }

    // dashArray
    int dashOffset = 0;
    float[] dashArray = null;

    if( lst.isSetDashArray1() )
    {
      List dashArray1 = lst.getDashArray1();
      dashArray = new float[dashArray1.size()];
      for( int i = 0; i < dashArray.length; i++ )
      {
        Object elt = dashArray1.get( i );
        if( elt instanceof Integer )
        {
          dashArray[i] = ((Float) elt).floatValue();
        }
      }
    }
    if( lst.isSetDashOffset() )
    {
      dashOffset = lst.getDashOffset();
    }
    style.setDash( dashOffset, dashArray );

    // color
    if( lst.isSetLineColor() )
    {
      style.setColor( colorByteToRGB( lst.getLineColor() ) );
    }

    return style;
  }

  /**
   * this is needed as separate (and redundant) method: StrokeTypes aren't LineTypes (they don't contain title
   * elements); their equivalent elements had to be declared as groups in XML Schema, so they aren't related to each
   * other
   */
  @SuppressWarnings("unchecked")
  public static void setStrokeAttributes( ILineStyle style, StrokeType st )
  {

    // visible
    if( st.isSetIsVisible() )
    {
      boolean isVisible = st.getIsVisible();
      style.setVisible( isVisible );
    }

    // alpha
    if( st.isSetAlpha() )
    {
      style.setAlpha( byteToInt( st.getAlpha()[0] ) );
    }

    // width
    if( st.isSetWidth() )
    {
      style.setWidth( st.getWidth() );
    }

    // dashArray
    float dashOffset = 0;
    float[] dashArray = null;

    if( st.isSetDashArray1() )
    {
      List dashArray1 = st.getDashArray1();
      dashArray = new float[dashArray1.size()];
      for( int i = 0; i < dashArray.length; i++ )
      {
        Object elt = dashArray1.get( i );
        if( elt instanceof Integer )
        {
          dashArray[i] = ((Float) elt).floatValue();
        }
      }
    }
    if( st.isSetDashOffset() )
    {
      dashOffset = st.getDashOffset();
    }
    style.setDash( dashOffset, dashArray );

    // color
    if( st.isSetLineColor() )
    {
      style.setColor( colorByteToRGB( st.getLineColor() ) );
    }
  }

  public static IAreaStyle createAreaStyle( AreaStyleType ast, URL context )
  {
    IAreaStyle style = StyleUtils.getDefaultAreaStyle();

    style.setTitle( ast.getTitle() );
    // visible
    if( ast.isSetIsVisible() )
    {
      boolean isVisible = ast.getIsVisible();
      style.setVisible( isVisible );
    }

    // alpha
    if( ast.isSetAlpha() )
    {
      int alpha = byteToInt( ast.getAlpha()[0] );
      style.setAlpha( alpha );
    }

    if( ast.isSetFill() )
    {
      FillType fill = ast.getFill();
      if( fill.getColorFill() != null )
      {
        ColorFillType cft = fill.getColorFill();
        ColorFill colorFill = new ColorFill( colorByteToRGB( cft.getColor() ) );
        style.setFill( colorFill );
      }
      else if( fill.getImageFill() != null )
      {
        ImageFillType ift = fill.getImageFill();
        String imgPath = ift.getImageFile();

        int width = -1;
        int height = -1;
        if( ift.isSetWidth() )
        {
          width = ift.getWidth();
        }
        if( ift.isSetHeight() )
        {
          height = ift.getHeight();
        }
        ImageData id = ChartFactoryUtilities.loadImageData( context, imgPath, width, height );
        ImageFill imageFill = new ImageFill( id );
        style.setFill( imageFill );
      }
      // else: use default fill
    }

    // outline
    if( ast.isSetStroke() )
    {
      ILineStyle ls = StyleUtils.getDefaultLineStyle();
      setStrokeAttributes( ls, ast.getStroke() );
      style.setStroke( ls );
    }

    return style;
  }

  public static ITextStyle createTextStyle( TextStyleType tst )
  {
    ITextStyle style = StyleUtils.getDefaultTextStyle();

    style.setTitle( tst.getTitle() );
    // visible
    if( tst.isSetIsVisible() )
    {
      boolean isVisible = tst.getIsVisible();
      style.setVisible( isVisible );
    }

    // alpha
    if( tst.isSetAlpha() )
    {
      int alpha = byteToInt( tst.getAlpha()[0] );
      style.setAlpha( alpha );
    }

    // font family
    if( tst.isSetFontFamily() )
    {
      // TODO: check if family exists
      style.setFamily( tst.getFontFamily() );
    }

    // background color
    if( tst.isSetFillColor() )
    {
      style.setFillColor( colorByteToRGB( tst.getFillColor() ) );
    }

    // text color
    if( tst.isSetTextColor() )
    {
      style.setTextColor( colorByteToRGB( tst.getTextColor() ) );
    }

    // text size
    if( tst.isSetSize() )
    {
      style.setHeight( tst.getSize() );
    }

    // font style
    if( tst.isSetFontStyle() )
    {
      Enum configfontStyle = tst.getFontStyle();
      if( configfontStyle.toString().equals( FONTSTYLE.ITALIC.toString() ) )
      {
        style.setFontStyle( FONTSTYLE.ITALIC );
      }
      else if( configfontStyle.toString().equals( FONTSTYLE.NORMAL.toString() ) )
      {
        style.setFontStyle( FONTSTYLE.NORMAL );
      }
      else
      {
        // keep default
      }
    }

    // font weight
    if( tst.isSetFontWeight() )
    {
      de.openali.odysseus.chartconfig.x010.FontWeightType.Enum fontWeight = tst.getFontWeight();
      if( fontWeight.toString().equals( FONTWEIGHT.BOLD.toString() ) )
      {
        style.setWeight( FONTWEIGHT.BOLD );
      }
      if( fontWeight.toString().equals( FONTWEIGHT.NORMAL.toString() ) )
      {
        style.setWeight( FONTWEIGHT.NORMAL );
      }
      else
      {
        // keep default
      }
    }

    return style;
  }

  /**
   * @param b
   *            a byte value
   */
  private static int byteToInt( byte b )
  {
    return b & 0xff;
  }

  /**
   * @param color
   *            3 byte array
   */
  private static RGB colorByteToRGB( byte[] color )
  {
    int red = byteToInt( color[0] );
    int green = byteToInt( color[1] );
    int blue = byteToInt( color[2] );
    return new RGB( red, green, blue );
  }

}
