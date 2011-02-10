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
package org.kalypso.ui.wizard.sensor;

import java.io.File;
import java.util.TimeZone;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;

/**
 * @author doemming
 */
public class ObservationImportSelection implements ISelection
{
  private final INativeObservationAdapter m_selectedAdapter;

  private final IFile m_fileTarget;

  private final File m_fileSource;

  public boolean isAppend()
  {
    return m_append;
  }

  private final boolean m_append;

  private final boolean m_retainMetadata;

  private final TimeZone m_sourceTimezone;

  public ObservationImportSelection( final File fileSource, final IFile fileTarget, final INativeObservationAdapter adapter,
      final boolean append, final boolean retainMetadata, final TimeZone timezone )
  {
    m_fileSource = fileSource;
    m_fileTarget = fileTarget;
    m_selectedAdapter = adapter;
    m_append = append;
    m_retainMetadata = retainMetadata;
    m_sourceTimezone = timezone;
  }

  /**
   * @see org.eclipse.jface.viewers.ISelection#isEmpty()
   */
  @Override
  public boolean isEmpty()
  {
    return m_fileSource == null || m_fileTarget == null || m_selectedAdapter == null;
  }

  public File getFileSource()
  {
    return m_fileSource;
  }

  public IFile getFileTarget( )
  {
    return m_fileTarget;
  }

  public INativeObservationAdapter getNativeAdapter()
  {
    return m_selectedAdapter;
  }

  public boolean isRetainMetadata()
  {
    return m_retainMetadata;
  }

  public TimeZone getSourceTimezone( )
  {
    return m_sourceTimezone;
  }
}