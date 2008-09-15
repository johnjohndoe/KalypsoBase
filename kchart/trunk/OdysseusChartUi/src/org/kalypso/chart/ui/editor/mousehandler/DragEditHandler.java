package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.view.IChartDragHandler;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;
import de.openali.odysseus.chart.framework.view.impl.PlotCanvas;

/**
 * Registers itself upon creation and deregisters at disposal.
 * 
 * @author alibu
 * @author kimwerner
 */
public class DragEditHandler implements IChartDragHandler
{
  private final ChartComposite m_chart;

  private EditInfo m_editInfo = null;

  private final int m_trashHold;

  private EditInfo m_clickInfo = null;

  public DragEditHandler( final ChartComposite chart )
  {
    this( chart, 0 );
  }

  public DragEditHandler( final ChartComposite chart, final int trashHold )
  {
    m_chart = chart;
    m_trashHold = trashHold;
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDoubleClick( final MouseEvent e )
  {
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDown( final MouseEvent e )
  {
    m_clickInfo = m_chart.getPlot().getTooltipInfo();
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( final MouseEvent e )
  {
    if( (m_editInfo != null) && (m_editInfo.m_layer instanceof IEditableChartLayer) )
    {
      IEditableChartLayer layer = (IEditableChartLayer) m_editInfo.m_layer;
      final Point editPoint = new Point( e.x, e.y );
      layer.commitDrag( editPoint, m_editInfo );
      m_clickInfo = null;
      m_editInfo = null;
      final PlotCanvas plot = m_chart.getPlot();
      plot.setIsEditing( false );
      plot.setTooltipInfo( null );
    }
    else if( (m_clickInfo != null) && (m_clickInfo.m_layer instanceof IEditableChartLayer) )
    {
      ((IEditableChartLayer) (m_clickInfo.m_layer)).commitDrag( m_clickInfo.m_pos, m_clickInfo );
      m_clickInfo = null;
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( final MouseEvent e )
  {
    if( m_clickInfo == null || m_clickInfo.m_layer == null )
      return;
    final boolean isLocked = m_clickInfo.m_layer instanceof IEditableChartLayer ? ((IEditableChartLayer) m_clickInfo.m_layer).isLocked() : true;
    final boolean allowEditing = !isLocked && ((Math.abs( e.x - m_clickInfo.m_pos.x ) > m_trashHold) || (Math.abs( e.y - m_clickInfo.m_pos.y ) > m_trashHold));
    if( (m_editInfo == null) && allowEditing )
      m_editInfo = new EditInfo( m_clickInfo );

    if( m_editInfo != null )
    {
      final PlotCanvas plot = m_chart.getPlot();
      final ITooltipChartLayer layer = m_editInfo.m_layer;
      if( layer instanceof IEditableChartLayer )
      {
        IEditableChartLayer eLayer = (IEditableChartLayer) layer;
        final Point editPoint = new Point( e.x, e.y );
        final EditInfo editMoveInfo = eLayer.drag( editPoint, m_editInfo );
        plot.setIsEditing( true );
        plot.setTooltipInfo( editMoveInfo );
      }
    }
  }

  /**
   * @see org.kalypso.chart.framework.view.IChartDragHandler#getCursor()
   */
  public Cursor getCursor( )
  {
    return null;
  }

}
