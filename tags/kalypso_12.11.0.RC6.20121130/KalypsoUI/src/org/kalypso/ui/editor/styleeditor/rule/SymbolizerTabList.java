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
package org.kalypso.ui.editor.styleeditor.rule;

import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.kalypso.commons.eclipse.jface.viewers.ITabItem;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.StyleInput;
import org.kalypso.ui.editor.styleeditor.tabs.AbstractTabList;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.Symbolizer;

/**
 * @author Gernot Belger
 */
public class SymbolizerTabList extends AbstractTabList<Rule>
{
  public SymbolizerTabList( final IStyleInput<Rule> input )
  {
    super( input );

    init( NO_PREFERED_SELECTION );
  }

  @Override
  public void refresh( )
  {
    final Map<Symbolizer, ISymbolizerTabItem> oldItems = new IdentityHashMap<>();
    final ITabItem[] items = getItems();
    for( final ITabItem item : items )
      oldItems.put( ((ISymbolizerTabItem)item).getSymbolizer(), (ISymbolizerTabItem)item );

    internalClear();

    final Rule rule = getData();

    final Symbolizer[] symbolizers = rule == null ? new Symbolizer[0] : rule.getSymbolizers();
    for( final Symbolizer element : symbolizers )
    {
      if( oldItems.containsKey( element ) )
        internalAddItem( oldItems.get( element ) );
      else
        internalAddItem( new SymbolizerTabItem<>( new StyleInput<>( element, getInput() ) ) );
    }

    fireChanged();
  }

  void addSymbolizer( final Symbolizer symbolizer )
  {
    final ITabItem item = addNewItem( symbolizer );
    setSelection( item );
  }

  private ITabItem addNewItem( final Symbolizer symbolizer )
  {
    final Rule rule = getData();
    rule.addSymbolizer( symbolizer );

    getInput().fireStyleChanged();

    final ITabItem[] items = getItems();
    return items[items.length - 1];
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabList#removeItem(org.kalypso.ui.editor.styleeditor.tab.ITabItem)
   */
  @Override
  public void removeItem( final ITabItem item )
  {
    final ISymbolizerTabItem symbolizerItem = (ISymbolizerTabItem)item;

    final Symbolizer symbolizer = symbolizerItem.getSymbolizer();

    final Rule rule = getData();
    rule.removeSymbolizer( symbolizer );

    getInput().fireStyleChanged();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabList#moveBackward(int)
   */
  @Override
  public void moveBackward( final int index )
  {
    Assert.isTrue( index > 0 );
    Assert.isTrue( index < size() );

    final Rule rule = getData();
    final Symbolizer[] symbolizers = rule.getSymbolizers();
    final Symbolizer[] newSymbolizer = symbolizers.clone();
    newSymbolizer[index] = symbolizers[index - 1];
    newSymbolizer[index - 1] = symbolizers[index];

    rule.setSymbolizers( newSymbolizer );

    getInput().fireStyleChanged();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabList#moveForward(int)
   */
  @Override
  public void moveForward( final int index )
  {
    Assert.isTrue( index >= 0 );
    Assert.isTrue( index < size() - 1 );

    moveBackward( index + 1 );
  }
}
