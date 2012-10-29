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

package org.kalypso.metadoc.configuration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Default implementation of the IPublishingConfiguration interface
 *
 * @author schlienger
 */
public class PublishingConfiguration extends ExtendedProperties
{
  private final List<IConfigurationListener> m_listeners;

  public PublishingConfiguration( )
  {
    m_listeners = new ArrayList<>( 10 );
  }

  private void fireConfigurationChanged( final String key )
  {
    final IConfigurationListener[] listeners = m_listeners.toArray( new IConfigurationListener[m_listeners.size()] );
    for( final IConfigurationListener listener : listeners )
    {
      try
      {
        listener.configurationChanged( this, key );
      }
      catch( final Throwable t )
      {
        t.printStackTrace();
      }
    }
  }

  public void addListener( final IConfigurationListener listener )
  {
    m_listeners.add( listener );
  }

  public void removeListener( final IConfigurationListener listener )
  {
    m_listeners.remove( listener );
  }

  @Override
  public synchronized void clear( )
  {
    super.clear();

    fireConfigurationChanged( null );
  }

  @Override
  public synchronized Object put( final Object key, final Object value )
  {
    if( value == null )
      return null;

    final Object put = super.put( key, value );

    fireConfigurationChanged( (String) key );

    return put;
  }

  @Override
  public synchronized Object remove( final Object key )
  {
    final Object remove = super.remove( key );

    fireConfigurationChanged( (String) key );

    return remove;
  }
}