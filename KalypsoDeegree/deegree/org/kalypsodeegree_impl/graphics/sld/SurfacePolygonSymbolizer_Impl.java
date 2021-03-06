/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.graphics.sld;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.Geometry;
import org.kalypsodeegree.graphics.sld.PolygonColorMapEntry;
import org.kalypsodeegree.graphics.sld.SurfacePolygonSymbolizer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.xml.Marshallable;

/**
 * @author Thomas Jung
 */
public class SurfacePolygonSymbolizer_Impl extends Symbolizer_Impl implements SurfacePolygonSymbolizer
{
  private PolygonColorMap m_colorMap;

  /**
   * Creates a new PolygonSymbolizer_Impl object.
   */
  public SurfacePolygonSymbolizer_Impl( )
  {
    this( new PolygonColorMap_Impl(), null, UOM.pixel );
  }

  /**
   * constructor initializing the class with the <PolygonSymbolizer>
   */
  public SurfacePolygonSymbolizer_Impl( final PolygonColorMap colorMap, final Geometry geometry, final UOM uom )
  {
    super( geometry, uom );

    setColorMap( colorMap );
  }

  /**
   * @see org.kalypsodeegree_impl.graphics.sld.Symbolizer_Impl#paint(org.eclipse.swt.graphics.GC,
   *      org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public void paint( final GC gc, final Feature feature ) throws FilterEvaluationException
  {
    final Rectangle clipping = gc.getClipping();

    gc.setForeground( gc.getDevice().getSystemColor( SWT.COLOR_BLACK ) );
    gc.setBackground( gc.getDevice().getSystemColor( SWT.COLOR_WHITE ) );
    gc.setLineAttributes( new LineAttributes( 1 ) );

    /* we draw 2 rects in the colors of the color map and a black rectangle around it */
    final PolygonColorMapEntry[] colorMapEntries = m_colorMap.getColorMap();

    if( colorMapEntries.length == 0 )
      return;

    final Fill startFill = colorMapEntries[0].getFill();
    final double startOpacity = startFill.getOpacity( null );
    final java.awt.Color startColor = startFill.getFill( null );

    final Color fillColorStart = new Color( gc.getDevice(), startColor.getRed(), startColor.getGreen(), startColor.getBlue() );

    final int startAlpha = (int) (startOpacity * 255);
    gc.setAlpha( startAlpha );
    gc.setBackground( fillColorStart );
    gc.fillRectangle( clipping.x, clipping.y, clipping.width - 1, clipping.height / 2 );

    final Fill endFill = colorMapEntries[colorMapEntries.length - 1].getFill();
    final double endOpacity = endFill.getOpacity( null );
    final java.awt.Color endColor = endFill.getFill( null );

    final Color fillColorEnd = new Color( gc.getDevice(), endColor.getRed(), endColor.getGreen(), endColor.getBlue() );

    final int endAlpha = (int) (endOpacity * 255);
    gc.setAlpha( endAlpha );
    gc.setBackground( fillColorEnd );
    gc.fillRectangle( clipping.x, clipping.height / 2, clipping.width - 1, clipping.height - 1 );

    // the black border
    gc.setForeground( gc.getDevice().getSystemColor( SWT.COLOR_BLACK ) );
    gc.drawRectangle( clipping.x, clipping.y, clipping.width - 1, clipping.height - 1 );

    fillColorStart.dispose();
    fillColorEnd.dispose();
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.SurfacePolygonSymbolizer#getColorMap()
   */
  @Override
  public PolygonColorMap getColorMap( )
  {
    return m_colorMap;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.SurfacePolygonSymbolizer#setColorMap(org.kalypsodeegree_impl.graphics.sld.PolygonColorMap)
   */
  @Override
  public void setColorMap( final PolygonColorMap colorMap )
  {
    m_colorMap = colorMap;
  }

  /**
   * Produces a textual representation of this object.
   *
   * @return the textual representation
   */
  @Override
  public String toString( )
  {
    final StringBuffer sb = new StringBuffer();
    sb.append( "<sldExt:SurfacePolygonSymbolizer xmlns:sldExt=\"" + SLDFactory.SLDNS_EXT + "\"" );

    final UOM uom = getUom();

    if( uom != null )
    {
      sb.append( " uom=\"" + uom.name() + "\">" );
    }
    else
      sb.append( ">\n" );

    if( getGeometry() != null )
    {
      sb.append( getGeometry() ).append( "\n" );
    }

    if( getColorMap() != null )
    {
      sb.append( getColorMap() ).append( "\n" );
    }

    sb.append( "</sldExt:SurfacePolygonSymbolizer>\n" );

    return sb.toString();
  }

  /**
   * exports the content of the PolygonSymbolizer as XML formated String
   *
   * @return xml representation of the PolygonSymbolizer
   */
  @Override
  public String exportAsXML( )
  {
    final StringBuffer sb = new StringBuffer( 1000 );

    sb.append( "<SurfacePolygonSymbolizer xmlns:sldExt=\"" + SLDFactory.SLDNS_EXT + "\"" );

    final UOM uom = getUom();

    if( uom != null )
    {
      sb.append( " uom=\"" + uom.name() + "\">" );
    }
    else
      sb.append( ">\n" );

    final Geometry geometry = getGeometry();
    if( geometry != null )
    {
      sb.append( ((Marshallable) geometry).exportAsXML() );
    }
    if( m_colorMap != null )
    {
      sb.append( ((Marshallable) m_colorMap).exportAsXML() );
    }
    sb.append( "</SurfacePolygonSymbolizer>" );

    return sb.toString();
  }
}