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

import java.awt.Color;
import java.util.Collection;
import java.util.Formatter;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;
import org.kalypsodeegree.graphics.sld.LineSymbolizer;
import org.kalypsodeegree.graphics.sld.ParameterValueType;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.RasterSymbolizer;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.xml.Marshallable;

/**
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version $Revision$ $Date$
 */
public class RasterSymbolizer_Impl extends Symbolizer_Impl implements RasterSymbolizer, Marshallable
{
  private ParameterValueType m_opacity;

  private SortedMap<Double, ColorMapEntry> m_colorMap;

  private Symbolizer m_imageOutline;

  private ShadedRelief m_shadedRelief;

  /**
   * @param imageOutline
   *          See {@link #setImageOutline(Symbolizer)}
   */
  public RasterSymbolizer_Impl( final ParameterValueType opacity, final SortedMap<Double, ColorMapEntry> colorMap, final Symbolizer imageOutline, final ShadedRelief shadedRelief )
  {
    setOpacity( opacity );
    setColorMap( colorMap );
    setShadedRelief( shadedRelief );
    setImageOutline( imageOutline );
  }

  @Override
  public ParameterValueType getOpacity( )
  {
    return m_opacity;
  }

  @Override
  public void setOpacity( final ParameterValueType opacity )
  {
    m_opacity = opacity;
  }

  @Override
  public SortedMap<Double, ColorMapEntry> getColorMap( )
  {
    return m_colorMap;
  }

  @Override
  public void setColorMap( final SortedMap<Double, ColorMapEntry> colorMap )
  {
    m_colorMap = colorMap;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.RasterSymbolizer#getImageOutline()
   */
  @Override
  public Symbolizer getImageOutline( )
  {
    return m_imageOutline;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.RasterSymbolizer#setImageOutline(org.kalypsodeegree.graphics.sld.Symbolizer)
   */
  @Override
  public void setImageOutline( final Symbolizer imageOutline )
  {
    Assert.isTrue( imageOutline == null || imageOutline instanceof LineSymbolizer || imageOutline instanceof PolygonSymbolizer );

    m_imageOutline = imageOutline;
  }

  @Override
  public ShadedRelief getShadedRelief( )
  {
    return m_shadedRelief;
  }

  @Override
  public void setShadedRelief( final ShadedRelief shadedRelief )
  {
    m_shadedRelief = shadedRelief;
  }

  @Override
  public String exportAsXML( )
  {
    final Formatter formatter = new Formatter();

    formatter.format( "<RasterSymbolizer" );

    final UOM uom = getUom();
    if( uom != null )
      formatter.format( " uom=\"%s\"", uom.name() );

    formatter.format( ">%n" );

    if( m_opacity != null )
    {
      formatter.format( "<Opacity>%s</Opacity>", ((Marshallable) m_opacity).exportAsXML() );
    }

    if( m_colorMap != null )
    {
      formatter.format( "<ColorMap>%n" );
      for( final Map.Entry<Double, ColorMapEntry> entry : m_colorMap.entrySet() )
      {
        final ColorMapEntry colorMapEntry = entry.getValue();
        formatter.format( colorMapEntry.exportAsXML() );
      }
      formatter.format( "</ColorMap>%n" );
    }

    if( m_shadedRelief != null )
      formatter.format( m_shadedRelief.exportAsXML() );

    if( m_imageOutline != null )
    {
      formatter.format( "<ImageOutline>%n" );
      formatter.format( m_imageOutline.exportAsXML() );
      formatter.format( "</ImageOutline>%n" );
    }

    formatter.format( "</RasterSymbolizer>%n" );

    return formatter.toString();
  }

  @Override
  public void paint( final GC gc, final Feature feature )
  {
    // TODO: decide, if we should show the overal opacity
// final ParameterValueType opacity = getOpacity();
// final double opacityValue = opacity == null ? 1.0 : Double.parseDouble( opacity.evaluate( feature ) );
    final double opacityValue = 1.0;

    final Rectangle clipping = gc.getClipping();

    // FIXME
    if( m_colorMap.size() == 0 )
      return;

    final Collection<ColorMapEntry> values = m_colorMap.values();
    final ColorMapEntry[] entries = values.toArray( new ColorMapEntry[values.size()] );

    gc.setForeground( gc.getDevice().getSystemColor( SWT.COLOR_BLACK ) );
    gc.setBackground( gc.getDevice().getSystemColor( SWT.COLOR_WHITE ) );
    gc.setLineAttributes( new LineAttributes( 1 ) );

    /* we draw 2 rects in the colors of the color map and a black rectangle around it */
    final Color colorAwtStart = entries[0].getColorAndOpacity();
    final org.eclipse.swt.graphics.Color colorStart = new org.eclipse.swt.graphics.Color( gc.getDevice(), colorAwtStart.getRed(), colorAwtStart.getGreen(), colorAwtStart.getBlue() );
    gc.setBackground( colorStart );
    final double alphaStart = colorAwtStart.getAlpha() / 255.0;
    gc.setAlpha( (int) ((alphaStart * opacityValue) * 255.0) );
    gc.fillRectangle( clipping.x, clipping.y, clipping.width - 1, clipping.height / 2 );

    final Color colorAwtEnd = entries[entries.length - 1].getColorAndOpacity();
    final org.eclipse.swt.graphics.Color colorEnd = new org.eclipse.swt.graphics.Color( gc.getDevice(), colorAwtEnd.getRed(), colorAwtEnd.getGreen(), colorAwtEnd.getBlue() );
    gc.setBackground( colorEnd );
    final double alphaEnd = colorAwtEnd.getAlpha() / 255.0;
    gc.setAlpha( (int) ((alphaEnd * opacityValue) * 255.0) );
    gc.fillRectangle( clipping.x, clipping.height / 2, clipping.width - 1, clipping.height - 1 );

    // the black border
    gc.setForeground( gc.getDevice().getSystemColor( SWT.COLOR_BLACK ) );
    gc.drawRectangle( clipping.x, clipping.y, clipping.width - 1, clipping.height - 1 );

    colorStart.dispose();
    colorEnd.dispose();
  }

}