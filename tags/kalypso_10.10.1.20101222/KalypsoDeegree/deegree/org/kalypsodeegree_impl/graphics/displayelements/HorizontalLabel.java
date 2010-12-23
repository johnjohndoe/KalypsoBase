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
package org.kalypsodeegree_impl.graphics.displayelements;

import java.awt.BasicStroke;
import java.awt.Color;
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
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.graphics.sld.Symbolizer_Impl.UOM;

/**
 * This is a horizontal label with style information and screen coordinates, ready to be rendered to the view.
 * <p>
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
class HorizontalLabel implements Label
{
  private final String caption;

  private final int[] xpoints = new int[4];

  private final int[] ypoints = new int[4];

  // width and height of the caption
  private final int w, h;

  private final Color m_color;

  private final Font font;

  private final int descent, ascent;

  private final Halo m_halo;

  private final Feature feature;

  HorizontalLabel( final String caption, final Font font, final Color color, final LineMetrics metrics, final Feature feature, final Halo halo, final int x,
      final int y, final int w, final int h, final double anchorPoint[], final double[] displacement )
      {

    this.caption = caption;
    this.font = font;
    this.m_color = color;
    this.descent = (int)metrics.getDescent();
    this.ascent = (int)metrics.getAscent();
    this.feature = feature;
    this.m_halo = halo;

    this.w = w;
    this.h = h;

    final int dx = (int)( -anchorPoint[0] * w + displacement[0] + 0.5 );
    final int dy = (int)( anchorPoint[1] * h - displacement[1] + 0.5 );

    // vertices of label boundary
    xpoints[0] = x + dx;
    ypoints[0] = y + dy;
    xpoints[1] = x + w + dx;
    ypoints[1] = y + dy;
    xpoints[2] = x + w + dx;
    ypoints[2] = y - h + dy;
    xpoints[3] = x + dx;
    ypoints[3] = y - h + dy;
      }

  public String getCaption()
  {
    return caption;
  }

  @Override
  public void paintBoundaries( final Graphics2D g )
  {
    setColor( g, new Color( 0x888888 ), 0.5 );
    g.fillPolygon( xpoints, ypoints, xpoints.length );
    g.setColor( Color.BLACK );

    // render the text
    //g.drawString( caption, xpoints [0], ypoints [0] - descent);
  }

  /**
   * Renders the label (including halo) to the submitted <tt>Graphics2D</tt> context.
   * <p>
   * 
   * @param g
   *          <tt>Graphics2D</tt> context to be used
   */
  @Override
  public void paint( final Graphics2D g )
  {
    // render the halo (only if specified)
    if( m_halo != null )
    {
      try
      {
        paintHalo( g, m_halo, xpoints[0], ypoints[0] - descent );
      }
      catch( final FilterEvaluationException e )
      {
        e.printStackTrace();
      }
    }

    // render the text
    setColor( g, m_color, 1.0 );
    g.setFont( font );
    g.drawString( caption, xpoints[0] + 0.5f, (ypoints[0] - descent) );
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
   * 
   * @throws FilterEvaluationException
   *           if the evaluation of a <tt>ParameterValueType</tt> fails
   */
  private void paintHalo( final Graphics2D g, final Halo halo, final int x, final int y ) throws FilterEvaluationException
  {
    final int radius = (int)halo.getRadius( feature );

    // only draw filled rectangle or circle, if Fill-Element is given
    final Fill fill = halo.getFill();

    if( fill != null )
    {
      final GraphicFill gFill = fill.getGraphicFill();

      if( gFill != null )
      {
        final BufferedImage texture = gFill.getGraphic().getAsImage( feature, UOM.pixel, null );
        final Rectangle anchor = new Rectangle( 0, 0, texture.getWidth( null ), texture.getHeight( null ) );
        g.setPaint( new TexturePaint( texture, anchor ) );
      }
      else
      {
        final double opacity = fill.getOpacity( feature );
        final Color color = fill.getFill( feature );
        setColor( g, color, opacity );
      }
    }
    else
    {
      g.setColor( Color.white );
    }

    // FIXME: the whole halo is not painted according to the SLD spec;
    // The normal meaning is to add a lining to the edge of text.

    g.fillRect( x - radius, y - ascent - radius, w + (radius * 2), h + (radius * 2) );

    // only stroke outline, if Stroke-Element is given
    final org.kalypsodeegree.graphics.sld.Stroke stroke = halo.getStroke();

    if( stroke != null )
    {
      final double opacity = stroke.getOpacity( feature );

      if( opacity > 0.01 )
      {
        Color color = stroke.getStroke( feature );
        final int alpha = (int)Math.round( opacity * 255 );
        final int red = color.getRed();
        final int green = color.getGreen();
        final int blue = color.getBlue();
        color = new Color( red, green, blue, alpha );
        g.setColor( color );

        final float[] dash = stroke.getDashArray( feature );

        // use a simple Stroke if dash == null or dash length < 2
        BasicStroke bs = null;
        final float strokeWidth = (float)stroke.getWidth( feature );

        if( ( dash == null ) || ( dash.length < 2 ) )
        {
          bs = new BasicStroke( strokeWidth );
        }
        else
        {
          bs = new BasicStroke( strokeWidth, stroke.getLineCap( feature ), stroke.getLineJoin( feature ), 10.0f, dash,
              stroke.getDashOffset( feature ) );
          bs = new BasicStroke( strokeWidth, stroke.getLineCap( feature ), stroke.getLineJoin( feature ), 1.0f, dash,
              1.0f );
        }
        g.setStroke( bs );

        g.drawRect( x - radius, y - ascent - radius, w + 2 * radius, h + 2 * radius );
      }
    }
  }

  @Override
  public int getX()
  {
    return xpoints[0];
  }

  @Override
  public int getY()
  {
    return ypoints[0];
  }

  @Override
  public int getMaxX()
  {
    return xpoints[1];
  }

  @Override
  public int getMaxY()
  {
    return ypoints[1];
  }

  @Override
  public int getMinX()
  {
    return xpoints[3];
  }

  @Override
  public int getMinY()
  {
    return ypoints[3];
  }

  /**
   * Determines if the label intersects with another label.
   * <p>
   * 
   * @param that
   *          label to test
   * @return true if the labels intersect
   */
  @Override
  public boolean intersects( final Label that )
  {
    if( !( that instanceof HorizontalLabel ) )
    {
      System.out.println( "Intersection test for rotated labels is " + "not implemented yet!" );
      return false;
    }

    // coordinates of this GM_Envelope's BBOX
    final double west1 = getMinX();
    final double south1 = getMinY();
    final double east1 = getMaxX();
    final double north1 = getMaxY();

    // coordinates of the other GM_Envelope's BBOX
    final double west2 = ( (HorizontalLabel)that ).getMinX();
    final double south2 = ( (HorizontalLabel)that ).getMinY();
    final double east2 = ( (HorizontalLabel)that ).getMaxX();
    final double north2 = ( (HorizontalLabel)that ).getMaxY();

    // special cases: one box lays completly inside the other one
    if( ( west1 <= west2 ) && ( south1 <= south2 ) && ( east1 >= east2 ) && ( north1 >= north2 ) )
    {
      return true;
    }
    if( ( west1 >= west2 ) && ( south1 >= south2 ) && ( east1 <= east2 ) && ( north1 <= north2 ) )
    {
      return true;
    }
    // in any other case of intersection, at least one line of the BBOX has
    // to cross a line of the other BBOX
    // check western boundary of box 1
    // "touching" boxes must not intersect
    if( ( west1 >= west2 ) && ( west1 < east2 ) )
    {
      if( ( south1 <= south2 ) && ( north1 > south2 ) )
      {
        return true;
      }

      if( ( south1 < north2 ) && ( north1 >= north2 ) )
      {
        return true;
      }
    }
    // check eastern boundary of box 1
    // "touching" boxes must not intersect
    if( ( east1 > west2 ) && ( east1 <= east2 ) )
    {
      if( ( south1 <= south2 ) && ( north1 > south2 ) )
      {
        return true;
      }

      if( ( south1 < north2 ) && ( north1 >= north2 ) )
      {
        return true;
      }
    }
    // check southern boundary of box 1
    // "touching" boxes must not intersect
    if( ( south1 >= south2 ) && ( south1 < north2 ) )
    {
      if( ( west1 <= west2 ) && ( east1 > west2 ) )
      {
        return true;
      }

      if( ( west1 < east2 ) && ( east1 >= east2 ) )
      {
        return true;
      }
    }
    // check northern boundary of box 1
    // "touching" boxes must not intersect
    if( ( north1 > south2 ) && ( north1 <= north2 ) )
    {
      if( ( west1 <= west2 ) && ( east1 > west2 ) )
      {
        return true;
      }

      if( ( west1 < east2 ) && ( east1 >= east2 ) )
      {
        return true;
      }
    }
    return false;
  }

  private Graphics2D setColor( final Graphics2D g2, Color color, final double opacity )
  {
    if( opacity < 0.999 )
    {
      final int alpha = (int)Math.round( opacity * 255 );
      final int red = color.getRed();
      final int green = color.getGreen();
      final int blue = color.getBlue();
      color = new Color( red, green, blue, alpha );
    }

    g2.setColor( color );
    return g2;
  }

  @Override
  public String toString()
  {
    return caption;
  }
}