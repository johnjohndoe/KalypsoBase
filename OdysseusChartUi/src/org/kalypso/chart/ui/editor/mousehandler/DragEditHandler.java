package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.EditableChartLayerVisitor;
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
    final ILayerManager layerManager = getChart().getChartModel().getLayerManager();

    final EditableChartLayerVisitor visitor = new EditableChartLayerVisitor();
    layerManager.accept( visitor );

    final IEditableChartLayer[] layers = visitor.getLayers();

    for( final IEditableChartLayer layer : layers )
    {
      if( layer.getHover( point ) != null )
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
    if( canSnap( getChart().screen2plotPoint( new Point( e.x, e.y ) ) ) || m_editInfo != null )
      return e.display.getSystemCursor( SWT.CURSOR_HAND );

    return super.getCursor( e );
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
      if( m_editInfo != null && m_editInfo.getLayer() != null )
        ((IEditableChartLayer) m_editInfo.getLayer()).commitDrag( start, editInfo );
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
    if( m_editInfo.getLayer() != null )
    {
      if( ((IEditableChartLayer) editInfo.getLayer()).isLocked() )
        m_editInfo = null;
      else
        getChart().setEditInfo( ((IEditableChartLayer) editInfo.getLayer()).drag( start, m_editInfo ) );
    }
  }
}
