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
package org.kalypso.ui.editor.styleeditor.tabs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.kalypso.commons.eclipse.jface.viewers.ITabItem;
import org.kalypso.commons.eclipse.jface.viewers.ITabListListener;
import org.kalypso.commons.eclipse.jface.viewers.ITypedTabList;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;

/**
 * @author Gernot Belger
 */
public abstract class AbstractTabList<DATA> implements ITypedTabList<DATA>
{
  protected static final int NO_PREFERED_SELECTION = -1;

  private final Collection<ITabListListener> m_listeners = new HashSet<ITabListListener>();

  private final List<ITabItem> m_items = new ArrayList<ITabItem>();

  private final IStyleInput<DATA> m_input;

  private ITabItem m_selectedItem = null;

  public AbstractTabList( final IStyleInput<DATA> input )
  {
    m_input = input;
  }

  protected void init( final int preferedSelection )
  {
    refresh();

    initializeSelection( preferedSelection );
  }

  private void initializeSelection( final int preferedSelection )
  {
    final ITabItem[] items = getItems();
    if( items.length == 0 )
      return;

    if( preferedSelection == NO_PREFERED_SELECTION )
      setSelection( items[0] );
    else
      setSelection( items[preferedSelection] );
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabList#dispose()
   */
  @Override
  public void dispose( )
  {
    m_listeners.clear();
    m_items.clear();
  }

  @Override
  public final void addTabListener( final ITabListListener l )
  {
    m_listeners.add( l );
  }

  @Override
  public final void removeTabListener( final ITabListListener l )
  {
    m_listeners.remove( l );
  }

  protected final void fireChanged( )
  {
    final ITabListListener[] listeners = m_listeners.toArray( new ITabListListener[m_listeners.size()] );
    for( final ITabListListener listener : listeners )
      listener.changed( null );
  }

  private void fireSelectionChanged( )
  {
    final ITabListListener[] listeners = m_listeners.toArray( new ITabListListener[m_listeners.size()] );
    for( final ITabListListener listener : listeners )
      listener.selectionChanged( m_selectedItem );
  }

  protected IStyleInput<DATA> getInput( )
  {
    return m_input;
  }

  @Override
  public final DATA getData( )
  {
    if( m_input == null )
      return null;

    return m_input.getData();
  }

  protected final void fireStylseChanged( )
  {
    if( m_input == null )
      return;

    m_input.fireStyleChanged();
  }

  public final IFeatureType getFeatureType( )
  {
    final IStyleInput<DATA> input = getInput();
    if( input == null )
      return null;

    return input.getFeatureType();
  }

  protected void internalClear( )
  {
    m_items.clear();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabList#size()
   */
  @Override
  public int size( )
  {
    return m_items.size();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabList#getItems()
   */
  @Override
  public ITabItem[] getItems( )
  {
    return m_items.toArray( new ITabItem[m_items.size()] );
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabList#getSelection()
   */
  @Override
  public ITabItem getSelection( )
  {
    return m_selectedItem;
  }

  protected void internalAddItem( final ITabItem item )
  {
    m_items.add( item );
  }

  @Override
  public void setSelection( final ITabItem selectedElement )
  {
    if( m_selectedItem != selectedElement )
    {
      m_selectedItem = selectedElement;
      fireSelectionChanged();
    }
  }
}
