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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.swt.custom.ValidateCellEditorListener;
import org.kalypso.ogc.gml.table.celleditors.DefaultCellValidators;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;
import org.kalypso.zml.ui.table.provider.strategy.editing.IZmlEditingStrategy;

import com.google.common.base.Objects;

/**
 * @author Dirk Kuch
 */
public class ZmlEditingSupport extends EditingSupport
{
  private static final Color COLOR_ERROR = new Color( null, 255, 10, 10 );

  protected final TextCellEditor m_cellEditor;

  private String m_lastEdited;

  private final ExtendedZmlTableColumn m_column;

  private final ZmlLabelProvider m_labelProvider;

  public ZmlEditingSupport( final ExtendedZmlTableColumn column, final ZmlLabelProvider labelProvider )
  {
    super( column.getTable().getTableViewer() );
    m_column = column;
    m_labelProvider = labelProvider;
    final TableViewer viewer = column.getTable().getTableViewer();

    m_cellEditor = new TextCellEditor( (Composite) viewer.getControl(), SWT.NONE );

    viewer.getControl().addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        m_cellEditor.dispose();
      }
    } );
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

}
