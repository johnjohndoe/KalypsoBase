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
package org.kalypso.grid.parallel;

import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.grid.IGeoGridWalker;
import org.kalypso.grid.IGeoWalkingStrategy;
import org.kalypso.grid.areas.IGeoGridArea;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Gernot Belger
 */
public class SequentialBinaryGeoGridWalkingStrategy implements IGeoWalkingStrategy
{
  private final SequentialBinaryGeoGridReader m_reader;

  public SequentialBinaryGeoGridWalkingStrategy( final SequentialBinaryGeoGridReader reader )
  {
    m_reader = reader;
  }

  @Override
  public Object walk( final IGeoGrid grid, final IGeoGridWalker pwo, final IGeoGridArea walkingArea, final IProgressMonitor monitor ) throws GeoGridException, OperationCanceledException
  {
    Assert.isTrue( grid == m_reader );

    Assert.isTrue( walkingArea == null );

    pwo.start( grid );

    final int sizeX = m_reader.getSizeX();
    final int sizeY = m_reader.getSizeX();

    final Coordinate origin = m_reader.getOrigin();
    final Coordinate offsetX = m_reader.getOffsetX();
    final Coordinate offsetY = m_reader.getOffsetY();

    monitor.beginTask( "---", sizeX * sizeY );

    try
    {
      while( true )
      {
        final ParallelBinaryGridProcessorBean nextBlock = m_reader.getNextBlock();
        if( nextBlock == null )
          break;

        final long startPosition = nextBlock.getStartPosition();

        /* walk this block */
        for( int i = 0; i < nextBlock.getSize(); i++ )
        {
          final long globalPosition = i + startPosition;

          final int x = (int) (globalPosition % sizeX);
          final int y = (int) (globalPosition / sizeX);

          final double cx = origin.x + x * offsetX.x + y * offsetY.x;
          final double cy = origin.y + x * offsetX.y + y * offsetY.y;
          final double z = nextBlock.getValue( i );

          final Coordinate crd = new Coordinate( cx, cy, z );

          pwo.operate( x, y, crd );

        }

        if( monitor.isCanceled() )
          throw new OperationCanceledException();
        monitor.worked( nextBlock.getSize() );
      }

      return pwo.finish();
    }
    catch( final IOException e )
    {
      e.printStackTrace();
      throw new GeoGridException( e.getLocalizedMessage(), e );
    }
  }
}
