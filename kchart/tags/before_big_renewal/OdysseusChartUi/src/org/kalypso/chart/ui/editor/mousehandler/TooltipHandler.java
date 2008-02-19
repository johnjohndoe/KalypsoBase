package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.impl.view.ChartComposite;
import org.kalypso.chart.framework.model.layer.EditInfo;
import org.kalypso.chart.framework.model.layer.IEditableChartLayer;

/**
 * handler to manage tooltips; registers on creation, deregisters on disposation
 * 
 * @author alibu
 */
public class TooltipHandler implements MouseListener, MouseMoveListener
{
  private final ChartComposite m_chart;

  public TooltipHandler( final ChartComposite chart )
  {
    m_chart = chart;

    chart.getPlot().addMouseListener( this );
    chart.getPlot().addMouseMoveListener( this );
  }

  public void dispose( )
  {
    if( !m_chart.isDisposed() )
    {
      m_chart.removeMouseListener( this );
      m_chart.removeMouseMoveListener( this );
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDoubleClick( final MouseEvent e )
  {
    // ignore
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDown( final MouseEvent e )
  {
    // nothing to do
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( final MouseEvent e )
  {
    // nothing to do
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( final MouseEvent e )
  {
    if( m_chart.getPlot().isEditing() )
    {
      m_chart.getPlot().getTooltipInfo().pos = new Point( e.x, e.y );
      m_chart.getPlot().redraw();
    }
    else if( !m_chart.getPlot().isDragging() )
    {
      final Point point = new Point( e.x, e.y );

      for( final IEditableChartLayer layer : m_chart.getModel().getLayerManager().getEditableLayers() )
      {
        if( layer.isVisible() )
        {
          final EditInfo info = layer.getEditInfo( point );
          if( info != null )
          {
            info.pos = point;
            m_chart.getPlot().setTooltipInfo( info );
            m_chart.getPlot().redraw();
            return;
          }
        }
      }

      m_chart.getPlot().setTooltipInfo( null );
      m_chart.getPlot().redraw();
    }
  }
}
