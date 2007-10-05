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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManager;
import org.kalypso.chart.ui.editor.dnd.ChartLayerTransfer;

/**
 * @author Gernot Belger
 * @author alibu
 */
public class ChartEditorOutlinePage implements IContentOutlinePage
{
  CheckboxTableViewer m_viewer;

  private final ChartEditor m_editor;

  public ChartEditorOutlinePage( final ChartEditor editor )
  {
    m_editor = editor;
  }

  /**
   * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    final Table table = new Table( parent, SWT.CHECK );
    final TableColumn column = new TableColumn( table, SWT.NONE );
    column.setWidth( table.getSize().x );

    table.addControlListener( new ControlAdapter()
    {
      /**
       * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
       */
      @Override
      public void controlResized( final ControlEvent e )
      {
        column.setWidth( table.getSize().x );
      }
    } );

    m_viewer = new CheckboxTableViewer( table );

    m_viewer.setContentProvider( new ArrayContentProvider() );
    m_viewer.setLabelProvider( new ChartLabelProvider( m_editor ) );

    m_viewer.addCheckStateListener( new ICheckStateListener()
    {
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        handleCheckStateChanged( (IChartLayer) event.getElement(), event.getChecked() );
      }
    } );

    updateControl();

    // Drag'n drop
    // add drag and drop support
    int ops = DND.DROP_MOVE;

    DragSourceListener dragSL = new DragSourceListener()
    {

      public void dragFinished( DragSourceEvent event )
      {
        // TODO Auto-generated method stub
        if( event.detail == DND.DROP_MOVE )
        {
          System.out.println( "moved" );
        }
        else
        {
          System.out.println( event.detail );
        }

        System.out.println( "drag finished" );
      }

      public void dragSetData( DragSourceEvent event )
      {
        IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
        IChartLayer[] layers = (IChartLayer[]) selection.toList().toArray( new IChartLayer[selection.size()] );
        // da sollte jetzt nur eins drin sein
        IChartLayer layer = layers[0];
        event.data = layer.getId();

      }

      public void dragStart( DragSourceEvent event )
      {
        System.out.println( "drag started" );
      }

    };

    ViewerDropAdapter vda = new ViewerDropAdapter( m_viewer )
    {

      @Override
      public boolean performDrop( Object data )
      {
        String id = (String) data;
        // ILayerManager lm = m_editor.getChartComposite().getModel().getLayerManager();
        System.out.println( "Dropped Layer with id " + id );
        IChartLayer selectedObject = (IChartLayer) getCurrentTarget();
        if( selectedObject != null )
        {

          return true;
        }
        else
          return false;

      }

      @Override
      public boolean validateDrop( Object target, int operation, TransferData transferType )
      {
        return ChartLayerTransfer.getInstance().isSupportedType( transferType );
      }

    };

    m_viewer.addDragSupport( ops, new Transfer[] { ChartLayerTransfer.getInstance() }, dragSL );
    m_viewer.addDropSupport( ops, new Transfer[] { ChartLayerTransfer.getInstance() }, vda );

  }

  protected void handleCheckStateChanged( final IChartLayer layer, final boolean checked )
  {
    m_editor.getChartComposite().getModel().getLayerManager().setLayerVisibility( layer, checked );
  }

  public void dispose( )
  {
  }

  /**
   * @see org.eclipse.ui.part.IPage#getControl()
   */
  public Control getControl( )
  {
    return m_viewer.getControl();
  }

  /**
   * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
   */
  public void setActionBars( final IActionBars actionBars )
  {

  }

  /**
   * @see org.eclipse.ui.part.IPage#setFocus()
   */
  public void setFocus( )
  {
    m_viewer.getControl().setFocus();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void addSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_viewer.addSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection( )
  {
    return m_viewer.getSelection();
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_viewer.removeSelectionChangedListener( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  public void setSelection( final ISelection selection )
  {
    m_viewer.setSelection( selection );
  }

  /* default-visible */void updateControl( )
  {
    ILayerManager layerManager = m_editor.getChartComposite().getModel().getLayerManager();
    IChartLayer[] layers = layerManager.getLayers();

    // Reihenfolge umkehren, damit zuerst gezeichnete Layer auch in der Liste weiter unten liegen
    IChartLayer[] revertedLayers = new IChartLayer[layers.length];
    for( int i = 0; i < layers.length; i++ )
    {
      revertedLayers[i] = layers[layers.length - 1 - i];
    }

    m_viewer.setInput( revertedLayers );
    for( int i = 0; i < layers.length; i++ )
      m_viewer.setChecked( layers[i], layers[i].isVisible() );
  }

}
