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
package org.kalypso.swtchart.configuration;

import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.swtchart.chart.styles.ILayerStyle;
import org.kalypso.swtchart.chart.styles.IStyledElement;
import org.kalypso.swtchart.chart.styles.LayerStyle;
import org.kalypso.swtchart.chart.styles.StyledLine;
import org.kalypso.swtchart.chart.styles.StyledPoint;
import org.kalypso.swtchart.chart.styles.StyledPolygon;
import org.kalypso.swtchart.chart.styles.StyledText;
import org.kalypso.swtchart.configuration.parameters.IParameterContainer;
import org.kalypso.swtchart.configuration.parameters.impl.FontStyleParser;
import org.kalypso.swtchart.configuration.parameters.impl.LineStyleParser;
import org.kalypso.swtchart.configuration.parameters.impl.NumberParser;
import org.kalypso.swtchart.configuration.parameters.impl.ParameterHelper;
import org.kalypso.swtchart.configuration.parameters.impl.RGBParser;
import org.ksp.chart.configuration.LayerType;
import org.ksp.chart.configuration.RefType;
import org.ksp.chart.configuration.StyleType;
import org.ksp.chart.configuration.LayerType.Style;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

/**
 * @author burtscher
 */
public class StyleLoader
{

   public static ILayerStyle createStyle(LayerType configLayer)
   {
     LayerStyle ls=new LayerStyle();
     
     List<RefType> styleRefs=null;
     Style style = configLayer.getStyle();
     if (style!=null)
       styleRefs=style.getStyleRef();
     if (styleRefs!=null)
     {
       for( RefType styleRef : styleRefs )
      {
         StyleType configStyle=(StyleType) styleRef.getRef();
         IStyledElement ise=createStyledElement( configStyle );
         if (ise!=null)
           ls.add( ise );
      }
     }
     return ls;
   }


  /**
   * creates a style object from a configuration styletype object
   */
  public static IStyledElement createStyledElement( StyleType st )
  {
    IStyledElement se=null;
    if( st != null )
    {
      IParameterContainer ph=new ParameterHelper();
      ph.addParameters( st.getParameters(), st.getName());
      String type = st.getType();
      
      if (type.compareTo( "POINT" )==0)
      { 
        se=createPoint(st, ph);
      }
      else if (type.compareTo( "POLYGON" )==0)
      {
        se=createPolygon(st, ph);
        
      }
      else if (type.compareTo( "LINE" )==0)
      {
        se=createLine(st, ph);
        
      }
      else if (type.compareTo( "FONT" )==0)
      {
        se=createFont(st, ph);
      }
    }
    return se;
  }

  /**
   * creates a StyledLine object from a style configuration 
   */
  public static StyledLine createLine( StyleType lt, IParameterContainer ph )
  {
    ph.addParameters( lt.getParameters(), lt.getName() );
    String id=lt.getName();
    
    
    int alpha = ph.getParsedParameterValue( "alpha", "255", id, new NumberParser()).intValue() ;
    int lineWidth = ph.getParsedParameterValue( "lineWidth", "1", id, new NumberParser()).intValue() ;
    RGB lineColor = ph.getParsedParameterValue( "lineColor", "#000000", id, new RGBParser());
    int lineStyle = ph.getParsedParameterValue( "lineStyle", "SOLID", id, new LineStyleParser());

    StyledLine sl = new StyledLine( lineWidth, lineColor, lineStyle, alpha );
    return sl;
  }

  /**
   * creates a StyledPolygon object from a style configuration 
   */
  public static StyledPolygon createPolygon( StyleType pt, IParameterContainer ph )
  {
    StyledPolygon sp = null;
    if( pt != null )
    {
      String id=pt.getName();
      int borderWidth = ph.getParsedParameterValue( "borderWidth", "0", id, new NumberParser() ).intValue();
      RGB borderColor = ph.getParsedParameterValue( "borderColor", "#000000", id, new RGBParser());
      RGB fillColor = ph.getParsedParameterValue( "fillColor", "#ffffff", id, new RGBParser());
      int alpha = ph.getParsedParameterValue( "alpha", "255", id, new NumberParser() ).intValue();
      sp = new StyledPolygon( fillColor, borderWidth, borderColor, alpha );
    }
    return sp;
  }

  /**
   * creates a StyledText object from a style configuration 
   */
  public static StyledText createFont( StyleType pt, IParameterContainer ph )
  {
    StyledText sp = null;
    if( pt != null )
    {
      String id=pt.getName();
      String fontName = ph.getParameterValue( "fontName", "arial", id);
      int fontStyle = ph.getParsedParameterValue( "fontName", "NORMAL", id, new FontStyleParser());
      int fontSize = ph.getParsedParameterValue( "fontSize", "10", id, new NumberParser()).intValue();
      RGB foregroundColor = ph.getParsedParameterValue( "textColor", "#ffffff", id, new RGBParser());
      RGB backgroundColor = ph.getParsedParameterValue( "backgroundColor", "#000000", id, new RGBParser());
      int alpha = ph.getParsedParameterValue( "alpha", "255", id, new NumberParser() ).intValue();
      sp = new StyledText( foregroundColor, backgroundColor, fontName, fontStyle, fontSize, alpha );
    }
    return sp;
  }

  /**
   * creates a StyledPoint object from a style configuration 
   */
  public static StyledPoint createPoint( StyleType pt, IParameterContainer ph )
  {
    StyledPoint sp = null;
    if( pt != null )
    {
      String id=pt.getName();
      int pointWidth = ph.getParsedParameterValue( "pointWidth", "5", id, new NumberParser()).intValue();
      int pointHeight = ph.getParsedParameterValue( "pointHeight", "5", id, new NumberParser()).intValue();
      int borderWidth = ph.getParsedParameterValue( "borderWidth", "1", id, new NumberParser()).intValue();
      RGB fillColor = ph.getParsedParameterValue( "fillColor", "#ffffff", id, new RGBParser());
      RGB borderColor = ph.getParsedParameterValue( "borderColor", "#000000", id, new RGBParser());
      int alpha = ph.getParsedParameterValue( "alpha", "255", id, new NumberParser() ).intValue();
      sp = new StyledPoint( pointWidth, pointHeight, fillColor, borderWidth, borderColor, alpha );
    }
    return sp;
  }

  /**
   * creates a rgb object from 3byte as returned by the unmarshalling process of heybinary values
   */
  public static RGB createColor( byte[] cbyte )
  {
    int r = hexToInt( HexBin.encode( new byte[] { cbyte[0] } ) );
    int g = hexToInt( HexBin.encode( new byte[] { cbyte[1] } ) );
    int b = hexToInt( HexBin.encode( new byte[] { cbyte[2] } ) );
    return new RGB( r, g, b );
  }

  /**
   * transforms a hex string into an integer
   * 
   * @param strHex hexdec representation of the number as String 
   * @return integer representation
   */
  public static int hexToInt( String strHex )
  {
    String str;
    String hexVals = "0123456789ABCDEF";
    int i, val, n;

    val = 0;
    n = 1;
    str = strHex.toUpperCase();

    for( i = str.length() - 1; i >= 0; i--, n *= 16 )
    {
      val += n * hexVals.indexOf( str.charAt( i ) );
    }
    return val;
  }
}
