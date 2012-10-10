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
package org.kalypso.grid;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.coverage.GridRange;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain.OffsetVector;
import org.kalypsodeegree_impl.model.cv.GridRange_Impl;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * {@link IGridMetaReader} implementation for World-Files.
 *
 * @author Dirk Kuch
 */
public class GridMetaReaderWorldFile extends AbstractGridMetaReader
{
  private final URL m_worldFile;

  private WorldFile m_world;

  private final URL m_image;

  private final IStatus m_valid;

  public GridMetaReaderWorldFile( final URL image, final URL worldFile )
  {
    m_image = image;
    m_worldFile = worldFile;
    m_valid = setup();
  }

  IStatus setup( )
  {
    if( m_worldFile == null )
    {
      final String msg = String.format( "No world file found for %s", m_image );
      return new Status( IStatus.ERROR, KalypsoDeegreePlugin.getID(), msg );
    }

    InputStream is = null;
    try
    {
      is = m_worldFile.openStream();
      m_world = new WorldFileReader().readWorldFile( is );
      return Status.OK_STATUS;
    }
    catch( final IOException e )
    {
      e.printStackTrace();
      final String msg = String.format( "Failed to access world file %s: %s", m_worldFile, e.toString() );
      return new Status( IStatus.ERROR, KalypsoDeegreePlugin.getID(), msg, e );
    }
    finally
    {
      IOUtils.closeQuietly( is );
    }

  }

  @Override
  public double getOriginCornerY( )
  {
    return m_world.getUlcy();
  }

  @Override
  public double getVectorXy( )
  {
    return m_world.getRasterXGeoY();
  }

  @Override
  public double getVectorYx( )
  {
    return m_world.getRasterYGeoX();
  }

  @Override
  public double getVectorXx( )
  {
    return m_world.getRasterXGeoX();
  }

  @Override
  public double getVectorYy( )
  {
    return m_world.getRasterYGeoY();
  }

  @Override
  public double getOriginCornerX( )
  {
    return m_world.getUlcx();
  }

  @Override
  public RectifiedGridDomain getDomain( final String crs )
  {
    // TODO: JAI bug, does not close the underlying stream
    final RenderedOp image = JAI.create( "url", m_image ); //$NON-NLS-1$
    final TiledImage tiledImage = new TiledImage( image, true );
    final int height = tiledImage.getHeight();
    final int width = tiledImage.getWidth();

    final double[] lows = new double[] { 0, 0 };
    final double[] highs = new double[] { width, height };

    final GridRange gridRange = new GridRange_Impl( lows, highs );

    final OffsetVector offsetX = getOffsetX();
    final OffsetVector offsetY = getOffsetY();
    final GM_Position upperLeftCorner = getUpperLeftCorner();

    final GM_Point origin = GeometryFactory.createGM_Point( upperLeftCorner, crs );

    return new RectifiedGridDomain( origin, offsetX, offsetY, gridRange );
  }

  @Override
  public IStatus isValid( )
  {
    return m_valid;
  }
}