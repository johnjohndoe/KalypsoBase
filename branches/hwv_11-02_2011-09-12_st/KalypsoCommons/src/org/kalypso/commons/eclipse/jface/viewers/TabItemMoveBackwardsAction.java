package org.kalypso.commons.eclipse.jface.viewers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Event;

/**
 * @author Gernot Belger
 */
public final class TabItemMoveBackwardsAction extends Action implements ITabAction
{
  private final TabViewer m_tabViewer;

  public TabItemMoveBackwardsAction( final TabViewer tabViewer )
  {
    m_tabViewer = tabViewer;

  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final ITabList list = (ITabList) m_tabViewer.getInput();
    final int index = m_tabViewer.getTabFolder().getSelectionIndex();
    if( index > 0 )
    {
      final ISelection preserveSelection = m_tabViewer.getSelection();
      list.moveBackward( index );
      m_tabViewer.setSelection( preserveSelection );
    }
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.ITabAction#update(org.eclipse.swt.custom.CTabFolder)
   */
  @Override
  public void update( final CTabFolder folder )
  {
    final ITabList list = (ITabList) m_tabViewer.getInput();
    if( list == null )
    {
      setEnabled( false );
      return;
    }

    final int index = m_tabViewer.getTabFolder().getSelectionIndex();
    final ITabItem[] items = list.getItems();
    setEnabled( items.length > 1 && index > 0 );
  }
}