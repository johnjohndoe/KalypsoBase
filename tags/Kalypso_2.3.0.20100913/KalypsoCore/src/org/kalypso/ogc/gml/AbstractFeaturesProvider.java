/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.ogc.gml;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.kalypsodeegree.model.feature.event.ModellEvent;

/**
 * @author Gernot Belger
 */
public abstract class AbstractFeaturesProvider implements IFeaturesProvider
{
  private final Collection<IFeaturesProviderListener> m_listeners = Collections.synchronizedSet( new HashSet<IFeaturesProviderListener>() );

  @Override
  public void dispose( )
  {
    m_listeners.clear();
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#addFeaturesProviderListener(org.kalypso.ogc.gml.IFeaturesProviderListener)
   */
  @Override
  public void addFeaturesProviderListener( final IFeaturesProviderListener l )
  {
    m_listeners.add( l );
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#removeFeaturesProviderListener(org.kalypso.ogc.gml.IFeaturesProviderListener)
   */
  @Override
  public void removeFeaturesProviderListener( final IFeaturesProviderListener l )
  {
    m_listeners.remove( l );
  }

  protected final void fireFeaturesChanged( final ModellEvent modellEvent )
  {
    final IFeaturesProviderListener[] listeners = m_listeners.toArray( new IFeaturesProviderListener[m_listeners.size()] );
    for( final IFeaturesProviderListener listener : listeners )
    {
      SafeRunner.run( new SafeRunnable( "Failed to inform listener" )
      {
        @Override
        public void run( ) throws Exception
        {
          listener.featuresChanged( AbstractFeaturesProvider.this, modellEvent );
        }
      } );
    }
  }

}
