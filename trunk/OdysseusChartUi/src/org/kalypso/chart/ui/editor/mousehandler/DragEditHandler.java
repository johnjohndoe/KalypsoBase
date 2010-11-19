package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author kimwerner
 */
public class DragEditHandler extends AbstractChartDragHandler
{

  EditInfo m_editInfo = null;

  public DragEditHandler( final IChartComposite chart )
  {
    this( chart, 5 );
  }

  public DragEditHandler( final IChartComposite chart, final int trashold )
  {
    super( chart, trashold );
  }

  private final boolean canSnap( final Point point )
  {
    final IEditableChartLayer[] eLayers = getChart().getChartModel().getLayerManager().getEditableLayers();
    for( final IEditableChartLayer layer : eLayers )
    {
      if( !layer.isLocked() && layer.isVisible() && layer.getHover( getChart().screen2plotPoint( point ) ) != null )
      {
        return true;
      }
    }
    return false;
  }

  /**
   * @see org.kalypso.chart.framework.view.IChartDragHandler#getCursor()
   */
  @Override
  public Cursor getCursor( final MouseEvent e )
  {
    if( canSnap( new Point( e.x, e.y ) ) || m_editInfo != null )
      return e.display.getSystemCursor( SWT.CURSOR_HAND );

    return e.display.getSystemCursor( SWT.CURSOR_ARROW );
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler#doMouseUpAction(org.eclipse.swt.graphics.Point,
   *      org.eclipse.swt.graphics.Point, de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseUpAction( final Point start, final EditInfo editInfo )
  {
    try
    {
      if( m_editInfo != null && m_editInfo.m_layer != null )
        ((IEditableChartLayer) m_editInfo.m_layer).commitDrag( start, editInfo );
    }
    finally
    {
      m_editInfo = null;
    }
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler#doMouseMoveAction(org.eclipse.swt.graphics.Point,
   *      org.eclipse.swt.graphics.Point, de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseMoveAction( final Point start, final EditInfo editInfo )
  {
    if( m_editInfo == null )
      m_editInfo = editInfo;
    getChart().setEditInfo( ((IEditableChartLayer) m_editInfo.m_layer).drag( start, m_editInfo ) );
  }

  // m_chart.setTooltipInfo( null );
// }
}
