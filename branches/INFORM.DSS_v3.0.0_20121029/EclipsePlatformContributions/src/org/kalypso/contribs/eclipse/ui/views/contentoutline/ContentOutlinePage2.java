/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.ui.views.contentoutline;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * ContentOutlinePage that allows subclasses to specify which tree viewer to use
 * 
 * @author schlienger
 */
public abstract class ContentOutlinePage2 extends Page implements IContentOutlinePage, ISelectionChangedListener
{
  private final ListenerList m_selectionChangedListeners = new ListenerList( ListenerList.IDENTITY );

  protected TreeViewer m_treeViewer;

  /**
   * Create a new content outline page.
   */
  protected ContentOutlinePage2( )
  {
    super();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  @Override
  public void addSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_selectionChangedListeners.add( listener );
  }

  /**
   * The <code>ContentOutlinePage</code> implementation of this <code>IContentOutlinePage</code> method creates a tree
   * viewer using createTreeViewer( Composite ). Subclasses may override createTreeViewer to provide another TreeViewer
   * than the default one.
   * <p>
   * Subclasses must extend this method to configure the tree viewer with a proper content provider, label provider, and
   * input element.
   * 
   * @param parent
   */
  @Override
  public void createControl( final Composite parent )
  {
    m_treeViewer = createTreeViewer( parent );
    m_treeViewer.addSelectionChangedListener( this );
  }

  /**
   * Creates an instance of TreeViewer. This default implementation creates a <code>TreeViewer</code>. You may choose to
   * override it and create an instance of another subclass of TreeViewer.
   * 
   * @param parent
   * @return new instance of TreeViewer
   */
  protected TreeViewer createTreeViewer( final Composite parent )
  {
    return new TreeViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
  }

  /**
   * Fires a selection changed event.
   * 
   * @param selection
   *          the new selection
   */
  protected void fireSelectionChanged( final ISelection selection )
  {
    // create an event
    final SelectionChangedEvent event = new SelectionChangedEvent( this, selection );

    // fire the event
    final Object[] listeners = m_selectionChangedListeners.getListeners();
    for( int i = 0; i < listeners.length; ++i )
    {
      final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
      SafeRunnable.run( new SafeRunnable()
      {
        @Override
        public void run( )
        {
          l.selectionChanged( event );
        }
      } );
    }
  }

  /**
   * @see org.eclipse.ui.part.IPage#getControl()
   */
  @Override
  public Control getControl( )
  {
    if( m_treeViewer == null )
      return null;
    return m_treeViewer.getControl();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  @Override
  public ISelection getSelection( )
  {
    if( m_treeViewer == null )
      return StructuredSelection.EMPTY;
    return m_treeViewer.getSelection();
  }

  /**
   * Returns this page's tree viewer.
   * 
   * @return this page's tree viewer, or <code>null</code> if <code>createControl</code> has not been called yet
   */
  protected TreeViewer getTreeViewer( )
  {
    return m_treeViewer;
  }

  /**
   * @see org.eclipse.ui.part.IPageBookViewPage#init(org.eclipse.ui.part.IPageSite)
   */
  @Override
  public void init( final IPageSite pageSite )
  {
    super.init( pageSite );
    pageSite.setSelectionProvider( this );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  @Override
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_selectionChangedListeners.remove( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    fireSelectionChanged( event.getSelection() );
  }

  /**
   * Sets focus to a part in the page.
   */
  @Override
  public void setFocus( )
  {
    m_treeViewer.getControl().setFocus();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void setSelection( final ISelection selection )
  {
    if( m_treeViewer != null )
      m_treeViewer.setSelection( selection );
  }
}