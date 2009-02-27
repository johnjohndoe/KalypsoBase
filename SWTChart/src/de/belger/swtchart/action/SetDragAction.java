package de.belger.swtchart.action;

import org.eclipse.jface.action.Action;

import de.belger.swtchart.mouse.DragHandler;

/**
 * @author belger
 */
public class SetDragAction extends Action
{
  private final DragHandler m_dragHandler;
  private final IChartDragAction m_action;

  public SetDragAction( final DragHandler dragHandler, final IChartDragAction action, final String name )
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
