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
package org.kalypso.commons.databinding.swt;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.kalypso.commons.java.util.AbstractModelObject;

/**
 * A data object that holds a file (or dir) and a file history.
 * 
 * @author Gernot Belger
 */
public class FileAndHistoryData extends AbstractModelObject
{
  public static final String PROPERTY_HISTORY = "history"; //$NON-NLS-1$

  public static final String PROPERTY_FILE = "file"; //$NON-NLS-1$

  public static final String PROPERTY_PATH = "path"; //$NON-NLS-1$

  private IPath m_path;

  private String[] m_history = new String[0];

  private final String m_name;

  /**
   * A names, that makes this data object unique within several data objects of the same kind.
   */
  public FileAndHistoryData( final String name )
  {
    m_name = name;
  }

  public void init( final IDialogSettings settings )
  {
    if( settings == null )
      return;

    final String section = getHistorySettingName();
    final String[] names = settings.getArray( section );
    if( names != null )
      setHistory( names );
  }

  protected String getHistorySettingName( )
  {
    return m_name + "_" + PROPERTY_HISTORY; //$NON-NLS-1$
  }

  public void storeSettings( final IDialogSettings settings )
  {
    if( settings == null )
      return;

    final IPath path = getPath();

    // update source names history
    final String[] history = getHistory();
    final Set<String> historySet = new LinkedHashSet<>();
    // New entry on, top; avoid duplicate entries
    if( path != null )
      historySet.add( path.toPortableString() );
    historySet.addAll( Arrays.asList( history ) );

    final String section = getHistorySettingName();
    settings.put( section, historySet.toArray( new String[historySet.size()] ) );
  }

  public File getFile( )
  {
    if( m_path == null )
      return null;

    return m_path.toFile();
  }

  public String[] getHistory( )
  {
    return m_history;
  }

  public void setHistory( final String[] history )
  {
    final String[] oldValue = history;

    m_history = history;

    firePropertyChange( PROPERTY_HISTORY, oldValue, history );
  }

  public void setFile( final File file )
  {
    final IPath path = file == null ? null : new Path( file.getAbsolutePath() );
    setPath( path );

  }

  public void setPath( final IPath path )
  {
    final File oldFile = getFile();
    final Object oldValue = m_path;

    m_path = path;

    firePropertyChange( PROPERTY_PATH, oldValue, path );
    firePropertyChange( PROPERTY_FILE, oldFile, getFile() );
  }

  public IPath getPath( )
  {
    return m_path;
  }
}