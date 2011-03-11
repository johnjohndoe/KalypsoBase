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

import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.IObservationListener;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
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

  /**
   * @see org.kalypso.ogc.sensor.filter.IObservationFilter#initFilter(java.lang.Object,
   *      org.kalypso.ogc.sensor.IObservation, java.net.URL)
   */
  @Override
  @SuppressWarnings("unused")
  // exception warning Ignored, else we got errors in implementors
  public void initFilter( final Object conf, final IObservation obs, final URL context ) throws SensorException
  {
    m_obs = obs;

    appendSettings( getMetadataList() );
  }

  protected abstract void appendSettings( MetadataList metadata );

  @Override
  public boolean equals( final Object obj )
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.0" ) ); //$NON-NLS-1$

    return m_obs.equals( obj );
  }

  @Override
  public IAxis[] getAxes( )
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.1" ) ); //$NON-NLS-1$

    return m_obs.getAxes();
  }

  @Override
  public MetadataList getMetadataList( )
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.3" ) ); //$NON-NLS-1$

    return m_obs.getMetadataList();
  }

  @Override
  public String getName( )
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.4" ) ); //$NON-NLS-1$

    return m_obs.getName();
  }

  @Override
  public ITupleModel getValues( final IRequest args ) throws SensorException
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.6" ) ); //$NON-NLS-1$

    return m_obs.getValues( args );
  }

  @Override
  public int hashCode( )
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.7" ) ); //$NON-NLS-1$

    return m_obs.hashCode();
  }

  @Override
  public void setValues( final ITupleModel values ) throws SensorException
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.9" ) ); //$NON-NLS-1$

    m_obs.setValues( values );
  }

  @Override
  public String toString( )
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.10" ) ); //$NON-NLS-1$

    return m_obs.toString();
  }

  @Override
  public void addListener( final IObservationListener listener )
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.11" ) ); //$NON-NLS-1$

    m_obs.addListener( listener );
  }

  @Override
  public void removeListener( final IObservationListener listener )
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.12" ) ); //$NON-NLS-1$

    m_obs.removeListener( listener );
  }

  @Override
  public void fireChangedEvent( final Object source )
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.14" ) ); //$NON-NLS-1$

    m_obs.fireChangedEvent( source );
  }

  @Override
  public String getHref( )
  {
    if( m_obs == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.filter.filters.AbstractObservationFilter.15" ) ); //$NON-NLS-1$

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

  /**
   * @see org.kalypso.ogc.sensor.IObservation#accept(org.kalypso.ogc.sensor.visitor.IObservationVisitor,
   *      org.kalypso.ogc.sensor.request.IRequest)
   */
  @Override
  public final void accept( final IObservationVisitor visitor, final IRequest request ) throws SensorException
  {
    Observations.accept( this, visitor, request );
  }
}