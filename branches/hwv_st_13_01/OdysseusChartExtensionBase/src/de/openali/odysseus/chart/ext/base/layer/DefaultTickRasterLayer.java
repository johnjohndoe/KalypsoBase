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
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author kimwerner
 */
public class DefaultTickRasterLayer extends AbstractLineLayer
{
  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#getId()
   */
  private static final String ID = "de.openali.odysseus.chart.ext.base.layer.DefaultTickRasterLayer"; //$NON-NLS-1$

  public DefaultTickRasterLayer( final ILayerProvider provider, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( provider, lineStyle, pointStyle );
    setIdentifier( ID );
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
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
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
    final IAxis targetAxis = getTargetAxis();
    final IAxis domainAxis = getDomainAxis();
    if( !domainAxis.isVisible() || !targetAxis.isVisible() )
      return;

    final IAxisRenderer domainRenderer = domainAxis.getRenderer();
    final IAxisRenderer targetRenderer = targetAxis.getRenderer();

    final Number[] domTicks = domainRenderer.getTicks( domainAxis, gc );
    final Number[] valTicks = targetRenderer.getTicks( targetAxis, gc );

    final int width = gc.getClipping().width;
    final int heigth = gc.getClipping().height;
// final int width = domainAxis.getScreenHeight();
// final int heigth = targetAxis.getScreenHeight();
    for( final Number domTick : domTicks )
    {
      final Point p1 = new Point( domainAxis.numericToScreen( domTick ), 0 );
      final Point p2 = new Point( domainAxis.numericToScreen( domTick ), heigth );

      drawLine( gc, p1, p2 );
    }

    for( final Number valTick : valTicks )
    {
      final Point p1 = new Point( 0, targetAxis.numericToScreen( valTick ) );
      final Point p2 = new Point( width, targetAxis.numericToScreen( valTick ) );

      drawLine( gc, p1, p2 );
    }

    // TODO Rahmenelement zeichnet ins chart
// final FullRectangleFigure figureRect = new FullRectangleFigure();
// final IPointStyle pointStyle = getPointFigure().getStyle();
// if( pointStyle.isVisible() )
// {
// figureRect.setStyle( new AreaStyle( new ColorFill( pointStyle.getInlineColor() ), pointStyle.getAlpha(),
// pointStyle.getStroke(), pointStyle.isFillVisible() ) );
// figureRect.setRectangle( new Rectangle( -1, -1, width + 5, heigth + 5 ) );
// figureRect.paint( gc );
// }
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#isLegend()
   */
  @Override
  public boolean isLegend( )
  {
    return false;
  }
}
