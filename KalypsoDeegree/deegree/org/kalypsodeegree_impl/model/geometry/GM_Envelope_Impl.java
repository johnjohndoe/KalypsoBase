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
package org.kalypsodeegree_impl.model.geometry;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;

/**
 * a boundingbox as child of a GM_Polygon isn't part of the iso19107 spec but it simplifies the geometry handling within
 * jago
 * <P>
 * ------------------------------------------------------------
 * </P>
 * 
 * @author Andreas Poth href="mailto:poth@lat-lon.de"
 * @author Markus Bedel href="mailto:bedel@giub.uni-bonn.de"
 * @version $Id$
 */
public class GM_Envelope_Impl implements GM_Envelope, Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 1081219767894344990L;

  private final double m_minX;

  private final double m_minY;

  private final double m_maxX;

  private final double m_maxY;

  /**
   * The coordinate system of the positions, contained in this envelope.
   */
  private final String m_coordinateSystem;

  /**
   * Creates a new GM_Envelope_Impl object.
   */
  public GM_Envelope_Impl( )
  {
    m_minX = 0.0;
    m_minY = 0.0;
    m_maxX = 0.0;
    m_maxY = 0.0;
    m_coordinateSystem = null;
  }

  /**
   * Creates a new GM_Envelope_Impl object.
   * 
   * @param min
   *          The min position.
   * @param max
   *          The max position.
   * @param coordinateSystem
   *          The coordinate system of the positions, contained in this envelope.
   */
  public GM_Envelope_Impl( final GM_Position min, final GM_Position max, final String coordinateSystem )
  {
    m_minX = Math.min( min.getX(), max.getX() );
    m_minY = Math.min( min.getY(), max.getY() );
    m_maxX = Math.max( min.getX(), max.getX() );
    m_maxY = Math.max( min.getY(), max.getY() );

    m_coordinateSystem = coordinateSystem;
  }

  public GM_Envelope_Impl( final double x1, final double y1, final double x2, final double y2, final String coordinateSystem )
  {
    m_minX = Math.min( x1, x2 );
    m_minY = Math.min( y1, y2 );
    m_maxX = Math.max( x1, x2 );
    m_maxY = Math.max( y1, y2 );
    m_coordinateSystem = coordinateSystem;
  }

  /**
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone( )
  {
    return new GM_Envelope_Impl( m_minX, m_minY, m_maxX, m_maxY, m_coordinateSystem );
  }

  public double getMinX( )
  {
    return m_minX;
  }

  public double getMinY( )
  {
    return m_minY;
  }

  public double getMaxX( )
  {
    return m_maxX;
  }

  public double getMaxY( )
  {
    return m_maxY;
  }

  /**
   * returns the minimum coordinates of bounding box
   */
  public GM_Position getMin( )
  {
    return GeometryFactory.createGM_Position( m_minX, m_minY );
  }

  /**
   * returns the maximum coordinates of bounding box
   */
  public GM_Position getMax( )
  {
    return GeometryFactory.createGM_Position( m_maxX, m_maxY );
  }

  /**
   * returns the width of bounding box
   */
  public double getWidth( )
  {
    return m_maxX - m_minX;
  }

  /**
   * returns the height of bounding box
   */
  public double getHeight( )
  {
    return m_maxY - m_minY;
  }

  /**
   * returns true if the bounding box contains the point specified by the given x and y coordinates
   */
  public boolean contains( final double pointX, final double pointY )
  {
    return (pointX >= m_minX) && (pointX <= m_maxX) && (pointY >= m_minY) && (pointY <= m_maxY);
  }

  /**
   * returns true if the bounding box contains the specified GM_Point
   */
  public boolean contains( final GM_Position point )
  {
    return contains( point.getX(), point.getY() );
  }

  /**
   * returns true if this envelope and the submitted intersects
   */
  public boolean intersects( final GM_Envelope bb )
  {
    // TODO: true or false?
    if( bb == null )
      return true;

    // coordinates of this GM_Envelope's BBOX
    final double minx1 = m_minX;
    final double miny1 = m_minY;
    final double maxx1 = m_maxX;
    final double maxy1 = m_maxY;

    // coordinates of the other GM_Envelope's BBOX
    final double minx2 = bb.getMinX();
    final double miny2 = bb.getMinY();
    final double maxx2 = bb.getMaxX();
    final double maxy2 = bb.getMaxY();

    if( !(Math.max( minx1, minx2 ) <= Math.min( maxx1, maxx2 )) )
      return false;
    if( !(Math.max( miny1, miny2 ) <= Math.min( maxy1, maxy2 )) )
      return false;
    return true;
  }

  /**
   * returns true if all points of the submitted bounding box are within this bounding box
   */
  public boolean contains( final GM_Envelope bb )
  {
    if( bb == null )
      return false;
    final double minX = bb.getMin().getX();
    final double minY = bb.getMin().getY();
    final double maxX = bb.getMax().getX();
    final double maxY = bb.getMax().getY();
    return (contains( minX, minY ) && contains( minX, maxY ) && contains( maxX, minY ) && contains( maxX, maxY ));
  }

  /**
   * returns a new GM_Envelope object representing the intersection of this GM_Envelope with the specified GM_Envelope.
   * * Note: If there is no intersection at all GM_Envelope will be null.
   * 
   * @param bb
   *          the GM_Envelope to be intersected with this GM_Envelope
   * @return the largest GM_Envelope contained in both the specified GM_Envelope and in this GM_Envelope.
   */
  public GM_Envelope createIntersection( final GM_Envelope bb )
  {
    Rectangle2D rect = new Rectangle2D.Double( bb.getMin().getX(), bb.getMin().getY(), bb.getWidth(), bb.getHeight() );
    final Rectangle2D rect2 = new Rectangle2D.Double( this.getMin().getX(), this.getMin().getY(), this.getWidth(), this.getHeight() );

    if( rect2.intersects( bb.getMin().getX(), bb.getMin().getY(), bb.getWidth(), bb.getHeight() ) )
    {
      rect = rect.createIntersection( rect2 );
    }
    else
    {
      rect = null;
    }

    if( rect == null )
    {
      return null;
    }

    final double xmin = rect.getX();
    final double ymin = rect.getY();
    final double xmax = rect.getX() + rect.getWidth();
    final double ymax = rect.getY() + rect.getHeight();

    // TODO Check coordinate systems, if equal.
    return new GM_Envelope_Impl( xmin, ymin, xmax, ymax, m_coordinateSystem );
  }

  /**
   * checks if this point is completly equal to the submitted geometry
   */
  @Override
  public boolean equals( final Object other )
  {
    return equals( other, false );
  }

  /**
   * Checks if this point is completly equal to the submitted geometry
   * 
   * @param exact
   *          If <code>false</code>, the positions are compared by {@link GM_Position#equals(Object, false)}
   * @see GM_Position#equals(Object, boolean)
   */
  public boolean equals( final Object other, final boolean exact )
  {
    if( (other == null) || !(other instanceof GM_Envelope_Impl) )
      return false;

    final GM_Envelope otherEnvelope = (GM_Envelope) other;

    if( !ObjectUtils.equals( m_coordinateSystem, otherEnvelope.getCoordinateSystem() ) )
      return false;

    final double mute = exact ? Double.MIN_NORMAL : GM_Position.MUTE;

    if( Math.abs( m_minX - otherEnvelope.getMinX() ) > mute )
      return false;
    if( Math.abs( m_minY - otherEnvelope.getMinY() ) > mute )
      return false;
    if( Math.abs( m_maxX - otherEnvelope.getMaxX() ) > mute )
      return false;
    if( Math.abs( m_maxY - otherEnvelope.getMaxY() ) > mute )
      return false;

    return true;
  }

  public GM_Envelope getBuffer( final double b )
  {
    final double minX = m_minX - b;
    final double minY = m_minY - b;
    final double maxX = m_maxX + b;
    final double maxY = m_maxY + b;

    return GeometryFactory.createGM_Envelope( minX, minY, maxX, maxY, m_coordinateSystem );
  }

  public GM_Envelope getMerged( final GM_Position pos )
  {
    if( pos == null )
      return this;

    final double minx = Math.min( m_minX, pos.getX() );
    final double miny = Math.min( m_minY, pos.getY() );
    final double maxx = Math.max( m_maxX, pos.getX() );
    final double maxy = Math.max( m_maxY, pos.getY() );

    // TODO Check coordinate systems, if equal.
    return GeometryFactory.createGM_Envelope( minx, miny, maxx, maxy, m_coordinateSystem );

  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Envelope#getMerged(org.kalypsodeegree.model.geometry.GM_Envelope)
   */
  public GM_Envelope getMerged( final GM_Envelope envelope )
  {
    if( envelope == null )
      return this;

    final double minx = Math.min( m_minX, envelope.getMin().getX() );
    final double miny = Math.min( m_minY, envelope.getMin().getY() );
    final double maxx = Math.max( m_maxX, envelope.getMax().getX() );
    final double maxy = Math.max( m_maxY, envelope.getMax().getY() );

    // TODO Check coordinate systems, if equal.
    return GeometryFactory.createGM_Envelope( minx, miny, maxx, maxy, m_coordinateSystem );
  }

  @Override
  public String toString( )
  {
    String ret = null;
    ret = "minX = " + m_minX + "\n";
    ret += "minY = " + m_minY + "\n";
    ret += "maxX = " + m_maxX + "\n";
    ret += "maxY = " + m_maxY + "\n";
    return ret;
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Envelope#getPaned(org.kalypsodeegree.model.geometry.GM_Point)
   */
  public GM_Envelope getPaned( final GM_Point center )
  {
    final double dx = getMax().getX() - getMin().getX();
    final double dy = getMax().getY() - getMin().getY();
    final double minx = center.getX() - dx / 2d;
    final double maxx = center.getX() + dx / 2d;
    final double miny = center.getY() - dy / 2d;
    final double maxy = center.getY() + dy / 2d;

    // TODO Check coordinate systems, if equal.
    return GeometryFactory.createGM_Envelope( minx, miny, maxx, maxy, m_coordinateSystem );
  }

  /**
   * @see org.kalypsodeegree.model.geometry.GM_Envelope#getCoordinateSystem()
   */
  public String getCoordinateSystem( )
  {
    return m_coordinateSystem;
  }
}

/*
 * Changes to this class. What the people haven been up to: $Log$ Changes to this class. What the people haven been up
 * to: Revision 1.21 2008/05/22 15:26:59 devgernot Changes to this class. What the people haven been up to: Changed
 * KalypsoDeegree file header. Changes to this class. What the people haven been up to: Changes to this class. What the
 * people haven been up to: Revision 1.20 2008/05/05 12:57:33 thuel Changes to this class. What the people haven been up
 * to: envelope calculation made more mathematical Changes to this class. What the people haven been up to: Changes to
 * this class. What the people haven been up to: Revision 1.19 2008/03/27 17:18:20 albert Changes to this class. What
 * the people haven been up to: - transformation of raster Changes to this class. What the people haven been up to:
 * Changes to this class. What the people haven been up to: Revision 1.18 2008/01/16 15:12:04 skurzbach Changes to this
 * class. What the people haven been up to: model adaptors based on gml feature type, theme factory extension for maps,
 * adaptor for discretization model (dm): no inverted edges anymore, introduced version 1.0 of dm, removed wrappers for
 * inverted edges, adapted geometry drawing and import/export (2d) of dm, removed double usage of xerces (profiling
 * error) Changes to this class. What the people haven been up to: Changes to this class. What the people haven been up
 * to: Revision 1.17 2007/08/09 17:58:20 devgernot Changes to this class. What the people haven been up to: Some code
 * cleanup for WMS-Theme. Removed unnecessary image transformation stuff. Changes to this class. What the people haven
 * been up to: Revision 1.16 2006/05/28 15:47:16 devgernot - GML-Version is now determined automatically! Use
 * annotations, default is 2.1; - some yellow thingies - repaired some tests (KalypsoCommon, Core is clean, some Test in
 * KalypsoTest are still not running due to GMLSchemaParser/Writer Problems) Revision 1.15 2005/09/29 12:35:21 doemming
 * *** empty log message *** Revision 1.14 2005/09/18 16:22:58 belger *** empty log message *** Revision 1.13 2005/07/21
 * 02:56:47 doemming *** empty log message *** Revision 1.12 2005/06/19 15:10:01 doemming *** empty log message ***
 * Revision 1.11 2005/04/17 21:19:24 doemming *** empty log message *** Revision 1.10 2005/03/08 11:01:04 doemming ***
 * empty log message *** Revision 1.9 2005/03/02 18:17:17 doemming *** empty log message *** Revision 1.8 2005/02/20
 * 18:56:50 doemming *** empty log message *** Revision 1.7 2005/02/15 17:13:49 doemming *** empty log message ***
 * Revision 1.6 2005/01/18 12:50:41 doemming *** empty log message *** Revision 1.5 2004/10/07 14:09:10 doemming ***
 * empty log message *** Revision 1.1 2004/09/02 23:56:51 doemming *** empty log message *** Revision 1.3 2004/08/31
 * 13:54:32 doemming *** empty log message *** Revision 1.13 2004/03/02 07:38:14 poth no message Revision 1.12
 * 2004/02/23 07:47:50 poth no message Revision 1.11 2004/01/27 07:55:44 poth no message Revision 1.10 2004/01/08
 * 09:50:22 poth no message Revision 1.9 2003/09/14 14:05:08 poth no message Revision 1.8 2003/07/10 15:24:23 mrsnyder
 * Started to implement LabelDisplayElements that are bound to a Polygon. Fixed error in
 * GM_MultiSurface_Impl.calculateCentroidArea(). Revision 1.7 2003/07/03 12:32:26 poth no message Revision 1.6
 * 2003/03/20 12:10:29 mrsnyder Rewrote intersects() method. Revision 1.5 2003/03/19 15:30:04 axel_schaefer Intersects:
 * crossing envelopes, but points are not in envelope
 */
