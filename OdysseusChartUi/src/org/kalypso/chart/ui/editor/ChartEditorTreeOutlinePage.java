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
package org.kalypso.chart.ui.editor;

import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.dnd.ChartLayerTransfer;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IExpandableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;

/**
 * @author alibu
 * @author kimwerner
 */
public class ChartEditorTreeOutlinePage implements IContentOutlinePage
{

  protected IChartPart m_chartPart;

  protected ICheckStateListener m_checkStateListener = null;

  protected ChartEditorTreeContentProvider m_contentProvider = null;

  private final ILayerManagerEventListener m_eventListener;

  protected ISelectionChangedListener m_selectionChangeListener = null;

  protected CheckboxTreeViewer m_treeViewer;

  public ChartEditorTreeOutlinePage( IChartPart editor )
  {
    m_chartPart = editor;
    m_contentProvider = new ChartEditorTreeContentProvider( m_chartPart.getChartComposite().getChartModel() );
    m_eventListener = new AbstractLayerManagerEventListener()
    {

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerAdded(de.openali.
       *      odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerAdded( IChartLayer layer )
      {
        refreshItems( m_contentProvider.getParent( layer ) );
        m_treeViewer.setChecked( layer, true );
        handleCheckStateChanged( layer, true );
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerContentChanged(de
       *      .openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerContentChanged( IChartLayer layer )
      {
       refreshItems( layer );

      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerMoved(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerMoved( IChartLayer layer )
      {
        refreshItems( layer );
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerRemoved(de.openali
       *      .odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerRemoved( IChartLayer layer )
      {
        m_treeViewer.remove( layer );
      }

    };
    m_checkStateListener = new ICheckStateListener()
    {
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        Object elt = event.getElement();
        if( elt instanceof IChartLayer )
        {
          handleCheckStateChanged( (IChartLayer) event.getElement(), event.getChecked() );
        }
        else
        {
          // children of layers cannot be unchecked
          Object treeParent = m_contentProvider.getParent( elt );
          m_treeViewer.setChecked( elt, m_treeViewer.getChecked( treeParent ) );
        }
      }
    };
    m_selectionChangeListener = new ISelectionChangedListener()
    {
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final ISelection selection = event.getSelection();
        final IStructuredSelection struct = (IStructuredSelection) selection;
        final Object element = struct.getFirstElement();
        if( element instanceof IChartLayer )
          handleSelectionChanged( (IChartLayer) element );
      }
    };
  }

  protected void addDropSupport( )
  {
    // Drag'n drop

    // add drag and drop support only for move operation
    int ops = DND.DROP_MOVE;

    DragSourceListener dragSL = new DragSourceListener()
    {

      public void dragFinished( DragSourceEvent event )
      {
        // nothing to do
      }

      @SuppressWarnings("unchecked")
      public void dragSetData( DragSourceEvent event )
      {
        // resolve selected layer
        IStructuredSelection selection = (IStructuredSelection) m_treeViewer.getSelection();

        List list = selection.toList();
        // only one can be selected
        Object elt = list.get( 0 );
        // only layers can be dragged
        if( elt instanceof IChartLayer )
        {
          IChartLayer layer = (IChartLayer) elt;
          event.data = layer.getId();
        }
      }

      public void dragStart( DragSourceEvent event )
      {
        // nothing to do
      }

    };

    ViewerDropAdapter vda = new ViewerDropAdapter( m_treeViewer )
    {

      @Override
      public boolean performDrop( Object data )
      {
        if( data != null )
        {
          String id = (String) data;
          ILayerManager lm = m_chartPart.getChartComposite().getChartModel().getLayerManager();
          Object targetLayer = getCurrentTarget();
          Object parent = null;
          // find dragged
          IChartLayer draggedLayer = lm.getLayerById( id );
          if( draggedLayer == null )
          {
            parent = m_contentProvider.getParent( targetLayer );
            lm = parent instanceof IExpandableChartLayer ? ((IExpandableChartLayer) parent).getLayerManager() : null;
            draggedLayer = lm == null ? null : lm.getLayerById( id );
          }
          if( draggedLayer == null )
            return false;
          // either the dragged layer is dropped on another layer, then it will be
          // moved to its position

          if( targetLayer != null && targetLayer instanceof IChartLayer )
            lm.moveLayerToPosition( draggedLayer, lm.getLayerPosition( (IChartLayer) targetLayer ) );
          // or it was dropped at the end of the list, then it will be moved to the lowest position
          else
            lm.moveLayerToPosition( draggedLayer, 0 );

          refreshItems( parent );
          return true;
        }
        return false;
      }

      @Override
      public boolean validateDrop( Object target, int operation, TransferData transferType )
      {
        if( ChartLayerTransfer.getInstance().isSupportedType( transferType ) )
        {
          final Object o = getSelectedObject();
          if( getCurrentTarget() == null )
            return false;

          final Object parent1 = m_contentProvider.getParent( getCurrentTarget() );
          final Object parent2 = m_contentProvider.getParent( o );
          return parent1 == parent2;
        }
        return false;
      }
    };

    m_treeViewer.addDragSupport( ops, new Transfer[] { ChartLayerTransfer.getInstance() }, dragSL );
    m_treeViewer.addDropSupport( ops, new Transfer[] { ChartLayerTransfer.getInstance() }, vda );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void addSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_treeViewer.addSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    final Tree tree = new Tree( parent, SWT.CHECK );
    tree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    final TreeColumn column = new TreeColumn( tree, SWT.NONE );
    column.setWidth( tree.getSize().x );
    tree.addControlListener( new ControlAdapter()
    {
      /**
       * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
       */
      @Override
      public void controlResized( final ControlEvent e )
      {
        column.setWidth( tree.getSize().x );
      }
    } );

    m_treeViewer = new CheckboxTreeViewer( tree );

    m_treeViewer.setContentProvider( m_contentProvider );
    m_treeViewer.setLabelProvider( new ChartTreeLabelProvider( m_chartPart ) );
    m_treeViewer.addCheckStateListener( m_checkStateListener );

    m_treeViewer.addSelectionChangedListener( m_selectionChangeListener );

    m_chartPart.getChartComposite().getChartModel().getLayerManager().addListener( m_eventListener );

    addDropSupport();

    updateControl();
  }

  /**
   * @see org.eclipse.ui.part.IPage#dispose()
   */
  @Override
  public void dispose( )
  {
    m_chartPart.getChartComposite().getChartModel().getLayerManager().removeListener( m_eventListener );
    m_treeViewer.removeCheckStateListener( m_checkStateListener );
    m_treeViewer.removeSelectionChangedListener( m_selectionChangeListener );
  }

  public ICheckStateListener getCheckStateListener( )
  {
    return m_checkStateListener;
  }

  public ChartEditorTreeContentProvider getContentProvider( )
  {
    return m_contentProvider;
  }

  /**
   * @see org.eclipse.ui.part.IPage#getControl()
   */
  public Control getControl( )
  {
    return m_treeViewer.getControl();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection( )
  {
    return m_treeViewer.getSelection();
  }

  public ISelectionChangedListener getSelectionChangeListener( )
  {
    return m_selectionChangeListener;
  }

  public CheckboxTreeViewer getTreeViewer( )
  {
    return m_treeViewer;
  }

  protected void handleCheckStateChanged( final IChartLayer layer, final boolean checked )
  {
    layer.setVisible( checked );

    Object[] children = m_contentProvider.getChildren( layer );
    setChildChecked( children, checked );
    if( checked )
      setParentChecked( m_contentProvider.getParent( layer ), true );
  }

  protected void handleSelectionChanged( final IChartLayer layer )
  {
    layer.setActive( true );

  }

  protected void refreshItems( final Object parent )
  {
    Object[] o = m_treeViewer.getCheckedElements();
    m_treeViewer.refresh( parent);
    m_treeViewer.setCheckedElements( o );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_treeViewer.removeSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
   */
  public void setActionBars( final IActionBars actionBars )
  {

  }

  private final void setChecked( final ILayerManager mngr )
  {
    for( IChartLayer layer : mngr.getLayers() )
    {
      boolean checked = layer.isVisible();
      m_treeViewer.setChecked( layer, checked );
      Object[] children = m_contentProvider.getChildren( layer );
      for( Object object : children )
      {
        if( object instanceof IChartLayer )
        {
          m_treeViewer.setChecked( object, ((IChartLayer) object).isVisible() );
          if( object instanceof IExpandableChartLayer )
            setChecked( ((IExpandableChartLayer) layer).getLayerManager() );
        }
        else
          m_treeViewer.setChecked( object, checked );
      }
    }
  }

  public void setCheckStateListener( ICheckStateListener checkStateListener )
  {
    m_checkStateListener = checkStateListener;
  }

  final private void setChildChecked( final Object[] children, final boolean checked )
  {
    for( Object child : children )
    {
      if( child instanceof IChartLayer )
      {
        ((IChartLayer) child).setVisible( checked );
        m_treeViewer.setChecked( child, checked );
        setChildChecked( m_contentProvider.getChildren( child ), checked );
      }
    }
  }

  public void setContentProvider( ChartEditorTreeContentProvider contentProvider )
  {
    m_contentProvider = contentProvider;
  }

  /**
   * @see org.eclipse.ui.part.IPage#setFocus()
   */
  public void setFocus( )
  {
    m_treeViewer.getControl().setFocus();
  }

  final private void setParentChecked( final Object parent, final boolean checked )
  {
    if( parent == null )
      return;
    if( parent instanceof IChartLayer )
      ((IChartLayer) parent).setVisible( checked );
    m_treeViewer.setChecked( parent, checked );
    setParentChecked( m_contentProvider.getParent( parent ), checked );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  public void setSelection( final ISelection selection )
  {
    m_treeViewer.setSelection( selection );
  }

  public void setSelectionChangeListener( ISelectionChangedListener selectionChangeListener )
  {
    m_selectionChangeListener = selectionChangeListener;
  }

  public void updateControl( )
  {
    IChartModel model = m_chartPart.getChartComposite().getChartModel();
    m_treeViewer.setInput( model );
    setChecked( model.getLayerManager() );

  }

}
