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
package org.kalypsodeegree_impl.graphics.displayelements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.elevation.ElevationException;
import org.kalypsodeegree.model.elevation.IElevationModel;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author Gernot Belger
 */
public class TriangulatedSurfacePolygonDisplayElement extends DisplayElement_Impl
{
  private final IElevationModel m_surface;

  private final IElevationColorModel m_colorModel;

  public TriangulatedSurfacePolygonDisplayElement( final Feature feature, final IElevationModel surface, final IElevationColorModel colorModel )
  {
    super( feature );

    m_surface = surface;
    m_colorModel = colorModel;
  }

  @Override
  public void paint( final Graphics g, final GeoTransform projection, final IProgressMonitor monitor )
  {
    try
    {
      m_colorModel.setProjection( projection );

      /* screen rect */
      final double destWidth = projection.getDestWidth();
      final double destHeight = projection.getDestHeight();
      final Envelope screenRect = new Envelope( 0, destWidth, 0, destHeight );

      /* tin rect */
      final GM_Envelope tinBBox = m_surface.getBoundingBox();
      final Envelope tinEnvelope = JTSAdapter.export( tinBBox );
      final double tinScreenMinX = projection.getDestX( tinEnvelope.getMinX() );
      final double tinScreenMinY = projection.getDestY( tinEnvelope.getMinY() );
      final double tinScreenMaxX = projection.getDestX( tinEnvelope.getMaxX() );
      final double tinScreenMaxY = projection.getDestY( tinEnvelope.getMaxY() );
      final Envelope screenTinRect = new Envelope( tinScreenMinX, tinScreenMaxX, tinScreenMinY, tinScreenMaxY );

      final Envelope intersection = screenRect.intersection( screenTinRect );

      /* walk over pixels of intersection */
      final int fromX = (int) Math.ceil( intersection.getMinX() );
      final int toX = (int) Math.floor( intersection.getMaxX() );
      final int fromY = (int) Math.ceil( intersection.getMinY() );
      final int toY = (int) Math.floor( intersection.getMaxY() );

      final String srs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

      final Graphics2D g2 = (Graphics2D) g;

      final int tileSize = 100;

      final BufferedImage buffer = new BufferedImage( tileSize, tileSize, BufferedImage.TYPE_INT_ARGB );

      for( int x = fromX; x < toX; x += tileSize )
      {
        for( int y = fromY; y < toY; y += tileSize )
          drawTile( g2, buffer, projection, srs, x, y, toX, toY, tileSize );
      }
    }
    catch( final ElevationException e )
    {
      e.printStackTrace();
    }
  }

  private void drawTile( final Graphics2D g2, final BufferedImage buffer, final GeoTransform projection, final String srs, final int fromX, final int fromY, final int maxX, final int maxY, final int tileSize ) throws ElevationException
  {
    // REMARK: size of pixel; if > 1; a block of pxp pixels is rendered in the same color (big speedup!)
    final int p = 2;

    final int width = Math.min( tileSize, maxX - fromX + 1 );
    final int height = Math.min( tileSize, maxY - fromY + 1 );

    for( int x = fromX; x < fromX + width; x += p )
    {
      final double sourceX = projection.getSourceX( x );

      for( int y = fromY; y < fromY + height; y += p )
      {
        final double sourceY = projection.getSourceY( y );

        final double value = m_surface.getElevation( GeometryFactory.createGM_Point( sourceX, sourceY, srs ) );
        final Color color = m_colorModel.getColor( value );
        final int rgb = color == null ? 0 : color.getRGB();

        /* Paint pixel in given color */
        for( int i = 0; i < p; i++ )
        {
          for( int j = 0; j < p; j++ )
          {
            final int xP = x - fromX + i;
            final int yP = y - fromY + j;

            if( xP < width && yP < height )
            {
              buffer.setRGB( xP, yP, rgb );
            }
          }
        }
      }
    }

    g2.drawImage( buffer, fromX, fromY, fromX + width, fromY + height, 0, 0, width, height, null );
    // g2.drawImage( buffer, fromX, fromY, null );
  }
}