/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.core.ant.copyobservation;

import java.io.File;
import java.net.URL;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.contribs.java.util.logging.LoggerUtilities;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.timeseries.datasource.IDataSourceItem;
import org.kalypso.ogc.sensor.timeseries.merged.MergedObservation;
import org.kalypso.ogc.sensor.timeseries.merged.ObservationSource;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.simulation.core.ant.AbstractMonitoredFeatureVisitor;
import org.kalypso.simulation.core.ant.copyobservation.target.ICopyObservationTarget;
import org.kalypso.simulation.core.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author Gernot Belger
 * @author Dirk Kuch
 */
public class CopyObservationFeatureVisitor extends AbstractMonitoredFeatureVisitor implements FeatureVisitor
{
  private final ILogger m_logger;

  private final MetadataList m_metadata;

  private final ICopyObservationTarget m_target;

  private final ICopyObservationSource m_sources;

  /**
   * @param metadata
   *          All entries will be added to the target observation
   */
  public CopyObservationFeatureVisitor( final ICopyObservationSource source, final ICopyObservationTarget target, final MetadataList metadata, final ILogger logger )
  {
    m_sources = source;
    m_target = target;
    m_metadata = metadata;
    m_logger = logger;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureVisitor#visit(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public final boolean visit( final Feature feature )
  {
    try
    {
      final String targetHref = m_target.getTargetHref( feature );
      setCurrentSubTask( targetHref );

      final ObservationSource[] sources = m_sources.getObservationSources( feature );

      final URL targetLocation = UrlResolverSingleton.getDefault().resolveURL( m_target.getContext(), targetHref );
      final File targetFile = getTargetFile( targetLocation );

      final MergedObservation result = new MergedObservation( targetLocation.toString(), sources );

      final MetadataList metadata = result.getMetadataList();
      updateMetaData( metadata, feature );

      CopyObservationHelper.setCopyObservationSources( metadata, sources );

      final IRequest request = new ObservationRequest( m_target.getTargetDateRange() );
      writeTargetObservation( targetFile, result, request );

      refreshWorkspace( targetLocation );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      m_logger.log( Level.WARNING, LoggerUtilities.CODE_SHOW_DETAILS, Messages.getString( "org.kalypso.ogc.util.CopyObservationFeatureVisitor.3" ) + feature == null ? "" : feature.getId() + "\t" + e.getLocalizedMessage() );//$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }

    return true;
  }

  private void refreshWorkspace( final URL targetLocation )
  {
    try
    {
      final IFile iTarget = getTargetResource( targetLocation );
      if( iTarget != null )
      {
        // FIXME: is this enough? What happens if the mkdir on the local file creates new folders?
        iTarget.refreshLocal( DEPTH_INFINITE, null );
      }
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      m_logger.log( Level.WARNING, LoggerUtilities.CODE_SHOW_DETAILS, "refreshing local workspace failed" );
    }

  }

  private File getTargetFile( final URL targetLocation )
  {
    final IFile eclipseFile = ResourceUtilities.findFileFromURL( targetLocation );
    if( eclipseFile != null )
      return eclipseFile.getLocation().toFile();

    // TODO: resolve local file url
    if( "file".equals( targetLocation.getProtocol() ) )
      return new File( targetLocation.getFile() );

    return null;
  }

  private IFile getTargetResource( final URL targetLocation )
  {
    return ResourceUtilities.findFileFromURL( targetLocation );
  }

  private void writeTargetObservation( final File file, final IObservation resultObs, final IRequest request ) throws SensorException
  {
    file.getParentFile().mkdirs();
    ZmlFactory.writeToFile( resultObs, file, request );
  }

  private void updateMetaData( final MetadataList metadata, final Feature feature )
  {

    for( final Entry<Object, Object> entry : m_metadata.entrySet() )
    {
      /* don't overwrite data source entries! */
      final String metaKey = (String) entry.getKey();
      if( metaKey.startsWith( IDataSourceItem.MD_DATA_SOURCE_ITEM ) )
        continue;
      else if( metaKey.startsWith( IDataSourceItem.MD_DATA_SOURCE_ITEM_REPOSITORY ) )
        continue;

      final String metaValue = FeatureHelper.tokenReplace( feature, (String) entry.getValue() );
      metadata.put( metaKey, metaValue );
    }

    /* set forecast meta data, might be used in diagram for instance to mark the forecast range */
    MetadataHelper.setTargetForecast( metadata, m_target.getTargetForecastDateRange() );
    MetadataHelper.setTargetDateRange( metadata, m_target.getTargetDateRange() );
  }

}
