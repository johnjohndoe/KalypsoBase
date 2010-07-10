package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author kimwerner
 */
public class DragEditHandler extends AbstractChartDragHandler
{
  
  IEditableChartLayer m_layer = null;

  public DragEditHandler( final ChartComposite chart )
  {
    this( chart, 5 );
  }

  public DragEditHandler( final ChartComposite chart, final int trashhold )
  {
    super( chart, trashhold );
  }

  private final boolean canSnap( final Point point )
  {
    IEditableChartLayer[] eLayers = getChart().getChartModel().getLayerManager().getEditableLayers();
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
   * @see org.kalypso.chart.framework.view.IChartDragHandler#getCursor()
   */
  @Override
  public Cursor getCursor( final MouseEvent e )
  {
    if( canSnap( new Point( e.x, e.y ) )||m_layer!=null )
      return e.display.getSystemCursor( SWT.CURSOR_HAND ) ;

     return  e.display.getSystemCursor( SWT.CURSOR_ARROW );
  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler#doMouseUpAction(org.eclipse.swt.graphics.Point,
   *      org.eclipse.swt.graphics.Point, de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseUpAction( Point start, EditInfo editInfo )
  {

    if( m_layer == null )
      return;
    m_layer.commitDrag( start, editInfo );
    m_layer = null;

  }

  /**
   * @see org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler#doMouseMoveAction(org.eclipse.swt.graphics.Point,
   *      org.eclipse.swt.graphics.Point, de.openali.odysseus.chart.framework.model.layer.EditInfo)
   */
  @Override
  public void doMouseMoveAction( Point start, EditInfo editInfo )
  {
    if( editInfo.m_layer != null && editInfo.m_layer instanceof IEditableChartLayer )
    {
      final IEditableChartLayer layer = (IEditableChartLayer) editInfo.m_layer;
      if( !layer.isLocked() && layer.isVisible() )
        m_layer = layer;
      getChart().setChartInfo( m_layer.drag( start, editInfo ) );
    }
  }

}
