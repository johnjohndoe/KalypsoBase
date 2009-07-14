/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraï¿½e 22
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
package org.kalypso.gml.ui.wizard.grid;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.kalypso.grid.IGridMetaReader;
import org.kalypso.transformation.ui.CRSSelectionListener;
import org.kalypso.transformation.ui.CRSSelectionPanel;
import org.kalypsodeegree.KalypsoDeegreePlugin;

/**
 * @author Dirk Kuch
 * @author Gernot Belger
 */
public class PageSelectGeodataFiles extends WizardPage
{
  private static final String SETTINGS_FILE_PATH = "fullFilePath";

  private static final String SETTINGS_FOLDER_PATH = "gridFolderPath";

  private static final String SETTINGS_SRS_NAME = "srsName";

  private File[] m_files;

  private final IGridMetaReader m_rasterReader = null;

  protected String m_crs;

  private String m_gridFolderPath;

  private final boolean m_allowUserChangeGridFolder;

  /**
   * @param chooseGridFolder
   *          If <code>true</code>, the user is asked for the destination folder to store the grid files into.
   * @param allowUserChangeGridFolder
   *          If <code>false</code>, the entry field for the grid folder is hidden Resets to <code>true</code>, if
   *          'gridFolder' is null..
   */
  public PageSelectGeodataFiles( final String pageName, final IContainer gridFolder, final boolean allowUserChangeGridFolder )
  {
    super( pageName );

    m_allowUserChangeGridFolder = gridFolder == null ? true : allowUserChangeGridFolder;
    m_gridFolderPath = gridFolder == null ? null : gridFolder.getFullPath().toPortableString();
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    setPageComplete( false );

    final Composite container = new Composite( parent, SWT.NULL );
    container.setLayout( new GridLayout() );
    setControl( container );

    final Group fileGroup = new Group( container, SWT.NONE );
    fileGroup.setLayout( new GridLayout( 2, false ) );
    fileGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    fileGroup.setText( "Rasterdatei(en)" );

    final Text tFile = new Text( fileGroup, SWT.BORDER );
    tFile.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        handleModified( tFile.getText() );
        updatePageComplete();
      }
    } );
    tFile.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );
    tFile.setEnabled( true );

    final Button buttonFile = new Button( fileGroup, SWT.NONE );
    buttonFile.setText( "..." );
    buttonFile.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        browseForFile( tFile );
      }
    } );

    /* Coordinate system combo */
    final CRSSelectionPanel crsPanel = new CRSSelectionPanel( container, SWT.NONE );
    crsPanel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    crsPanel.setToolTipText( "Koordinatensystem der Raster-Datei" );
    m_crs = getDialogSettings().get( SETTINGS_SRS_NAME );
    if( m_crs == null )
      m_crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    crsPanel.setSelectedCRS( m_crs );
    crsPanel.addSelectionChangedListener( new CRSSelectionListener()
    {
      @Override
      protected void selectionChanged( final String selectedCRS )
      {
        m_crs = selectedCRS;
        updatePageComplete();
      }
    } );

    createGridFolderControl( container );

    final IDialogSettings dialogSettings = getDialogSettings();
    final String fullPath = dialogSettings.get( SETTINGS_FILE_PATH );
    if( fullPath != null )
      tFile.setText( fullPath );

    updatePageComplete();
  }

  private void createGridFolderControl( final Composite container )
  {
    final Group folderGroup = new Group( container, SWT.NONE );
    folderGroup.setLayout( new GridLayout( 2, false ) );
    folderGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    folderGroup.setText( "Zielverzeichnis" );
    folderGroup.setToolTipText( "Zielverzeichnis für die Rasterdateien im Arbeitsbereich" );

    final Text tFolder = new Text( folderGroup, SWT.BORDER );
    tFolder.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        handleFolderModified( tFolder.getText() );
      }
    } );
    tFolder.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );
    tFolder.setEnabled( m_allowUserChangeGridFolder );

    final Button buttonFolder = new Button( folderGroup, SWT.NONE );
    buttonFolder.setText( "..." );
    buttonFolder.setEnabled( m_allowUserChangeGridFolder );
    buttonFolder.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        browseForFolder( tFolder );
      }
    } );

    final IDialogSettings dialogSettings = getDialogSettings();
    final String fullPath = dialogSettings.get( SETTINGS_FOLDER_PATH );

    if( m_gridFolderPath != null )
      tFolder.setText( m_gridFolderPath );
    else if( fullPath != null )
      tFolder.setText( fullPath );
  }

  protected void handleFolderModified( final String text )
  {
    m_gridFolderPath = text;
    updatePageComplete();
  }

  protected void browseForFolder( final Text tFolder )
  {
    IContainer initialRoot = null;
    if( m_gridFolderPath != null )
    {
      try
      {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IResource member = root.findMember( Path.fromPortableString( m_gridFolderPath ) );
        if( member instanceof IContainer )
          initialRoot = (IContainer) member;
      }
      catch( final IllegalArgumentException e )
      {
        // ignore htis exception, else we cannot continue if user enters rubbish
        e.printStackTrace();
      }
    }

    final ContainerSelectionDialog dialog = new ContainerSelectionDialog( getShell(), initialRoot, false, "Wählen Sie das Zielverzeichnis:" );
    dialog.setTitle( "Zielverzeichnis" );
    if( dialog.open() == Window.OK )
      tFolder.setText( ((IPath) dialog.getResult()[0]).toPortableString() );
  }

  protected void handleModified( final String text )
  {
    final String[] names = text.split( ";" );
    m_files = new File[names.length];
    for( int i = 0; i < names.length; i++ )
      m_files[i] = new File( names[i] );
  }

  protected void updatePageComplete( )
  {
    setPageComplete( false );

    final String error = validate();

// if( m_crs != null )
// {
// try
// {
// m_rasterReader = verifier.getRasterMetaReader( docLocation.toFile().toURL(), m_crs );
// }
// catch( final MalformedURLException e )
// {
// e.printStackTrace();
// }
//
// updateTextBoxes( m_rasterReader );
// }

    setPageComplete( error == null );
    setErrorMessage( error );

    /* If page is complete, we may save the settings */
    final String lastFile = (m_files == null || m_files.length == 0) ? null : m_files[0].getAbsolutePath();

    if( error == null )
    {
      getDialogSettings().put( SETTINGS_FOLDER_PATH, m_gridFolderPath );
      getDialogSettings().put( SETTINGS_FILE_PATH, lastFile );
      getDialogSettings().put( SETTINGS_SRS_NAME, m_crs );
    }
  }

  private String validate( )
  {
    if( m_files == null || m_files.length == 0 )
      return "Please select a file";

    for( final File file : m_files )
    {
      if( !file.exists() )
        return "Rasterdatei existiert nicht: " + file.getAbsolutePath();
    }

    if( m_gridFolderPath == null || m_gridFolderPath.isEmpty() )
      return "Destination folder not specified";

    final IPath path = Path.fromPortableString( m_gridFolderPath );
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IResource member = root.findMember( path );
    if( !(member instanceof IContainer) )
      return "Kein Verzeichnis";

    if( !member.exists() )
      return "Zielverzeichnis existiert nicht";

// final GridFileVerifier verifier = new GridFileVerifier();
// try
// {
// if( !verifier.verify( toFile.toURI().toURL() ) )
// return "Please select a valid geodata file";
// }
// catch( final MalformedURLException e )
// {
// e.printStackTrace();
// return e.getLocalizedMessage();
// }

    if( m_crs == null )
      return "No projection defined. Choose one projection from the list, please.";

    return null;
  }

  protected void browseForFile( final Text tFile )
  {
    final String fullPath = getDialogSettings().get( SETTINGS_FILE_PATH );
    m_files = ImportGridUtilities.chooseFiles( getShell(), "Rasterdaten Import", fullPath );

    final StringBuffer sb = new StringBuffer();
    for( final File file : m_files )
    {
      if( sb.length() > 0 )
        sb.append( ';' );
      sb.append( file.getAbsolutePath() );
    }

    tFile.setText( sb.toString() );
  }

  public File[] getSelectedFiles( )
  {
    return m_files;
  }

  public String getProjection( )
  {
    return m_crs;
  }

  public IGridMetaReader getReader( )
  {
    return m_rasterReader;
  }

  public IContainer getGridFolder( )
  {
    final IPath path = Path.fromPortableString( m_gridFolderPath );
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IResource member = root.findMember( path );
    if( member instanceof IContainer )
      return (IContainer) member;

    return null;
  }

}