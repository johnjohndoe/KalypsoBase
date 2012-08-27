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
      final int fromX = (int) Math.floor( intersection.getMinX() );
      final int toX = (int) Math.ceil( intersection.getMaxX() );
      final int fromY = (int) Math.floor( intersection.getMinY() );
      final int toY = (int) Math.ceil( intersection.getMaxY() );

      final String srs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

      final Graphics2D g2 = (Graphics2D) g;

      final int tileSize = 100;

      for( int x = fromX; x < toX; x += tileSize )
      {
        final double sourceX = projection.getSourceX( x );

        for( int y = fromY; y < toY; y += tileSize )
        {
          drawTile( g2, projection, srs, x, y, toX, toY, tileSize );

          final double sourceY = projection.getSourceY( y );

          final double value = m_surface.getElevation( GeometryFactory.createGM_Point( sourceX, sourceY, srs ) );
          if( !Double.isNaN( value ) )
          {
            final Color color = m_colorModel.getColor( value );
            if( color != null )
            {
              g.setColor( color );
              // g2.drawRect( x, y, 1, 1 );
              // buffer.setRGB( x - fromX, y - fromY, color.getRGB() );
            }
          }
        }
      }
    }
    catch( final ElevationException e )
    {
      e.printStackTrace();
    }
  }

  private void drawTile( final Graphics2D g2, final GeoTransform projection, final String srs, final int fromX, final int fromY, final int maxX, final int maxY, final int tileSize ) throws ElevationException
  {
    final int width = Math.min( tileSize, maxX - fromX + 1 );
    final int heigth = Math.min( tileSize, maxY - fromY + 1 );

    final BufferedImage buffer = new BufferedImage( width, heigth, BufferedImage.TYPE_INT_ARGB );

    for( int x = fromX; x < fromX + width; x++ )
    {
      final double sourceX = projection.getSourceX( x );

      for( int y = fromY; y < fromY + heigth; y++ )
      {
        final double sourceY = projection.getSourceY( y );

        final double value = m_surface.getElevation( GeometryFactory.createGM_Point( sourceX, sourceY, srs ) );
        if( !Double.isNaN( value ) )
        {
          final Color color = m_colorModel.getColor( value );
          if( color != null )
          {
            // g.setColor( color );
            // g2.drawRect( x, y, 1, 1 );
            buffer.setRGB( x - fromX, y - fromY, color.getRGB() );
          }
        }
      }
    }

    g2.drawImage( buffer, fromX, fromY, null );
  }
}