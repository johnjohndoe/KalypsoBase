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
import java.net.URL;
import java.util.TimeZone;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class ImportObservationOperation implements ICoreRunnableWithProgress
{
  private final ImportObservationData m_data;

  private final IAxis[] m_axesSrc;

  private final IAxis[] m_axesNew;

  private final ObservationImportSelection m_selection;

  public ImportObservationOperation( final ImportObservationData data, final IAxis[] axesSrc, final IAxis[] axesNew, final ObservationImportSelection selection )
  {
    m_data = data;
    m_axesSrc = axesSrc;
    m_axesNew = axesNew;
    m_selection = selection;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    final Pair<IStatus, IObservation> importResult = createSourceObservation();
    final IStatus importStatus = importResult.getLeft();
    final IObservation srcObservation = importResult.getRight();

    if( srcObservation != null )
    {
      final TargetObservation targetObservation = createTargetObservation();
      final IObservation newObservation = createNewObservation( srcObservation, targetObservation );

      writeResult( newObservation );
    }

    return importStatus;
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
      final IStatus status = new Status( IStatus.ERROR, KalypsoZmlUI.PLUGIN_ID, Messages.getString( "ImportObservationOperation.0" ), e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
  }

  private TargetObservation createTargetObservation( ) throws CoreException
  {
    try
    {
      final IFile fileTarget = m_data.getTargetFile();

      if( fileTarget.exists() && (m_selection.isAppend() || m_selection.isRetainMetadata()) )
      {
        final URL targetLocation = ResourceUtilities.createURL( fileTarget );
        final IObservation targetObservation = ZmlFactory.parseXML( targetLocation );
        final ITupleModel tuppelModelTarget = targetObservation.getValues( null );

        final int countTarget = m_selection.isAppend() ? tuppelModelTarget.size() : 0;

        return new TargetObservation( targetObservation, tuppelModelTarget, countTarget );
      }

      return new TargetObservation( null, null, 0 );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final IStatus status = new Status( IStatus.ERROR, KalypsoZmlUI.PLUGIN_ID, Messages.getString( "ImportObservationOperation.1" ), e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
  }

  private Pair<IStatus, IObservation> createSourceObservation( ) throws CoreException
  {
    try
    {
      final File fileSource = m_data.getSourceFileData().getFile();
      final TimeZone timezone = m_data.getTimezoneParsed();
      final INativeObservationAdapter nativaAdapter = m_data.getAdapter();

      final IStatus status = nativaAdapter.doImport( fileSource, timezone, false );

      final IObservation srcObservation = nativaAdapter.getObservation();
//      if( srcObservation != null )
//        return srcObservation;
//
//      final String title = Messages.getString( "org.kalypso.ui.wizards.imports.observation.ImportObservationWizard.2" ); //$NON-NLS-1$
//      final String message = Messages.getString( "org.kalypso.ui.wizards.imports.observation.ImportObservationWizard.3" ); //$NON-NLS-1$
//
//      if( !MessageDialog.openQuestion( m_shell, title, message ) )
//        return null;
//
//      /* status = */nativaAdapter.doImport( fileSource, timezone, true );
//      return nativaAdapter.getObservation();

      return Pair.of( status, srcObservation );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final String message = String.format( Messages.getString( "ImportObservationOperation.2" ) ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoZmlUI.PLUGIN_ID, message, e );
      throw new CoreException( status );
    }
  }

  private IObservation createNewObservation( final IObservation srcObservation, final TargetObservation targetObservation ) throws CoreException
  {
    try
    {
      final ITupleModel srcModel = srcObservation.getValues( null );
      final int countSrc = srcModel.size();

      targetObservation.fillSourceData( countSrc, srcModel, m_axesSrc, m_axesNew );

      final String name = srcObservation.getName();

      final MetadataList metadata = createMetadata( srcObservation, targetObservation );

      return targetObservation.createNewObservation( name, metadata );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
      final IStatus status = new Status( IStatus.ERROR, KalypsoZmlUI.PLUGIN_ID, Messages.getString( "ImportObservationOperation.3" ), e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
  }

  private MetadataList createMetadata( final IObservation srcObservation, final TargetObservation targetObservation )
  {
    final MetadataList metadata = new MetadataList();
    if( m_selection.isRetainMetadata() )
      metadata.putAll( targetObservation.getMetadataList() );
    metadata.putAll( srcObservation.getMetadataList() );
    return metadata;
  }
}
