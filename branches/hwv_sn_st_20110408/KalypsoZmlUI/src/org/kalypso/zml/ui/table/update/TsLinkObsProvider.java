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
package org.kalypso.zml.ui.table.update;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.ui.core.zml.TSLinkWithName;
import org.kalypso.zml.ui.table.memento.ILabeledObsProvider;

/**
 * @author Gernot Belger
 */
public class TsLinkObsProvider implements ILabeledObsProvider
{
  private final TSLinkWithName m_link;

  private final IObsProvider m_delegate;

  public TsLinkObsProvider( final TSLinkWithName link, final IObsProvider delegate )
  {
    m_link = link;
    m_delegate = delegate;
  }

  /**
   * @see org.kalypso.zml.ui.table.memento.ILabeledObsProvider#getLabel()
   */
  @Override
  public String getLabel( )
  {
    final String tokenizedName = m_link.getName();
    final IObservation observation = getObservation();

    final IAxis valueAxis = AxisUtils.findValueAxis( observation.getAxes() );
    return ObservationTokenHelper.replaceTokens( tokenizedName, observation, valueAxis );
  }

  @Override
  public void addListener( final IObsProviderListener listener )
  {
    m_delegate.addListener( listener );
  }

  @Override
  public void removeListener( final IObsProviderListener listener )
  {
    m_delegate.removeListener( listener );
  }

  @Override
  public void dispose( )
  {
    m_delegate.dispose();
  }

  @Override
  public IRequest getArguments( )
  {
    return m_delegate.getArguments();
  }

  @Override
  public IObservation getObservation( )
  {
    return m_delegate.getObservation();
  }

  @Override
  public boolean isLoaded( )
  {
    return m_delegate.isLoaded();
  }

  @Override
  public boolean isValid( )
  {
    return m_delegate.isValid();
  }

  /**
   * @see org.kalypso.ogc.sensor.provider.IObsProvider#copy()
   */
  @Override
  public IObsProvider copy( )
  {
    throw new UnsupportedOperationException();
  }

}
