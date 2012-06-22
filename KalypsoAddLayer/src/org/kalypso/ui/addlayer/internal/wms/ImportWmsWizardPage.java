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
package org.kalypso.ui.addlayer.internal.wms;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.contribs.eclipse.jface.action.ActionButton;
import org.kalypso.ui.i18n.Messages;

/**
 * This is a page for importing a WMS Layer.
 *
 * @author Kuepferle, Doemming (original)
 * @author Holger Albert
 */
public class ImportWmsWizardPage extends WizardPage
{
  /**
   * This constant stores the minimum list width.
   */
  private static final int MIN_LIST_WIDTH = 250;

  private final ImportWmsData m_data;

  private IDataBinding m_binding;

  public ImportWmsWizardPage( final String pageName, final ImportWmsData data )
  {
    this( pageName, Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.1" ), null, data ); //$NON-NLS-1$
  }

  public ImportWmsWizardPage( final String pageName, final String title, final ImageDescriptor titleImage, final ImportWmsData data )
  {
    super( pageName, title, titleImage );

    m_data = data;

    /* The page is not complete in the beginning. */
    setPageComplete( false );

    setDescription( Messages.getString("ImportWmsWizardPage.0") ); //$NON-NLS-1$
  }

  @Override
  public void createControl( final Composite parent )
  {
    m_binding = new DatabindingWizardPage( this, null );

    /* Create the main composite. */
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout( 1, false ) );
    // panel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    createAddressLine( panel ).setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    /* Capabilities info */
    createCapabilitiesInfo( panel ).setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    /* Create the section for the layer selection. */
    final Group layerGroup = new Group( panel, SWT.NONE );
    layerGroup.setLayout( new GridLayout( 3, false ) );
    layerGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    layerGroup.setText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.7" ) ); //$NON-NLS-1$

    /* The capabilities tree. */
    final TreeViewer capabilitiesTree = new TreeViewer( layerGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    capabilitiesTree.setContentProvider( new WMSCapabilitiesContentProvider() );
    capabilitiesTree.setLabelProvider( new WMSCapabilitiesLabelProvider() );
    capabilitiesTree.setAutoExpandLevel( 1 );

    final GridData treeData = new GridData( SWT.FILL, SWT.FILL, true, true );
    treeData.widthHint = MIN_LIST_WIDTH;
    treeData.minimumWidth = MIN_LIST_WIDTH;
    treeData.minimumHeight = MIN_LIST_WIDTH;
    capabilitiesTree.getControl().setLayoutData( treeData );

    /* The layer button composite. */
    final GridData layerButtonData = new GridData( SWT.CENTER, SWT.FILL, false, true );
    layerButtonData.widthHint = 37;
    layerButtonData.minimumWidth = 37;

    final Composite buttonPanel = new Composite( layerGroup, SWT.NONE );
    buttonPanel.setLayout( new GridLayout( 1, false ) );

    /* The layer viewer. */
    final ListViewer layerViewer = new ListViewer( layerGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
    layerViewer.setContentProvider( new ArrayContentProvider() );
    layerViewer.setLabelProvider( new WMSCapabilitiesLabelProvider() );

    final GridData layerViewerData = new GridData( SWT.FILL, SWT.FILL, true, true );
    layerViewerData.widthHint = MIN_LIST_WIDTH;
    layerViewerData.minimumWidth = MIN_LIST_WIDTH;
    layerViewerData.minimumHeight = MIN_LIST_WIDTH;
    layerViewer.getControl().setLayoutData( layerViewerData );
    layerViewer.setComparator( new ViewerComparator() );

    /* The multi layer button. */
    createMultiLayerControl( layerGroup );

    /**
     * Actions
     */
    /* The layer button for adding a layer. */
    final IAction addAction = new AddLayerAction( capabilitiesTree, layerViewer, m_data );
    final Button layerButtonAdd = ActionButton.createButton( null, buttonPanel, addAction );
    layerButtonAdd.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );

    /* The layer button for removing a layer. */
    // FIXME
    final Action removeAction = new RemoveLayerAction( layerViewer, m_data );
    final Button layerButtonRemove = ActionButton.createButton( null, buttonPanel, removeAction );
    layerButtonRemove.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );

    /**
     * Binding
     */
    final IObservableValue targetInput = ViewersObservables.observeInput( capabilitiesTree );
    final IObservableValue modelInput = BeansObservables.observeValue( m_data, ICapabilitiesData.PROPERTY_CAPABILITIES );
    m_binding.bindValue( targetInput, modelInput );

    final IObservableSet chosenLayers = m_data.getChosenLayerSet();
    final IBeanValueProperty propertyValue = PojoProperties.value( "title" ); //$NON-NLS-1$
    ViewerSupport.bind( layerViewer, chosenLayers, propertyValue );

    /* The double click listener for the capabilities tree. */
    capabilitiesTree.addDoubleClickListener( new IDoubleClickListener()
    {
      @Override
      public void doubleClick( final DoubleClickEvent event )
      {
        addAction.runWithEvent( null );
      }
    } );

    layerViewer.addDoubleClickListener( new IDoubleClickListener()
    {
      @Override
      public void doubleClick( final DoubleClickEvent event )
      {
        removeAction.runWithEvent( null );
      }
    } );

    /* Set the control. */
    setControl( panel );
  }

  private Control createAddressLine( final Composite parent )
  {
    /* Create the section for the URL selection. */
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout( 4, false ) );

    /* The url label. */
    final Label urlLabel = new Label( panel, SWT.NONE );
    urlLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
    urlLabel.setText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.2" ) ); //$NON-NLS-1$
    urlLabel.setToolTipText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.3" ) ); //$NON-NLS-1$

    /**
     * Address field
     */
    final ComboViewer addressField = new ComboViewer( panel, SWT.BORDER );
    final GridData urlTextData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    addressField.getControl().setLayoutData( urlTextData );
    addressField.getControl().setToolTipText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.4" ) ); //$NON-NLS-1$

    /**
     * Refresh button
     */
    final Action loadAction = new LoadCapabilitiesAction( m_data );
    /* final Button loadButton = */ActionButton.createButton( null, panel, loadAction );

    // Strange, does not work,...?
// /* Binding */
// final ISWTObservableValue targetEnablement = SWTObservables.observeEnabled( loadButton );
// final IObservableValue modelEnablement = BeansObservables.observeValue( m_data,
// ICapabilitiesData.PROPERTY_VALID_ADDRESS );
// m_binding.bindValue( targetEnablement, modelEnablement );

    /**
     * Favorites button
     */
    final Action favoritesAction = new WMSFavoritesAction( m_data );
    final Button favoritesButton = ActionButton.createButton( null, panel, favoritesAction );
    favoritesButton.setLayoutData( new GridData( SWT.BEGINNING, SWT.FILL, false, false ) );

    /**
     * Binding
     */
    final ISWTObservableValue targetAddress = SWTObservables.observeText( addressField.getControl() );
    final IObservableValue modelAddress = BeansObservables.observeValue( m_data, ImportWmsData.PROPERTY_ADDRESS );
    m_binding.bindValue( targetAddress, modelAddress );

    final IObservableSet serviceHistory = m_data.getServiceHistorySet();
    final IBeanValueProperty propertyValue = PojoProperties.value( "address" ); //$NON-NLS-1$
    ViewerSupport.bind( addressField, serviceHistory, propertyValue );

    return panel;
  }

  private Control createCapabilitiesInfo( final Composite parent )
  {
    final Group infoGroup = new Group( parent, SWT.NONE );
    infoGroup.setText( Messages.getString("ImportWmsWizardPage.1") ); //$NON-NLS-1$
    infoGroup.setLayout( new FillLayout() );

    new CapabilitiesComposite( infoGroup, m_data, m_binding );

    return infoGroup;
  }

  private void createMultiLayerControl( final Composite parent )
  {
    final Button checkbox = new Button( parent, SWT.CHECK );
    checkbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 3, 1 ) );

    checkbox.setText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.10" ) ); //$NON-NLS-1$
    checkbox.setToolTipText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.11" ) ); //$NON-NLS-1$

    /**
     * BIDING
     */
    final ISWTObservableValue targetEnablement = SWTObservables.observeEnabled( checkbox );
    final IObservableValue modelEnablement = BeansObservables.observeValue( m_data, ImportWmsData.PROPERTY_MULTI_LAYER_ENABLEMENT );
    m_binding.bindValue( targetEnablement, modelEnablement );

    final ISWTObservableValue targetValue = SWTObservables.observeSelection( checkbox );
    final IObservableValue modelValue = BeansObservables.observeValue( m_data, ImportWmsData.PROPERTY_MULTI_LAYER );
    m_binding.bindValue( targetValue, modelValue );
  }
}