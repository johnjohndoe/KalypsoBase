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
package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.view.impl.AxisCanvas;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author burtscher1
 */
public class AxisDragPanHandler extends AbstractAxisDragHandler
{

  public AxisDragPanHandler( ChartComposite chartComposite )
  {
    super( chartComposite );
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.IAxisDragHandler#setUseCurrentAxis(boolean)
   */
  public void setUseCurrentAxis( @SuppressWarnings("unused") boolean useCurrentAxis )
  {

  }

  /**
   * @see org.kalypso.chart.framework.view.IChartDragHandler#getCursor()
   */
  @Override
  public Cursor getCursor( )
  {
    return Display.getDefault().getSystemCursor( SWT.CURSOR_SIZEALL );
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( MouseEvent e )
  {
    m_mouseDragEnd = getPos( e );
    double diff = m_mouseDragEnd - m_mouseDragStart;
    if( m_mouseDragStart != -1 && Math.abs( diff ) > 0 )
    {

      // // Zoom-Anzeige in AxisCanvas
      final AxisCanvas curAc = (AxisCanvas) e.getSource();
// m_axes.get( curAc );
      // final ORIENTATION ori = curAxis.getPosition().getOrientation();

      if( m_applyOnAllAxes )
      {
// for( Entry<AxisCanvas, IAxis> entry : m_axes.entrySet() )
// {
// IAxis axis = entry.getValue();
        for( final IAxis axis : getAxis( getOrientation( curAc ) ) )
        {
          // AchsenOffset zurücksetzen
          // entry.getKey().setPanOffsetInterval( new Point( 0, 0 ) );
          m_chartComposite.getAxisCanvas( axis ).setPanOffsetInterval( new Point( 0, 0 ) );
          panAxis( m_mouseDragStart, m_mouseDragEnd, axis );
        }
        // }
        // PlotOffset zurücksetzen
        m_chartComposite.getPlot().setPanOffset( null, new Point( 0, 0 ) );
      }
      else
      {
        // zugehörige Layer rausfinden
        final IAxis curAxis = curAc.getAxis();
        IChartLayer[] pannedLayers = m_chartComposite.getChartModel().getAxis2Layers().get( curAxis ).toArray( new IChartLayer[] {} );
        m_chartComposite.getPlot().setPanOffset( pannedLayers, new Point( 0, 0 ) );

        curAc.setPanOffsetInterval( new Point( 0, 0 ) );
        panAxis( m_mouseDragStart, m_mouseDragEnd, curAxis );

      }
    }

    m_mouseDragStart = -1;
    m_mouseDragEnd = -1;

  }

  private void panAxis( int startPos, int endPos, IAxis axis )
  {
    Number startNum = axis.screenToNumeric( startPos );
    Number endNum = axis.screenToNumeric( endPos );
    double diff = startNum.doubleValue() - endNum.doubleValue();
    IDataRange<Number> oldRange = axis.getNumericRange();
    Number newMin = oldRange.getMin().doubleValue() + diff;
    Number newMax = oldRange.getMax().doubleValue() + diff;
    axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { newMin, newMax } ) );
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( MouseEvent e )
  {
    if( m_mouseDragStart != -1 )
    {
      m_mouseDragEnd = getPos( e );
      int diff = m_mouseDragEnd - m_mouseDragStart;
      boolean hasStarted = true;
      // mindestens 5 Pixel Bewegung - sonst wird nichts ausgeführt; nach dem ersten Überschreiten der 5-Pixel-Grenze
      // dürfen auch kleinere Abstände gewählt werden
      if( Math.abs( diff ) > 5 || hasStarted )
      {
        hasStarted = true;
        // // Zoom-Anzeige in AxisCanvas
        final AxisCanvas curAc = (AxisCanvas) e.getSource();
// m_axes.get( curAc );
        final ORIENTATION ori = getOrientation( curAc );

        if( m_applyOnAllAxes )
        {
// for( AxisCanvas ac : m_axes.keySet() )
// {
// if( m_axes.get( ac ).getPosition().getOrientation().equals( ori ) )
// {
          for( final IAxis axis : getAxis( ori ) )
          {
            if( ori.equals( ORIENTATION.HORIZONTAL ) )
              m_chartComposite.getAxisCanvas( axis ).setPanOffsetInterval( new Point( diff, 0 ) );
            else
              m_chartComposite.getAxisCanvas( axis ).setPanOffsetInterval( new Point( 0, diff ) );
          }

          if( ori.equals( ORIENTATION.HORIZONTAL ) )
          {
            m_chartComposite.getPlot().setPanOffset( null, new Point( m_mouseDragStart - e.x, 0 ) );
          }
          else
          {
            m_chartComposite.getPlot().setPanOffset( null, new Point( 0, m_mouseDragStart - e.y ) );
          }

        }
        else
        {
          final IAxis curAxis = curAc.getAxis();
          // AxisCanvas verschieben
          if( ori.equals( ORIENTATION.HORIZONTAL ) )
            curAc.setPanOffsetInterval( new Point( diff, 0 ) );
          else
            curAc.setPanOffsetInterval( new Point( 0, diff ) );

          // zugehörige Layer rausfinden
          IChartLayer[] pannedLayers = m_chartComposite.getChartModel().getAxis2Layers().get( curAxis ).toArray( new IChartLayer[] {} );

          if( ori.equals( ORIENTATION.HORIZONTAL ) )
          {
            m_chartComposite.getPlot().setPanOffset( pannedLayers, new Point( m_mouseDragStart - e.x, 0 ) );
          }
          else
          {
            m_chartComposite.getPlot().setPanOffset( pannedLayers, new Point( 0, m_mouseDragStart - e.y ) );
          }

        }

      }
    }

  }
}
