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

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateSetStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.set.IObservableSet;

/**
 * Helper that wraps all information needed for a data binding of
 * {@link org.eclipse.core.databinding.observable.set.IObservableSet}s:
 * <ul>
 * <li>model and target values</li>
 * <li>converters</li>
 * <li>validators</li>
 * </ul>
 * 
 * @author Gernot Belger
 */
public class DataSetBinder extends AbstractDataBinder
{
  private final IObservableSet m_target;

  private final IObservableSet m_model;

  public DataSetBinder( final IObservableSet target, final IObservableSet model )
  {
    m_target = target;
    m_model = model;
  }

  @Override
  protected Binding doApply( final DataBindingContext context )
  {
    final UpdateSetStrategy targetToModel = new UpdateSetStrategy();
    final IConverter targetToModelConverter = getTargetToModelConverter();
    if( targetToModelConverter != null )
      targetToModel.setConverter( targetToModelConverter );

    final UpdateSetStrategy modelToTarget = new UpdateSetStrategy();

    final IConverter modelToTargetConverter = getModelToTargetConverter();
    if( modelToTargetConverter != null )
      modelToTarget.setConverter( modelToTargetConverter );

    return context.bindSet( m_target, m_model, targetToModel, modelToTarget );
  }
}