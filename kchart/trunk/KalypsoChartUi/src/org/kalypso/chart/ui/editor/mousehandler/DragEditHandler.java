package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.model.layer.EditInfo;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.IEditableChartLayer;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.chart.framework.view.IChartDragHandler;
import org.kalypso.chart.framework.view.PlotCanvas;

/**
 * Registers itself upon creation and deregisters at disposal.
 * 
 * @author alibu
 */
// TODO: introduce an abstract clas, the extends from KeyAdapter, MouseAdapter and so on, so you only need to implement
// what you really whant to.
public class DragEditHandler implements IChartDragHandler
{
  private final ChartComposite m_chart;

  private EditInfo m_editInfo = null;

  private boolean m_isEditing = false;

  public DragEditHandler( final ChartComposite chart )
  {
    m_chart = chart;
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
    final PlotCanvas plot = m_chart.getPlot();
    m_editInfo = m_chart.getPlot().getTooltipInfo();
    if( m_editInfo == null )
      return;
    if( m_editInfo != null )
    {
      m_isEditing = true;
      plot.setIsEditing( true );
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( final MouseEvent e )
  {
    if( m_isEditing )
    {
      System.out.println();
      m_editInfo = null;
      m_isEditing = false;
      final PlotCanvas plot = m_chart.getPlot();
      plot.setIsEditing( false );
      plot.setTooltipInfo( null );
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( final MouseEvent e )
  {
    if( m_isEditing )
    {
      final IEditableChartLayer layer = m_editInfo.layer;
      final Point editPoint = new Point( e.x, e.y );
      final EditInfo editMoveInfo = layer.edit( editPoint, m_editInfo );
      m_chart.getPlot().setTooltipInfo( editMoveInfo );
      m_chart.getPlot().invalidate( new IChartLayer[] { layer } );
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
