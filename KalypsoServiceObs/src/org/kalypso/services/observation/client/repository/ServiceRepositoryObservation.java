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
package org.kalypso.services.observation.client.repository;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.cache.ObservationCache;
import org.kalypso.ogc.sensor.event.IObservationListener;
import org.kalypso.ogc.sensor.event.ObservationChangeType;
import org.kalypso.ogc.sensor.event.ObservationEventAdapter;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.util.Observations;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.services.observation.ObservationServiceUtils;
import org.kalypso.services.observation.sei.DataBean;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.services.observation.sei.ObservationBean;
import org.xml.sax.InputSource;

/**
 * An IObservation that comes from the Kalypso OCS.
 * 
 * @author schlienger
 */
public class ServiceRepositoryObservation implements IObservation
{
  private final IObservationService m_service;

  private final ObservationBean m_bean;

  private IObservation m_observation = null;

  private final ObservationEventAdapter m_evtPrv = new ObservationEventAdapter( this );

  private MetadataList m_beanMedatadata;

  public ServiceRepositoryObservation( final IObservationService service, final ObservationBean bean )
  {
    m_service = service;
    m_bean = bean;
  }

  /**
   * Lazy loading.
   * 
   * @return IObservation loaded from the server
   */
  private IObservation getRemote( final IRequest args ) throws SensorException
  {
    if( args == null && m_observation != null )
      return m_observation;

    m_observation = loadFromServer( args );

    return m_observation;
  }

  /**
   * Uses the webservice to request the observation.
   * 
   * @return IObservation loaded from the server
   */
  private IObservation loadFromServer( final IRequest args ) throws SensorException
  {
    String href = m_bean.getId();
    if( args != null )
      href = org.kalypso.ogc.sensor.zml.ZmlURL.insertRequest( href, args );

    InputStream ins = null;

    try
    {
      final DataBean db = m_service.readData( href );

      ins = new BufferedInputStream( db.getDataHandler().getInputStream() );
      final IObservation obs = ZmlFactory.parseXML( new InputSource( ins ), null ); //$NON-NLS-1$
      ins.close();

      m_service.clearTempData( db.getId() );

      return obs;
    }
    catch( final Exception e ) // generic exception caught for simplicity
    {
      throw new SensorException( e );
    }
    finally
    {
      IOUtils.closeQuietly( ins );
    }
  }

  @Override
  public final String getName( )
  {
    return m_bean.getName();
  }

  @Override
  public final MetadataList getMetadataList( )
  {
    if( Objects.isNotNull( m_observation ) )
    {
      /** merge metadata otherwise new properties will get lost */
      if( m_beanMedatadata != null )
        return merge( m_observation.getMetadataList(), m_beanMedatadata );

      return m_observation.getMetadataList();
    }
    else if( Objects.isNotNull( m_beanMedatadata ) )
      return m_beanMedatadata;

    m_beanMedatadata = new MetadataList();
    final Map<Object, Object> entries = m_bean.getMetadataList();
    for( final Entry<Object, Object> entry : entries.entrySet() )
      m_beanMedatadata.put( entry.getKey(), entry.getValue() );

    return m_beanMedatadata;
  }

  /**
   * appended "additional"/new properties from supplement to base meta data list
   */
  private MetadataList merge( final MetadataList base, final MetadataList supplement )
  {
    final Set<Entry<Object, Object>> entries = supplement.entrySet();
    for( final Entry<Object, Object> entry : entries )
    {
      final String key = (String) entry.getKey();

      final String property = base.getProperty( key, "" ); //$NON-NLS-1$
      if( StringUtils.isEmpty( property ) )
        base.setProperty( key, (String) entry.getValue() );
    }

    return base;
  }

  @Override
  public final IAxis[] getAxes( )
  {
    try
    {
      if( Objects.isNotNull( m_observation ) )
        return m_observation.getAxes();

      return getRemote( null ).getAxes();
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
      throw new IllegalStateException( e.getLocalizedMessage() );
    }
  }

  @Override
  public final synchronized ITupleModel getValues( final IRequest args ) throws SensorException
  {
    ITupleModel values = ObservationCache.getInstance().getValues( this );

    if( Objects.isNull( values ) )
    {
      values = getRemote( args ).getValues( null );
      ObservationCache.getInstance().addValues( this, values );
    }

    return values;
  }

  @Override
  @Deprecated
  public final void setValues( final ITupleModel values )
  {
    throw new UnsupportedOperationException( "Not used anymore. Use repository#setData instead" ); //$NON-NLS-1$
  }

  @Override
  public final void addListener( final IObservationListener listener )
  {
    m_evtPrv.addListener( listener );
  }

  @Override
  public final void removeListener( final IObservationListener listener )
  {
    m_evtPrv.removeListener( listener );
  }

  @Override
  public final void fireChangedEvent( final Object source, final ObservationChangeType type )
  {
    m_evtPrv.fireChangedEvent( source, type );
  }

  @Override
  public final String getHref( )
  {
    return ObservationServiceUtils.addServerSideId( m_bean.getId() );
  }

  @Override
  public void accept( final IObservationVisitor visitor, final IRequest request, final int direction ) throws SensorException
  {
    Observations.accept( this, visitor, request, direction );
  }

  @Override
  public boolean isEmpty( )
  {
    // TODO
    return false;
  }
}