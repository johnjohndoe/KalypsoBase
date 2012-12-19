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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kalypso.ui.i18n.Messages;

/**
 * This dialog is for manageing the favorites of the WMS wizard page.
 *
 * @author Holger Albert
 */
public class WMSFavoritesDialog extends Dialog
{
  /**
   * The last successfully used service URL.
   */
  private final CapabilitiesInfo m_lastService;

  /**
   * All favorite services, which are displayed in the list viewer. This list can be modified by the viewer.
   */
  private final List<CapabilitiesInfo> m_serviceHistory;

  /**
   * This variable stores the current selected service.
   */
  private CapabilitiesInfo m_selectedService = new CapabilitiesInfo( StringUtils.EMPTY );

  /**
   * @param shell
   *          The parent shell, or null to create a top-level shell.
   * @param lastService
   *          The last successfully used service URL.
   * @param lastServices
   *          The last successfully used services. The favorites, to call them another way.
   */
  public WMSFavoritesDialog( final Shell shell, final CapabilitiesInfo lastService, final CapabilitiesInfo[] lastServices )
  {
    super( shell );

    m_lastService = lastService;

    /* Copy all services to a list, which is allowed to modify. */
    m_serviceHistory = new LinkedList<>( Arrays.asList( lastServices ) );
  }

  @Override
  protected Control createDialogArea( final Composite parent )
  {
    /* Set the title. */
    getShell().setText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.WMSFavoritesDialog.0" ) ); //$NON-NLS-1$

    /* Create the main composite. */
    final Composite panel = (Composite) super.createDialogArea( parent );
    panel.setLayout( new GridLayout( 1, false ) );
    panel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

    /* Selection composite. */
    final Composite selectionComposite = new Composite( panel, SWT.NONE );
    selectionComposite.setLayout( new GridLayout( 2, false ) );
    selectionComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    /* The selection label. */
    final Label selectionLabel = new Label( selectionComposite, SWT.NONE );
    selectionLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
    selectionLabel.setText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.WMSFavoritesDialog.1" ) ); //$NON-NLS-1$

    /* The selection service text. */
    final Text selectionServiceText = new Text( selectionComposite, SWT.BORDER );
    final GridData selectionServiceData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    selectionServiceData.widthHint = 200;
    selectionServiceData.minimumWidth = 200;
    selectionServiceText.setLayoutData( selectionServiceData );
    selectionServiceText.setText( "" ); //$NON-NLS-1$
    selectionServiceText.setToolTipText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.WMSFavoritesDialog.2" ) ); //$NON-NLS-1$
    selectionServiceText.setEditable( false );

    /* The service group. */
    final Group serviceGroup = new Group( panel, SWT.NONE );
    serviceGroup.setLayout( new GridLayout( 1, false ) );
    serviceGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    serviceGroup.setText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.WMSFavoritesDialog.3" ) ); //$NON-NLS-1$

    /* The service viewer. */
    final ListViewer serviceViewer = new ListViewer( serviceGroup, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL );
    final GridData serviceViewerData = new GridData( SWT.FILL, SWT.FILL, true, true );
    serviceViewerData.heightHint = 200;
    serviceViewerData.minimumHeight = 200;
    serviceViewerData.widthHint = 475;
    serviceViewerData.minimumWidth = 475;
    serviceViewer.getList().setLayoutData( serviceViewerData );

    /* Set the input. */
    serviceViewer.setContentProvider( new ArrayContentProvider() );
    serviceViewer.setLabelProvider( new LabelProvider() );
    serviceViewer.setInput( m_serviceHistory );

    /* The delete service button. */
    final Button deleteServiceButton = new Button( serviceGroup, SWT.NONE );
    deleteServiceButton.setLayoutData( new GridData( SWT.END, SWT.CENTER, true, false ) );
    deleteServiceButton.setText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.WMSFavoritesDialog.4" ) ); //$NON-NLS-1$
    deleteServiceButton.setToolTipText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.WMSFavoritesDialog.5" ) ); //$NON-NLS-1$

    /* The selection changed listener for the service viewer. */
    serviceViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final ISelection selection = event.getSelection();
        if( !(selection instanceof StructuredSelection) )
          return;

        final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
        final String label = handleSelectiiponChanged( structuredSelection );
        selectionServiceText.setText( label );
      }
    } );

    /* The selection listener for the delete service button. */
    deleteServiceButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final ISelection selection = serviceViewer.getSelection();
        handleRemoveServiceSelected( (IStructuredSelection) selection );

        /* Refresh the viewer. */
        serviceViewer.refresh();

        /* Reset the selected fields, in case, it was the deleted one. They will reset the result members. */
        selectionServiceText.setText( "" ); //$NON-NLS-1$
      }
    } );

    /* Set the default, if possible. */
    if( m_lastService != null )
    {
      if( m_serviceHistory.contains( m_lastService ) )
        serviceViewer.setSelection( new StructuredSelection( m_lastService ) );
    }

    return panel;
  }

  protected String handleSelectiiponChanged( final IStructuredSelection selection )
  {
    if( selection.isEmpty() )
    {
      m_selectedService = new CapabilitiesInfo( StringUtils.EMPTY );
      return StringUtils.EMPTY;
    }
    else
    {
      /* There could be only one element selected. */
      final CapabilitiesInfo firstElement = (CapabilitiesInfo) selection.getFirstElement();

      /* Set it as selected. */
      m_selectedService = firstElement;
      return firstElement.getAddress();
    }
  }

  protected void handleRemoveServiceSelected( final IStructuredSelection selection )
  {
    m_serviceHistory.removeAll( ((StructuredSelection) selection).toList() );
  }

  protected void handleServiceAddressChanged( final String text )
  {
    if( StringUtils.isBlank( text ) )
      m_selectedService = new CapabilitiesInfo( StringUtils.EMPTY );
    else
      m_selectedService = new CapabilitiesInfo( text );
  }

  @Override
  protected void cancelPressed( )
  {
    /* There should never be a result, if cancel is pressed. */
    m_selectedService = null;

    super.cancelPressed();
  }

  /**
   * This function returns the selected service.
   *
   * @return The selected service.
   */
  public CapabilitiesInfo getSelectedService( )
  {
    return m_selectedService;
  }

  public CapabilitiesInfo[] getServiceHistory( )
  {
    return m_serviceHistory.toArray( new CapabilitiesInfo[m_serviceHistory.size()] );
  }
}