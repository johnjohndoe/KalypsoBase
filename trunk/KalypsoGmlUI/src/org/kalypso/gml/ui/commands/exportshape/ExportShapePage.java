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

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserDelegateSave;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChangedListener;

/**
 * @author belger
 */
public class ExportShapePage extends WizardPage
{
  private FileChooserGroup m_fileChooserGroup;

  private final FileChooserDelegateSave m_fileDelegate;

  public ExportShapePage( final String pageName, final String fileName )
  {
    super( pageName );

    setTitle( "Shape File" );
    setDescription( "Please choose the target shape file on this page." );

    m_fileDelegate = new FileChooserDelegateSave();
    m_fileDelegate.setFileName( fileName );
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
    panel.setLayout( new GridLayout() );

    // examine what we got and ask user
    // TODO: only use file extension which make sense (dbf OR shp)

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

    final Group fileChooserControl = m_fileChooserGroup.createControl( panel, SWT.NONE );
    fileChooserControl.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );

    setControl( panel );
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

  public File getFile( )
  {
    return m_fileChooserGroup.getFile();
  }

  public String getShapeFileBase( )
  {
    final File file = m_fileChooserGroup.getFile();
    final String path = file.getAbsolutePath();
    if( path.toLowerCase().endsWith( ".shp" ) || path.toLowerCase().endsWith( ".dbf" ) ) //$NON-NLS-1$ //$NON-NLS-2$
      return FileUtilities.nameWithoutExtension( path );

    return path;
  }
}
