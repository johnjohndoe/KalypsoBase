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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.ui.forms.MessageProvider;

public class FileChooserDelegateDirectory implements IFileChooserDelegate
{
  @Override
  public String getButtonText( )
  {
    return "Choose Directory";
  }

  @Override
  public int getTextBoxStyle( )
  {
    return SWT.BORDER;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IFileChooserDelegate#chooseFile(org.eclipse.swt.widgets.Shell,
   *      java.io.File)
   */
  @Override
  public File chooseFile( final Shell shell, final File currentFile )
  {
    final DirectoryDialog dialog = new DirectoryDialog( shell );
    if( currentFile != null )
      dialog.setFilterPath( currentFile.getAbsolutePath() );
    dialog.setText( getButtonText() );
    dialog.setMessage( "Please choose a directory: " );
    final String dir = dialog.open();
    if( dir == null )
      return null;

    return new File( dir );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IFileChooserDelegate#validate(java.io.File)
   */
  @Override
  public IMessageProvider validate( final File file )
  {
    final String path = file == null ? "" : file.getPath().trim();
    if( path.length() == 0 )
      return new MessageProvider( "Es muss ein Verzeichnis angegeben werden", IMessageProvider.ERROR );

    if( !file.exists() )
      return new MessageProvider( "Das angegebene Verzeichnis existiert nicht", IMessageProvider.ERROR );

    if( !file.isDirectory() )
      return new MessageProvider( "Die angegebene Datei ist kein Verzeichnis", IMessageProvider.ERROR );

    return null;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IFileChooserDelegate#getInitialPath(java.lang.String)
   */
  @Override
  public String getInitialPath( final String savedPath )
  {
    return savedPath;
  }

}