/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.ui.addlayer.internal.wms;

import java.util.Map;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.contribs.eclipse.jface.action.ActionHyperlink;
import org.kalypso.core.status.StatusComposite;
import org.kalypso.core.status.StatusCompositeValue;
import org.kalypso.ui.addlayer.internal.ImageProviderExtensions;
import org.kalypso.ui.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class CapabilitiesComposite extends Composite
{
  private final ICapabilitiesData m_data;

  private final IDataBinding m_binding;

  public CapabilitiesComposite( final Composite parent, final ICapabilitiesData data, final IDataBinding binding )
  {
    super( parent, SWT.NONE );

    m_data = data;
    m_binding = binding;

    GridLayoutFactory.swtDefaults().numColumns( 2 ).applyTo( this );

    createContent();
  }

  private void createContent( )
  {
    createStatusControls();
    // FIXME: trigger from outside
    final boolean showAddress = false;
    if( showAddress )
      createAddressControls();

    createOpenCapabilitiesControls();
    createImageProviderControls();
    createTitleControls();
    createAbstractControls();
  }

  private void createAddressControls( )
  {
    final Label label = new Label( this, SWT.NONE );

    label.setText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.2" ) ); //$NON-NLS-1$
    label.setToolTipText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.3" ) ); //$NON-NLS-1$

    final Text addressField = new Text( this, SWT.BORDER );
    addressField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    addressField.setToolTipText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.3" ) ); //$NON-NLS-1$
    addressField.setEnabled( false );

    /**
     * Binding
     */
    final ISWTObservableValue targetAddress = SWTObservables.observeText( addressField, new int[] { SWT.FocusOut, SWT.DefaultSelection } );
    final IObservableValue modelAddress = BeansObservables.observeValue( m_data, ImportWmsData.PROPERTY_ADDRESS );
    m_binding.bindValue( targetAddress, modelAddress );
  }

  private void createOpenCapabilitiesControls( )
  {
    /* placeholder */
    new Label( this, SWT.NONE );

    /* Open capabilities in external browser */
    final Action openCapabilitiesAction = new OpenCapabilitiesAction( m_data );
    final ImageHyperlink link = ActionHyperlink.createHyperlink( null, this, getStyle(), openCapabilitiesAction );
    link.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    /* Binding */
    final ISWTObservableValue targetEnablement = SWTObservables.observeEnabled( link );
    final IObservableValue modelEnablement = BeansObservables.observeValue( m_data, ICapabilitiesData.PROPERTY_VALID_ADDRESS );
    m_binding.bindValue( targetEnablement, modelEnablement );
  }

  private void createImageProviderControls( )
  {
    final String toolTipText = Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.5" ); //$NON-NLS-1$

    final Label label = new Label( this, SWT.NONE );
    // TODO: better label?
    label.setText( Messages.getString( "CapabilitiesComposite.0" ) ); //$NON-NLS-1$
    label.setToolTipText( toolTipText );

    final ComboViewer imageProviderViewer = new ComboViewer( this, SWT.READ_ONLY );
    final GridData urlComboData = new GridData( SWT.BEGINNING, SWT.CENTER, false, false );
    urlComboData.widthHint = 100;
    urlComboData.minimumWidth = 100;
    imageProviderViewer.getCombo().setLayoutData( urlComboData );
    // TODO: bad tooltip
    imageProviderViewer.getCombo().setToolTipText( toolTipText );

    final Map<String, String> imageProviders = ImageProviderExtensions.getImageProviders();

    imageProviderViewer.setContentProvider( new ArrayContentProvider() );
    imageProviderViewer.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        final String id = (String)element;
        return imageProviders.get( id );
      }
    } );

    /* Set the input. */
    final String[] providerIDS = imageProviders.keySet().toArray( new String[imageProviders.size()] );
    imageProviderViewer.setInput( providerIDS );

    /**
     * Binding
     */
    final IViewerObservableValue targetImageProvider = ViewersObservables.observeSingleSelection( imageProviderViewer );
    final IObservableValue modelImageProvider = BeansObservables.observeValue( m_data, ImportWmsData.PROPERTY_IMAGE_PROVIDER );
    m_binding.bindValue( targetImageProvider, modelImageProvider );
  }

  private void createStatusControls( )
  {
    final Label label = new Label( this, SWT.NONE );
    label.setText( Messages.getString( "CapabilitiesComposite.1" ) ); //$NON-NLS-1$

    final StatusComposite statusControl = new StatusComposite( this, StatusComposite.DETAILS );
    statusControl.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    /**
     * Binding
     */
    final StatusCompositeValue targetStatus = new StatusCompositeValue( statusControl );
    final IObservableValue modelStatus = BeansObservables.observeValue( m_data, ICapabilitiesData.PROPERTY_LOAD_STATUS );
    m_binding.bindValue( targetStatus, modelStatus );
  }

  private void createTitleControls( )
  {
    final Label label = new Label( this, SWT.NONE );

    label.setText( Messages.getString( "CapabilitiesComposite.2" ) ); //$NON-NLS-1$

    final Text field = new Text( this, SWT.BORDER );
    field.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    field.setEnabled( false );

    /* Binding */
    final ISWTObservableValue targetAddress = SWTObservables.observeText( field );
    final IObservableValue modelAddress = BeansObservables.observeValue( m_data, ImportWmsData.PROPERTY_TITLE );
    m_binding.bindValue( targetAddress, modelAddress );
  }

  private void createAbstractControls( )
  {
    final Label label = new Label( this, SWT.NONE );

    label.setText( Messages.getString( "CapabilitiesComposite.3" ) ); //$NON-NLS-1$

    final Text field = new Text( this, SWT.BORDER );
    field.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    field.setEnabled( false );

    /* Binding */
    final ISWTObservableValue targetAddress = SWTObservables.observeText( field );
    final IObservableValue modelAddress = BeansObservables.observeValue( m_data, ImportWmsData.PROPERTY_ABSTRACT );
    m_binding.bindValue( targetAddress, modelAddress );
  }
}