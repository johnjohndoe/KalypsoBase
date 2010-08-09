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
package org.kalypso.ogc.sensor.cache;

import org.eclipse.core.runtime.IAdaptable;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.shiftone.cache.Cache;
import org.shiftone.cache.policy.lfu.LfuCacheFactory;

/**
 * A very simple cache for observations...
 * 
 * @author schlienger
 */
public class ObservationCache
{
  private static ObservationCache INSTANCE = null;

  public static ObservationCache getInstance( )
  {
    if( INSTANCE == null )
    {
      // timeout of 4 minutes and cache size of 200
      INSTANCE = new ObservationCache( 1000 * 60 * 4, 200 );
    }

    return INSTANCE;
  }

  public static void clearCache( )
  {
    if( INSTANCE != null )
      INSTANCE.clear();
  }

  /** our cache */
  private final Cache m_cache;

  public ObservationCache( final int timeout, final int size )
  {
    final LfuCacheFactory factory = new LfuCacheFactory();
    m_cache = factory.newInstance( "view.observations", timeout, size ); //$NON-NLS-1$
  }

  public IObservation getObservationFor( final IAdaptable adapt )
  {
    synchronized( m_cache )
    {
      IObservation obs = (IObservation) m_cache.getObject( adapt );

      if( obs == null )
      {
        try
        {
          obs = (IObservation) adapt.getAdapter( IObservation.class );
        }
        catch( final IllegalArgumentException ex )
        {
          ex.printStackTrace();

          return null;
        }

        // still null, then this item is not adaptable
        if( obs == null )
          return null;

        m_cache.addObject( adapt, obs );
      }

      return obs;
    }
  }

  public ITupleModel getValues( final IObservation obs )
  {
    synchronized( m_cache )
    {
      return (ITupleModel) m_cache.getObject( obs );
    }
  }

  public void addValues( final IObservation obs, final ITupleModel values )
  {
    synchronized( m_cache )
    {
      m_cache.addObject( obs, values );
    }
  }

  private void clear( )
  {
    synchronized( m_cache )
    {
      m_cache.clear();
    }
  }
}