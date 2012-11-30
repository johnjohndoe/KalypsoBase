/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.commons.eclipse.jface.viewers;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.commons.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class TabListContentProvider implements IStructuredContentProvider
{
  private final ITabListListener m_tabListener = new ITabListListener()
  {
    /**
     * @see org.kalypso.ui.editor.styleeditor.tab.ITabListListener#changed(org.kalypso.ui.editor.styleeditor.tab.ITabItem[])
     */
    @Override
    public void changed( final ITabItem[] items )
    {
      handleChanged( items );
    }

    /**
     * @see org.kalypso.ui.editor.styleeditor.tab.ITabListListener#selectionChanged(org.kalypso.ui.editor.styleeditor.tab.ITabItem)
     */
    @Override
    public void selectionChanged( final ITabItem selectedItem )
    {
      handleListSelectionChanged( selectedItem );
    }
  };

  final CTabFolder2Adapter m_folderListener = new CTabFolder2Adapter()
  {
    /**
     * @see org.eclipse.swt.custom.CTabFolder2Listener#close(org.eclipse.swt.custom.CTabFolderEvent)
     */
    @Override
    public void close( final CTabFolderEvent event )
    {
      final CTabItem item = (CTabItem) event.item;
      event.doit = handleTabClosed( item );
    }
  };

  private final ISelectionChangedListener m_tabSelectionListener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
      handleTabSelectionChanged( (ITabItem) selection.getFirstElement() );
    }
  };

  private TabViewer m_viewer;

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_viewer != null )
    {
      final CTabFolder tabFolder = m_viewer.getTabFolder();
      tabFolder.removeCTabFolder2Listener( m_folderListener );
    }
  }

  protected void handleTabSelectionChanged( final ITabItem selection )
  {
    final ITabList input = getInput();
    input.setSelection( selection );

    // updateTabActions();
  }

  protected void handleListSelectionChanged( final ITabItem selectedItem )
  {
    if( m_viewer == null )
      return;

    if( selectedItem == null )
      m_viewer.setSelection( StructuredSelection.EMPTY );
    else
      m_viewer.setSelection( new StructuredSelection( selectedItem ), true );
  }

  protected boolean handleTabClosed( final CTabItem item )
  {
    final CTabFolder folder = m_viewer.getTabFolder();
    final Shell shell = folder.getShell();

    final ITabItem element = (ITabItem) item.getData( TabViewer.DATA_CONTENT );
    final String ruleName = element.getItemLabel();
    if( !confirmTabClose( shell, ruleName ) )
      return false;

    final int index = folder.indexOf( item );
    final int oldSize = folder.getItemCount();
    final CTabItem selection = folder.getSelection();
    final boolean isSelected = selection == item;

    final ITabList input = getInput();
    input.removeItem( element );
    if( isSelected )
    {
      final int index2select = Math.max( 0, index == oldSize - 1 ? index - 1 : index );
      if( folder.getItemCount() > 0 )
      {
        // STRANGE: we need to do both selection, else the selection events will not correctly be processed
        folder.setSelection( index2select );
        final CTabItem item2Select = folder.getItem( index2select );
        m_viewer.setSelection( new StructuredSelection( item2Select ) );
      }
      else
        m_viewer.setSelection( StructuredSelection.EMPTY );

    }

    return true;
  }

  protected boolean confirmTabClose( final Shell shell, final String ruleName )
  {
    final String message = String.format( Messages.getString("TabListContentProvider_0"), ruleName ); //$NON-NLS-1$
    return MessageDialog.openConfirm( shell, Messages.getString("TabListContentProvider_1"), message ); //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    Assert.isTrue( viewer instanceof TabViewer );

    if( m_viewer != null )
    {
      final CTabFolder tabFolder = m_viewer.getTabFolder();
      if( !tabFolder.isDisposed() )
        tabFolder.removeCTabFolder2Listener( m_folderListener );

      m_viewer.removeSelectionChangedListener( m_tabSelectionListener );
    }

    if( oldInput instanceof ITabList )
    {
      final ITabList list = (ITabList) oldInput;
      list.removeTabListener( m_tabListener );
    }

    m_viewer = (TabViewer) viewer;

    if( newInput instanceof ITabList )
    {
      final ITabList list = (ITabList) newInput;
      list.addTabListener( m_tabListener );
    }

    if( m_viewer != null )
    {
      final CTabFolder tabFolder = m_viewer.getTabFolder();
      if( !tabFolder.isDisposed() )
        tabFolder.addCTabFolder2Listener( m_folderListener );

      m_viewer.addSelectionChangedListener( m_tabSelectionListener );
    }
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  @Override
  public Object[] getElements( final Object inputElement )
  {
    final ITabList listInput = (ITabList) inputElement;
    return listInput.getItems();
  }

  protected void handleChanged( final ITabItem[] items )
  {
    if( items == null )
      smartRefresh();
    else
    {
      for( final ITabItem item : items )
        m_viewer.update( item, null );
    }
  }

  private void smartRefresh( )
  {
    final ITabList list = getInput();
    final CTabFolder folder = m_viewer.getTabFolder();

    final ITabItem[] elements = list.getItems();
    final CTabItem[] items = folder.getItems();

    int elementIndex = 0;
    int itemIndex = 0;
    for( elementIndex = 0; elementIndex < elements.length; elementIndex++ )
    {
      final ITabItem element = elements[elementIndex];
      final Object itemElement = elementIndex < items.length ? items[itemIndex].getData( TabViewer.DATA_CONTENT ) : null;
      if( ObjectUtils.equals( itemElement, element ) )
        m_viewer.update( element, null );
      else
      {
        // break on first real difference, we need to recreate tabs from here on
        break;
      }

      itemIndex++;
    }

    // Dispose tabs
    for( int j = itemIndex; j < items.length; j++ )
      items[j].dispose();
    // Re-create tabs
    for( int j = elementIndex; j < elements.length; j++ )
      m_viewer.createTab( elements[j] );
  }

  private ITabList getInput( )
  {
    return (ITabList) m_viewer.getInput();
  }
}
