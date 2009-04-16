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
package org.kalypso.ogc.gml.outline;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.kalypso.ogc.gml.AbstractKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.RuleTreeObject;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModellAdapter;

/**
 * Content provider for modifying the outline tree. It filters the styles and the rules.
 *
 * @author Holger Albert
 */
public class GisMapOutlineContentProvider extends BaseWorkbenchContentProvider
{
  private final ITreeViewerListener m_treeListener = new ITreeViewerListener()
  {
    /**
     * @see org.eclipse.jface.viewers.ITreeViewerListener#treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent)
     */
    public void treeCollapsed( final TreeExpansionEvent event )
    {
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeViewerListener#treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent)
     */
    public void treeExpanded( final TreeExpansionEvent event )
    {
      updateChildrenCheckState( event.getElement() );
    }
  };

  private final IMapModellListener m_modelListener = new MapModellAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeActivated(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeActivated( final IMapModell source, final IKalypsoTheme previouslyActive, final IKalypsoTheme nowActive )
    {
      final GisMapOutlineLabelProvider labelProvider = getLabelProvider();
      final TreeViewer viewer = getViewer();

      if( viewer != null && !viewer.getControl().isDisposed() )
      {
        viewer.getControl().getDisplay().asyncExec( new Runnable()
        {
          public void run( )
          {
            if( previouslyActive != null && nowActive != null )
              labelProvider.elementsChanged( previouslyActive, nowActive );
            else if( previouslyActive != null )
              labelProvider.elementsChanged( previouslyActive );
            else if( nowActive != null )
              labelProvider.elementsChanged( nowActive );
          }
        } );
      }
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeAdded(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeAdded( final IMapModell source, final IKalypsoTheme theme )
    {
      refreshViewer( theme );
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeOrderChanged(org.kalypso.ogc.gml.mapmodel.IMapModell)
     */
    @Override
    public void themeOrderChanged( final IMapModell source )
    {
      refreshViewer( null );
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeRemoved(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, boolean)
     */
    @Override
    public void themeRemoved( final IMapModell source, final IKalypsoTheme theme, final boolean lastVisibility )
    {
      final TreeViewer viewer = getViewer();
      if( viewer != null && !viewer.getControl().isDisposed() )
      {
        viewer.getControl().getDisplay().asyncExec( new Runnable()
        {
          public void run( )
          {
            if( !viewer.getControl().isDisposed() )
              viewer.remove( theme );
          }
        } );
      }
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeVisibilityChanged(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, boolean)
     */
    @Override
    public void themeVisibilityChanged( final IMapModell source, final IKalypsoTheme theme, final boolean visibility )
    {
      final Viewer viewer = getViewer();
      final Control control = viewer.getControl();
      if( viewer instanceof CheckboxTreeViewer && !control.isDisposed() )
      {
        control.getDisplay().asyncExec( new Runnable()
        {
          public void run( )
          {
            if( !control.isDisposed() )
              updateCheckState( theme );
          }
        } );
      }
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.IMapModellListener#themeStatusChanged(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeStatusChanged( final IMapModell source, final IKalypsoTheme theme )
    {
      refreshViewer( theme );
    }
  };

  private TreeViewer m_viewer;

  private final GisMapOutlineLabelProvider m_labelProvider;

  private boolean m_isCompact = true;

  public GisMapOutlineContentProvider( final GisMapOutlineLabelProvider labelProvider )
  {
    m_labelProvider = labelProvider;
  }

  protected TreeViewer getViewer( )
  {
    return m_viewer;
  }

  protected GisMapOutlineLabelProvider getLabelProvider( )
  {
    return m_labelProvider;
  }

  /**
   * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getChildren(java.lang.Object)
   */
  @Override
  public Object[] getChildren( final Object element )
  {
    final Object[] childrenReverse = getChildrenReverse( element ).clone();
    if( !(element instanceof IMapModell) )
      ArrayUtils.reverse( childrenReverse );
    return childrenReverse;
  }

  private Object[] getChildrenReverse( final Object element )
  {
    /* A moment please, if the theme is configured, not to show its children, then ignore them. */
    // TODO: should configurable for every level
    if( element instanceof AbstractKalypsoTheme )
    {
      final AbstractKalypsoTheme theme = (AbstractKalypsoTheme) element;
      if( theme.shouldShowLegendChildren() == false )
        return new Object[] {};
    }

    if( m_isCompact )
      return compactChildren( element );

    return super.getChildren( element );
  }

  private Object[] compactChildren( final Object element )
  {
    final Object[] children = super.getChildren( element );

    // Do not compactify the model itself; maybe introduce a common interface
    // that decided, if compactification is allowed
    if( element instanceof IMapModell )
      return children;

    /* Normal case none or more than one children */
    if( children == null || children.length != 1 )
      return children;

    return getChildrenReverse( children[0] );
  }

  /**
   * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getParent(java.lang.Object)
   */
  @Override
  public Object getParent( final Object element )
  {
    /* Only rules may jump above parents. */
    if( !(element instanceof RuleTreeObject) )
      return super.getParent( element );

    /* Get the parent for the rule (this would be the style). */
    final Object style = super.getParent( element );

    /* Get the parent of the style (this would be a theme). */
    final Object theme = super.getParent( style );

    /* Get the children of the theme (this would be all styles). */
    final Object[] styles = getChildren( theme );

    /* Check, if there are more than one style. If so, return the normal result. */
    if( styles.length > 1 )
      return style;

    /* Otherwise, there is only one style. */
    return theme;
  }

  /**
   * @see org.eclipse.ui.model.WorkbenchContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    m_viewer = (TreeViewer) viewer;

    if( oldInput instanceof IMapModell )
      ((IMapModell) oldInput).removeMapModelListener( m_modelListener );

    if( newInput instanceof IMapModell )
      ((IMapModell) newInput).addMapModelListener( m_modelListener );

    m_viewer.addTreeListener( m_treeListener );

    // Reset check state later, else it will not work, as no children have been created yet
    m_viewer.getControl().getDisplay().asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        updateChildrenCheckState( newInput );
      }
    } );
  }

  /**
   * Updates the check state of all children of the given element.<br>
   * Does NOT recurse further into the element
   */
  protected void updateChildrenCheckState( final Object parent )
  {
    final Object[] children = getChildren( parent );
    for( final Object child : children )
      updateCheckState( child );
  }

  /**
   * Updates grayed and checked stated of the given object.<br>
   * Does NOT recurse
   **/
  protected void updateCheckState( final Object object )
  {
    if( m_viewer instanceof ICheckable )
    {
      final ICheckable checkable = (ICheckable) m_viewer;

      final boolean checked = m_labelProvider.isChecked( object );
// final boolean grayed = m_labelProvider.isGrayed( object );
      checkable.setChecked( object, checked );
    }
  }

  protected void refreshViewer( final Object element )
  {
    final TreeViewer viewer = getViewer();
    final Control control = viewer.getControl();
    if( viewer == null || control.isDisposed() )
      return;

    final Object elementToRefresh = element == null ? m_viewer.getInput() : element;

    control.getDisplay().asyncExec( new Runnable()
    {
      public void run( )
      {
        if( control.isDisposed() )
          return;

        viewer.refresh( elementToRefresh );
        updateCheckState( elementToRefresh );
        if( viewer.getExpandedState( elementToRefresh ) )
          updateChildrenCheckState( elementToRefresh );
      }
    } );
  }

  public boolean isCompact( )
  {
    return m_isCompact;
  }

  public void setCompact( final boolean compact )
  {
    if( compact == m_isCompact )
      return;

    m_isCompact = compact;

    refreshViewer( null );
  }

}