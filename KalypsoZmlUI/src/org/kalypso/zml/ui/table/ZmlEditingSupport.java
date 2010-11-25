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
package org.kalypso.zml.ui.table;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.binding.BaseColumn;
import org.kalypso.zml.ui.table.provider.IZmlValueReference;
import org.kalypso.zml.ui.table.provider.ZmlTableRow;

/**
 * @author Dirk Kuch
 */
public class ZmlEditingSupport extends EditingSupport
{
  protected final TextCellEditor m_cellEditor;

  private final BaseColumn m_type;

  public ZmlEditingSupport( final BaseColumn type, final TableViewerColumn viewer )
  {
    super( viewer.getViewer() );

    m_type = type;
    m_cellEditor = new TextCellEditor( (Composite) viewer.getViewer().getControl(), SWT.NONE );

    viewer.getViewer().getControl().addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        m_cellEditor.dispose();
      }
    } );
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
    if( element instanceof ZmlTableRow )
    {
      try
      {
        final ZmlTableRow row = (ZmlTableRow) element;

        final IZmlValueReference reference = row.get( m_type.getType() );
        if( reference == null )
          return "";

        final Object value = reference.getValue();
        final String format = m_type.getFormat();

        return String.format( format == null ? "%s" : format, value );
      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
   */
  @Override
  protected void setValue( final Object element, final Object value )
  {
    if( element instanceof ZmlTableRow && value instanceof String )
    {
      try
      {
        final ZmlTableRow row = (ZmlTableRow) element;
        final IZmlValueReference reference = row.get( m_type.getType() );

        final Object targetValue = getTargetValue( reference, value );
        if( !targetValue.equals( reference.getValue() ) )
          reference.update( targetValue );
      }
      catch( final SensorException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }
    else
      throw new NotImplementedException();
  }

  private Object getTargetValue( final IZmlValueReference reference, final Object value )
  {
    Assert.isTrue( value instanceof String );

    final IAxis axis = reference.getAxis();
    final Class< ? > clazz = axis.getDataClass();

    if( Double.class == clazz )
    {
      return NumberUtils.parseDouble( (String) value );
    }
    else
      throw new NotImplementedException();
  }
}
