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
package org.kalypso.gml.ui.coverage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.coverage.imports.CoverageFormats;
import org.kalypso.gml.ui.coverage.imports.ICoverageImporter;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;

/**
 * @author belger
 */
public final class ImportCoveragesOperation implements ICoreRunnableWithProgress
{
  private final String m_crs;

  private final IContainer m_dataContainer;

  private final ICoverageCollection m_coverageContainer;

  private final File[] m_selectedFiles;

  private final ImportCoverageData m_data;

  public ImportCoveragesOperation( final ImportCoverageData data )
  {
    m_data = data;
    m_crs = data.getSourceSRS();
    m_dataContainer = data.getDataContainer();
    m_coverageContainer = data.getCoverageContainer();
    m_selectedFiles = data.getSelectedFiles();
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final IStatusCollector log = new StatusCollector( KalypsoGmlUIPlugin.id() );
    final String taskName = Messages.getString( "org.kalypso.gml.ui.wizard.grid.AddRectifiedGridCoveragesWizard.5" ); //$NON-NLS-1$

    final SubMonitor progress = SubMonitor.convert( monitor, taskName, m_selectedFiles.length ); //$NON-NLS-1$
    final Collection<ICoverage> newCoverages = new ArrayList<>( m_selectedFiles.length );
    for( final File dataFile : m_selectedFiles )
    {
      final String name = dataFile.getName();

      try
      {
        final ICoverage coverage = importCoverage( dataFile, progress.newChild( 1, SubMonitor.SUPPRESS_NONE ) );
        newCoverages.add( coverage );

        log.add( IStatus.OK, name );
      }
      catch( final CoreException e )
      {
        log.add( e.getStatus().getSeverity(), name, e );
      }
    }

    m_data.setNewCoverages( newCoverages.toArray( new ICoverage[newCoverages.size()] ) );

    return log.asMultiStatus( taskName );
  }

  private ICoverage importCoverage( final File dataFile, final IProgressMonitor monitor ) throws CoreException
  {
    final ICoverageImporter importer = CoverageFormats.findImporter( dataFile );
    if( importer == null )
    {
      final String name = dataFile.getName();
      final IStatus status = new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), String.format( Messages.getString( "ImportCoveragesOperation.1" ), name ) ); //$NON-NLS-1$
      throw new CoreException( status );
    }

    return importer.importCoverage( m_coverageContainer, dataFile, m_crs, m_dataContainer, monitor );
  }
}