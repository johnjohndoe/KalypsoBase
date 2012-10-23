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
package org.kalypso.model.wspm.ui.view.chart.layer.wsp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.kalypso.commons.java.lang.Arrays;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.contribs.eclipse.ui.plugin.AbstractUIPluginExt;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.util.ProfileUtil;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;

/**
 * Create {@link WaterlevelRenderData} for a given profile and water levels.
 *
 * @author Gernot Belger
 */
public class WaterlevelsRenderWorker
{
  private final Collection<WaterlevelRenderData> m_data = new ArrayList<>();

  private final IProfile m_profile;

  private final IWspLayerData m_wspData;

  public WaterlevelsRenderWorker( final IProfile profile, final IWspLayerData data )
  {
    m_profile = profile;
    m_wspData = data;
  }

  public WaterlevelRenderData[] getResult( )
  {
    return m_data.toArray( new WaterlevelRenderData[m_data.size()] );
  }

  public IStatus execute( )
  {
    try
    {
      return doExecute();
    }
    catch( final Exception e )
    {
      /* Log the error message. */
      final Status status = new Status( IStatus.ERROR, AbstractUIPluginExt.ID, "Failed to render water levels", e ); //$NON-NLS-1$
      KalypsoModelWspmUIPlugin.getDefault().getLog().log( status );
      return status;
    }
  }

  private IStatus doExecute( )
  {
    if( m_wspData == null )
      return Status.OK_STATUS;

    /* Get the profile. */
    if( m_profile == null )
      return Status.OK_STATUS;

    /* Get the station. */
    final BigDecimal station = ProfileUtil.stationToBigDecimal( m_profile.getStation() );

    /* Get all active water levels */
    final Object[] activeElements = m_wspData.getActiveElements();

    final ILabelProvider wspLabelProvider = m_wspData.createLabelProvider();

    /* Paint the values for the active names. */
    final IStatusCollector stati = new StatusCollector( KalypsoModelWspmUIPlugin.ID );
    for( final Object element : activeElements )
    {
      // FIXME: ignore elements that have attached waterlevel objects

      /* Resolve value here, so we do not need to give wsp data to worker */
      final double value = getValue( element, station );
      final String label = wspLabelProvider.getText( element );

      final WaterlevelRenderWorker worker = new WaterlevelRenderWorker( m_profile, value );

      final IStatus status = worker.execute();
      stati.add( status );

      final WaterlevelRenderSegment[] result = worker.getResult();
      if( !Arrays.isEmpty( result ) )
      {
        final WaterlevelRenderData renderData = new WaterlevelRenderData( label, value, result );
        m_data.add( renderData );
      }
    }
    return stati.asMultiStatusOrOK( "Failed to render water levels" ); //$NON-NLS-1$
  }

  private double getValue( final Object element, final BigDecimal station )
  {
    return m_wspData.searchValue( element, station );
  }
}