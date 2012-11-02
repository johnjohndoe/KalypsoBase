/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Gernot Belger
 */
public class TabViewer extends StructuredViewer
{
  public static final String DATA_CONTENT = "content"; //$NON-NLS-1$

  private final CTabFolder m_folder;

  private int m_minLabelLength = -1;

  private int m_maxLabelLength = -1;

  private final ToolBarManager m_toolbarManager = new ToolBarManager( SWT.HORIZONTAL );

  public TabViewer( final Composite parent, final int style )
  {
    this( new CTabFolder( parent, style ) );
  }

  public TabViewer( final CTabFolder folder )
  {
    m_folder = folder;

    final ToolBar toolBar = m_toolbarManager.createControl( m_folder );
    m_folder.setTopRight( toolBar );

    m_folder.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleTabSelected();
      }
    } );
  }

  public CTabFolder getTabFolder( )
  {
    return m_folder;
  }

  public void setMinLabelLength( final int minLabelLength )
  {
    m_minLabelLength = minLabelLength;
  }

  public void setMaxLabelLength( final int maxLabelLength )
  {
    m_maxLabelLength = maxLabelLength;
  }

  /**
   * @see org.eclipse.jface.viewers.StructuredViewer#setLabelProvider(org.eclipse.jface.viewers.IBaseLabelProvider)
   */
  @Override
  public void setLabelProvider( final IBaseLabelProvider labelProvider )
  {
    Assert.isTrue( labelProvider instanceof ITabControlProvider );

    super.setLabelProvider( labelProvider );
  }

  /**
   * @see org.eclipse.jface.viewers.ContentViewer#getLabelProvider()
   */
  @Override
  public ITabControlProvider getLabelProvider( )
  {
    return (ITabControlProvider) super.getLabelProvider();
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object, java.lang.Object)
   */
  @Override
  protected void inputChanged( final Object input, final Object oldInput )
  {
    super.inputChanged( input, oldInput );

    refresh();
  }

  /**
   * @see org.eclipse.jface.viewers.StructuredViewer#doFindInputItem(java.lang.Object)
   */
  @Override
  protected CTabItem doFindInputItem( final Object element )
  {
    return doFindItem( element );
  }

  /**
   * @see org.eclipse.jface.viewers.StructuredViewer#doFindItem(java.lang.Object)
   */
  @Override
  protected CTabItem doFindItem( final Object element )
  {
    Assert.isNotNull( element );

    final CTabItem[] items = m_folder.getItems();
    for( final CTabItem item : items )
    {
      final Object content = item.getData( DATA_CONTENT );
      if( ObjectUtils.equals( content, element ) )
        return item;
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.StructuredViewer#doUpdateItem(org.eclipse.swt.widgets.Widget, java.lang.Object,
   *      boolean)
   */
  @Override
  protected void doUpdateItem( final Widget item, final Object element, final boolean fullMap )
  {
    Assert.isTrue( item instanceof CTabItem );
    Assert.isTrue( item instanceof CTabItem );

    final CTabItem tabItem = (CTabItem) item;

    updateItemLabel( tabItem, element );

    final ITabControlProvider controlProvider = getLabelProvider();
    controlProvider.updateControl( element );
  }

  /**
   * @see org.eclipse.jface.viewers.StructuredViewer#getSelectionFromWidget()
   */
  @Override
  protected List< ? > getSelectionFromWidget( )
  {
    final CTabItem selectedItem = m_folder.getSelection();
    if( selectedItem == null )
      return Collections.emptyList();

    final Object element = selectedItem.getData( DATA_CONTENT );
    // element might be null during refresh
    if( element == null )
      return Collections.emptyList();

    return Collections.singletonList( element );
  }

  /**
   * @see org.eclipse.jface.viewers.StructuredViewer#internalRefresh(java.lang.Object)
   */
  @Override
  protected void internalRefresh( final Object element )
  {
    final Object input = getInput();

    if( element == null || element == input )
      internalRefreshAll();
    else
      internalRefreshElement( element );
  }

  private void internalRefreshAll( )
  {
    /* Clear all */
    final CTabItem[] items = m_folder.getItems();
    for( final CTabItem item : items )
      destroyItem( item );

    /* Re-create all */
    final Object[] elements = getSortedChildren( getRoot() );
    for( final Object element : elements )
      createTab( element );
  }

  public void createTab( final Object element )
  {
    final ITabControlProvider provider = getLabelProvider();
    final int itemStyle = provider.getItemStyle( element );
    final CTabItem item = new CTabItem( m_folder, itemStyle );

    createItemContent( item, element );
  }

  private void destroyItem( final CTabItem item )
  {
    clearItem( item );
    item.dispose();
  }

  private void internalRefreshElement( final Object element )
  {
    Assert.isNotNull( element );

    /* The order of elements might have changed -> we find the item by index */
    final Object[] sortedChildren = getSortedChildren( getRoot() );
    final int elementIndex = ArrayUtils.indexOf( sortedChildren, element );
    final CTabItem item = m_folder.getItems()[elementIndex];
    clearItem( item );
    createItemContent( item, element );
  }

  private void clearItem( final CTabItem item )
  {
    if( item.isDisposed() )
      return;

    item.setData( DATA_CONTENT, null );
    item.setText( StringUtils.EMPTY );
    item.setToolTipText( null );
  }

  private void createItemContent( final CTabItem item, final Object element )
  {
    item.setData( DATA_CONTENT, element );

    final ITabControlProvider controlProvider = getLabelProvider();
    final Control contentControl = controlProvider.createControl( m_folder, element );

    if( contentControl != null )
      item.setControl( contentControl );

    updateItemLabel( item, element );

    final Image image = controlProvider.getImage( element );
    item.setImage( image );

    return;
  }

  private void updateItemLabel( final CTabItem item, final Object element )
  {
    final ITabControlProvider controlProvider = getLabelProvider();

    item.setToolTipText( null );
    final String text = controlProvider.getText( element );
    if( text != null )
    {
      final String label = shortenLabel( text );
      item.setText( StringUtils.rightPad( label, m_minLabelLength ) );
      if( !label.equals( text ) )
        item.setToolTipText( text );
    }
  }

  private String shortenLabel( final String text )
  {
    if( m_maxLabelLength == -1 )
      return text;

    return StringUtils.abbreviate( text, m_maxLabelLength );
  }

  /**
   * @see org.eclipse.jface.viewers.StructuredViewer#reveal(java.lang.Object)
   */
  @Override
  public void reveal( final Object element )
  {
    final CTabItem item = (CTabItem) findItem( element );
    if( item != null )
      m_folder.showItem( item );
  }

  /**
   * Only lists of one element are supported.
   *
   * @see org.eclipse.jface.viewers.StructuredViewer#setSelectionToWidget(java.util.List, boolean)
   */
  @Override
  protected void setSelectionToWidget( final List l, final boolean reveal )
  {
    Assert.isTrue( l.size() < 2 );

    if( l.size() == 0 )
    {
      // TODO: hm, is it possible to remove the selection?
      return;
    }

    final Object element = l.get( 0 );
    Assert.isNotNull( element );

    final CTabItem item = (CTabItem) findItem( element );
    if( item == null )
      return;

    m_folder.setSelection( item );
    if( reveal )
      m_folder.showItem( item );
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#getControl()
   */
  @Override
  public Control getControl( )
  {
    return m_folder;
  }

  protected void handleTabSelected( )
  {
    final ISelection selection = getSelection();
    fireSelectionChanged( new SelectionChangedEvent( this, selection ) );
  }

  public void removeTab( final Object element )
  {
    final CTabItem item = (CTabItem) findItem( element );
    if( item == null )
      return;

    destroyItem( item );
  }

  public Object findElement( final CTabItem item )
  {
    return item.getData( DATA_CONTENT );
  }

  public ToolBarManager getToolbar( )
  {
    return m_toolbarManager;
  }

  public void updateToolbar( )
  {
    m_toolbarManager.update( true );
    final Point tbSize = m_toolbarManager.getControl().computeSize( SWT.DEFAULT, SWT.DEFAULT );
    // REMARK: need to explicitely set item height, else the toolbar will not show up
    m_folder.setTabHeight( tbSize.y );

  }

}
