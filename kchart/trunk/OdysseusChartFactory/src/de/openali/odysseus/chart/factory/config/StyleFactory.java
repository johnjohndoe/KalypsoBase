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

import java.io.File;
import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.impl.model.style.AreaStyle;
import de.openali.odysseus.chart.framework.impl.model.style.ImageMarker;
import de.openali.odysseus.chart.framework.impl.model.style.LineStyle;
import de.openali.odysseus.chart.framework.impl.model.style.OvalMarker;
import de.openali.odysseus.chart.framework.impl.model.style.PointStyle;
import de.openali.odysseus.chart.framework.impl.model.style.PolygonMarker;
import de.openali.odysseus.chart.framework.impl.util.FigureUtilities;
import de.openali.odysseus.chartconfig.x010.AreaStyleType;
import de.openali.odysseus.chartconfig.x010.ImageMarkerType;
import de.openali.odysseus.chartconfig.x010.PointStyleType;
import de.openali.odysseus.chartconfig.x010.PointType;
import de.openali.odysseus.chartconfig.x010.PolygonMarkerType;
import de.openali.odysseus.chartconfig.x010.StrokeType;

/**
 * @author alibu
 */
public class StyleFactory
{

  // TODO: save ConfigStyle info

  public static PointStyle createPointStyle( PointStyleType pst )
  {
    PointStyle style = FigureUtilities.getDefaultPointStyle();

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
      String configPath = imageMarker.getPath();
      File file = new File( configPath );
      style.setMarker( new ImageMarker( file ) );
    }
    else if( pst.isSetOvalMarker() )
    {
      style.setMarker( new OvalMarker() );
    }
    else
    {
      // do nothing - use default
    }

    StrokeType strokeStyle = pst.getStrokeStyle();
    if( strokeStyle != null )
    {
      style.setOutline( createLineStyle( strokeStyle ) );
    }

    return style;
  }

  public static LineStyle createLineStyle( StrokeType st )
  {
    LineStyle style = FigureUtilities.getDefaultLineStyle();

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
    int dashOffset = 0;
    int[] dashArray = null;

    if( st.isSetDashArray1() )
    {
      List dashArray1 = st.getDashArray1();
      // TODO: weiter
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
    return style;
  }

  public static AreaStyle createAreaStyle( AreaStyleType ast )
  {
    AreaStyle style = FigureUtilities.getDefaultAreaStyle();

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

    if( ast.isSetFillColor() )
    {
      style.setInlineColor( colorByteToRGB( ast.getFillColor() ) );
    }

    // outline
    if( ast.isSetOutlineStyle() )
    {
      style.setOutline( createLineStyle( ast.getOutlineStyle() ) );
    }

    return style;
  }

  /**
   * @param b
   *            a byte value
   */
  private static int byteToInt( byte b )
  {

    int i = b;
    i &= 0x000000FF;

    return i;
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
