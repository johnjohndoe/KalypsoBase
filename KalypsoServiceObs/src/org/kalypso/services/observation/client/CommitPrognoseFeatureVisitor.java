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

package org.kalypso.services.observation.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.observation.util.ObservationHelper;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITuppleModel;
import org.kalypso.ogc.sensor.MetadataList;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTuppleModel;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IWriteableRepository;
import org.kalypso.repository.RepositoryException;
import org.kalypso.repository.utils.RepositoryUtils;
import org.kalypso.services.observation.KalypsoServiceObsActivator;
import org.kalypso.services.observation.i18n.Messages;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;

/**
 * This feature visitor copies one timeserie over another. It is used to commit the prognose timeseries to the IMS.
 * 
 * @author schlienger
 */
public class CommitPrognoseFeatureVisitor implements FeatureVisitor
{
  private static final Logger LOG = Logger.getLogger( CommitPrognoseFeatureVisitor.class.getName() );

  private final Collection<IStatus> m_stati = new ArrayList<IStatus>();

  private final String m_sourceTS;

  private final String m_targetTS;

  private final URL m_context;

  private final String m_sourceFilter;

  public CommitPrognoseFeatureVisitor( final URL context, final String sourceTS, final String targetTS, final String sourceFilter )
  {
    m_context = context;
    m_sourceFilter = sourceFilter;
    m_sourceTS = sourceTS;
    m_targetTS = targetTS;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureVisitor#visit(org.kalypsodeegree.model.feature.Feature)
   */
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
      return new Status( IStatus.WARNING, KalypsoServiceObsActivator.getID(), 0, Messages.getString( "org.kalypso.services.observation.client.CommitPrognoseFeatureVisitor.0", m_sourceTS, id ), null ); //$NON-NLS-1$

    if( targetLink == null )
      return new Status( IStatus.WARNING, KalypsoServiceObsActivator.getID(), 0, Messages.getString( "org.kalypso.services.observation.client.CommitPrognoseFeatureVisitor.0", m_targetTS, id ), null ); //$NON-NLS-1$

    final String sourceHref = sourceLink.getHref();
    final String targetHref = targetLink.getHref();
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
    final String filteredSourceHref;
    if( m_sourceFilter != null && m_sourceFilter.length() > 0 && sourceHref.indexOf( '?' ) == -1 )
      filteredSourceHref = sourceHref + "?" + m_sourceFilter; //$NON-NLS-1$
    else
      filteredSourceHref = sourceHref;

    final IUrlResolver resolver = UrlResolverSingleton.getDefault();

    final URL urlRS = resolver.resolveURL( m_context, filteredSourceHref );
    final IObservation source = ZmlFactory.parseXML( urlRS, filteredSourceHref );

    try
    {
      // copy values from source into dest, expecting full compatibility

      final IObservation target = optimisticValuesCopy( source );
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
        throw new NotImplementedException();

    }
    catch( final IllegalArgumentException e )
    {
      return new Status( IStatus.WARNING, KalypsoServiceObsActivator.getID(), 0, Messages.getString( "org.kalypso.services.observation.client.CommitPrognoseFeatureVisitor.3", source ), e );
    }

    return Status.OK_STATUS;
  }

  public final IStatus[] getStati( )
  {
    return m_stati.toArray( new IStatus[m_stati.size()] );
  }

  /**
   * Copy the values from source into dest. Only copies the values of the axes that are found in the dest AND in source
   * observation.
   * 
   * @param source
   *          source observation from which values are read
   * @param dest
   *          destination observation into which values are copied
   * @param args
   *          [optional, can be null] variable arguments
   * @param fullCompatibilityExpected
   *          when true an InvalidStateException is thrown to indicate that the full compatibility cannot be guaranteed.
   *          The full compatibility is expressed in terms of the axes: the source observation must have the same axes
   *          as the dest observation. If false, just the axes from dest that where found in source are used, thus
   *          leading to potential null values in the tupple model
   * @return model if some values have been copied, null otherwise
   * @throws SensorException
   * @throws IllegalStateException
   *           when compatibility is wished but could not be guaranteed
   */
  private static IObservation optimisticValuesCopy( final IObservation source ) throws SensorException, IllegalStateException
  {

    // leeres Request-Intervall, wir wollen final eigentlich nur die Metadaten+Achsen
    final IAxis[] axes = source.getAxisList();
    final String href = source.getHref();
    final String identifier = source.getIdentifier();
    final String name = source.getName();
    final MetadataList metadataList = source.getMetadataList();

    final SimpleObservation target = new SimpleObservation( href, identifier, name, true, metadataList, axes );

    final IAxis[] srcAxes = source.getAxisList();
    final IAxis[] destAxes = target.getAxisList();

    final ITuppleModel values = source.getValues( null );
    if( values == null )
      return null;

    final Map<IAxis, IAxis> map = new HashMap<IAxis, IAxis>();
    for( int i = 0; i < destAxes.length; i++ )
    {
      try
      {
        final IAxis A = ObservationUtilities.findAxisByType( srcAxes, destAxes[i].getType() );

        map.put( destAxes[i], A );
      }
      catch( final NoSuchElementException e )
      {
        if( !KalypsoStatusUtils.isStatusAxis( destAxes[i] ) )
          throw new IllegalStateException( "Required axis" + destAxes[i] + " from" + target + " could not be found in" + source );

        // else ignored, try with next one
      }
    }

    if( map.size() == 0 || values.getCount() == 0 )
      return null;

    final SimpleTuppleModel model = new SimpleTuppleModel( destAxes );

    for( int i = 0; i < values.getCount(); i++ )
    {
      final Object[] tupple = new Object[destAxes.length];

      for( int j = 0; j < destAxes.length; j++ )
      {
        final IAxis srcAxis = map.get( destAxes[j] );

        if( srcAxis != null )
          tupple[model.getPositionFor( destAxes[j] )] = values.getElement( i, srcAxis );
        else if( KalypsoStatusUtils.isStatusAxis( destAxes[j] ) )
          tupple[model.getPositionFor( destAxes[j] )] = new Integer( KalypsoStati.BIT_OK );
      }

      model.addTupple( tupple );
    }

    target.setValues( model );

    return target;
  }
}
