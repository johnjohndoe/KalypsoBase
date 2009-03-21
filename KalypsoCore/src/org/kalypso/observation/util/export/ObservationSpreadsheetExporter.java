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
package org.kalypso.observation.util.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Exports an IObservation<TupleResult> to an CSV-Spreadsheet
 *
 * @author Dirk Kuch
 */
public class ObservationSpreadsheetExporter implements ICoreRunnableWithProgress
{

  private final IObservation<TupleResult> m_observation;

  private final IFile m_file;

  public ObservationSpreadsheetExporter( final IObservation<TupleResult> observation, final IFile file )
  {
    m_observation = observation;
    m_file = file;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final FileWriter fileWriter = new FileWriter( m_file.getLocation().toFile() );

      final CSVWriter writer = new CSVWriter( fileWriter, ';', '"' );

      final TupleResult result = m_observation.getResult();

      /* header */
      final IComponent[] components = result.getComponents();
      final List<String> values = new ArrayList<String>();

      for( final IComponent component : components )
      {
        values.add( component.getName() );
      }

      writer.writeNext( values.toArray( new String[] {} ) );

      /* values */
      for( final IRecord record : result )
      {

        values.clear();
        for( int i = 0; i < components.length; i++ )
        {
          final Object value = record.getValue( i );
          values.add( value.toString() );
        }

        writer.writeNext( values.toArray( new String[] {} ) );
      }

      writer.close();
      fileWriter.close();
    }
    catch( final IOException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.createErrorStatus( "Exporting observation as spreadsheet failed." ) );
    }

    return Status.OK_STATUS;
  }

}
