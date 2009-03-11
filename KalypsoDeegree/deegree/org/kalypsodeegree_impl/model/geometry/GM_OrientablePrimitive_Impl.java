/*--------------- Kalypso-Deegree-Header ------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 
 history:
 
 Files in this package are originally taken from deegree and modified here
 to fit in kalypso. As goals of kalypso differ from that one in deegree
 interface-compatibility to deegree is wanted but not retained always. 
 
 If you intend to use this software in other ways than in kalypso 
 (e.g. OGC-web services), you should consider the latest version of deegree,
 see http://www.deegree.org .

 all modifications are licensed as deegree, 
 original copyright:
 
 Copyright (C) 2001 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/exse/
 lat/lon GmbH
 http://www.lat-lon.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypsodeegree_impl.model.geometry;

import java.io.Serializable;

import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_OrientablePrimitive;

/**
 * default implementation of the GM_OrientablePrimitive interface from package jago.model. the implementation is
 * abstract because it doesn't make sense to instantiate it.
 * <p>
 * ------------------------------------------------------------
 * </p>
 * 
 * @version 8.6.2001
 * @author Andreas Poth
 *         <p>
 */
abstract class GM_OrientablePrimitive_Impl extends GM_Primitive_Impl implements GM_OrientablePrimitive, Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 5655221930434396483L;

  private char m_orientation = '+';

  /**
   * the constructor sets the curves orientation
   * 
   * @param crs
   *            spatial reference system of the geometry
   * @param orientation
   *            orientation of the curve ('+'|'-')
   * @exception GM_Exception
   *                will be thrown if orientation is invalid
   */
  protected GM_OrientablePrimitive_Impl( String crs, char orientation ) throws GM_Exception
  {
    super( crs );
    setOrientation( orientation );
  }

  /**
   * returns the orientation of a curve
   * 
   * @return curve orientation ('+'|'-')
   */
  public char getOrientation( )
  {
    return m_orientation;
  }

  /**
   * sets the curves orientation
   * 
   * @param orientation
   *            orientation of the curve ('+'|'-')
   * @exception GM_Exception
   *                will be thrown if orientation is invalid
   */
  public void setOrientation( char orientation ) throws GM_Exception
  {
    if( (orientation != '+') && (orientation != '-') )
    {
      throw new GM_Exception( orientation + " isn't a valid direction" );
    }

    m_orientation = orientation;
  }
}