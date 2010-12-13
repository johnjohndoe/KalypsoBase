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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.displayelements.Label;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.GraphicFill;
import org.kalypsodeegree.graphics.sld.Halo;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.graphics.sld.Symbolizer_Impl.UOM;

/**
 * This is a rotated label with style information and screen coordinates, ready to be rendered to the view.
 * <p>
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
class RotatedLabel implements Label
{
  private final String m_caption;

  private final int[] m_xpoints;

  private final int[] m_ypoints;

  private final double m_rotation;

  private final int m_width;

  private final int m_height;

  private final Color m_color;

  private final Font m_font;

  private final int descent, ascent;

  private final Halo m_halo;

  private final Feature m_feature;

  RotatedLabel( final String caption, final Font font, final Color color, final LineMetrics metrics, final Feature feature, final Halo halo, final int x, final int y,
      final int w, final int h, final double rotation, final double anchorPoint[], final double[] displacement )
      {

    this.m_caption = caption;
    this.m_font = font;
    this.m_color = color;
    this.descent = (int)metrics.getDescent();
    this.ascent = (int)metrics.getAscent();
    this.m_feature = feature;
    this.m_halo = halo;
    this.m_rotation = rotation;

    this.m_width = w;
    this.m_height = h;

    // vertices of label boundary
    final int[] xpoints = new int[4];
    final int[] ypoints = new int[4];
    xpoints[0] = x;
    ypoints[0] = y;
    xpoints[1] = x + w;
    ypoints[1] = y;
    xpoints[2] = x + w;
    ypoints[2] = y - h;
    xpoints[3] = x;
    ypoints[3] = y - h;

    // get rotated + translated points
    this.m_xpoints = new int[4];
    this.m_ypoints = new int[4];
    final int tx = xpoints[0];
    final int ty = ypoints[0];

    // transform all vertices of the boundary
    for( int i = 0; i < 4; i++ )
    {
      final int[] point = transformPoint( xpoints[i], ypoints[i], tx, ty, rotation, anchorPoint[0], anchorPoint[1], w, h,
          displacement[0], displacement[1] );
      this.m_xpoints[i] = point[0];
      this.m_ypoints[i] = point[1];
    }
      }

  public String getCaption()
  {
    return m_caption;
  }

  public double getRotation()
  {
    return m_rotation;
  }

  @Override
  public void paintBoundaries( final Graphics2D g )
  {
    setColor( g, new Color( 0x888888 ), 0.5 );
    g.fillPolygon( m_xpoints, m_ypoints, m_xpoints.length );
    g.setColor( Color.BLACK );

    // get the current transform
    final AffineTransform saveAT = g.getTransform();

    // translation parameters (rotation)
    final AffineTransform transform = new AffineTransform();

    // render the text
    transform.rotate( m_rotation, m_xpoints[0], m_ypoints[0] );
    g.setTransform( transform );
    //g.drawString( caption, xpoints [0], ypoints [0] - descent);

    // restore original transform
    g.setTransform( saveAT );
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

    // get the current transform
    final AffineTransform saveAT = g.getTransform();

    // perform transformation
    final AffineTransform transform = new AffineTransform();
    transform.rotate( m_rotation, m_xpoints[0], m_ypoints[0] );
    g.setTransform( transform );

    // render the halo (only if specified)
    if( m_halo != null )
    {
      try
      {
        paintHalo( g, m_halo, m_xpoints[0], m_ypoints[0] - descent );
      }
      catch( final FilterEvaluationException e )
      {
        e.printStackTrace();
      }
    }

    // render the text
    setColor( g, m_color, 1.0 );
    g.setFont( m_font );
    g.drawString( m_caption, m_xpoints[0], m_ypoints[0] - descent );

    // restore original transform
    g.setTransform( saveAT );
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

    g.fillRect( x - radius, y - ascent - radius, m_width + 2 * radius, m_height + 2 * radius );

    // only stroke outline, if Stroke-Element is given
    final org.kalypsodeegree.graphics.sld.Stroke stroke = halo.getStroke();

    if( stroke != null )
    {
      final double opacity = stroke.getOpacity( m_feature );

      if( opacity > 0.01 )
      {
        Color color = stroke.getStroke( m_feature );
        final int alpha = (int)Math.round( opacity * 255 );
        final int red = color.getRed();
        final int green = color.getGreen();
        final int blue = color.getBlue();
        color = new Color( red, green, blue, alpha );
        g.setColor( color );

        final float[] dash = stroke.getDashArray( m_feature );

        // use a simple Stroke if dash == null or dash length < 2
        BasicStroke bs = null;
        final float strokeWidth = (float) stroke.getWidth( m_feature );

        if( ( dash == null ) || ( dash.length < 2 ) )
        {
          bs = new BasicStroke( strokeWidth );
        }
        else
        {
          bs = new BasicStroke( strokeWidth, stroke.getLineCap( m_feature ), stroke.getLineJoin( m_feature ), 10.0f, dash, stroke.getDashOffset( m_feature ) );
          bs = new BasicStroke( strokeWidth, stroke.getLineCap( m_feature ), stroke.getLineJoin( m_feature ), 1.0f, dash,
              1.0f );
        }

        g.setStroke( bs );

        g.drawRect( x - radius, y - ascent - radius, m_width + 2 * radius, m_height + 2 * radius );
      }
    }
  }

  @Override
  public int getX()
  {
    return m_xpoints[0];
  }

  @Override
  public int getY()
  {
    return m_ypoints[0];
  }

  @Override
  public int getMaxX()
  {
    return m_xpoints[1];
  }

  @Override
  public int getMaxY()
  {
    return m_ypoints[1];
  }

  @Override
  public int getMinX()
  {
    return m_xpoints[3];
  }

  @Override
  public int getMinY()
  {
    return m_ypoints[3];
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
    System.out.println( "Intersection test for rotated labels is " + "not implemented yet!" );
    return false;
  }

  private int[] transformPoint( final int x, final int y, final int tx, final int ty, final double rotation, final double anchorPointX,
      final double anchorPointY, final int w, final int h, final double displacementX, final double displacementY )
  {

    final double cos = Math.cos( rotation );
    final double sin = Math.sin( rotation );
    final double dx = -anchorPointX * w;
    //		double dy = anchorPointY * h;
    final double dy = anchorPointY * h - displacementY;

    final double m00 = cos;
    final double m01 = -sin;
    final double m02 = cos * dx - sin * dy + tx - tx * cos + ty * sin;
    final double m10 = sin;
    final double m11 = cos;
    final double m12 = sin * dx + cos * dy + ty - tx * sin - ty * cos;

    final int[] point2 = new int[2];

    point2[0] = (int)( m00 * x + m01 * y + m02 + 0.5 + displacementX );
    point2[1] = (int)( m10 * x + m11 * y + m12 + 0.5 );
    //		point2 [1] = (int) (m10 * x + m11 * y + m12 + 0.5 - displacementY);
    return point2;
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
    return m_caption;
  }
}