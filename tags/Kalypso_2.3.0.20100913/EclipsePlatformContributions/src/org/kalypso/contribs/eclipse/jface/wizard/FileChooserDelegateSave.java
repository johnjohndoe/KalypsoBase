/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
import org.kalypso.contribs.eclipse.i18n.Messages;
import org.kalypso.contribs.eclipse.ui.forms.MessageProvider;

public class FileChooserDelegateSave extends FileChooserDelegateFile
{
  public FileChooserDelegateSave( )
  {
    super( SWT.SAVE );
  }

  public FileChooserDelegateSave( final String[] filterNames, final String[] filterExtensions )
  {
    super( SWT.SAVE, filterNames, filterExtensions );
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
  protected String updateFileName( final String filename, final String suffix )
  {
    return FileChooserGroup.setSuffix( filename, suffix );
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
}