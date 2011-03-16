/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.zml.ui.core.provider.observation;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.provider.PooledObsProvider;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.ui.core.zml.TSLinkWithName;

/**
 * @author Dirk Kuch
 */
public class AsynchronousObservationProvider extends PooledObsProvider
{
  private final String m_type;

  public AsynchronousObservationProvider( final TSLinkWithName link )
  {
    this( link, null );
  }

  public AsynchronousObservationProvider( final TSLinkWithName link, final String type )
  {
    super( new PoolableObjectType( "zml", link.getHref(), link.getContext(), true ) ); //$NON-NLS-1$
    m_type = type;
  }

  /**
   * @see org.kalypso.ogc.sensor.provider.AbstractObsProvider#getObservation()
   */
  @Override
  public IObservation getObservation( )
  {
    final IObservation observation = super.getObservation();
    if( Objects.isNull( observation ) )
      return null;

    /** if type is defined - check type exists as value axis. otherwise return null */
    if( Objects.isNotNull( m_type ) )
      if( !AxisUtils.hasAxis( observation.getAxes(), m_type ) )
        return null;

    return observation;
  }
}
