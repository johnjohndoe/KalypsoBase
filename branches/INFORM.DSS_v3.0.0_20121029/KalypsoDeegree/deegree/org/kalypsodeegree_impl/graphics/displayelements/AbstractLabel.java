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
package org.kalypsodeegree_impl.graphics.displayelements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;

import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.displayelements.Label;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.GraphicFill;
import org.kalypsodeegree.graphics.sld.Halo;
import org.kalypsodeegree.graphics.sld.Stroke;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.graphics.sld.Symbolizer_Impl.UOM;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Gernot Belger
 */
abstract class AbstractLabel implements Label
{
  private final String m_caption;

  private final int m_width;

  private final int m_height;

  private final Color m_color;

  private final Font m_font;

  private final int m_descent;

  private final int m_ascent;

  private final Halo m_halo;

  private final Feature m_feature;

  private Polygon m_boundary;

  private Geometry m_bufferedBoundary;

  protected AbstractLabel( final String caption, final Font font, final Color color, final LineMetrics metrics, final Feature feature, final Halo halo, final Dimension bounds )
  {
    m_caption = caption;
    m_font = font;
    m_color = color;
    m_descent = (int) metrics.getDescent();
    m_ascent = (int) metrics.getAscent();
    m_feature = feature;
    m_halo = halo;

    m_width = bounds.width;
    m_height = bounds.height;
  }

  protected final void setBoundary( final Polygon boundary )
  {
    m_boundary = boundary;

    double buffer = 0;
    if( m_halo != null )
    {
      try
      {
        final double radius = m_halo.getRadius( m_feature );
        buffer += radius;

        final Stroke stroke = m_halo.getStroke();
        if( stroke != null )
        {
          final double width = stroke.getWidth( m_feature );
          buffer += width / 2;
        }
      }
      catch( final FilterEvaluationException e )
      {
        // ignore here
      }
    }

    m_bufferedBoundary = m_boundary.buffer( buffer );
  }

  @Override
  public Geometry getBoundary( )
  {
    return m_bufferedBoundary;
  }

  protected final Polygon getBounds( )
  {
    return m_boundary;
  }

  protected final int getWidth( )
  {
    return m_width;
  }

  protected final int getHeight( )
  {
    return m_height;
  }

  public final String getCaption( )
  {
    return m_caption;
  }

  @Override
  public final void paintBoundaries( final Graphics2D g )
  {
    setColor( g, new Color( 0x888888 ), 0.5 );

    final Coordinate[] coordinates = m_boundary.getCoordinates();

    final int[] xs = new int[coordinates.length - 1];
    final int[] ys = new int[coordinates.length - 1];

    for( int i = 0; i < xs.length; i++ )
    {
      xs[i] = (int) coordinates[i].x;
      ys[i] = (int) coordinates[i].y;
    }

    g.fillPolygon( xs, ys, xs.length );
  }

  private Graphics2D setColor( final Graphics2D g2, final Color color, final double opacity )
  {
    Color colorToSet;

    // FIXME: why this case?
    if( opacity < 0.999 )
    {
      final int alpha = (int) Math.round( opacity * 255 );
      final int red = color.getRed();
      final int green = color.getGreen();
      final int blue = color.getBlue();
      colorToSet = new Color( red, green, blue, alpha );
    }
    else
      colorToSet = color;

    g2.setColor( colorToSet );
    return g2;
  }

  @Override
  public final String toString( )
  {
    return m_caption;
  }

  /**
   * Renders the label's halo to the submitted <tt>Graphics2D</tt> context.
   * <p>
   * 
   * @param g
   *          <tt>Graphics2D</tt> context to be used
   * @param halo
   *          <tt>Halo</tt> from the SLD
   * @param x
   *          x-coordinate of the label
   * @param y
   *          y-coordinate of the label
   * @throws FilterEvaluationException
   *           if the evaluation of a <tt>ParameterValueType</tt> fails
   */
  protected final void paintHalo( final Graphics2D g, final Halo halo, final int x, final int y ) throws FilterEvaluationException
  {
    final int radius = (int) halo.getRadius( m_feature );

    // only draw filled rectangle or circle, if Fill-Element is given
    final Fill fill = halo.getFill();

    if( fill != null )
    {
      final GraphicFill gFill = fill.getGraphicFill();

      if( gFill != null )
      {
        final BufferedImage texture = gFill.getGraphic().getAsImage( m_feature, UOM.pixel, null );
        final Rectangle anchor = new Rectangle( 0, 0, texture.getWidth( null ), texture.getHeight( null ) );
        g.setPaint( new TexturePaint( texture, anchor ) );
      }
      else
      {
        final double opacity = fill.getOpacity( m_feature );
        final Color color = fill.getFill( m_feature );
        setColor( g, color, opacity );
      }
    }
    else
    {
      g.setColor( Color.white );
    }

    g.fillRect( x - radius, y - m_ascent - radius, m_width + 2 * radius, m_height + 2 * radius );

    // only stroke outline, if Stroke-Element is given
    final org.kalypsodeegree.graphics.sld.Stroke stroke = halo.getStroke();

    if( stroke != null )
    {
      final double opacity = stroke.getOpacity( m_feature );

      if( opacity > 0.01 )
      {
        Color color = stroke.getStroke( m_feature );
        final int alpha = (int) Math.round( opacity * 255 );
        final int red = color.getRed();
        final int green = color.getGreen();
        final int blue = color.getBlue();
        color = new Color( red, green, blue, alpha );
        g.setColor( color );

        final float[] dash = stroke.getDashArray( m_feature );

        // use a simple Stroke if dash == null or dash length < 2
        BasicStroke bs = null;
        final float strokeWidth = (float) stroke.getWidth( m_feature );

        if( dash == null || dash.length < 2 )
        {
          bs = new BasicStroke( strokeWidth );
        }
        else
        {
          bs = new BasicStroke( strokeWidth, stroke.getLineCap( m_feature ), stroke.getLineJoin( m_feature ), 10.0f, dash, stroke.getDashOffset( m_feature ) );
          bs = new BasicStroke( strokeWidth, stroke.getLineCap( m_feature ), stroke.getLineJoin( m_feature ), 1.0f, dash, 1.0f );
        }

        g.setStroke( bs );

        g.drawRect( x - radius, y - m_ascent - radius, m_width + 2 * radius, m_height + 2 * radius );
      }
    }
  }

  protected final void doPaint( final Graphics2D g )
  {
    final Coordinate[] coordinates = m_boundary.getCoordinates();

    final int x = (int) coordinates[0].x;
    final int y = (int) coordinates[0].y;

    // render the halo (only if specified)
    if( m_halo != null )
    {
      try
      {

        paintHalo( g, m_halo, x, y - m_descent );
      }
      catch( final FilterEvaluationException e )
      {
        e.printStackTrace();
      }
    }

    // render the text
    setColor( g, m_color, 1.0 );
    g.setFont( m_font );
    g.drawString( m_caption, x, y - m_descent );
  }

  @Override
  public boolean intersects( final Label that )
  {
    final Geometry boundary = getBoundary();
    final Geometry otherBounds = that.getBoundary();
    return otherBounds.intersects( boundary );
  }
}