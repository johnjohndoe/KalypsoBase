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
package de.openali.odysseus.chart.ext.base.layer;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * @author kimwerner
 */
public class DefaultTickRasterLayer extends AbstractLineLayer
{
  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#getId()
   */
  private static final String ID = "de.openali.odysseus.chart.ext.base.layer.DefaultTickRasterLayer";

  public DefaultTickRasterLayer( final ICoordinateMapper coordinateMapper, final ILineStyle lineStyle )
  {
    super( lineStyle, null );

    setCoordinateMapper( coordinateMapper );
    setId( ID );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    // don't calculate
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( )
  {
    // don't calculate
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    final IAxis domAxis = getDomainAxis();
    final IAxis tarAxis = getTargetAxis();
    if( !domAxis.isVisible() || !tarAxis.isVisible() )
      return;

    final Number[] domTicks = domAxis.getRenderer().getTicks( getDomainAxis(), gc );
    final Number[] valTicks = tarAxis.getRenderer().getTicks( getTargetAxis(), gc );

    final int width = gc.getClipping().width;
    final int heigth = gc.getClipping().height;

    for( int i = 0; i < domTicks.length; i++ )
    {
      final Point p1 = new Point( getDomainAxis().numericToScreen( domTicks[i] ), 0 );
      final Point p2 = new Point( getDomainAxis().numericToScreen( domTicks[i] ), heigth );

      drawLine( gc, p1, p2 );
    }

    for( int i = 0; i < valTicks.length; i++ )
    {
      final Point p1 = new Point( 0, getTargetAxis().numericToScreen( valTicks[i] ) );
      final Point p2 = new Point( width, getTargetAxis().numericToScreen( valTicks[i] ) );

      drawLine( gc, p1, p2 );
    }

  }

}
