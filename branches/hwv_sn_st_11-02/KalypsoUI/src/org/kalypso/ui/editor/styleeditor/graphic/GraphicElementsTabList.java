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
package org.kalypso.ui.editor.styleeditor.graphic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.kalypso.commons.eclipse.jface.viewers.ITabItem;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.StyleInput;
import org.kalypso.ui.editor.styleeditor.tabs.AbstractTabList;
import org.kalypsodeegree.graphics.sld.ExternalGraphic;
import org.kalypsodeegree.graphics.sld.Graphic;
import org.kalypsodeegree.graphics.sld.Mark;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Gernot Belger
 */
public class GraphicElementsTabList extends AbstractTabList<Graphic>
{
  public GraphicElementsTabList( final IStyleInput<Graphic> input )
  {
    super( input );

    init( NO_PREFERED_SELECTION );
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.AbstractTabList#inputChanged()
   */
  @Override
  public void refresh( )
  {
    final Map<Object, IGraphicElementItem> oldItems = new IdentityHashMap<Object, IGraphicElementItem>();
    final ITabItem[] items = getItems();
    for( final ITabItem item : items )
      oldItems.put( ((IGraphicElementItem) item).getElement(), (IGraphicElementItem) item );

    internalClear();

    final Graphic graphic = getData();
    final Object[] elements = graphic == null ? ArrayUtils.EMPTY_OBJECT_ARRAY : graphic.getMarksAndExtGraphics();
    for( final Object element : elements )
    {
      if( oldItems.containsKey( element ) )
        internalAddItem( oldItems.get( element ) );
      else
      {
        final IGraphicElementItem newItem = createItem( element );
        internalAddItem( newItem );
      }
    }

    fireChanged();
  }

  private IGraphicElementItem createItem( final Object element )
  {
    if( element instanceof Mark )
      return new MarkTabItem( new StyleInput<Mark>( (Mark) element, getInput() ) );

    if( element instanceof ExternalGraphic )
      return new ExternalGraphicItem( new StyleInput<ExternalGraphic>( (ExternalGraphic) element, getInput() ) );

    throw new NotImplementedException();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabList#removeItem(org.kalypso.ui.editor.styleeditor.tab.ITabItem)
   */
  @Override
  public void removeItem( final ITabItem item )
  {
    final IGraphicElementItem elementItem = (IGraphicElementItem) item;

    final Object element = elementItem.getElement();

    final Graphic graphic = getData();
    graphic.removeMarksAndExtGraphic( element );

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

    final Graphic graphic = getData();
    final Object[] elements = graphic.getMarksAndExtGraphics();
    final Object[] newElements = elements.clone();
    newElements[index] = elements[index - 1];
    newElements[index - 1] = elements[index];

    graphic.setMarksAndExtGraphics( newElements );

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

  public ITabItem addNewMark( )
  {
    final Mark newMark = StyleFactory.createMark( "square" ); //$NON-NLS-1$
    return addNewItem( newMark );
  }

  public ITabItem addNewExternalGraphic( )
  {
    final IStyleInput<Graphic> input = getInput();
    final URL styleContext = input.getContext();
    final IUrlResolver2 resolver = new IUrlResolver2()
    {
      @Override
      public URL resolveURL( final String relativeOrAbsolute ) throws MalformedURLException
      {
        if( StringUtils.isBlank( relativeOrAbsolute ) )
          return null;

        return new URL( styleContext, relativeOrAbsolute );
      }
    };

    final ExternalGraphic newExternalGraphic = StyleFactory.createExternalGraphic( resolver, null, null );
    return addNewItem( newExternalGraphic );
  }

  private ITabItem addNewItem( final Object element )
  {
    final Graphic graphic = getData();
    if( graphic == null )
      return null;

    graphic.addMarksAndExtGraphic( element );

    getInput().fireStyleChanged();

    final ITabItem[] items = getItems();
    return items[items.length - 1];
  }
}