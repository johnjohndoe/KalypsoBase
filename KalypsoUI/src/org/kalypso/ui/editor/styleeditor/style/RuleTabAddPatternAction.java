package org.kalypso.ui.editor.styleeditor.style;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.CTabFolder;
import org.kalypso.commons.eclipse.jface.viewers.ITabAction;
import org.kalypso.commons.eclipse.jface.viewers.TabViewer;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public final class RuleTabAddPatternAction extends Action implements ITabAction
{
  private final TabViewer m_viewer;

  public RuleTabAddPatternAction( final TabViewer viewer )
  {
    super( Messages.getString( "org.kalypso.ui.editor.styleeditor.RuleTabItemBuilder.2" ), ImageProvider.IMAGE_STYLEEDITOR_ADD_RULE_PATTERN ); //$NON-NLS-1$

    m_viewer = viewer;

    setToolTipText( MessageBundle.STYLE_EDITOR_ADD_RULE_PATTERN );
  }

  /**
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run( )
  {
    final RuleOrPatternCollection input = (RuleOrPatternCollection)m_viewer.getInput();
    input.addNewPatternCollection();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.ITabAction#update(org.eclipse.swt.custom.CTabFolder)
   */
  @Override
  public void update( final CTabFolder folder )
  {
    final RuleOrPatternCollection input = (RuleOrPatternCollection)m_viewer.getInput();
    setEnabled( input != null && input.canAddPatternRule() );
  }
}