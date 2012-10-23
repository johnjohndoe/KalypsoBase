package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.EditableChartLayerVisitor;
import de.openali.odysseus.chart.framework.view.IChartComposite;

// FIXME: separate drag and hover
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
    final IChartModel chartModel = getChart().getChartModel();

    final EditableChartLayerVisitor visitor = new EditableChartLayerVisitor();
    chartModel.accept( visitor );

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

  @Override
  public void doMouseMoveAction( final Point start, final EditInfo editInfo )
  {
    if( m_editInfo == null )
      m_editInfo = editInfo;

    final ITooltipChartLayer layer = m_editInfo.getLayer();
    if( layer instanceof IEditableChartLayer )
      getChart().setEditInfo( ((IEditableChartLayer)layer).drag( start, m_editInfo ) );
  }

  @Override
  public void doMouseUpAction( final Point start, final EditInfo editInfo )
  {
    try
    {
      if( editInfo != null && editInfo.getLayer() != null )
        ((IEditableChartLayer)editInfo.getLayer()).commitDrag( start, editInfo );
    }
    finally
    {
      m_editInfo = null;
    }
  }

  @Override
  public void mouseMove( final MouseEvent e )
  {
    if( canSnap(  new Point( e.x, e.y )) )
      setCursor( SWT.CURSOR_HAND );
    else
      setCursor( SWT.CURSOR_ARROW );

    super.mouseMove( e );
  }
}
