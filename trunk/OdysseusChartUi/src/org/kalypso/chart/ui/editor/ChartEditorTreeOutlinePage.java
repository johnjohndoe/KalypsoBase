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
package org.kalypso.chart.ui.editor;

import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
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
  protected ICheckStateListener m_checkStateListener = null;

  protected ChartEditorTreeContentProvider m_contentProvider;

  private final ChartTreeLabelProvider m_labelProvider;

  private final ILayerManagerEventListener m_eventListener;

  protected ISelectionChangedListener m_selectionChangeListener = null;

  protected CheckboxTreeViewer m_treeViewer;

  protected final IChartModel m_model;

  public ChartEditorTreeOutlinePage( final IChartModel model )
  {
    m_model = model;

    final ChartTreeLabelProvider chartTreeLabelProvider = new ChartTreeLabelProvider();
    m_labelProvider = chartTreeLabelProvider;
    m_contentProvider = new ChartEditorTreeContentProvider( model );
    m_eventListener = new AbstractLayerManagerEventListener()
    {
      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerVisibilityChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerVisibilityChanged( final IChartLayer layer )
      {
        if( layer instanceof IExpandableChartLayer )
        {
          for( final IChartLayer l : ((IExpandableChartLayer) layer).getLayerManager().getLayers() )
          {
            onLayerVisibilityChanged( l );
          }
        }
        m_treeViewer.setChecked( layer, layer.isVisible() );
        m_treeViewer.refresh( layer );
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerAdded(de.openali.
       *      odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerAdded( final IChartLayer layer )
      {
        refreshItems( m_contentProvider.getParent( layer ) );
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerContentChanged(de
       *      .openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerContentChanged( final IChartLayer layer )
      {
        refreshItems( layer );
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerMoved(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerMoved( final IChartLayer layer )
      {
        final Object parent = m_contentProvider.getParent( layer );
        refreshItems( parent );
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerRemoved(de.openali
       *      .odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerRemoved( final IChartLayer layer )
      {
        m_treeViewer.remove( layer );
        chartTreeLabelProvider.clearLayer( layer );
      }
    };
    m_checkStateListener = new ICheckStateListener()
    {
      @Override
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        final Object elt = event.getElement();
        if( elt instanceof IChartLayer )
        {
          ((IChartLayer) elt).setVisible( event.getChecked() );
        }
        else
        {
          m_treeViewer.update( elt, null );
        }
      }
    };
    m_selectionChangeListener = new ISelectionChangedListener()
    {
      @Override
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
    final int ops = DND.DROP_MOVE;

    final DragSourceListener dragSL = new DragSourceListener()
    {

      @Override
      public void dragFinished( final DragSourceEvent event )
      {
        // nothing to do
      }

      @Override
      public void dragSetData( final DragSourceEvent event )
      {
        // resolve selected layer
        final IStructuredSelection selection = (IStructuredSelection) m_treeViewer.getSelection();

        final List< ? > list = selection.toList();
        // only one can be selected
        final Object elt = list.get( 0 );
        // only layers can be dragged
        if( elt instanceof IChartLayer )
        {
          final IChartLayer layer = (IChartLayer) elt;
          event.data = layer.getId();
        }
      }

      @Override
      public void dragStart( final DragSourceEvent event )
      {
        // nothing to do
      }
    };

    final ViewerDropAdapter vda = new ViewerDropAdapter( m_treeViewer )
    {
      @Override
      public boolean performDrop( final Object data )
      {
        if( data != null && m_model != null )
        {
          final String id = (String) data;
          ILayerManager layerManager = m_model.getLayerManager();

          final Object targetLayer = getCurrentTarget();
          Object parent = null;
          // find dragged
          IChartLayer draggedLayer = layerManager.getLayerById( id );
          if( draggedLayer == null )
          {
            parent = m_contentProvider.getParent( targetLayer );
            layerManager = parent instanceof IExpandableChartLayer ? ((IExpandableChartLayer) parent).getLayerManager() : null;
            draggedLayer = layerManager == null ? null : layerManager.getLayerById( id );
          }
          if( draggedLayer == null )
            return false;
          // either the dragged layer is dropped on another layer, then it will be
          // moved to its position

          if( targetLayer != null && targetLayer instanceof IChartLayer )
            layerManager.moveLayerToPosition( draggedLayer, layerManager.getLayerPosition( (IChartLayer) targetLayer ) );
          // or it was dropped at the end of the list, then it will be moved to the lowest position
          else
            layerManager.moveLayerToPosition( draggedLayer, 0 );

          refreshItems( parent );
          return true;
        }
        return false;
      }

      @Override
      public boolean validateDrop( final Object target, final int operation, final TransferData transferType )
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
  @Override
  public void addSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_treeViewer.addSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    final Tree tree = new Tree( parent, SWT.CHECK );
    tree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    m_treeViewer = new CheckboxTreeViewer( tree );

    m_treeViewer.setContentProvider( m_contentProvider );
    m_treeViewer.setLabelProvider( m_labelProvider );

    m_treeViewer.setCheckStateProvider( new ChartTreeCheckstateProvider() );

    m_treeViewer.addCheckStateListener( m_checkStateListener );

    m_treeViewer.addSelectionChangedListener( m_selectionChangeListener );

    m_model.getLayerManager().addListener( m_eventListener );

    addDropSupport();

    updateControl();
  }

  /**
   * @see org.eclipse.ui.part.IPage#dispose()
   */
  @Override
  public void dispose( )
  {
    m_model.getLayerManager().removeListener( m_eventListener );
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
  @Override
  public Control getControl( )
  {
    return m_treeViewer.getControl();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  @Override
  public ISelection getSelection( )
  {
    return m_treeViewer.getSelection();
  }

  public ISelectionChangedListener getSelectionChangeListener( )
  {
    return m_selectionChangeListener;
  }

  protected void handleCheckStateChanged( final IChartLayer layer, final boolean checked )
  {
    layer.setVisible( checked );
  }

  protected void handleSelectionChanged( final IChartLayer layer )
  {
    layer.setActive( true );

  }

  protected void refreshItems( final Object parent )
  {
    m_treeViewer.refresh( parent );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  @Override
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_treeViewer.removeSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
   */
  @Override
  public void setActionBars( final IActionBars actionBars )
  {
  }

  public void setCheckStateListener( final ICheckStateListener checkStateListener )
  {
    m_checkStateListener = checkStateListener;
  }

  public void setContentProvider( final ChartEditorTreeContentProvider contentProvider )
  {
    m_contentProvider = contentProvider;
  }

  /**
   * @see org.eclipse.ui.part.IPage#setFocus()
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
    m_treeViewer.setSelection( selection );
  }

  public void setSelectionChangeListener( final ISelectionChangedListener selectionChangeListener )
  {
    m_selectionChangeListener = selectionChangeListener;
  }

  public void updateControl( )
  {
    // TODO: works against eclipse best practice: we should replace the input instead of reusing it!
    if( m_treeViewer.getInput() == null )
      m_treeViewer.setInput( m_model );
    else
      m_treeViewer.refresh();
  }

  /**
   * Selects ands shows the given layer.<br>
   * If needed, the tree is expanded to show the element.
   */
  public void selectLayer( final IChartLayer layer )
  {
    m_treeViewer.setExpandedElements( new Object[] { layer } );
    m_treeViewer.expandToLevel( 2 );
    setSelection( new StructuredSelection( layer ) );
  }
}
