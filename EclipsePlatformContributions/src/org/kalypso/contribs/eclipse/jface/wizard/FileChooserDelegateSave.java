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
package org.kalypso.contribs.eclipse.jface.wizard;

import java.io.File;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.kalypso.contribs.eclipse.i18n.Messages;
import org.kalypso.contribs.eclipse.ui.forms.MessageProvider;

public class FileChooserDelegateSave extends FileChooserDelegateFile
{
  public FileChooserDelegateSave( )
  {
    super( SWT.SAVE );
  }

  public FileChooserDelegateSave( final String[] filterNames, final String[] filterExtensions, final boolean optional )
  {
    super( SWT.SAVE, filterNames, filterExtensions, optional );
  }

  @Override
  public String getButtonText( )
  {
    return Messages.getString( "org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.1" ); //$NON-NLS-1$
  }

  @Override
  public int getTextBoxStyle( )
  {
    return SWT.BORDER;
  }

  @Override
  public String updateFileName( final String filename, final String suffix )
  {
    return setSuffix( filename, suffix );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.FileChooserDelegateFile#validate(java.io.File)
   */
  @Override
  public IMessageProvider validate( final File file )
  {
    final IMessageProvider validate = super.validate( file );
    if( validate != null )
      return validate;

    if( !file.exists() )
      return null;

    if( file.isDirectory() )
      return new MessageProvider( "Die angegebene Datei ist ein Verzeichnis.", IMessageProvider.ERROR );

    return new MessageProvider( "Die angegebene Datei existiert bereits und wird überschrieben.", IMessageProvider.WARNING );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.FileChooserDelegateFile#getInitialPath(java.lang.String)
   */
  @Override
  public String getInitialPath( final String savedPath )
  {
    final String initialPath = super.getInitialPath( savedPath );

    return initialPath;
  }

  private static String setSuffix( final String fileName, final String suffix )
  {
    if( "*".equals( suffix ) )
      return fileName;
// FIXME: use FilenameUtils !
    final int indexDot = fileName.lastIndexOf( '.' );

    if( FileChooserGroup.DIRECTORY_FILTER_SUFFIX.equals( suffix ) )
    {
      if( fileName.endsWith( File.separator ) )
        return fileName;
      if( indexDot < 0 )
        return fileName + File.separator;
      return fileName.substring( 0, fileName.lastIndexOf( File.separator ) + 1 );
    }

    if( fileName.endsWith( File.separator ) )
      return fileName + "*." + suffix;
    if( indexDot < 0 )
      return fileName + File.separator + "*." + suffix;

    return fileName.substring( 0, indexDot + 1 ) + suffix;
  }

}