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

public class FileChooserDelegateOpen extends FileChooserDelegateFile
{
  public FileChooserDelegateOpen( )
  {
    super( SWT.OPEN );
  }

  public FileChooserDelegateOpen( final String[] filterNames, final String[] filterExtensions, final boolean optional )
  {
    super( SWT.OPEN, filterNames, filterExtensions, optional );
  }

  @Override
  public String getButtonText( )
  {
    return Messages.getString( "org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.0" ); //$NON-NLS-1$
  }

  @Override
  public int getTextBoxStyle( )
  {
    return SWT.BORDER;
  }

  @Override
  public String updateFileName( final String filename, final String suffix )
  {
    return filename;
  }

  @Override
  public IMessageProvider validate( final File file )
  {
    final IMessageProvider validate = super.validate( file );
    if( validate != null )
      return validate;

    if( file != null && !file.exists() )
      return new MessageProvider( "Die angegebene Datei existiert nicht.", IMessageProvider.ERROR );

    if( file != null && file.isDirectory() )
      return new MessageProvider( "Die angegebene Datei ist ein Verzeichnis.", IMessageProvider.ERROR );

    return null;
  }
}