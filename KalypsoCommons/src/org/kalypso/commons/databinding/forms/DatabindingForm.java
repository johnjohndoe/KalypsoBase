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
package org.kalypso.commons.databinding.forms;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.jface.databinding.swt.ISWTObservable;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.kalypso.commons.databinding.IDataBinder;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.conversion.StatusToMessageConverter;
import org.kalypso.commons.databinding.validation.MultiValidator;

/**
 * @author Gernot Belger
 */
public class DatabindingForm implements IDataBinding
{
  private final DataBindingContext m_bindingContext = new DataBindingContext();

  private final Form m_form;

  private final FormToolkit m_toolkit;

  private FormSupport m_support;

  public DatabindingForm( final ScrolledForm form, final FormToolkit toolkit )
  {
    this( form, form.getForm(), toolkit );
  }

  public DatabindingForm( final Form form, final FormToolkit toolkit )
  {
    this( null, form, toolkit );
  }

  private DatabindingForm( final ScrolledForm scrolledForm, final Form form, final FormToolkit toolkit )
  {
    m_form = form;
    m_toolkit = toolkit;

    form.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        dispose();
      }
    } );

    if( scrolledForm == null )
      m_support = FormSupport.create( form, m_bindingContext );
    else
      m_support = FormSupport.create( scrolledForm, m_bindingContext );
  }

  public Form getForm( )
  {
    return m_form;
  }

  @Override
  public void dispose( )
  {
    // REMARK: must be disposed before binding context is disposed, else we get problems
    m_support.dispose();

    m_bindingContext.dispose();
  }

  @Override
  public FormToolkit getToolkit( )
  {
    return m_toolkit;
  }

  @Override
  public DataBindingContext getBindingContext( )
  {
    return m_bindingContext;
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
    final UpdateValueStrategy targetToModel = new UpdateValueStrategy();
    final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();

    if( validators.length > 0 )
    {
      final IValidator validator = new MultiValidator( validators );
      targetToModel.setAfterConvertValidator( validator );
    }

    if( targetToModelConverter != null )
      targetToModel.setConverter( targetToModelConverter );

    if( modelToTargetConverter != null )
      modelToTarget.setConverter( modelToTargetConverter );

    final Binding binding = m_bindingContext.bindValue( targetValue, modelValue, targetToModel, modelToTarget );
    final IObservableValue validationStatus = binding.getValidationStatus();

    final Control control = findControl( targetValue );
    final IObservableValue decoratorValue = new FormControlDecoratorValue( m_form.getMessageManager() );

    final UpdateValueStrategy decoratorUpdater = new UpdateValueStrategy();
    decoratorUpdater.setConverter( new StatusToMessageConverter( control ) );

    m_bindingContext.bindValue( decoratorValue, validationStatus, null, decoratorUpdater );

    return binding;
  }

  @Override
  public Binding bindValue( final IDataBinder binder )
  {
    return binder.apply( m_bindingContext );
  }

  private Control findControl( final IObservableValue targetValue )
  {
    if( targetValue instanceof ISWTObservable )
    {
      final Widget widget = ((ISWTObservable)targetValue).getWidget();
      if( widget instanceof Control )
        return (Control)widget;
    }

    return null;
  }

  public void deactivate( )
  {
    m_support.setActive( false );
  }

  public void activate( )
  {
    m_support.setActive( true );
  }
}
