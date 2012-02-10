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
package org.kalypso.contribs.eclipse.utils;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IInputValidator;

/**
 * RhThis input validator checks the user input aggainst the filename of the original file and the filenames of the
 * other files contained in the parent of the original file. The new filename must be unique.
 * 
 * @author Holger Albert
 */
public class NewFilenameValidator implements IInputValidator
{
  /**
   * The original file.
   */
  private File m_file;

  /**
   * If true, the file extension will be preserved. The entered input will be handled as basename of the new file. This
   * means, the file extension will always be added to the new text for validation purposes.
   */
  private boolean m_preserveExtension;

  /**
   * The constructor.
   * 
   * @param file
   *          The original file.
   * @param preserveExtension
   *          If true, the file extension will be preserved. The entered input will be handled as basename of the new
   *          file. This means, the file extension will always be added to the new text for validation purposes.
   */
  public NewFilenameValidator( IFile file, boolean preserveExtension )
  {
    m_preserveExtension = preserveExtension;
    m_file = file.getLocation().toFile();
  }

  /**
   * The constructor.
   * 
   * @param file
   *          The original file.
   * @param preserveExtension
   *          If true, the file extension will be preserved. The entered input will be handled as basename of the new
   *          file. This means, the file extension will always be added to the new text for validation purposes.
   */
  public NewFilenameValidator( File file, boolean preserveExtension )
  {
    m_file = file;
    m_preserveExtension = preserveExtension;
  }

  /**
   * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
   */
  @Override
  public String isValid( String newText )
  {
    if( newText == null || newText.length() == 0 )
      return "Geben Sie einen neuen Dateinamen an.";

    File newFile = null;
    String extension = getFileExtension( m_file.getName() );
    if( !m_preserveExtension || (extension == null || extension.length() == 0) )
      newFile = new File( m_file.getParentFile(), newText );
    else
      newFile = new File( m_file.getParentFile(), String.format( "%s%s", newText, extension ) );

    if( newFile.exists() )
      return String.format( "Die Datei '%s' existiert bereits.", newFile.getName() );

    return null;
  }

  /**
   * This function returns the file extension. The sequence after the last point will be considered as the extension. It
   * does not matter, how long this extension is.
   * 
   * @param completeFilename
   *          The filename, which file extension will be returned.
   * @return The file extension or null.
   */
  private String getFileExtension( String complete )
  {
    /* Search the position of the extensions dot. */
    int lastIndexOf = complete.lastIndexOf( "." );
    if( lastIndexOf != -1 )
      return complete.substring( lastIndexOf, complete.length() );

    return null;
  }
}