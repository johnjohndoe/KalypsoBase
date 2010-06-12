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
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.contribs.java.util.logging.LoggerUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITuppleModel;
import org.kalypso.ogc.sensor.MetadataList;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.timeseries.TimeserieUtils;
import org.kalypso.ogc.sensor.timeseries.forecast.ForecastTuppleModel;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.simulation.core.ant.AbstractMonitoredFeatureVisitor;
import org.kalypso.simulation.core.ant.copyobservation.source.ObservationSource;
import org.kalypso.simulation.core.ant.copyobservation.target.ICopyObservationTarget;
import org.kalypso.simulation.core.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author Gernot Belger
 */
public class CopyObservationFeatureVisitor extends AbstractMonitoredFeatureVisitor implements FeatureVisitor
{
  private final ILogger m_logger;

  private final Properties m_metadata;

  private final ICopyObservationTarget m_target;

  private final ICopyObservationSource m_sources;

  /**
   * @param metadata
   *          All entries will be added to the target observation
   */
  public CopyObservationFeatureVisitor( final ICopyObservationSource source, final ICopyObservationTarget target, final Properties metadata, final ILogger logger )
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

      final IObservation resultObs = combineResultObservation( sources );
      updateMetaData( resultObs, sources );
      udpateMetaData( resultObs, feature );

      final IRequest request = new ObservationRequest( m_target.getTargetDateRange() );
      writeTargetObservation( targetFile, resultObs, request );

      final IFile targetfile = getTargetResource( targetLocation );
      if( targetfile != null )
      {
        // FIXKME: is this enough? What happens if the mkdir on the local file creates new folders?
        targetfile.refreshLocal( DEPTH_INFINITE, null );
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      m_logger.log( Level.WARNING, LoggerUtilities.CODE_SHOW_DETAILS, Messages.getString( "org.kalypso.ogc.util.CopyObservationFeatureVisitor.3" ) + feature == null ? "" : feature.getId() + "\t" + e.getLocalizedMessage() );//$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }

    return true;
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

  private void udpateMetaData( final IObservation resultObs, final Feature feature )
  {
    if( feature == null )
      return;

    // put additional metadata that we got from outside
    final MetadataList mdl = resultObs.getMetadataList();

    for( final Entry<Object, Object> element : m_metadata.entrySet() )
    {
      final Entry<Object, Object> entry = element;
      final String metaValue = FeatureHelper.tokenReplace( feature, (String) entry.getValue() );
      final String metaKey = (String) entry.getKey();
      mdl.put( metaKey, metaValue );
    }
  }

  private void updateMetaData( final IObservation resultObs, final ObservationSource[] sources )
  {
    /* set forecast metadata, might be used in diagram for instance to mark the forecast range */
    TimeserieUtils.setTargetForecast( resultObs, m_target.getTargetForecastDateRange() );
    TimeserieUtils.setTargetDateRange( resultObs, m_target.getTargetDateRange() );

    // put additional metadata that we got from outside
    final MetadataList mdl = resultObs.getMetadataList();

    int count = 0;
    for( final ObservationSource source : sources )
    {
      mdl.putAll( CopyObservationHelper.getSourceMetadataSettings( source, count ) );
      count++;
    }
  }

  private IObservation combineResultObservation( final ObservationSource[] sources ) throws SensorException
  {
    final List<ITuppleModel> models = new ArrayList<ITuppleModel>();
    for( final ObservationSource source : sources )
    {
      final IObservation observation = source.getObservation();
      final ObservationRequest request = new ObservationRequest( source.getDateRange() );

      models.add( observation.getValues( request ) );
    }

    if( models.size() == 0 )
      return null;

    final ForecastTuppleModel tuppleModel = new ForecastTuppleModel( models.toArray( new ITuppleModel[] {} ) );

    final IObservation baseObservation = sources[0].getObservation();
    final MetadataList metadataList = (MetadataList) baseObservation.getMetadataList().clone();
    final IAxis[] axes = baseObservation.getAxisList();

    return new SimpleObservation( null, null, null, false, metadataList, axes, tuppleModel );
  }
}
