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
package org.kalypso.contribs.eclipse.jface.wizard;

import java.io.File;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.ui.forms.MessageProvider;

public class FileChooserDelegateDirectory implements IFileChooserDelegate
{
  public String getButtonText( )
  {
    return "Choose Directory";
  }

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

}