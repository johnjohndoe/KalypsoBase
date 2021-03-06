/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.shape.deegree;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.kalypso.shape.ShapeDataException;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

class TinIterator implements Iterator<TinPointer>
{
  private final GMLXPath m_geometry;

  private final Feature[] m_tinFeatures;

  private TinPointer m_pointer;

  public TinIterator( final GMLXPath geoemtry, final Feature[] tinFeatures )
  {
    m_geometry = geoemtry;
    m_tinFeatures = tinFeatures;
    m_pointer = searchNext( new TinPointer( null, 0, -1, null ) );
  }

  private TinPointer searchNext( final TinPointer start )
  {
    try
    {
      int featureIndex = start.getFeatureIndex();
      int triangleIndex = start.getTriangleIndex();

      /* Loop to skip empty tins */
      while( true )
      {
        triangleIndex++;

        if( featureIndex >= m_tinFeatures.length )
          return null;

        final Feature feature = m_tinFeatures[featureIndex];
        final GM_TriangulatedSurface tin;
        if( start.getTin() == null )
          tin = getTin( m_geometry, feature );
        else
          tin = start.getTin();

        final int tinSize = tin.size();
        if( triangleIndex < tinSize )
          return new TinPointer( m_tinFeatures[featureIndex], featureIndex, triangleIndex, tin );

        /* Skip to next tin */
        triangleIndex = 0;
        featureIndex++;
      }
    }
    catch( final ShapeDataException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  static GM_TriangulatedSurface getTin( final GMLXPath geometry, final Feature feature ) throws ShapeDataException
  {
    try
    {
      final Object property = GMLXPathUtilities.query( geometry, feature );
      if( property == null )
        return null;

      if( property instanceof GM_TriangulatedSurface )
        return (GM_TriangulatedSurface) property;

      throw new ShapeDataException( String.format( "Geometry is not a GM_TriangualtedSurface: %s", geometry.toString() ) ); //$NON-NLS-1$
    }
    catch( final GMLXPathException e )
    {
      throw new ShapeDataException( "Unable to access triangulated surface", e ); //$NON-NLS-1$
    }
  }

  @Override
  public boolean hasNext( )
  {
    return m_pointer != null;
  }

  @Override
  public TinPointer next( )
  {
    if( m_pointer == null )
      throw new NoSuchElementException();

    final TinPointer pointer = m_pointer;
    m_pointer = searchNext( pointer );

    return pointer;
  }

  @Override
  public void remove( )
  {
    throw new UnsupportedOperationException();
  }
}