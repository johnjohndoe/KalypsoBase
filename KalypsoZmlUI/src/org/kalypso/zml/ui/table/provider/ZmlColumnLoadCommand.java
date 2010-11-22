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
package org.kalypso.zml.ui.table.provider;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.template.IObsProvider;
import org.kalypso.ogc.sensor.template.IObsProviderListener;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.IZmlColumnModel;
import org.kalypso.zml.ui.table.IZmlTableColumn;
import org.kalypso.zml.ui.table.schema.DataColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlColumnLoadCommand implements IObsProviderListener
{
  private boolean m_canceled = false;

  private final IZmlTableColumn m_column;

  private final IZmlColumnModel m_model;

  public ZmlColumnLoadCommand( final IZmlColumnModel model, final IZmlTableColumn column )
  {
    m_model = model;
    m_column = column;

    synchronized( this )
    {
      final IObsProvider provider = column.getObsProvider();
      if( provider.isLoaded() )
      {
        execute();
      }
      else
      {
        provider.addListener( this );
      }
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProviderListener#observationLoadedEvent()
   */
  @Override
  public void observationReplaced( )
  {
    // FIXME: this happens, if the zml does not exist.
    // It would be nice, to keep this listener,maybe it appears later....
    // but: who will dipose me?

    // final IObservation observation = m_element.getObsProvider().getObservation();
    // if( observation == null )
    // return;

    m_column.getObsProvider().removeListener( this );

    synchronized( this )
    {
      execute();
    }
  }

  public void cancel( )
  {
    m_canceled = true;
  }

  private void execute( )
  {
    if( m_canceled )
      return;

    try
    {
      final IObsProvider provider = m_column.getObsProvider();
      final IObservation observation = provider.getObservation();
      if( observation == null )
        return;

      final ITupleModel model = observation.getValues( null );

      final DataColumnType type = m_model.getDataColumnType( m_column.getId() );
      m_model.addColumn( new ZmlTableColumn( m_model, m_column, observation, model, type ) );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProviderListener#observationChangedX(java.lang.Object)
   */
  @Override
  public void observationChanged( final Object source )
  {
    m_model.fireModelChanged();
  }

}
