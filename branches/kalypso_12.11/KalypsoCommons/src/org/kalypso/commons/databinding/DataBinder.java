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
package org.kalypso.commons.databinding;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;

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
public class DataBinder extends AbstractDataBinder
{
  private final Collection<IValidator> m_targetAfterGetValidator = new ArrayList<>( 1 );

  private final Collection<IValidator> m_targetAfterConvertValidator = new ArrayList<>( 1 );

  private final Collection<IValidator> m_targetBeforeSetValidator = new ArrayList<>( 1 );

  private final Collection<IValidator> m_modelAfterGetValidator = new ArrayList<>( 1 );

  private final Collection<IValidator> m_modelAfterConvertValidator = new ArrayList<>( 1 );

  private final Collection<IValidator> m_modelBeforeSetValidator = new ArrayList<>( 1 );

  private final IObservableValue m_target;

  private final IObservableValue m_model;

  public DataBinder( final IObservableValue target, final IObservableValue model )
  {
    m_target = target;
    m_model = model;
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public final void addTargetAfterGetValidator( final IValidator validator )
  {
    checkApplied();

    if( validator != null )
      m_targetAfterGetValidator.add( validator );
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public final void addTargetAfterConvertValidator( final IValidator validator )
  {
    checkApplied();

    if( validator != null )
      m_targetAfterConvertValidator.add( validator );
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public final void addTargetBeforeSetValidator( final IValidator validator )
  {
    checkApplied();

    if( validator != null )
      m_targetBeforeSetValidator.add( validator );
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public final void addModelAfterGetValidator( final IValidator validator )
  {
    checkApplied();

    if( validator != null )
      m_modelAfterGetValidator.add( validator );
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public final void addModelAfterConvertValidator( final IValidator validator )
  {
    checkApplied();

    if( validator != null )
      m_modelAfterConvertValidator.add( validator );
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public final void addModelBeforeSetValidator( final IValidator validator )
  {
    checkApplied();

    if( validator != null )
      m_modelBeforeSetValidator.add( validator );
  }

  @Override
  protected Binding doApply( final DataBindingContext context )
  {
    final UpdateValueStrategy targetToModel = new UpdateValueStrategy();
    final IConverter targetToModelConverter = getTargetToModelConverter();
    if( targetToModelConverter != null )
      targetToModel.setConverter( targetToModelConverter );

    targetToModel.setAfterGetValidator( asValidator( m_targetAfterGetValidator ) );
    targetToModel.setAfterConvertValidator( asValidator( m_targetAfterConvertValidator ) );
    targetToModel.setBeforeSetValidator( asValidator( m_targetBeforeSetValidator ) );

    final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
    final IConverter modelToTargetConverter = getModelToTargetConverter();
    if( modelToTargetConverter != null )
      modelToTarget.setConverter( modelToTargetConverter );

    modelToTarget.setAfterGetValidator( asValidator( m_modelAfterGetValidator ) );
    modelToTarget.setAfterConvertValidator( asValidator( m_modelAfterConvertValidator ) );
    modelToTarget.setBeforeSetValidator( asValidator( m_modelBeforeSetValidator ) );

    return context.bindValue( m_target, m_model, targetToModel, modelToTarget );
  }
}