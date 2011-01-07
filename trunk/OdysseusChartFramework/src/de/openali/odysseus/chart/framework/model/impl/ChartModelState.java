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
package de.openali.odysseus.chart.framework.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.IChartModelState;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IExpandableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;

/**
 * @author kimwerner
 */
public class ChartModelState implements IChartModelState
{
  private final String m_activeLayer;

  private final Map<String, Boolean> m_visibleLayer = new HashMap<String, Boolean>();

  private final List< ? > m_positionList;

  public ChartModelState( final ILayerManager mngr )
  {
    m_activeLayer = saveStateActive( mngr );

    saveStateVisible( mngr, m_visibleLayer );

    m_positionList = saveStatePosition( mngr );

  }

  private void saveStateVisible( final ILayerManager mngr, final Map<String, Boolean> map )
  {
    for( final IChartLayer layer : mngr.getLayers() )
    {
      map.put( layer.getId(), layer.isVisible() );
      if( layer instanceof IExpandableChartLayer )
      {
        saveStateVisible( ((IExpandableChartLayer) layer).getLayerManager(), map );
      }
    }
  }

  protected final String saveStateActive( final ILayerManager mngr )
  {
    for( final IChartLayer layer : mngr.getLayers() )
    {
      if( layer.isActive() )
        return layer.getId();
    }
    return ""; //$NON-NLS-1$
  }

  private List<Object> saveStatePosition( final ILayerManager mngr )
  {
    final List<Object> list = new ArrayList<Object>();

    for( final IChartLayer layer : mngr.getLayers() )
    {
      list.add( layer.getId() );
      if( layer instanceof IExpandableChartLayer )
      {
        final List<Object> subList = saveStatePosition( ((IExpandableChartLayer) layer).getLayerManager() );
        list.add( subList );
      }
    }

    return list;
  }

  private void restoreStatePosition( final ILayerManager mngr, final List< ? > list )
  {
    if( mngr == null || list == null )
      return;

    int pos = 0;
    for( final Object o : list )
    {
      if( o instanceof List )
      {
        @SuppressWarnings("unchecked")
        final List<Object> l = (List<Object>) o;
        if( !l.isEmpty() )
        {
          final Object id = l.get( 0 );
          final IChartLayer layer = id == null ? null : mngr.getLayerById( id.toString() );
          if( layer != null )
          {
            mngr.moveLayerToPosition( layer, pos++ );
            if( layer instanceof IExpandableChartLayer )
            {
              restoreStatePosition( ((IExpandableChartLayer) layer).getLayerManager(), l );
            }
          }
        }
      }
      else
      {
        final IChartLayer layer = mngr.getLayerById( o.toString() );
        if( layer != null )
        {
          mngr.moveLayerToPosition( layer, pos++ );
        }
      }
    }
  }

  private void restoreStateVisible( final ILayerManager mngr, final Map<String, Boolean> map )
  {
    for( final IChartLayer layer : mngr.getLayers() )
    {
      final Boolean visible = map.get( layer.getId() );
      if( visible != null )
        layer.setVisible( visible );
      if( layer instanceof IExpandableChartLayer )
      {
        restoreStateVisible( ((IExpandableChartLayer) layer).getLayerManager(), map );
      }
    }
  }

  @Override
  public void restoreState( final IChartModel model )
  {
    if( model == null )
      return;
    final ILayerManager mngr = model.getLayerManager();
    if( mngr == null )
      return;
    final IChartLayer activelayer = mngr.getLayerById( m_activeLayer );
    if( activelayer != null )
    {
      activelayer.setActive( true );
    }
    // old active Layer removed, set first layer active
    else if( mngr.getLayers().length > 0 )
    {
      mngr.getLayers()[0].setActive( true );
    }
    restoreStatePosition( mngr, m_positionList );
    restoreStateVisible( mngr, m_visibleLayer );

  }
}
