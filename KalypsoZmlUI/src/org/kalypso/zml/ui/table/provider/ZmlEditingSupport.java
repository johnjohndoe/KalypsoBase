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
package org.kalypso.zml.ui.table.provider;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.swt.custom.ValidateCellEditorListener;
import org.kalypso.ogc.gml.table.celleditors.DefaultCellValidators;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.IZmlTableSelectionHandler;
import org.kalypso.zml.ui.table.model.IZmlTableCell;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;
import org.kalypso.zml.ui.table.provider.strategy.editing.IZmlEditingStrategy;

import com.google.common.base.Objects;

/**
 * @author Dirk Kuch
 */
public class ZmlEditingSupport extends EditingSupport
{
  public static final class ZmlTextCellEditor extends TextCellEditor
  {
    public ZmlTextCellEditor( final Composite parent, final int style )
    {
      super( parent, style );
    }

    public void stopEditing( )
    {
      if( isActivated() )
      {
        fireApplyEditorValue();
        deactivate();
      }
    }
  }

  private static final Color COLOR_ERROR = new Color( null, 255, 10, 10 );

  protected final ZmlTextCellEditor m_cellEditor;

  private String m_lastEdited;

  private final ExtendedZmlTableColumn m_column;

  private final ZmlLabelProvider m_labelProvider;

  private final IZmlTableSelectionHandler m_handler;

  public ZmlEditingSupport( final ExtendedZmlTableColumn column, final ZmlLabelProvider labelProvider, final IZmlTableSelectionHandler handler )
  {
    super( column.getTable().getTableViewer() );
    m_column = column;
    m_labelProvider = labelProvider;
    m_handler = handler;
    final TableViewer viewer = column.getTable().getTableViewer();

    m_cellEditor = new ZmlTextCellEditor( (Composite) viewer.getControl(), SWT.NONE );

    m_cellEditor.getControl().addTraverseListener( new TraverseListener()
    {
      @Override
      public void keyTraversed( final TraverseEvent e )
      {
        if( e.detail == SWT.TRAVERSE_TAB_PREVIOUS )
        {
          movePrevious();
          e.doit = false;
        }
        else if( e.detail == SWT.TRAVERSE_TAB_NEXT )
        {
          moveNext();
          e.doit = false;
        }
        else if( e.detail == SWT.TRAVERSE_ARROW_PREVIOUS && e.keyCode == SWT.ARROW_UP )
        {
          movePrevious();
          e.doit = false;
        }
        else if( e.detail == SWT.TRAVERSE_ARROW_NEXT && e.keyCode == SWT.ARROW_DOWN )
        {
          moveNext();
          e.doit = false;
        }

      }
    } );

    viewer.getControl().addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        m_cellEditor.dispose();
      }
    } );
  }

  protected void moveNext( )
  {
    final IZmlTableCell cell = m_handler.findActiveCellByPosition();
    if( org.kalypso.commons.java.lang.Objects.isNull( cell ) )
      return;

    final IZmlTableCell next = cell.findNextCell();
    update( next );
  }

  protected void movePrevious( )
  {
    final IZmlTableCell cell = m_handler.findActiveCellByPosition();
    if( org.kalypso.commons.java.lang.Objects.isNull( cell ) )
      return;

    final IZmlTableCell previous = cell.findPreviousCell();
    update( previous );
  }

  private void update( final IZmlTableCell cell )
  {
    m_cellEditor.stopEditing();

    final Object element = cell.getViewerCell().getElement();

    final StructuredSelection selection = (StructuredSelection) getViewer().getSelection();

    if( !selection.toList().contains( element ) )
      getViewer().setSelection( new StructuredSelection( element ), true );

    // Set the focus cell after the selection is updated because else
    // the cell is not scrolled into view
    // if (focusCellManager != null) {
    // focusCellManager.setFocusCell(focusCell);
    // }
  }

  private void setValidator( )
  {
    final IZmlModelColumn column = m_column.getModelColumn();
    if( column == null )
      return;

    final IAxis axis = column.getValueAxis();
    if( axis == null )
      return;

    final Class< ? > dataClass = axis.getDataClass();

    if( Double.class.equals( dataClass ) )
      m_cellEditor.setValidator( DefaultCellValidators.DOUBLE_VALIDATOR );
    else if( Integer.class.equals( dataClass ) )
      m_cellEditor.setValidator( DefaultCellValidators.INTEGER_VALIDATOR );
    else
      throw new NotImplementedException();

    m_cellEditor.addListener( new ValidateCellEditorListener( m_cellEditor, COLOR_ERROR ) );
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
   */
  @Override
  protected CellEditor getCellEditor( final Object element )
  {
    return m_cellEditor;
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
   */
  @Override
  protected boolean canEdit( final Object element )
  {
    return true;
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
   */
  @Override
  protected Object getValue( final Object element )
  {
    setValidator();

    if( element instanceof IZmlModelRow )
    {
      final IZmlEditingStrategy strategy = m_column.getEditingStrategy( m_labelProvider );
      m_lastEdited = strategy.getValue( (IZmlModelRow) element );
    }
    else
      m_lastEdited = null;

    return m_lastEdited;
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
   */
  @Override
  protected void setValue( final Object element, final Object value )
  {
    if( Objects.equal( m_lastEdited, value ) )
      return;

    if( element instanceof IZmlModelRow && value instanceof String )
    {
      final IZmlEditingStrategy strategy = m_column.getEditingStrategy( m_labelProvider );
      strategy.setValue( (IZmlModelRow) element, (String) value );
    }
  }

  public TextCellEditor getCellEditor( )
  {
    return m_cellEditor;
  }

}
