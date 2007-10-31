package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.model.layer.EditInfo;
import org.kalypso.chart.framework.model.layer.IEditableChartLayer;
import org.kalypso.chart.framework.view.ChartComposite;

/**
 * handler to manage tooltips; registers on creation, deregisters on disposation
 * 
 * @author alibu
 */
public class TooltipHandler implements MouseListener, MouseMoveListener
{
  private final ChartComposite m_chart;

  private boolean m_editingAllowed = false;

  private final int m_treshold = 3;

  private Point m_clickPoint;

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
   * If true, editing is allowed, else no editing can happen. Hover info however is always displayed.
   */
  public void setEditingAllowed( boolean editingAllowed )
  {
    m_editingAllowed = editingAllowed;
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
    final EditInfo editInfo = m_chart.getPlot().getHoverInfo();
    if( editInfo == null )
      return;
    m_clickPoint = new Point( e.x, e.y );
    final boolean allowEdit = editInfo.layer != null ? editInfo.layer.alwaysAllowsEditing() : false;
    if( (m_editingAllowed || allowEdit) && e.button == 1 )
    {
      m_chart.getPlot().setHoverInfo( null );
      final Point pos = new Point( e.x, e.y );
      editInfo.pos = pos;
      m_chart.getPlot().setEditInfo( editInfo );
      // m_chart.setCapture( true );
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( final MouseEvent e )
  {
    if( e.button == 1 )
    {
      final EditInfo editInfo = m_chart.getPlot().getEditInfo();
      final EditInfo hoverInfo = m_chart.getPlot().getHoverInfo();
      if( editInfo != null )
      {
        try
        {
          if( (Math.abs( e.x - m_clickPoint.x ) > m_treshold) || (Math.abs( e.y - m_clickPoint.y ) > m_treshold) )
          {
            editInfo.layer.edit( new Point( e.x, e.y ), editInfo.data );
          }
          else
          {
            editInfo.layer.setActivePoint( editInfo.data );
          }
        }
        finally
        {
          m_clickPoint = null;
          m_chart.getPlot().setEditInfo( null );
        }
      }
      else if( hoverInfo != null )
      {
        try
        {
          if( m_clickPoint != null && ((Math.abs( e.x - m_clickPoint.x ) < m_treshold) || (Math.abs( e.y - m_clickPoint.y ) < m_treshold)) )
          {
            hoverInfo.layer.setActivePoint( hoverInfo.data );
          }
        }
        finally
        {
          m_clickPoint = null;
        }
      }

    }
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( final MouseEvent e )
  {

    if( m_chart.getPlot().isEditing() )
    {
      m_chart.getPlot().getEditInfo().pos = new Point( e.x, e.y );
      m_chart.redraw();
    }
    else if( !m_chart.getPlot().isDragging() )
    {
      // to prevent holding the capture
      m_chart.setCapture( false );

      final Point point = new Point( e.x, e.y );

      for( final IEditableChartLayer layer : m_chart.getModel().getLayerManager().getEditableLayers() )
      {
        if( layer.isVisible() )
        {
          final EditInfo info = layer.getEditInfo( point );
          if( info != null )
          {
            info.pos = point;
            m_chart.getPlot().setHoverInfo( info );
            m_chart.getPlot().redraw();
            return;
          }
        }
      }

      m_chart.getPlot().setHoverInfo( null );
      m_chart.getPlot().redraw();
    }
  }
}
