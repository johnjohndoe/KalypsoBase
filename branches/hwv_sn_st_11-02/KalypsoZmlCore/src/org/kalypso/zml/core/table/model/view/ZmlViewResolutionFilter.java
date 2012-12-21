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
package org.kalypso.zml.core.table.model.view;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.event.IZmlModelColumnEvent;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.ZmlValues;

/**
 * zml table filter. provides support of different view resolutions (1h, 3h, ...) and stuetzstellenansicht
 * 
 * @author Dirk Kuch
 */
public class ZmlViewResolutionFilter
{
  private int m_resolution = 0;

  private int m_offset = 0;

  protected static class ZmlFilterBaseIndex
  {
    private int m_baseIndex = 0;

    private IZmlModel m_model = null;

    protected int getBaseIndex( final IZmlModel model )
    {
      if( m_model == null )
        update( model );
      else if( m_model != model )
        update( model );

      return m_baseIndex;
    }

    private void update( final IZmlModel model )
    {
      final IZmlModelRow[] rows = model.getRows();
      if( ArrayUtils.isEmpty( rows ) )
        return;

      final IZmlModelRow base = rows[0];
      final Date index = base.getIndex();

      final Calendar calendar = Calendar.getInstance();
      calendar.setTime( index );
      calendar.add( Calendar.HOUR_OF_DAY, -1 );

      m_baseIndex = ZmlViewResolutionFilter.ticksInHours( calendar.getTime() );
      m_model = model;
    }
  }

  private final ZmlFilterBaseIndex m_base = new ZmlFilterBaseIndex();

  private boolean m_stuetzstellenMode;

  private final ZmlModelViewport m_model;

  public ZmlViewResolutionFilter( final ZmlModelViewport model )
  {
    m_model = model;
  }

  protected static int ticksInHours( final Date date )
  {
    final long time = date.getTime();

    return (int) (time / 1000 / 60 / 60);
  }

  public boolean select( final IZmlModelRow row )
  {

    if( m_resolution == 0 )
    {
      if( m_stuetzstellenMode )
      {

        return hasStuetzstelle( row );
      }

      return true;
    }

    final Date index = row.getIndex();
    final int ticks = ticksInHours( index );

    final int base = m_base.getBaseIndex( row.getModel() );
    final int diff = Math.abs( base + m_offset - ticks );

    final int mod = diff % m_resolution;

    if( m_stuetzstellenMode )
    {
      if( hasStuetzstelle( row ) && mod == 0 )
        return true;
    }

    return mod == 0;
  }

  private boolean hasStuetzstelle( final IZmlModelRow row )
  {
    final IZmlModelCell[] references = row.getCells();
    for( final IZmlModelCell reference : references )
    {
      if( !(reference instanceof IZmlModelValueCell) )
        continue;

      try
      {
        if( ZmlValues.isStuetzstelle( (IZmlModelValueCell) reference ) )
          return true;
      }
      catch( final Throwable t )
      {
        KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    return false;
  }

  public void add2Offset( final int number )
  {
    if( m_offset + number > m_resolution )
      resetOffset();
    else
      m_offset += number;

    m_model.fireModelChanged( IZmlModelColumnEvent.RESOLUTION_CHANGED );
  }

  public void setParameters( final int resolution, final boolean mode )
  {
    if( m_resolution == resolution && m_stuetzstellenMode == mode )
      return;

    m_resolution = resolution;
    m_stuetzstellenMode = mode;

    m_model.fireModelChanged( IZmlModelColumnEvent.RESOLUTION_CHANGED );
  }

  public int getResolution( )
  {
    return m_resolution;
  }

  public boolean isStuetzstellenMode( )
  {
    return m_stuetzstellenMode;
  }

  public void resetOffset( )
  {
    m_offset = 1;
  }
}
