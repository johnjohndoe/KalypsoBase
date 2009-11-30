package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.view.IChartDragHandler;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;
import de.openali.odysseus.chart.framework.view.impl.PlotCanvas;

/**
 * Registers itself upon creation and deregisters at disposal.
 * 
 * @author kimwerner
 */
public class DragEditHandler implements IChartDragHandler
{
  private final ChartComposite m_chart;

  private EditInfo m_editInfo = null;

  private final int m_trashHold;

  private EditInfo m_clickInfo = null;

  private int m_deltaSnapX = 0;

  private int m_deltaSnapY = 0;

  private int m_startX = 0;

  private int m_startY = 0;

  IEditableChartLayer m_layer = null;

  public DragEditHandler( final ChartComposite chart )
  {
    this( chart, 5 );
  }

  public DragEditHandler( final ChartComposite chart, final int trashhold )
  {
    m_chart = chart;
    m_trashHold = trashhold;
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
    if( m_clickInfo != null )
    {
      m_deltaSnapX = e.x - m_clickInfo.m_pos.x;
      m_deltaSnapY = e.y - m_clickInfo.m_pos.y;
      m_startX = e.x;
      m_startY = e.y;
      if( m_clickInfo.m_layer instanceof IEditableChartLayer )
      {
        m_layer = (IEditableChartLayer) m_clickInfo.m_layer;
        if( !m_layer.isLocked() && m_layer.isVisible() )
          return;
      }
      /**
       * only use editable,visible,unlocked chartlayer
       */
      m_clickInfo = null;
      m_layer = null;
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( final MouseEvent e )
  {
    if( m_editInfo != null )
      m_layer.commitDrag( new Point( e.x - m_deltaSnapX, e.y - m_deltaSnapY ), m_editInfo );
    else if( m_clickInfo != null )
      m_layer.commitDrag( m_clickInfo.m_pos, m_clickInfo );

    m_clickInfo = null;
    m_layer = null;
    m_editInfo = null;
    m_chart.getPlot().setIsEditing( false );
    m_chart.getPlot().setTooltipInfo( null );
  }

  private final boolean canSnap( final Point point )
  {
    IEditableChartLayer[] eLayers = m_chart.getChartModel().getLayerManager().getEditableLayers();
    for( final IEditableChartLayer layer : eLayers )
    {
      if( !layer.isLocked() && layer.isVisible() && layer.getHover( point ) != null )
      {
        return true;
      }
    }
    return false;
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( final MouseEvent e )
  {
    final PlotCanvas plot = m_chart.getPlot();
    if( plot.isEditing() || canSnap( new Point( e.x, e.y ) ) )
    {
      plot.setCursor( Display.getDefault().getSystemCursor( SWT.CURSOR_HAND ) );
    }
    else
    {
      if( plot.getCursor() != null )
        plot.setCursor( Display.getDefault().getSystemCursor( SWT.CURSOR_ARROW ) );
    }

    if( m_layer == null )
      return;
    if( (m_editInfo == null) && ((Math.abs( e.x - m_startX ) > m_trashHold) || (Math.abs( e.y - m_startY ) > m_trashHold)) )
      m_editInfo = new EditInfo( m_clickInfo );

    if( m_editInfo != null )
    {
      plot.setIsEditing( true );
      plot.setTooltipInfo( m_layer.drag( new Point( e.x - m_deltaSnapX, e.y - m_deltaSnapY ), m_editInfo ) );
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
