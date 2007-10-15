package org.kalypso.chart.ui.editor.actions;

import org.eclipse.jface.action.Action;
import org.kalypso.chart.ui.editor.mouse.DragZoomHandler;

/**
 * @author belger
 */
public class SetDragAction extends Action
{
  private final DragZoomHandler m_dragHandler;

  private final IChartDragAction m_action;

  public SetDragAction( final DragZoomHandler dragHandler, final IChartDragAction action, final String name )
  {
    super( name, AS_RADIO_BUTTON );

    m_dragHandler = dragHandler;
    m_action = action;
  }

  @Override
  public void setChecked( final boolean checked )
  {
    super.setChecked( checked );

    m_dragHandler.setDragAction( checked ? m_action : null );
  }
}
