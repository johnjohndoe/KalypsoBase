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
package org.kalypso.shape;

import java.nio.charset.Charset;
import java.util.Iterator;

import org.kalypso.shape.dbf.IDBFValue;
import org.kalypso.shape.geometry.ISHPGeometry;

/**
 * Interface for providing data for generating shape files.
 * 
 * @author Thomas Jung
 * @author Gernot Belger
 */
public interface IShapeData
{
  public Charset getCharset( ) throws ShapeDataException;

  /**
   * Coordinate system of all the geometries this data provider returns.
   */
  public String getCoordinateSystem( );

  /** Type of shape (like POINTZ). One of {@link ShapeConst} constants. */
  public ShapeType getShapeType( ) throws ShapeDataException;

  public IDBFValue[] getFields( ) throws ShapeDataException;

  /**
   * Access to the data elements. Use the object retrieved from the iterator to call {@link #getGeometry(int)} and
   * {@link #getData(int, int)}
   */
  public Iterator< ? > iterator( );

  /** Returns the total size. @return -1 if the total size is not known. */
  public int size( ) throws ShapeDataException;

  public ISHPGeometry getGeometry( final Object element ) throws ShapeDataException;
}
