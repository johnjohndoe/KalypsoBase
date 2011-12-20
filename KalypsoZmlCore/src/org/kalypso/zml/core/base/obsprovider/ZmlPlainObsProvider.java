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
package org.kalypso.zml.core.base.obsprovider;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.metadata.IMetadataConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.PlainObsProvider;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.zml.core.base.IZmlSourceElement;

/**
 * @author Dirk Kuch
 */
public class ZmlPlainObsProvider extends PlainObsProvider implements IZmlSourceElement
{

  private String m_identifier;

  private final int m_index;

  public ZmlPlainObsProvider( final String identifier, final IObservation obs, final IRequest args, final int index )
  {
    super( obs, args );

    m_identifier = identifier;
    m_index = index;
  }

  @Override
  public IObsProvider getObsProvider( )
  {
    return this;
  }

  @Override
  public IPoolableObjectType getPoolKey( )
  {
    return null;
  }

  @Override
  public boolean isDirty( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLabel( )
  {
    final IObservation observation = getObservation();
    if( Objects.isNull( observation ) )
      return getIdentifier();

    final MetadataList metadata = observation.getMetadataList();
    final String name = (String) metadata.get( IMetadataConstants.MD_NAME );

    return String.format( "%s - %s", getIdentifier(), name );
  }

  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  @Override
  public void setIdentifier( final String identifier )
  {
    m_identifier = identifier;
  }

  @Override
  public int getIndex( )
  {
    return m_index;
  }

}
