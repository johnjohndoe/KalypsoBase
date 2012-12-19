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

import java.util.Collection;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.conversion.IConverter;
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
public abstract class AbstractDataBinder implements IDataBinder
{
  private boolean m_alreadyApplied = false;

  private IConverter m_targetToModelConverter;

  private IConverter m_modelToTargetConverter;

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public final void setTargetToModelConverter( final IConverter converter )
  {
    checkApplied();

    m_targetToModelConverter = converter;
  }

  protected IConverter getTargetToModelConverter( )
  {
    return m_targetToModelConverter;
  }

  /**
   * May not be used after {@link #apply(DataBindingContext)} has been called.
   */
  public final void setModelToTargetConverter( final IConverter converter )
  {
    checkApplied();

    m_modelToTargetConverter = converter;
  }

  protected IConverter getModelToTargetConverter( )
  {
    return m_modelToTargetConverter;
  }

  protected final void checkApplied( )
  {
    Assert.isTrue( !m_alreadyApplied );
  }

  protected static IValidator asValidator( final Collection<IValidator> validators )
  {
    if( validators.isEmpty() )
      return null;

    return new MultiValidator( validators.toArray( new IValidator[validators.size()] ) );
  }

  /**
   * Create the binding and applies the settings of this helper.
   */
  @Override
  public final Binding apply( final DataBindingContext context )
  {
    m_alreadyApplied = true;

    return doApply( context );
  }

  protected abstract Binding doApply( DataBindingContext context );
}