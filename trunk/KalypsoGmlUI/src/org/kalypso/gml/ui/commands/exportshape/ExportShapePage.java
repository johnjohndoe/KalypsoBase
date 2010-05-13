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
package org.kalypso.gml.ui.commands.exportshape;

import java.io.File;
import java.nio.charset.Charset;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.jface.viewers.CharsetViewer;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserDelegateSave;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChangedListener;
import org.kalypso.gml.ui.jface.ShapeCharsetUI;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypso.transformation.ui.CRSSelectionPanel;
import org.kalypsodeegree.KalypsoDeegreePlugin;

/**
 * @author belger
 */
public class ExportShapePage extends WizardPage
{
  private static final String SETTINGS_CRS = "crs"; //$NON-NLS-1$

  private static final String SETTINGS_WRITE_PRJ = "doWritePrj";//$NON-NLS-1$

  private FileChooserGroup m_fileChooserGroup;

  private final FileChooserDelegateSave m_fileDelegate;

  private CharsetViewer m_charsetViewer;

  private String m_crs;

  private boolean m_writePrj;

  public ExportShapePage( final String pageName, final String fileName )
  {
    super( pageName );

    setTitle( "Shape File" );
    setDescription( "Please choose the target shape file on this page." );

    m_fileDelegate = new FileChooserDelegateSave();
    m_fileDelegate.setFileName( fileName );
    // TODO: fetch filter from central place
    m_fileDelegate.addFilter( "ESRI Shape Files", "*.shp" );
    m_fileDelegate.addFilter( "DBase Files", "*.dbf" );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout( 3, false ) );

    createFileChooser( panel );

    createCoordinateSystemChooser( panel );

    final Control charsetControl = createCharsetChooser( panel );
    charsetControl.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    new Label( panel, SWT.NONE );

    final Control writePrjControl = createWritePrjControl( panel );
    writePrjControl.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 ) );

    setControl( panel );
  }

  private Control createWritePrjControl( final Composite panel )
  {
    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings != null )
      m_writePrj = dialogSettings.getBoolean( SETTINGS_WRITE_PRJ );

    final Button button = new Button( panel, SWT.CHECK );
    button.setText( "Write PRJ file (needs internet access)" );
    button.setToolTipText( "Fetches and saves the ESRI projection file (PRJ) from http://spatialreferences.org." );

    button.setSelection( m_writePrj );

    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleWritePrjChanged( button.getSelection() );
      }
    } );

    return button;
  }

  protected void handleWritePrjChanged( final boolean writePrj )
  {
    m_writePrj = writePrj;

    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings != null )
      dialogSettings.put( SETTINGS_WRITE_PRJ, writePrj );

    updateMessage();
  }

  private void createCoordinateSystemChooser( final Composite parent )
  {
    final Label label = new Label( parent, SWT.NONE );
    label.setText( "Coordinate System" );
    final String tooltip = "All exported geometries will be projected into the chosen coordinate system.";
    label.setToolTipText( tooltip );
    label.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

    final CRSSelectionPanel crsSelector = new CRSSelectionPanel( parent, CRSSelectionPanel.NO_GROUP );
    crsSelector.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
    crsSelector.setToolTipText( tooltip );

    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings != null )
    {
      final String crs = dialogSettings.get( SETTINGS_CRS );
      if( crs == null )
        crsSelector.setSelectedCRS( KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
      else
        crsSelector.setSelectedCRS( crs );
    }

    crsSelector.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        handleCrsChanged( crsSelector, dialogSettings );
      }

    } );

    m_crs = crsSelector.getSelectedCRS();
  }

  protected void handleCrsChanged( final CRSSelectionPanel crsSelector, final IDialogSettings dialogSettings )
  {
    final String selectedCRS = crsSelector.getSelectedCRS();
    if( dialogSettings != null )
      dialogSettings.put( SETTINGS_CRS, selectedCRS );

    m_crs = selectedCRS;

    updateMessage();
  }

  private Control createCharsetChooser( final Composite parent )
  {
    final Label charsetLabel = new Label( parent, SWT.NONE );
    charsetLabel.setText( "Charset" );
    final Charset shapeDefaultCharset = ShapeSerializer.getShapeDefaultCharset();
    final String tooltip = String.format( "The chosen charset will be used for string encodings of .dbf file. Default for ESRI Shape Files is %s.", shapeDefaultCharset.displayName() );
    charsetLabel.setToolTipText( tooltip );
    charsetLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

    final IDialogSettings dialogSettings = getDialogSettings();
    m_charsetViewer = ShapeCharsetUI.createCharsetViewer( parent, dialogSettings );
    m_charsetViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        updateMessage();
      }
    } );

    return m_charsetViewer.getControl();
  }

  private void createFileChooser( final Composite panel )
  {
    m_fileChooserGroup = new FileChooserGroup( m_fileDelegate );
    m_fileChooserGroup.setDialogSettings( getDialogSettings() );
    m_fileChooserGroup.addFileChangedListener( new FileChangedListener()
    {
      @Override
      public void fileChanged( final File file )
      {
        updateMessage();
      }
    } );

    m_fileChooserGroup.createControlsInGrid( panel );
  }

  protected void updateMessage( )
  {
    final IMessageProvider message = validate();
    if( message == null )
      setMessage( null );
    else
      setMessage( message.getMessage(), message.getMessageType() );
  }

  private IMessageProvider validate( )
  {
    final IMessageProvider fileMessage = m_fileChooserGroup.validate();
    if( fileMessage != null )
      return fileMessage;

    return null;
  }

  public Charset getCharset( )
  {
    return m_charsetViewer.getCharset();
  }

  public String getCoordinateSystem( )
  {
    return m_crs;
  }

  public String getShapeFileBase( )
  {
    final File file = m_fileChooserGroup.getFile();
    final String path = file.getAbsolutePath();
    if( path.toLowerCase().endsWith( ".shp" ) || path.toLowerCase().endsWith( ".dbf" ) ) //$NON-NLS-1$ //$NON-NLS-2$
      return FileUtilities.nameWithoutExtension( path );

    return path;
  }

  public boolean isWritePrj( )
  {
    return m_writePrj;
  }
}
