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
package org.kalypso.gml.processes.tin;

import java.net.URL;

import org.kalypsodeegree.model.elevation.ElevationException;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.tin.ITin;

/**
 * Wrapper around a {@link org.kalypsodeegree.model.geometry.GM_TriangulatedSurface} that implementents {@link ITin}
 *
 * @author Gernot Belger
 */
public class TriangulatedSurfaceTin implements ITin
{
  private final URL m_triangulatedSurfaceLocation;

  private TriangulatedSurfaceFeature m_surface;

  public TriangulatedSurfaceTin( final URL triangulatedSurfaceLocation )
  {
    m_triangulatedSurfaceLocation = triangulatedSurfaceLocation;
  }

  private synchronized void checkData( )
  {
    if( m_surface == null )
    {
      m_surface = loadData();
    }
  }

  private TriangulatedSurfaceFeature loadData( )
  {


    // TODO Auto-generated method stub

  }

  @Override
  public double getElevation( final GM_Point location ) throws ElevationException
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public GM_Envelope getBoundingBox( ) throws ElevationException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double getMinElevation( ) throws ElevationException
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getMaxElevation( ) throws ElevationException
  {
    // TODO Auto-generated method stub
    return 0;
  }
}