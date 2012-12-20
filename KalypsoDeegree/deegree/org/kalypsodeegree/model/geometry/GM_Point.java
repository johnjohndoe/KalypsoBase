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

package org.kalypsodeegree.model.geometry;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;

/**
 * The basic two dimensional geometry
 * <p>
 * -----------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @version $Revision$ $Date$
 *          <p>
 */
public interface GM_Point extends GM_AbstractGeometry
{
  QName POINT_ELEMENT = new QName( NS.GML3, "Point" );

  /**
   * returns the x-value of the point
   */
  double getX( );

  /**
   * returns the y-value of the point
   */
  double getY( );

  /**
   * returns the z-value of the point
   */
  double getZ( );

  /**
   * returns the x- and y-value of the point as a two dimensional array the first field contains the x- the second field
   * the y-value.
   */
  double[] getAsArray( );

  /**
   * returns the position of the point
   */
  GM_Position getPosition( );
}
