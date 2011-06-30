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
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractDatabinding implements IDataBinding
{
  private final DataBindingContext m_bindingContext = new DataBindingContext();

  private final FormToolkit m_toolkit;

  public AbstractDatabinding( final FormToolkit toolkit )
  {
    m_toolkit = toolkit;
  }

  @Override
  public DataBindingContext getBindingContext( )
  {
    return m_bindingContext;
  }

  @Override
  public FormToolkit getToolkit( )
  {
    return m_toolkit;
  }

  @Override
  public void dispose( )
  {
    m_bindingContext.dispose();
  }

  @Override
  public Binding bindValue( final IObservableValue targetValue, final IObservableValue modelValue, final IValidator... validators )
  {
    return bindValue( targetValue, modelValue, null, validators );
  }

  @Override
  public Binding bindValue( final IObservableValue targetValue, final IObservableValue modelValue, final IConverter converter, final IValidator... validators )
  {
    return bindValue( targetValue, modelValue, converter, null, validators );
  }

  @Override
  public Binding bindValue( final IObservableValue targetValue, final IObservableValue modelValue, final IConverter targetToModelConverter, final IConverter modelToTargetConverter, final IValidator... validators )
  {
    final DataBinder binder = new DataBinder( targetValue, modelValue );

    binder.setTargetToModelConverter( targetToModelConverter );
    binder.setModelToTargetConverter( modelToTargetConverter );

    for( final IValidator validator : validators )
      binder.addTargetAfterConvertValidator( validator );

    return bindValue( binder );
  }

  @Override
  public Binding bindValue( final IDataBinder binder )
  {
    return binder.apply( m_bindingContext );
  }
}