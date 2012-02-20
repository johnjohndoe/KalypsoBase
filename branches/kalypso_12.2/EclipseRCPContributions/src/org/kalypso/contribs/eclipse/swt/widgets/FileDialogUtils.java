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
package org.kalypso.contribs.eclipse.swt.widgets;

import java.io.File;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.FileDialog;
import org.kalypso.contribs.eclipse.i18n.Messages;

/**
 * @author Gernot Belger
 */
public final class FileDialogUtils
{
  public static String FILTERNAME_ALL_FILES_NO_EXTENSION = Messages.getString( "FileDialogUtils_0" ); //$NON-NLS-1$

  public static String FILTERNAME_ALL_FILES = Messages.getString( "FileDialogUtils_0" ) + " (*.*)"; //$NON-NLS-1$ //$NON-NLS-1$

  public static String FILTER_ALL_FILES = "*.*"; //$NON-NLS-1$

  private FileDialogUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" ); //$NON-NLS-1$
  }

  /**
   * Addds the standard 'All Files (*.*)' filter to the given dialog.
   */
  public static void addFAllFilesilter( final FileDialog dialog )
  {
    addFilter( dialog, FILTERNAME_ALL_FILES, FILTER_ALL_FILES );
  }

  /**
   * Adds a filter to this dialog, should be called before the dialog is opened.
   * 
   * @param name
   *          The filter's name (without (*.ext); the extension will be added automatically
   * @param extension
   *          The filter's extension, see {@link FileDialog#setFilterExtensions(String[])}
   */
  public static void addFilter( final FileDialog dialog, final String name, final String extension )
  {
    final String[] names = dialog.getFilterNames();
    final String[] extensions = dialog.getFilterExtensions();

    final String filterName = String.format( "%s (%s)", name, extension );

    final String[] newNames = ArrayUtils.add( names, filterName );
    final String[] newExtensions = ArrayUtils.add( extensions, extension );

    dialog.setFilterNames( newNames );
    dialog.setFilterExtensions( newExtensions );
  }

  /**
   * Opens a file dialog and returns the result as array of {@link File}s.
   * 
   * @return <code>null</code>, if the dialog was cancelled.
   */
  public static File[] open( final FileDialog dialog )
  {
    final String result = dialog.open();
    if( StringUtils.isBlank( result ) )
      return null;

    final String[] fileNames = dialog.getFileNames();
    final String filterPath = dialog.getFilterPath();
    final File[] files = new File[fileNames.length];
    for( int i = 0; i < files.length; i++ )
      files[i] = new File( filterPath, fileNames[i] );
    return files;
  }

  /**
   * Returns the filter extension selected by the user.
   * 
   * @return <code>null</code>, if no extension was selected.
   */
  public static String getSelectedExtension( final FileDialog dialog )
  {
    final int filterIndex = dialog.getFilterIndex();
    if( filterIndex == -1 )
      return null;

    final String[] filterExtensions = dialog.getFilterExtensions();
    if( filterIndex >= filterExtensions.length )
      return null;

    return filterExtensions[filterIndex];
  }
}