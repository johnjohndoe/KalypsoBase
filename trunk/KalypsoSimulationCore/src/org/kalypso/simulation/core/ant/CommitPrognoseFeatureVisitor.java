/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/

package org.kalypso.simulation.core.ant;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.observation.util.ObservationHelper;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.ITimeserieConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.ogc.sensor.zml.ZmlURL;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IWriteableRepository;
import org.kalypso.repository.RepositoryException;
import org.kalypso.repository.utils.RepositoryUtils;
import org.kalypso.simulation.core.KalypsoSimulationCorePlugin;
import org.kalypso.simulation.core.i18n.Messages;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;

/**
 * This feature visitor copies one timeserie over another. It is used to commit the prognose timeseries to the IMS.
 * 
 * @author schlienger
 */
public class CommitPrognoseFeatureVisitor extends AbstractMonitoredFeatureVisitor implements FeatureVisitor
{
  private static final Logger LOG = Logger.getLogger( CommitPrognoseFeatureVisitor.class.getName() );

  private final DateRange m_dateRange;

  private final Collection<IStatus> m_stati = new ArrayList<IStatus>();

  private final String m_sourceTS;

  private final String m_targetTS;

  private final URL m_context;

  private final String m_sourceFilter;

  public CommitPrognoseFeatureVisitor( final URL context, final String sourceTS, final String targetTS, final String sourceFilter, final DateRange dateRange )
  {
    m_context = context;
    m_sourceFilter = sourceFilter;
    m_sourceTS = sourceTS;
    m_targetTS = targetTS;
    m_dateRange = dateRange;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureVisitor#visit(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public final boolean visit( final Feature f )
  {
    m_stati.add( work( f ) );

    return true;
  }

  private IStatus work( final Feature f )
  {
    final String id = f.getId();

    final TimeseriesLinkType sourceLink = (TimeseriesLinkType) f.getProperty( m_sourceTS );
    final TimeseriesLinkType targetLink = (TimeseriesLinkType) f.getProperty( m_targetTS );
    if( sourceLink == null )
      return new Status( IStatus.WARNING, KalypsoSimulationCorePlugin.getID(), 0, Messages.getString( "org.kalypso.services.observation.client.CommitPrognoseFeatureVisitor.0", m_sourceTS, id ), null ); //$NON-NLS-1$

    if( targetLink == null )
      return new Status( IStatus.WARNING, KalypsoSimulationCorePlugin.getID(), 0, Messages.getString( "org.kalypso.services.observation.client.CommitPrognoseFeatureVisitor.0", m_targetTS, id ), null ); //$NON-NLS-1$

    final String sourceHref = sourceLink.getHref();
    final String targetHref = targetLink.getHref();

    setCurrentSubTask( sourceHref );

    try
    {
      return doIt( sourceHref, targetHref );
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      return StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.services.observation.client.CommitPrognoseFeatureVisitor.1" ) + id ); //$NON-NLS-1$
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
      return StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.services.observation.client.CommitPrognoseFeatureVisitor.2" ) + id ); //$NON-NLS-1$
    }
    catch( final RepositoryException e )
    {
      e.printStackTrace();
      return StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.services.observation.client.CommitPrognoseFeatureVisitor.2" ) + id ); //$NON-NLS-1$
    }
  }

  private IStatus doIt( final String sourceHref, final String targetHref ) throws MalformedURLException, SensorException, RepositoryException
  {
    final String filteredSourceHref = createFilteredSourceHref( sourceHref );

    final IUrlResolver resolver = UrlResolverSingleton.getDefault();

    final URL urlRS = resolver.resolveURL( m_context, filteredSourceHref );
    final IObservation source = ZmlFactory.parseXML( urlRS );

    try
    {
      // IMPORTANT: Die ZIELzeitreihe bestimmt, welche Achsen geschrieben werden!
      // Die Datenhaltung muss die Metadaten von WISKI beziehen
      // Wenn Daten in die Datenhaltung geschrieben werden, dürfen nur die Werte, keine
      // Metadaten o.ä. geändert werden.
      final IObservation targetTemplateObservation = fetchTargetTemplate( targetHref );
      final IAxis[] targetAxes = fetchTargetAxes( targetTemplateObservation );

      // copy values from source into dest, expecting full compatibility
      final IObservation target = optimisticValuesCopy( source, targetAxes );
      if( target == null )
        return StatusUtilities.createErrorStatus( "Fehler beim Ablegen der Ergebniszeitreihen. Konnte Werte nicht in die Zielzeitreihe kopieren" );

      final IRepository repository = RepositoryUtils.findRegisteredRepository( targetHref );
      if( repository instanceof IWriteableRepository )
      {
        final IWriteableRepository writeable = (IWriteableRepository) repository;

        final byte[] data = ObservationHelper.flushToByteArray( target );
        writeable.setData( targetHref, data );

        LOG.info( "Observation saved on server: " + targetHref ); //$NON-NLS-1$
      }
      else
      {
        if( repository == null )
          throw new RepositoryException( String.format( "Konnte Zeitreihen-Repository für '%s' nicht finden.", targetHref ) );
        else
          throw new RepositoryException( String.format( "Zeitreihen-Repository '%s' ist nicht beschreibbar ", repository.getLabel() ) );
      }

    }
    catch( final IllegalArgumentException e )
    {
      return new Status( IStatus.WARNING, KalypsoSimulationCorePlugin.getID(), 0, Messages.getString( "org.kalypso.services.observation.client.CommitPrognoseFeatureVisitor.3", source ), e );
    }

    return Status.OK_STATUS;
  }

  private IAxis[] fetchTargetAxes( final IObservation templateObservation )
  {
    final Collection<IAxis> result = new ArrayList<IAxis>();

    final IAxis[] axisList = templateObservation.getAxisList();

    for( final IAxis axis : axisList )
    {
      if( KalypsoStatusUtils.isStatusAxis( axis ) )
        continue;

      if( axis.isPersistable() || axis.isKey() )
        result.add( axis );
    }

    return result.toArray( new IAxis[result.size()] );
  }

  private IObservation fetchTargetTemplate( final String targetHref ) throws SensorException, MalformedURLException
  {
    // leeres Request-Intervall, wir wollen eigentlich nur die Metadaten+Achsen
    final String destRef = ZmlURL.insertRequest( targetHref, new ObservationRequest( new Date(), new Date() ) );
    final URL urlPG = UrlResolverSingleton.resolveUrl( m_context, destRef );
    return ZmlFactory.parseXML( urlPG );
  }

  private String createFilteredSourceHref( final String sourceHref )
  {
    final String filteredSourceHref;
    if( m_sourceFilter != null && m_sourceFilter.length() > 0 && sourceHref.indexOf( '?' ) == -1 )
      filteredSourceHref = sourceHref + "?" + m_sourceFilter; //$NON-NLS-1$
    else
      filteredSourceHref = sourceHref;
    return filteredSourceHref;
  }

  public final IStatus[] getStati( )
  {
    return m_stati.toArray( new IStatus[m_stati.size()] );
  }

  private IObservation optimisticValuesCopy( final IObservation source, final IAxis[] targetAxes ) throws SensorException, IllegalStateException
  {
    // leeres Request-Intervall, wir wollen final eigentlich nur die Metadaten+Achsen
    final String href = source.getHref();
    final String name = source.getName();

    final MetadataList targetMetadata = copyMetadata( source );

    final SimpleObservation target = new SimpleObservation( href, name, targetMetadata, targetAxes );

    final IAxis[] srcAxes = source.getAxisList();

    final IRequest request = getRequest();
    final ITupleModel values = source.getValues( request );
    if( values == null )
      return null;

    final Map<IAxis, IAxis> map = new HashMap<IAxis, IAxis>();
    for( int i = 0; i < targetAxes.length; i++ )
    {
      try
      {
        final IAxis axis = ObservationUtilities.findAxisByType( srcAxes, targetAxes[i].getType() );

        map.put( targetAxes[i], axis );
      }
      catch( final NoSuchElementException e )
      {
        if( !KalypsoStatusUtils.isStatusAxis( targetAxes[i] ) )
          throw new IllegalStateException( "Required axis" + targetAxes[i] + " from" + target + " could not be found in" + source );

        // else ignored, try with next one
      }
    }

    if( map.size() == 0 || values.getCount() == 0 )
      return null;

    final SimpleTupleModel model = new SimpleTupleModel( targetAxes );
    for( int i = 0; i < values.getCount(); i++ )
    {
      final Object[] tupple = new Object[targetAxes.length];

      for( int j = 0; j < targetAxes.length; j++ )
      {
        final IAxis srcAxis = map.get( targetAxes[j] );
        if( srcAxis != null )
          tupple[j] = values.getElement( i, srcAxis );
      }

      model.addTupple( tupple );
    }

    target.setValues( model );

    return target;
  }

  private MetadataList copyMetadata( final IObservation source )
  {
    final MetadataList metadataList = source.getMetadataList();
    final MetadataList targetMetadata = new MetadataList();
    targetMetadata.putAll( metadataList );

    targetMetadata.remove( ITimeserieConstants.MD_WQTABLE );
    targetMetadata.remove( ITimeserieConstants.MD_WQWECHMANN );
    return targetMetadata;
  }

  private IRequest getRequest( )
  {
    if( m_dateRange == null )
      return null;

    return new ObservationRequest( m_dateRange );
  }
}
