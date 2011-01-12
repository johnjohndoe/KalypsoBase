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
package org.kalypso.chart.ext.test.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.factory.provider.ILayerProvider;
import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;

/**
 * @author alibu
 */
public class TortenLayer extends AbstractChartLayer
{

  private final int m_pieces;

  public TortenLayer( final ILayerProvider provider, final int pieces )
  {
    super( provider );

    m_pieces = pieces;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image)
   */
  public void drawIcon( final Image img )
  {
    // TODO Auto-generated method stub
    final Rectangle size = img.getBounds();
    final int width = size.x;
    final int height = size.y;

    final GC gc = new GC( img );

    gc.setAntialias( SWT.ON );

    final int centerX = 0;
    final int centerY = 0;

    for( int i = 0; i < m_pieces; i++ )
    {
      final int angleStart = i * (int) (360.0f / m_pieces);

      final int angle = (int) ((360.0f / m_pieces));

      final java.awt.Color color = new java.awt.Color( java.awt.Color.HSBtoRGB( ((1.0f / m_pieces) * (i)), 1.0f, 1.0f ) );

      final RGB fillRGB = new RGB( color.getRed(), color.getGreen(), color.getBlue() );

      final Color fillColor = OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gc.getDevice(), fillRGB );
      gc.setBackground( fillColor );
      gc.fillArc( centerX, centerY, width - 1, height - 1, angleStart, angle );
      fillColor.dispose();

    }
    gc.setForeground( gc.getDevice().getSystemColor( SWT.COLOR_BLACK ) );
    gc.drawOval( 0, 0, width - 1, height - 1 );

    gc.dispose();

  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    final IAxis da = getDomainAxis();
    final IAxis ta = getTargetAxis();

    final int startX = da.numericToScreen( -100 );
    final int startY = ta.numericToScreen( -100 );
    final int endX = da.numericToScreen( 100 );
    final int endY = ta.numericToScreen( 100 );
    final int centerX = da.numericToScreen( 0 );
    final int centerY = ta.numericToScreen( 0 );

    final int width = Math.abs( endX - startX );
    final int height = Math.abs( endY - startY );

    final Device dev = gc.getDevice();
    gc.setForeground( dev.getSystemColor( SWT.COLOR_BLACK ) );

    gc.setBackground( dev.getSystemColor( SWT.COLOR_BLACK ) );

    gc.fillOval( startX, startY, width, height );

    gc.drawLine( centerX, startY, centerX, endY );
    gc.drawLine( startX, centerY, endX, centerY );

    for( int i = 0; i < m_pieces; i++ )
    {
      final int angleStart = i * (int) (360.0f / m_pieces);

      final int angle = (int) ((360.0f / m_pieces));

      final java.awt.Color color = new java.awt.Color( java.awt.Color.HSBtoRGB( ((1.0f / m_pieces) * (i)), 1.0f, 1.0f ) );

      final RGB fillRGB = new RGB( color.getRed(), color.getGreen(), color.getBlue() );

      final Color fillColor = OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( dev, fillRGB );
      gc.setBackground( fillColor );
      gc.setForeground( dev.getSystemColor( SWT.COLOR_BLACK ) );
      gc.setLineWidth( 5 );

      gc.fillArc( startX, startY, width, height, angleStart, angle );
      gc.drawArc( startX, startY, width, height, angleStart, angle );

    }

    gc.setForeground( dev.getSystemColor( SWT.COLOR_BLACK ) );
    gc.setLineWidth( 5 );
    for( int i = 0; i < m_pieces; i++ )
    {
      final int angleStart = (int) (i * (360.0f / m_pieces));

      final double angleRad = Math.toRadians( angleStart );

      final double h = 100.0f;
      final double g = (Math.sin( angleRad ) * h);
      final double a = (Math.cos( angleRad ) * h);
      final double gStrich = ta.numericToScreen( g ) - centerY;
      final double aStrich = da.numericToScreen( a ) - centerX;

      gc.drawLine( centerX, centerY, (int) (centerX + aStrich), (int) (centerY + gStrich) );

    }

    gc.setLineWidth( 10 );
    gc.setLineDash( new int[] { 10, 10 } );
    gc.setForeground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    for( int i = 0; i < m_pieces; i++ )
    {
      final int angleStart = (int) (i * (360.0f / m_pieces));

      final double angleRad = Math.toRadians( angleStart );

      final double h = 100.0f;
      final double g = (int) (Math.sin( angleRad ) * h);
      final double a = (int) (Math.cos( angleRad ) * h);
      final double gStrich = ta.numericToScreen( g ) - centerY;
      final double aStrich = da.numericToScreen( a ) - centerX;

      final double hStrich = (Math.sqrt( Math.pow( aStrich, 2 ) + Math.pow( gStrich, 2 ) ));

      final Transform t = new Transform( dev );
      t.translate( centerX, centerY );
      t.rotate( angleStart );

      gc.setTransform( t );
      gc.drawLine( 20, 0, (int) hStrich, 0 );

      t.dispose();
    }
    gc.setTransform( null );

  }

  @Override
  public IDataRange<Number> getDomainRange( )
  {
    return new ComparableDataRange<Number>( new Number[] { -100, 100 } );
  }

  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    return new ComparableDataRange<Number>( new Number[] { -100, 100 } );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getLegendEntries(org.eclipse.swt.graphics.Point)
   */
  @Override
  public ILegendEntry[] createLegendEntries( )
  {

    final List<ILegendEntry> entries = new ArrayList<ILegendEntry>();
    for( int i = 0; i < m_pieces; i++ )
    {

      final int count = i;
      final ILegendEntry entry = new LegendEntry( this, "Tortenst¸ck " + i )
      {
        @Override
        public void paintSymbol( final GC gc, final Point size )
        {
          // TortenLayer.this.createLegendEntries();
          final int width = size.x;
          final int height = size.y;
          final java.awt.Color color = new java.awt.Color( java.awt.Color.HSBtoRGB( ((1.0f / m_pieces) * (count)), 1.0f, 1.0f ) );
          final RGB fillRGB = new RGB( color.getRed(), color.getGreen(), color.getBlue() );
          final Color fillColor = OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gc.getDevice(), fillRGB );
          gc.setBackground( fillColor );
          gc.fillRectangle( 0, 0, width, height );
        }

        @Override
        public Point getMinimumSize( )
        {
          return new Point( 2, 2 );
        }

      };
      entries.add( entry );
    }

    return entries.toArray( new ILegendEntry[] {} );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    // nothing to do
  }
}
