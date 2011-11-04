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
package org.kalypso.zml.core.table.model.memento;

import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;
import org.kalypso.ogc.sensor.request.IRequest;

/**
 * @author Dirk Kuch
 */
public class LabeledObsProviderDelegate implements ILabeledObsProvider
{

  private final IObsProvider m_provider;

  private final String m_label;

  public LabeledObsProviderDelegate( final IObsProvider provider, final String label )
  {
    m_provider = provider;
    m_label = label;

  }

  @Override
  public void addListener( final IObsProviderListener listener )
  {
    m_provider.addListener( listener );

  }

  @Override
  public void removeListener( final IObsProviderListener listener )
  {
    m_provider.removeListener( listener );
  }

  @Override
  public IObsProvider copy( )
  {
    return m_provider.copy();
  }

  @Override
  public void dispose( )
  {
    m_provider.dispose();
  }

  @Override
  public IRequest getArguments( )
  {
    return m_provider.getArguments();
  }

  @Override
  public IObservation getObservation( )
  {
    return m_provider.getObservation();
  }

  @Override
  public boolean isLoaded( )
  {
    return m_provider.isLoaded();
  }

  @Override
  public boolean isValid( )
  {
    return m_provider.isValid();
  }

  @Override
  public String getLabel( )
  {
    return m_label;
  }

}
