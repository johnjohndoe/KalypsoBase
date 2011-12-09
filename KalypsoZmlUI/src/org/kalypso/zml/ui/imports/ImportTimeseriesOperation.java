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
package org.kalypso.zml.ui.imports;

import java.io.File;
import java.util.TimeZone;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.zml.ui.KalypsoZmlUI;

/**
 * @author Gernot Belger
 */
public class ImportTimeseriesOperation implements ICoreRunnableWithProgress
{
  private final ImportObservationData m_data;

  public ImportTimeseriesOperation( final ImportObservationData data )
  {
    m_data = data;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    final IObservation srcObservation = createSourceObservation();

    // TODO: enforce some metadata

    // TODO: do other stuff as well?

    writeResult( srcObservation );

    return Status.OK_STATUS;
  }

  private void writeResult( final IObservation newObservation ) throws CoreException
  {
    try
    {
      final IFile targetFile = m_data.getTargetFile();
      ZmlFactory.writeToFile( newObservation, targetFile );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final IStatus status = new Status( IStatus.ERROR, KalypsoZmlUI.PLUGIN_ID, "Failed to write result file", e );
      throw new CoreException( status );
    }
  }

  private IObservation createSourceObservation( ) throws CoreException
  {
    try
    {
      final File fileSource = m_data.getSourceFileData().getFile();
      final TimeZone timezone = m_data.getTimezoneParsed();
      final INativeObservationAdapter nativaAdapter = m_data.getAdapter();

      return nativaAdapter.createObservationFromSource( fileSource, timezone, false );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final String message = String.format( "Failed to import timeseries" );
      final IStatus status = new Status( IStatus.ERROR, KalypsoZmlUI.PLUGIN_ID, message, e );
      throw new CoreException( status );
    }
  }
}
