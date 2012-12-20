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
package org.kalypso.ogc.sensor.filter.filters;

import java.net.URL;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.event.IObservationListener;
import org.kalypso.ogc.sensor.event.ObservationChangeType;
import org.kalypso.ogc.sensor.filter.IObservationFilter;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.util.Observations;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;

/**
 * AbstractObservationFilter
 * 
 * @author schlienger
 */
public abstract class AbstractObservationFilter implements IObservationFilter
{
  private IObservation m_obs = null;

  @SuppressWarnings( "unused" )
  @Override
  // exception warning Ignored, else we got errors in implementors
  public void initFilter( final Object conf, final IObservation obs, final URL context ) throws SensorException
  {
    m_obs = obs;
    if( m_obs != null )
      appendSettings( getMetadataList() );
  }

  protected abstract void appendSettings( MetadataList metadata );

  private void checkObservation( )
  {
    if( m_obs == null )
      throw new IllegalStateException( "Internal observation is null" ); //$NON-NLS-1$
  }

  @Override
  public boolean equals( final Object obj )
  {
    checkObservation();

    return m_obs.equals( obj );
  }

  @Override
  public IAxis[] getAxes( )
  {
    checkObservation();

    return m_obs.getAxes();
  }

  @Override
  public MetadataList getMetadataList( )
  {
    checkObservation();

    return m_obs.getMetadataList();
  }

  @Override
  public String getName( )
  {
    checkObservation();

    return m_obs.getName();
  }

  @Override
  public ITupleModel getValues( final IRequest args ) throws SensorException
  {
    checkObservation();

    return m_obs.getValues( args );
  }

  @Override
  public int hashCode( )
  {
    checkObservation();

    return m_obs.hashCode();
  }

  @Override
  public void setValues( final ITupleModel values ) throws SensorException
  {
    checkObservation();

    m_obs.setValues( values );
  }

  @Override
  public String toString( )
  {
    checkObservation();

    return m_obs.toString();
  }

  @Override
  public void addListener( final IObservationListener listener )
  {
    checkObservation();

    m_obs.addListener( listener );
  }

  @Override
  public void removeListener( final IObservationListener listener )
  {
    checkObservation();

    m_obs.removeListener( listener );
  }

  @Override
  public void fireChangedEvent( final Object source, final ObservationChangeType type )
  {
    checkObservation();

    m_obs.fireChangedEvent( source, type );
  }

  @Override
  public String getHref( )
  {
    checkObservation();

    return m_obs.getHref();
  }

  protected IObservation getObservation( )
  {
    return m_obs;
  }

  protected void setObservation( final IObservation observation )
  {
    m_obs = observation;
  }

  @Override
  public final void accept( final IObservationVisitor visitor, final IRequest request, final int direction ) throws SensorException
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