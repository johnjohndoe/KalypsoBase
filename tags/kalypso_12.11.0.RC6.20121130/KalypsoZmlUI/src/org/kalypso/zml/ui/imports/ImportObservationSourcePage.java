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
package org.kalypso.zml.ui.imports;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.kalypso.commons.databinding.DataBinder;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.commons.databinding.swt.FileAndHistoryData;
import org.kalypso.commons.databinding.swt.FileBinding;
import org.kalypso.commons.databinding.validation.TimezoneStringValidator;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserDelegateOpen;
import org.kalypso.contribs.java.util.TimezoneUtilities;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;
import org.kalypso.ogc.sensor.metadata.ParameterTypeLabelProvider;
import org.kalypso.zml.ui.internal.i18n.Messages;

/**
 * @author doemming
 */
public class ImportObservationSourcePage extends WizardPage
{
  private final Set<IImportObservationSourceChangedListener> m_listeners = Collections.synchronizedSet( new LinkedHashSet<IImportObservationSourceChangedListener>() );

  private final ImportObservationData m_data;

  private DatabindingWizardPage m_binding;

  private final boolean m_updateMode;

  public ImportObservationSourcePage( final String pageName, final ImportObservationData data )
  {
    this( pageName, data, false );
  }

  public ImportObservationSourcePage( final String pageName, final ImportObservationData data, final boolean updateMode )
  {
    super( pageName, null, null );

    m_data = data;
    m_updateMode = updateMode;

    setDescription( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage0" ) ); //$NON-NLS-1$
    setTitle( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage1" ) ); //$NON-NLS-1$
    setPageComplete( false );
  }

  public void addListener( final IImportObservationSourceChangedListener listener )
  {
    m_listeners.add( listener );
  }

  @Override
  public void createControl( final Composite parent )
  {
    initializeDialogUnits( parent );

    m_binding = new DatabindingWizardPage( this, null );

    final Group group = new Group( parent, SWT.NONE );
    group.setLayout( new GridLayout( 3, false ) );
    group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    group.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage2" ) ); //$NON-NLS-1$

    setControl( group );

    createFileControl( group );
    createTimeZoneControl( group );
    createFormatControl( group );
    createParameterTypeControl( group );
  }

  private void createFileControl( final Composite parent )
  {
    final Label label = new Label( parent, SWT.NONE );
    label.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage3" ) ); //$NON-NLS-1$

    final FileAndHistoryData sourceFileData = m_data.getSourceFileData();

    final IObservableValue modelFile = BeansObservables.observeValue( sourceFileData, FileAndHistoryData.PROPERTY_FILE );
    final IObservableValue modelHistory = BeansObservables.observeValue( sourceFileData, FileAndHistoryData.PROPERTY_HISTORY );

    final FileChooserDelegateOpen delegate = new FileChooserDelegateOpen();
    // final INativeObservationAdapter[] adapters = m_data.getObservationAdapters();
    // FIXME: add filter from adapter
    // FIXME: get from helper
    delegate.addFilter( Messages.getString( "ImportObservationSourcePage.0" ), "*.*" ); //$NON-NLS-1$ //$NON-NLS-2$

    final FileBinding fileBinding = new FileBinding( m_binding, modelFile, delegate );

    final Control historyControl = fileBinding.createFileFieldWithHistory( parent, modelHistory );
    historyControl.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final Button searchButton = fileBinding.createFileSearchButton( parent, historyControl );
    setButtonLayoutData( searchButton );

    modelFile.addValueChangeListener( new IValueChangeListener()
    {
      @Override
      public void handleValueChange( final ValueChangeEvent event )
      {
        fireSourceFileChanged( sourceFileData.getFile() );
      }
    } );
  }

  protected void fireSourceFileChanged( final File file )
  {
    final IImportObservationSourceChangedListener[] listeners = m_listeners.toArray( new IImportObservationSourceChangedListener[] {} );
    for( final IImportObservationSourceChangedListener listener : listeners )
      listener.sourceFileChanged( file );
  }

  private void createFormatControl( final Composite parent )
  {
    final Label formatLabel = new Label( parent, SWT.NONE );
    formatLabel.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage5" ) ); //$NON-NLS-1$

    final ComboViewer formatCombo = new ComboViewer( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
    formatCombo.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    formatCombo.setContentProvider( new ArrayContentProvider() );
    formatCombo.setLabelProvider( new LabelProvider() );

    final INativeObservationAdapter[] input = m_data.getObservationAdapters();
    formatCombo.setInput( input );

    new Label( parent, SWT.NONE );

    /* Binding */
    final IViewerObservableValue target = ViewersObservables.observeSinglePostSelection( formatCombo );
    final IObservableValue model = BeansObservables.observeValue( m_data, ImportObservationData.PROPERTY_ADAPTER );

    m_binding.bindValue( target, model, new IValidator()
    {

      @Override
      public IStatus validate( final Object value )
      {
        if( Objects.isNull( value ) )
        {
          setErrorMessage( Messages.getString("ImportObservationSourcePage.1") ); //$NON-NLS-1$
          return Status.CANCEL_STATUS;
        }

        return Status.OK_STATUS;
      }
    } );
  }

  private void createTimeZoneControl( final Composite parent )
  {
    final Label timezoneLabel = new Label( parent, SWT.NONE );
    timezoneLabel.setText( Messages.getString( "ImportObservationSelectionWizardPage.0" ) ); //$NON-NLS-1$

    final String[] tz = TimezoneUtilities.getSupportedTimezones();

    final ComboViewer comboTimeZones = new ComboViewer( parent, SWT.BORDER | SWT.SINGLE | SWT.DROP_DOWN | SWT.READ_ONLY );
    comboTimeZones.getControl().setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    comboTimeZones.setContentProvider( new ArrayContentProvider() );
    comboTimeZones.setLabelProvider( new LabelProvider() );
    comboTimeZones.setInput( tz );

    comboTimeZones.addFilter( new TimezoneEtcFilter() );

    new Label( parent, SWT.NONE );

    /* Binding */
    final IViewerObservableValue targetSelection = ViewersObservables.observeSinglePostSelection( comboTimeZones );
    final ISWTObservableValue targetModification = SWTObservables.observeSelection( comboTimeZones.getControl() );

    final IObservableValue model = BeansObservables.observeValue( m_data, ImportObservationData.PROPERTY_TIMEZONE );

    final DataBinder modificationBinder = new DataBinder( targetModification, model );
    modificationBinder.addTargetAfterConvertValidator( new TimezoneStringValidator() );

    m_binding.bindValue( targetSelection, model );
    m_binding.bindValue( modificationBinder );
  }

  private void createParameterTypeControl( final Composite parent )
  {
    final Label formatLabel = new Label( parent, SWT.NONE );
    formatLabel.setText( Messages.getString( "ImportObservationSourcePage.2" ) ); //$NON-NLS-1$

    final ComboViewer parameterCombo = new ComboViewer( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
    parameterCombo.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final ParameterTypeLabelProvider labelProvider = new ParameterTypeLabelProvider();
    parameterCombo.setLabelProvider( labelProvider );
    parameterCombo.setContentProvider( new ArrayContentProvider() );

    parameterCombo.setSorter( new ViewerSorter()
    {
      @Override
      public int compare( final org.eclipse.jface.viewers.Viewer viewer, final Object e1, final Object e2 )
      {
        final String l1 = labelProvider.getText( e1 );
        final String l2 = labelProvider.getText( e2 );

        if( StringUtils.isNotEmpty( l1 ) && StringUtils.isNotEmpty( l2 ) )
          return l1.compareTo( l2 );

        return super.compare( viewer, e1, e2 );
      }
    } );

    final String[] parameterTypes = m_data.getAllowedParameterTypes();
    parameterCombo.setInput( parameterTypes );

    new Label( parent, SWT.NONE );

    /* Binding */
    final IViewerObservableValue target = ViewersObservables.observeSinglePostSelection( parameterCombo );
    final IObservableValue model = BeansObservables.observeValue( m_data, ImportObservationData.PROPERTY_PARAMETER_TYPE );

    m_binding.bindValue( target, model );

    if( m_updateMode )
      parameterCombo.getCombo().setEnabled( false );
  }
}