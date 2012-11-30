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
package org.kalypso.gml.ui.internal.feature.editProperties;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.databinding.DataBinder;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.commons.databinding.validation.NotNullValidator;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.filterdialog.model.FeatureTypeLabelProvider;

/**
 * @author Gernot Belger
 */
public class EditFeaturePropertiesPage extends WizardPage
{
  private final UIJob m_layoutJob = new UIJob( "Layout" ) //$NON-NLS-1$
  {
    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      doLayout();
      return Status.OK_STATUS;
    }
  };

  private final EditFeaturePropertiesData m_data;

  private DatabindingWizardPage m_binding;

  protected EditFeaturePropertiesPage( final String pageName, final EditFeaturePropertiesData data )
  {
    super( pageName );

    m_data = data;

    setTitle( Messages.getString( "org.kalypso.ui.editor.actions.BatchEditParametersInputDialog.0" ) ); //$NON-NLS-1$
    setDescription( Messages.getString( "org.kalypso.ui.editor.actions.FeatureBatchEditActionDelegate.1" ) );//$NON-NLS-1$
  }

  @Override
  public void createControl( final Composite parent )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    setControl( panel );
    GridLayoutFactory.swtDefaults().numColumns( 2 ).applyTo( panel );

    m_binding = new DatabindingWizardPage( this, null );

    // TODO: let user choose between selected and all features of the list

    createPropertyChooser( panel );
    createValueControl( panel );
    createRadioGroup( panel );

    final IObservableValue labelValue = BeansObservables.observeValue( m_data, EditFeaturePropertiesData.PROPERTY_VALUE_LABEL );
    labelValue.addValueChangeListener( new IValueChangeListener()
    {
      @Override
      public void handleValueChange( final ValueChangeEvent event )
      {
        relayout();
      }
    } );
  }

  private void createPropertyChooser( final Composite parent )
  {
    final Label label = new Label( parent, SWT.NONE );
    label.setText( Messages.getString( "EditFeaturePropertiesPage.0" ) ); //$NON-NLS-1$

    final ComboViewer comboViewer = new ComboViewer( parent, SWT.READ_ONLY | SWT.DROP_DOWN );
    comboViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    comboViewer.setContentProvider( new ArrayContentProvider() );
    comboViewer.setLabelProvider( new FeatureTypeLabelProvider() );
    comboViewer.addFilter( new EditFeaturePropertiesFilter() );

    final IObservableValue targetInput = ViewersObservables.observeInput( comboViewer );
    final IObservableValue modelInput = BeansObservables.observeValue( m_data, EditFeaturePropertiesData.PROPERTY_PROPERTIES );
    m_binding.bindValue( targetInput, modelInput );

    final IViewerObservableValue targetSelection = ViewersObservables.observeSinglePostSelection( comboViewer );
    final IObservableValue modelSelection = BeansObservables.observeValue( m_data, EditFeaturePropertiesData.PROPERTY_PROPERTY );
    final DataBinder binder = new DataBinder( targetSelection, modelSelection );
    binder.addTargetAfterGetValidator( new NotNullValidator<>( IPropertyType.class, IStatus.ERROR, Messages.getString( "EditFeaturePropertiesPage.1" ) ) ); //$NON-NLS-1$

    m_binding.bindValue( binder );
  }

  // FIXME: we cannot always assume that the property is editable with a text control
  private void createValueControl( final Composite parent )
  {
    final Label label = new Label( parent, SWT.NONE );

    /* Label binding */
    final ISWTObservableValue targetLabel = SWTObservables.observeText( label );
    final IObservableValue modelLabel = BeansObservables.observeValue( m_data, EditFeaturePropertiesData.PROPERTY_VALUE_LABEL );
    m_binding.bindValue( targetLabel, modelLabel );

    /* Value binding */
    final Text text = new Text( parent, SWT.BORDER );
    text.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final ISWTObservableValue targetValue = SWTObservables.observeText( text, SWT.Modify );
    final IObservableValue modelValue = BeansObservables.observeValue( m_data, EditFeaturePropertiesData.PROPERTY_VALUE );

    final DataBinder valueBinder = new DataBinder( targetValue, modelValue );
    valueBinder.setTargetToModelConverter( new StringToFeaturePropertyConverter( m_data ) );
    valueBinder.setModelToTargetConverter( new FeaturePropertyToStringConverter( m_data ) );
    m_binding.bindValue( valueBinder );

    /* Enabled only, if the current property can be edited as a string */
    final ISWTObservableValue targetEnabled = SWTObservables.observeEnabled( text );
    final IObservableValue modelEnabled = BeansObservables.observeValue( m_data, EditFeaturePropertiesData.PROPERTY_ENABLED );
    final DataBinder enabledBinder = new DataBinder( targetEnabled, modelEnabled );
    m_binding.bindValue( enabledBinder );

  }

  private void createRadioGroup( final Composite parent )
  {
    final Group radioButtonGroup = new Group( parent, SWT.SHADOW_ETCHED_IN );
    radioButtonGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
    radioButtonGroup.setText( Messages.getString( "org.kalypso.ui.editor.actions.FeatureBatchEditActionDelegate.3" ) ); //$NON-NLS-1$
    GridLayoutFactory.swtDefaults().applyTo( radioButtonGroup );

    final FeaturePropertyOperation currentOperation = m_data.getOperation();

    final EditFeaturePropertiesData data = m_data;
    for( final FeaturePropertyOperation operation : FeaturePropertyOperation.values() )
    {
      final String label = operation.toString();
      final String tooltip = operation.getDescription();

      final Button radio = new Button( radioButtonGroup, SWT.RADIO );
      radio.setText( label );
      radio.setToolTipText( tooltip );
      radio.setSelection( operation == currentOperation );

      radio.addSelectionListener( new SelectionAdapter()
      {
        @Override
        public void widgetSelected( final org.eclipse.swt.events.SelectionEvent e )
        {
          if( radio.getSelection() )
            data.setOperation( operation );
        }
      } );
    }

    final ISWTObservableValue target = SWTObservables.observeVisible( radioButtonGroup );
    final IObservableValue model = BeansObservables.observeValue( m_data, EditFeaturePropertiesData.PROPERTY_ISNUMERIC );
    m_binding.bindValue( target, model );
  }

  protected void relayout( )
  {
    m_layoutJob.cancel();
    m_layoutJob.schedule( 100 );
  }

  protected void doLayout( )
  {
    final Control control = getControl();
    if( control == null || control.isDisposed() )
      return;

    if( control instanceof Composite )
      ((Composite) control).layout( true, true );
  }
}