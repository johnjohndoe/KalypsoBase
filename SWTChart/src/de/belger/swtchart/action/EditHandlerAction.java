package de.belger.swtchart.action;

import org.eclipse.jface.action.Action;

import de.belger.swtchart.mouse.EditHandler;

/** Radio-Action to enable/disable the edit handler on the chart. */
public class EditHandlerAction extends Action
{
  private final EditHandler m_editHandler;

  public EditHandlerAction( final EditHandler editHandler )
  {
    super( "Editieren", AS_RADIO_BUTTON );

    m_editHandler = editHandler;
  }

  @Override
  public void setChecked( final boolean checked )
  {
    super.setChecked( checked );

    m_editHandler.setEditingAllowed( checked );
  }
}
