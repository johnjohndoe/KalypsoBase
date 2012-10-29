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
import org.eclipse.swt.widgets.Shell;

public interface IFileChooserDelegate
{
  String getButtonText( );

  int getTextBoxStyle( );

  /**
   * Shows the dialog and asks for the file.
   * 
   * @return <code>null</code>, if the dialog was cancelled.
   */
  File chooseFile( Shell shell, File currentFile );

  IMessageProvider validate( File file );

  /**
   * Returns the absolute file path with which the ui will be initialised.
   * 
   * @param savePath
   *          The previously saved path of the last session (saved in {@link org.eclipse.jface.dialogs.IDialogSettings}.
   */
  String getInitialPath( String savedPath );
}