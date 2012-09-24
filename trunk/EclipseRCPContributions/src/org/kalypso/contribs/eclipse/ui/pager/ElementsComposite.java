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
package org.kalypso.contribs.eclipse.ui.pager;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Dirk Kuch
 */
public class ElementsComposite extends Composite implements IElementsComposite
{

  private final FormToolkit m_toolkit;

  private Composite m_body;

  private final IElementPage[] m_pages;

  protected IElementPage m_selectedPage;

  private final int m_style;

  private boolean m_showComboViewer = false;

  Set<IElementPageListener> m_listeners = Collections.synchronizedSet( new LinkedHashSet<IElementPageListener>() );

  public ElementsComposite( final Composite parent, final FormToolkit toolkit, final IElementPage[] pages )
  {
    this( parent, toolkit, pages, pages[0] );
  }

  public IElementPage getSelectedPage( )
  {
    return m_selectedPage;
  }

  /**
   * @param pages
   *          pages which will be rendered
   * @param selectedPage
   *          preselection = pages[index]
   */
  public ElementsComposite( final Composite parent, final FormToolkit toolkit, final IElementPage[] pages, final IElementPage selectedPage )
  {
    this( parent, toolkit, pages, selectedPage, -1 );
  }

  public ElementsComposite( final Composite parent, final FormToolkit toolkit, final IElementPage[] pages, final IElementPage selectedPage, final int style )
  {
    super( parent, SWT.NULL );
    m_toolkit = toolkit;
    m_pages = pages;
    m_selectedPage = selectedPage;
    m_style = style;

    setLayout( GridLayoutFactory.fillDefaults().create() );

    update();
  }

  @Override
  public void dispose( )
  {
    for( final IElementPage page : m_pages )
    {
      page.dispose();
    }

    super.dispose();
  }

  @Override
  public final void update( )
  {
    if( isDisposed() )
      return;

    if( m_body != null )
    {
      if( !m_body.isDisposed() )
      {
        m_body.dispose();
      }

      m_body = null;
    }

    m_body = m_toolkit.createComposite( this );
    GridLayoutFactory.fillDefaults().applyTo( m_body );

    m_body.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    if( isComboEnabledHead() )
    {
      addCombo( m_body );
    }

    if( m_selectedPage != null )
    {
      m_selectedPage.render( m_body, m_toolkit );
    }

    if( m_pages.length > 1 && m_style == SWT.BOTTOM )
    {
      addCombo( m_body );
    }

    m_toolkit.adapt( this );

    m_body.layout();
    this.layout();
  }

  private boolean isComboEnabledHead( )
  {
    if( m_style == SWT.BOTTOM )
      return false;
    else if( m_pages.length > 1 )
      return true;
    else if( m_showComboViewer )
      return true;

    return false;
  }

  private void addCombo( final Composite body )
  {
    final ComboViewer viewer = new ComboViewer( body, SWT.SINGLE | SWT.READ_ONLY );
    viewer.getCombo().setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        if( element instanceof IElementPage )
        {
          final IElementPage page = (IElementPage) element;

          return page.getLabel();
        }
        return super.getText( element );
      }
    } );

    viewer.setInput( m_pages );
    viewer.setSelection( new StructuredSelection( m_selectedPage ) );

    viewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        final Object element = selection.getFirstElement();
        if( element instanceof IElementPage )
        {
          m_selectedPage = (IElementPage) element;

          fireSelectedPageChanged( m_selectedPage );
        }

        update();
      }
    } );

    fireSelectedPageChanged( m_selectedPage );
  }

  public void setShowAlwaysComboViewer( final boolean showComboViewer )
  {
    m_showComboViewer = showComboViewer;
  }

  public void addPageListener( final IElementPageListener listener )
  {
    m_listeners.add( listener );
  }

  public void removePageListener( final IElementPageListener listener )
  {
    m_listeners.remove( listener );
  }

  protected void fireSelectedPageChanged( final IElementPage page )
  {
    final IElementPageListener[] listeners = m_listeners.toArray( new IElementPageListener[] {} );
    for( final IElementPageListener listener : listeners )
    {
      listener.pageChanged( page.getIdentifier() );
    }
  }
}
