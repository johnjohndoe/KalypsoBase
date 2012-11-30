package org.kalypso.ui.editor.styleeditor.style;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.kalypso.commons.eclipse.jface.viewers.ITabItem;
import org.kalypso.commons.eclipse.jface.viewers.TabViewer;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public final class AddRuleItemAction extends Action
{
  private final TabViewer m_tabViewer;

  public AddRuleItemAction( final TabViewer tabViewer )
  {
    m_tabViewer = tabViewer;

    setText( Messages.getString( "org.kalypso.ui.editor.styleeditor.RuleTabItemBuilder.0" ) ); //$NON-NLS-1$
    setImageDescriptor( ImageProvider.IMAGE_STYLEEDITOR_ADD_RULE );
    setToolTipText( MessageBundle.STYLE_EDITOR_ADD_RULE );
  }

  /**
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run( )
  {
    final RuleOrPatternCollection list = (RuleOrPatternCollection)m_tabViewer.getInput();

    final ITabItem item = list.addNewItem();
    m_tabViewer.setSelection( new StructuredSelection( item ) );
  }
}