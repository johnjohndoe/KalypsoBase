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
package org.kalypso.ui.editor.styleeditor.binding;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.Assert;
import org.kalypso.commons.databinding.validation.MultiValidator;

/**
 * Helper that wraps all information needed for a data binding:
 * <ul>
 * <li>model and target values</li>
 * <li>converters</li>
 * <li>validators</li>
 * </ul>
 * 
 * @author Gernot Belger
 */
public class DataBinder
{
  private final Collection<IValidator> m_targetAfterGetValidator = new ArrayList<IValidator>( 1 );

  private final Collection<IValidator> m_targetAfterConvertValidator = new ArrayList<IValidator>( 1 );

  private final Collection<IValidator> m_targetBeforeSetValidator = new ArrayList<IValidator>( 1 );

  private final Collection<IValidator> m_modelAfterGetValidator = new ArrayList<IValidator>( 1 );

  private final Collection<IValidator> m_modelAfterConvertValidator = new ArrayList<IValidator>( 1 );

  private final Collection<IValidator> m_modelBeforeSetValidator = new ArrayList<IValidator>( 1 );

  private boolean m_alreadyApplied = false;

  private final IObservableValue m_target;

  private final IObservableValue m_model;

  private IConverter m_targetToModelConverter;

  private IConverter m_modelToTargetConverter;

  public DataBinder( final IObservableValue target, final IObservableValue model )
  {
    m_target = target;
    m_model = model;
  }

  private void checkApplied( )
  {
    Assert.isTrue( !m_alreadyApplied );
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public void setTargetToModelConverter( final IConverter converter )
  {
    checkApplied();

    m_targetToModelConverter = converter;
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public void setModelToTargetConverter( final IConverter converter )
  {
    checkApplied();

    m_modelToTargetConverter = converter;
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public void addTargetAfterGetValidator( final IValidator validator )
  {
    checkApplied();

    m_targetAfterGetValidator.add( validator );
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public void addTargetAfterConvertValidator( final IValidator validator )
  {
    checkApplied();

    m_targetAfterConvertValidator.add( validator );
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public void addTargetBeforeSetValidator( final IValidator validator )
  {
    checkApplied();

    m_targetBeforeSetValidator.add( validator );
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public void addModelAfterGetValidator( final IValidator validator )
  {
    checkApplied();

    m_modelAfterGetValidator.add( validator );
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public void addModelAfterConvertValidator( final IValidator validator )
  {
    checkApplied();

    m_modelAfterConvertValidator.add( validator );
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public void addModelBeforeSetValidator( final IValidator validator )
  {
    checkApplied();

    m_modelBeforeSetValidator.add( validator );
  }

  /**
   * Create the binding and applies the settings of this helper.
   */
  public Binding apply( final DataBindingContext context )
  {
    m_alreadyApplied = true;

    final UpdateValueStrategy targetToModel = new UpdateValueStrategy();
    if( m_targetToModelConverter != null )
      targetToModel.setConverter( m_targetToModelConverter );

    targetToModel.setAfterGetValidator( asValidator( m_targetAfterGetValidator ) );
    targetToModel.setAfterConvertValidator( asValidator( m_targetAfterConvertValidator ) );
    targetToModel.setBeforeSetValidator( asValidator( m_targetBeforeSetValidator ) );

    final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
    if( m_modelToTargetConverter != null )
      modelToTarget.setConverter( m_modelToTargetConverter );

    modelToTarget.setAfterGetValidator( asValidator( m_modelAfterGetValidator ) );
    modelToTarget.setAfterConvertValidator( asValidator( m_modelAfterConvertValidator ) );
    modelToTarget.setBeforeSetValidator( asValidator( m_modelBeforeSetValidator ) );

    return context.bindValue( m_target, m_model, targetToModel, modelToTarget );
  }

  private IValidator asValidator( final Collection<IValidator> validators )
  {
    if( validators.isEmpty() )
      return null;

    return new MultiValidator( validators.toArray( new IValidator[validators.size()] ) );
  }
}
