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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.chart.ui.editor.dnd.ChartLayerTransfer;

import com.google.common.base.Objects;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;

/**
 * @author alibu
 * @author kimwerner
 */
public class ChartEditorTreeOutlinePage extends Page implements IContentOutlinePage
{
  /** Constant for empty layer selection */
  private static final String SELECTION_NONE = "<none>"; //$NON-NLS-1$

  protected ICheckStateListener m_checkStateListener = null;

  protected ITreeContentProvider m_contentProvider;

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

    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      if( !isDisposed() )
        m_treeViewer.refresh();
      return Status.OK_STATUS;
    }
  }

  private final InvalidateOutlineJob m_invalidateOutlineJob = new InvalidateOutlineJob( "" ); //$NON-NLS-1$

  public ChartEditorTreeOutlinePage( )
  {
    this( new ChartEditorTreeContentProvider(), new ChartTreeLabelProvider() );
  }

  public ChartEditorTreeOutlinePage( final ITreeContentProvider contentProvider, final ITableLabelProvider labelProvider )
  {
    m_contentProvider = contentProvider;
    // FIXME: only as hot fix; we know that we have a ChartTreeLabelProvider; but feature patch does not work like this
    // -> change constructor later
    m_labelProvider = (ChartTreeLabelProvider)labelProvider;
    m_eventListener = new AbstractLayerManagerEventListener()
    {
      @Override
      public void onLayerVisibilityChanged( final IChartLayer layer )
      {
        updateLayer( layer );
      }

      @Override
      public void onLayerAdded( final IChartLayer layer )
      {
        refreshTreeViewer();
      }

      @Override
      public void onLayerContentChanged( final IChartLayer layer, final ContentChangeType type )
      {
        updateLayer( layer );
      }

      @Override
      public void onLayerMoved( final IChartLayer layer )
      {
        refreshTreeViewer();
      }

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
          ((IChartLayer)elt).setVisible( event.getChecked() );
        }
      }
    };
    m_selectionChangeListener = new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final ISelection selection = event.getSelection();
        final ITreeSelection struct = (ITreeSelection)selection;
        final TreePath path = struct.size() == 0 ? null : struct.getPaths()[0];
        final Object element = path == null ? null : path.getFirstSegment();
        if( element != null && element instanceof IChartLayer )
          ((IChartLayer)element).setActive( true );
      }
    };
    m_checkStateProvider = new ICheckStateProvider()
    {
      @Override
      public boolean isChecked( final Object element )
      {
        if( element instanceof IChartLayer )
        {
          return ((IChartLayer)element).isVisible();
        }
        return false;
      }

      @Override
      public boolean isGrayed( final Object element )
      {
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

  protected boolean isDisposed( )
  {
    if( m_treeViewer == null )
      return true;

    final Control control = m_treeViewer.getControl();
    if( control == null )
      return true;

    return control.isDisposed();
  }

  private void setInput( final IChartModel model )
  {
    if( isDisposed() )
      return;

    {
      /* Remember type of selected layer */
      final IChartModel oldModel = (IChartModel)m_treeViewer.getInput();
      final String currentSelection = findSelectedLayerId( oldModel );

      // BUGFIX: clear images on every model change; else the legend images will accumulate forever, because
      // - every model gets new images
      // - the legend is normally never closed, so the label provider never disposed.
      m_labelProvider.clearImages();

      m_treeViewer.setInput( model );

      final IChartLayer selectedLayer = findSelectedLayer( model, currentSelection );
      if( selectedLayer != null )
        m_treeViewer.setSelection( new StructuredSelection( selectedLayer ) );
    }
  }

  private IChartLayer findSelectedLayer( final IChartModel model, final String layerId )
  {
    if( model == null )
      return null;

    final IChartLayer[] layers = model.getLayerManager().getLayers();
    for( final IChartLayer layer : layers )
    {
      final String identifier = layer.getIdentifier();
      if( Objects.equal( layerId, identifier ) )
        return layer;
    }

    if( layers.length > 0 )
      return layers[0];

    return null;
  }

  private String findSelectedLayerId( final IChartModel model )
  {
    if( model == null )
      return SELECTION_NONE;

    final IStructuredSelection selection = (IStructuredSelection)m_treeViewer.getSelection();
    final Object firstElement = selection.getFirstElement();
    if( firstElement instanceof IChartLayer )
      return ((ILayerContainer)firstElement).getIdentifier();

    return SELECTION_NONE;
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
        final IStructuredSelection selection = (IStructuredSelection)m_treeViewer.getSelection();

        final List< ? > list = selection.toList();
        // only one can be selected
        final Object elt = list.get( 0 );
        // only layers can be dragged
        if( elt instanceof IChartLayer )
        {
          final IChartLayer layer = (IChartLayer)elt;
          event.data = layer.getIdentifier();
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
          final String id = (String)data;
          ILayerManager layerManager = m_model.getLayerManager();

          final Object targetLayer = getCurrentTarget();
          Object parent = null;
          // find dragged
          IChartLayer draggedLayer = layerManager.findLayer( id );
          if( draggedLayer == null )
          {
            parent = m_contentProvider.getParent( targetLayer );
            layerManager = parent instanceof IChartLayer ? ((IChartLayer)parent).getLayerManager() : null;
            draggedLayer = layerManager == null ? null : layerManager.findLayer( id );
          }
          if( draggedLayer == null )
            return false;
          // either the dragged layer is dropped on another layer, then it will be
          // moved to its position

          if( targetLayer != null && targetLayer instanceof IChartLayer )
          {
            final int layerPosition = layerManager.getLayerPosition( (IChartLayer)targetLayer );
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

  @Override
  public void createControl( final Composite parent )
  {
    final Tree tree = new Tree( parent, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION );

    m_treeViewer = new CheckboxTreeViewer( tree );
    m_treeViewer.setContentProvider( m_contentProvider );
    m_treeViewer.setLabelProvider( m_labelProvider );
    m_treeViewer.addSelectionChangedListener( m_selectionChangeListener );
    m_treeViewer.addCheckStateListener( m_checkStateListener );
    m_treeViewer.setCheckStateProvider( m_checkStateProvider );

    addDropSupport();

    updateControl();
  }

  public CheckboxTreeViewer getViewer( )
  {
    return m_treeViewer;
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

  @Override
  public void dispose( )
  {
    if( m_contentProvider != null )
      m_contentProvider.dispose();

    removeListener();

    super.dispose();
  }

  @Override
  public Control getControl( )
  {
    return m_treeViewer.getControl();
  }

  @Override
  public ISelection getSelection( )
  {
    return m_treeViewer.getSelection();
  }

  protected void handleCheckStateChanged( final IChartLayer layer, final boolean checked )
  {
    layer.setVisible( checked );
  }

  @Override
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_treeViewer.removeSelectionChangedListener( listener );
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

  @Override
  public void setFocus( )
  {
    m_treeViewer.getControl().setFocus();
  }

  @Override
  public void setSelection( final ISelection selection )
  {
    if( isDisposed() )
      return;

    m_treeViewer.setSelection( selection );
  }

  public void updateControl( )
  {
    if( m_treeViewer.getInput() == null )
      setInput( m_model );
    else
      m_treeViewer.refresh();
  }

  /**
   * Selects ands shows the given layer.<br>
   * If needed, the tree is expanded to show the element.
   */
  public void selectLayer( final IChartLayer layer )
  {
    setSelection( new StructuredSelection( layer ) );
  }

  public void saveSettings( final IDialogSettings settings )
  {
    final IChartModel model = getModel();
    if( model == null )
      return;

    if( m_treeViewer == null )
      return;

    final SaveChartLegendStateVisitor visitor = new SaveChartLegendStateVisitor( settings, m_treeViewer );
    model.accept( visitor );
  }

  public void restoreSettings( final IDialogSettings settings )
  {
    final IChartModel model = getModel();
    if( model == null )
      return;

    if( m_treeViewer == null )
      return;

    m_treeViewer.getTree().setRedraw( false );

    final RestoreChartLegendStateVisitor visitor = new RestoreChartLegendStateVisitor( settings, m_treeViewer );
    model.accept( visitor );

    visitor.applyState();

    m_treeViewer.getTree().setRedraw( true );
  }

  protected void updateLayer( final IChartLayer layer )
  {
    final String jobName = String.format( "Update outline: %s", layer.getTitle() ); //$NON-NLS-1$

    final CheckboxTreeViewer treeViewer = m_treeViewer;
    if( treeViewer == null )
      return;

    final UIJob updateJob = new UIJob( jobName )
    {

      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        if( !treeViewer.getControl().isDisposed() )
          treeViewer.update( layer, null );
        return Status.OK_STATUS;
      }
    };

    updateJob.setUser( false );
    updateJob.setSystem( true );

    updateJob.schedule( 50 );
  }
}