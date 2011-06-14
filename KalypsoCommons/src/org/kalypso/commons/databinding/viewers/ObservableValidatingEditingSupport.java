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
package org.kalypso.commons.databinding.viewers;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * Abstract implementation that add some validation support to {@link ObservableValidatingEditingSupport}.
 * 
 * @author Gernot Belger
 */
public abstract class ObservableValidatingEditingSupport extends ObservableValueEditingSupport
{
  private final CellEditor m_cellEditor;

  private final IValueProperty m_property;

  private final DataBindingContext m_dbc;

  private Object m_element;

  public ObservableValidatingEditingSupport( final ColumnViewer viewer, final DataBindingContext dbc, final IValueProperty property, final CellEditor cellEditor )
  {
    super( viewer, dbc );

    m_dbc = dbc;

    m_property = property;
    m_cellEditor = cellEditor;
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
   * @see org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport#doCreateElementObservable(java.lang.Object,
   *      org.eclipse.jface.viewers.ViewerCell)
   */
  @Override
  protected IObservableValue doCreateElementObservable( final Object element, final ViewerCell cell )
  {
    // REMARK: we keep a reference to the last element here; it will be used in the createBinding method which is called
    // soon after this one
    m_element = element;

    return m_property.observe( element );
  }

  /**
   * Overwritten in order to add a validator to the binding.
   */
  @Override
  protected Binding createBinding( final IObservableValue target, final IObservableValue model )
  {
    final UpdateValueStrategy targetToModel = new UpdateValueStrategy( UpdateValueStrategy.POLICY_CONVERT );

    final IValidator validator = createValidator( m_element );
    if( validator != null )
      targetToModel.setAfterConvertValidator( validator );

    return m_dbc.bindValue( target, model, targetToModel, null );
  }

  /**
   * Create a validator for the given element to be edited.<br/>
   * The default implementation returns <code>null</code>, overwrite to implement a validator.
   */
  protected IValidator createValidator( @SuppressWarnings("unused") final Object element )
  {
    return null;
  }

}
