/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.simulation.core.ant.copyobservation.source;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.FilterFactory;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.request.RequestFactory;
import org.kalypso.ogc.sensor.timeseries.merged.ObservationSource;
import org.kalypso.ogc.sensor.timeseries.merged.Source;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.ogc.sensor.zml.ZmlURL;
import org.kalypso.simulation.core.KalypsoSimulationCorePlugin;
import org.kalypso.simulation.core.ant.copyobservation.CopyObservationHelper;
import org.kalypso.simulation.core.ant.copyobservation.ICopyObservationSource;
import org.kalypso.simulation.core.i18n.Messages;
import org.kalypso.zml.request.Request;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractCopyObservationSource implements ICopyObservationSource
{
  private final Source[] m_sources;

  /**
   * Die Liste der Tokens und deren Ersetzung in der Form:
   * <p>
   * tokenName-featurePropertyName;tokenName-featurePropertyName;...
   * <p>
   * Die werden benutzt um token-replace im Zml-Href durchzuführen (z.B. um automatisch der Name der Feature als
   * Request-Name zu setzen)
   */
  private final URL m_context;

  public AbstractCopyObservationSource( final URL context, final Source[] sources )
  {
    m_context = context;
    m_sources = sources;
  }

  @Override
  public final ObservationSource[] getObservationSources( final Feature feature ) throws MalformedURLException, SensorException
  {
    final List<ObservationSource> sources = new ArrayList<ObservationSource>();
    for( final Source source : m_sources )
    {
      final IObservation observation = getObservation( feature, source );
      if( observation != null )
        sources.add( new ObservationSource( source, observation ) );
    }

    return sources.toArray( new ObservationSource[] {} );
  }

  protected abstract String getSourceLinkHref( Feature feature, final Source source );

  protected abstract Properties getReplaceTokens( final Feature feature );

  private IObservation getObservation( final Feature feature, final Source source ) throws MalformedURLException, SensorException
  {
    final String sourceHref = getSourceLinkHref( feature, source );
    if( sourceHref == null )
      return null;

    final String hrefWithFilter = ZmlURL.insertQueryPart( sourceHref, source.getFilter() );

    // filter variable might also contain request spec
    String hrefWithFilterAndRange = ZmlURL.insertRequest( hrefWithFilter, new ObservationRequest( source.getDateRange() ) );

    // token replacement
    final Properties properties = getReplaceTokens( feature );
    if( properties != null )
      hrefWithFilterAndRange = StringUtilities.replaceAll( hrefWithFilterAndRange, properties );

    final URL sourceURL = new UrlResolver().resolveURL( m_context, hrefWithFilterAndRange );

    try
    {
      final IObservation observation = ZmlFactory.parseXML( sourceURL );
      observation.getValues( null ); // trigger an potential exception

      return observation;
    }
    catch( final Throwable t )
    {
      final Request requestType = RequestFactory.parseRequest( hrefWithFilterAndRange );
      if( requestType == null )
        throw new SensorException( Messages.getString( "org.kalypso.ogc.util.CopyObservationFeatureVisitor.10" ) + hrefWithFilterAndRange, t );//$NON-NLS-1$

      // observation could not be created, use the request now
      final String message = String.format( "Abruf von '%s' fehlgeschlagen. Erzeuge synthetische Zeitreihe.", sourceHref );
      KalypsoSimulationCorePlugin.getDefault().getLog().log( StatusUtilities.createWarningStatus( message ) );
      final SimpleObservation synteticObservation = RequestFactory.createDefaultObservation( requestType );
      synteticObservation.setHref( sourceHref );

      CopyObservationHelper.setCopyObservationSources( synteticObservation.getMetadataList(), new ObservationSource( source, synteticObservation ) );

      return FilterFactory.createFilterFrom( source.getFilter(), synteticObservation, null );
    }
  }
}
