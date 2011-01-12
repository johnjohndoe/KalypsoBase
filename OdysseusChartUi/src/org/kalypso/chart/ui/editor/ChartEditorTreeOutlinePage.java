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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.chart.ui.editor.dnd.ChartLayerTransfer;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;

/**
 * @author alibu
 * @author kimwerner
 */
public class ChartEditorTreeOutlinePage implements IContentOutlinePage
{
  protected ICheckStateListener m_checkStateListener = null;

  protected ChartEditorTreeContentProvider m_contentProvider;

  protected ICheckStateProvider m_checkStateProvider = null;

  protected final ChartTreeLabelProvider m_labelProvider;

  private final ILayerManagerEventListener m_eventListener;

  protected ISelectionChangedListener m_selectionChangeListener;

  protected CheckboxTreeViewer m_treeViewer;

  protected IChartModel m_model = null;

  private final class InvalidateOutlineJob extends UIJob
  {
    public InvalidateOutlineJob( final String name )
    {
      super( name );
    }

    /**
     * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      if( notDisposed() )
        m_treeViewer.refresh();
      return Status.OK_STATUS;
    }
  }

  private final InvalidateOutlineJob m_invalidateOutlineJob = new InvalidateOutlineJob( "" );

  public ChartEditorTreeOutlinePage( )
  {
    this( new ChartEditorTreeContentProvider(), new ChartTreeLabelProvider() );
  }

  public ChartEditorTreeOutlinePage( final ChartEditorTreeContentProvider contentProvider, final ChartTreeLabelProvider labelProvider )
  {
    m_contentProvider = contentProvider;
    m_labelProvider = labelProvider;
    m_eventListener = new AbstractLayerManagerEventListener()
    {
      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerVisibilityChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerVisibilityChanged( final IChartLayer layer )
      {

        refreshTreeViewer();
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerAdded(de.openali.
       *      odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerAdded( final IChartLayer layer )
      {
        refreshTreeViewer();
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerContentChanged(de
       *      .openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerContentChanged( final IChartLayer layer )
      {
        refreshTreeViewer();
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerMoved(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerMoved( final IChartLayer layer )
      {
        refreshTreeViewer();
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerRemoved(de.openali
       *      .odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerRemoved( final IChartLayer layer )
      {
        refreshTreeViewer();
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
      }
    };
    m_selectionChangeListener = new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final ISelection selection = event.getSelection();
        final ITreeSelection struct = (ITreeSelection) selection;
        final TreePath path = struct.size() == 0 ? null : struct.getPaths()[0];
        final Object element = path == null ? null : path.getFirstSegment();
        if( element != null && element instanceof IChartLayer )
          ((IChartLayer) element).setActive( true );
      }
    };
    m_checkStateProvider = new ICheckStateProvider()
    {
      @Override
      public boolean isChecked( final Object element )
      {
        if( element instanceof IChartLayer )
        {
          return ((IChartLayer) element).isVisible();
        }
        return false;
      }

      @Override
      public boolean isGrayed( final Object element )
      {
        // TODO Auto-generated method stub
        return false;
      }
    };

  }

  protected final void refreshTreeViewer( )
  {
    m_invalidateOutlineJob.cancel();
    m_invalidateOutlineJob.schedule( 100 );
  }

  public void setCheckStateProvider( final ICheckStateProvider checkStateProvider )
  {
    if( m_checkStateProvider != null )
    {
      m_checkStateProvider = null;
    }
    m_treeViewer.setCheckStateProvider( checkStateProvider );
  }

  public IChartModel getModel( )
  {
    return m_model;
  }

  protected boolean notDisposed( )
  {
    return m_treeViewer != null && m_treeViewer.getControl() != null && !m_treeViewer.getControl().isDisposed();
  }

  private void setInput( final IChartModel model )
  {
    if( notDisposed() )
      m_treeViewer.setInput( model );
  }

  public void setModel( final IChartModel model )
  {
    removeListener();
    m_model = model;
    addListener();
    setInput( model );
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
            layerManager = parent instanceof IChartLayer ? ((IChartLayer) parent).getLayerManager() : null;
            draggedLayer = layerManager == null ? null : layerManager.getLayerById( id );
          }
          if( draggedLayer == null )
            return false;
          // either the dragged layer is dropped on another layer, then it will be
          // moved to its position

          if( targetLayer != null && targetLayer instanceof IChartLayer )
          {
            final int layerPosition = layerManager.getLayerPosition( (IChartLayer) targetLayer );
            if( layerPosition != -1 )
              layerManager.moveLayerToPosition( draggedLayer, layerPosition );
          }
          else
            layerManager.moveLayerToPosition( draggedLayer, 0 );

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
    if( m_selectionChangeListener != null )
    {
      m_treeViewer.removeSelectionChangedListener( m_selectionChangeListener );
      m_selectionChangeListener = null;
    }
    m_treeViewer.addSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    final Tree tree = new Tree( parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL );

    // BAD: we do not know the layout, so this may cause trouble!
    if( parent.getLayout() instanceof GridLayout )
      tree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    m_treeViewer = new CheckboxTreeViewer( tree );
    m_treeViewer.setContentProvider( m_contentProvider );
    m_treeViewer.setLabelProvider( m_labelProvider );
    m_treeViewer.addSelectionChangedListener( m_selectionChangeListener );
    m_treeViewer.addCheckStateListener( m_checkStateListener );
    m_treeViewer.setCheckStateProvider( m_checkStateProvider );

    addDropSupport();

    updateControl();
  }

  private final void addListener( )
  {
    final ILayerManager mngr = m_model == null ? null : m_model.getLayerManager();
    if( mngr != null )
      mngr.addListener( m_eventListener );
  }

  private final void removeListener( )
  {
    final ILayerManager mngr = m_model == null ? null : m_model.getLayerManager();
    if( mngr != null )
      mngr.removeListener( m_eventListener );
  }

  /**
   * @see org.eclipse.ui.part.IPage#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_contentProvider != null )
      m_contentProvider.dispose();

    removeListener();
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

  protected void handleCheckStateChanged( final IChartLayer layer, final boolean checked )
  {
    layer.setVisible( checked );
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
    if( m_checkStateListener != null )
    {
      m_treeViewer.removeCheckStateListener( m_checkStateListener );
      m_checkStateListener = null;
    }
    m_treeViewer.addCheckStateListener( checkStateListener );
  }

  public void setContentProvider( final ChartEditorTreeContentProvider contentProvider )
  {
    if( m_contentProvider != null )
    {
      m_contentProvider.dispose();
      m_contentProvider = null;
    }
    m_treeViewer.setContentProvider( contentProvider );
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
    if( notDisposed() )
      m_treeViewer.setSelection( selection );
  }

  public void updateControl( )
  {
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
    // m_treeViewer.setExpandedElements( new Object[] { layer } );
    setSelection( new StructuredSelection( layer ) );
  }
}
