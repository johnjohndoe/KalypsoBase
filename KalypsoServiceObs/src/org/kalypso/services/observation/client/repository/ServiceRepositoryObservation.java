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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.IObservationListener;
import org.kalypso.ogc.sensor.ITuppleModel;
import org.kalypso.ogc.sensor.MetadataList;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.cache.ObservationCache;
import org.kalypso.ogc.sensor.event.ObservationEventAdapter;
import org.kalypso.ogc.sensor.request.IRequest;
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
  private final IObservationService m_srv;

  private final ObservationBean m_ob;

  private IObservation m_obs = null;

  private final ObservationEventAdapter m_evtPrv = new ObservationEventAdapter( this );

  public ServiceRepositoryObservation( final IObservationService srv, final ObservationBean ob )
  {
    m_srv = srv;
    m_ob = ob;
  }

  /**
   * Lazy loading.
   * 
   * @return IObservation loaded from the server
   */
  private IObservation getRemote( final IRequest args ) throws SensorException
  {
    if( args == null && m_obs != null )
      return m_obs;

    m_obs = loadFromServer( args );

    return m_obs;
  }

  /**
   * Uses the webservice to request the observation.
   * 
   * @return IObservation loaded from the server
   */
  private IObservation loadFromServer( final IRequest args ) throws SensorException
  {
    String href = m_ob.getId();
    if( args != null )
      href = org.kalypso.ogc.sensor.zml.ZmlURL.insertRequest( href, args );

    InputStream ins = null;

    try
    {
      final DataBean db = m_srv.readData( href );

      ins = new BufferedInputStream( db.getDataHandler().getInputStream() );
      final IObservation obs = ZmlFactory.parseXML( new InputSource( ins ), "", null ); //$NON-NLS-1$
      ins.close();

      m_srv.clearTempData( db.getId() );

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
  public final String getIdentifier( )
  {
    return ObservationServiceUtils.addServerSideId( m_ob.getId() );
  }

  @Override
  public final String getName( )
  {
    return m_ob.getName();
  }

  @Override
  public final boolean isEditable( )
  {
    try
    {
      return getRemote( null ).isEditable();
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getMetadataList()
   */
  @Override
  public final MetadataList getMetadataList( )
  {
    if( m_obs != null )
      return m_obs.getMetadataList();

    final MetadataList md = new MetadataList();

    final Map<Object, Object> omdl = m_ob.getMetadataList();
    for( final Entry<Object, Object> entry : omdl.entrySet() )
      md.put( entry.getKey(), entry.getValue() );

    return md;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getAxisList()
   */
  @Override
  public final IAxis[] getAxisList( )
  {
    try
    {
      return getRemote( null ).getAxisList();
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
      throw new IllegalStateException( e.getLocalizedMessage() );
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getValues(org.kalypso.ogc.sensor.request.IRequest)
   */
  @Override
  public final synchronized ITuppleModel getValues( final IRequest args ) throws SensorException
  {
    ITuppleModel values = ObservationCache.getInstance().getValues( this );

    if( values == null )
    {
      values = getRemote( args ).getValues( null );

      ObservationCache.getInstance().addValues( this, values );
    }

    return values;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#setValues(org.kalypso.ogc.sensor.ITuppleModel)
   * @deprecated
   */
  @Override
  @Deprecated
  public final void setValues( final ITuppleModel values )
  {
    throw new NotImplementedException( "Not used anymore. Use repository#setData instead" );
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
  public final void clearListeners( )
  {
    m_evtPrv.clearListeners();
  }

  @Override
  public final void fireChangedEvent( final Object source )
  {
    m_evtPrv.fireChangedEvent( source );
  }

  @Override
  public final String getHref( )
  {
    return getIdentifier();
  }
}