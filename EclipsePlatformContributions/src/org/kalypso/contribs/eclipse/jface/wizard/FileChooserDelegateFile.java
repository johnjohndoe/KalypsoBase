/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Bj�rnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universit�t Hamburg-Harburg, Institut f�r Wasserbau, Hamburg, Germany
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
package org.kalypso.contribs.eclipse.jface.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.ui.forms.MessageProvider;
import org.kalypso.contribs.java.io.FilePattern;

public abstract class FileChooserDelegateFile implements IFileChooserDelegate
{
  private final Collection<String> m_filterNames = new ArrayList<>();

  private final Collection<String> m_filterExtensions = new ArrayList<>();

  private final int m_fileDialogType;

  private String m_filename;

  private final boolean m_optional;

  protected FileChooserDelegateFile( final int fileDialogType )
  {
    this( fileDialogType, new String[0], new String[0], false );
  }

  protected FileChooserDelegateFile( final int fileDialogType, final String[] filterNames, final String[] filterExtensions, final boolean optional )
  {
    m_fileDialogType = fileDialogType;

    Assert.isTrue( filterNames.length == filterExtensions.length );
    for( int i = 0; i < filterExtensions.length; i++ )
      addFilter( filterNames[i], filterExtensions[i] );

    m_optional = optional;
  }

  /**
   * Sets the initial filename.
   */
  public void setFileName( final String filename )
  {
    m_filename = filename;
  }

  /**
   * @param name
   *          Name of the filter i.e. 'All Files' (the (*.*) will be added automatically)
   * @param extension
   *          Extension filter i.e. *.* or *.sld
   * @see org.eclipse.swt.widgets.FileDialog#setFilterExtensions(String[])
   * @see org.eclipse.swt.widgets.FileDialog#setFilterNames(String[])
   */
  public void addFilter( final String name, final String extension )
  {
    m_filterNames.add( String.format( "%s (%s)", name, extension ) );
    m_filterExtensions.add( extension );
  }

  public void addFilter( final FilePattern extension )
  {
    m_filterNames.add( extension.getFilterLabel() );
    m_filterExtensions.add( extension.getPattern() );
  }

  private String[] getFilterExtensions( )
  {
    return m_filterExtensions.toArray( new String[m_filterExtensions.size()] );
  }

  private String[] getFilterNames( )
  {
    return m_filterNames.toArray( new String[m_filterNames.size()] );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IFileChooserDelegate#chooseFile(org.eclipse.swt.widgets.Shell, java.io.File)
   */
  @Override
  public File chooseFile( final Shell shell, final File currentFile )
  {
    final FileDialog dialog = new FileDialog( shell, m_fileDialogType );

    final String parentDir = getFilterPath( currentFile );
    final String filename = getFileName( currentFile );

    dialog.setFilterPath( parentDir );
    dialog.setFileName( filename );

    dialog.setText( getButtonText() );

    dialog.setFilterExtensions( getFilterExtensions() );
    dialog.setFilterNames( getFilterNames() );
    if( currentFile != null )
      dialog.setFilterIndex( getFilterIndex( currentFile.getAbsolutePath() ) );

    final String newFilename = dialog.open();
    if( newFilename == null )
      return null;

    final int index = dialog.getFilterIndex();
    if( index < 0 )
      return new File( newFilename );

    /* FIXME: probably buggy and does not always what was intended.... */
    if( this instanceof FileChooserDelegateSave )
    {
      // FIXME: assumes that all filters start with '*.'
      final String suffix = dialog.getFilterExtensions()[index].substring( 2 );

      final String updatedFilename = updateFileName( newFilename, suffix );
      return new File( updatedFilename );
    }

    return new File( newFilename );
  }

  private String getFileName( final File currentFile )
  {
    if( currentFile == null )
    {
      final String[] filterExtensions = getFilterExtensions();
      if( filterExtensions.length == 0 )
        return "";

      final String ext = filterExtensions[0];
      if( "*.*".equals( ext ) )
        return "";

      if( ext.startsWith( "*" ) )
        return ext;

      return "*" + ext;
    }

    return currentFile.getName();
  }

  protected String getFilterPath( final File currentFile )
  {
    if( currentFile == null )
      return null;

    return currentFile.getParent();
  }

  public abstract String updateFileName( String filename, String suffix );

  private int getFilterIndex( final String fileName )
  {
    final int index = fileName.lastIndexOf( "." );//$NON-NLS-1$
    final String suffix = index < 0 ? "*.*" : "*" + fileName.substring( index ); //$NON-NLS-1$ //$NON-NLS-2$

    int i = 0;
    final String[] filterExtensions = getFilterExtensions();
    for( final String filter : filterExtensions )
    {
      if( filter.equalsIgnoreCase( suffix ) )
        return i;

      i++;
    }
    return -1;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IFileChooserDelegate#validate(java.io.File)
   */
  @Override
  public IMessageProvider validate( final File file )
  {
    final String path = file == null ? "" : file.getPath().trim();//$NON-NLS-1$
    if( path.length() == 0 && !m_optional )
      return new MessageProvider( "Es muss eine Datei angegeben werden.", IMessageProvider.ERROR );

    return null;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IFileChooserDelegate#getInitialPath(java.lang.String)
   */
  @Override
  public String getInitialPath( final String savedPath )
  {
    if( m_filename == null )
      return savedPath;

    /* Replace filename with our filename. We keep the last directory */
    final File savedFile = savedPath == null ? null : new File( savedPath );
    final File savedDir = savedFile == null ? null : savedFile.getParentFile();
    final File userDir = new File( System.getProperty( "user.dir" ) );
    final File initialDir = savedDir == null ? userDir : savedDir;
    final File initialFile = new File( initialDir, m_filename );
    return initialFile.getAbsolutePath();
  }
}