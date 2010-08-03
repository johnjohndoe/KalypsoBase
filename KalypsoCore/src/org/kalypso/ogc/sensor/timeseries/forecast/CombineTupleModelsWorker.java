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
package org.kalypso.ogc.sensor.timeseries.forecast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;

/**
 * combines multiple ITupleModels to one result model.
 * 
 * @author Dirk Kuch
 */
public class CombineTupleModelsWorker implements ICoreRunnableWithProgress
{
  private final ITupleModel[] m_models;

  /**
   * the combined result model
   */
  private final SimpleTupleModel m_result;

  public CombineTupleModelsWorker( final ITupleModel[] models, final IAxis[] axisList )
  {
    m_models = models;
    m_result = new SimpleTupleModel( axisList );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final List<IStatus> statis = new ArrayList<IStatus>();

    Date lastDate = DateUtilities.getMinimum();

    for( final ITupleModel model : m_models )
    {
      try
      {
        final AxisMapping mapping = new AxisMapping( m_result.getAxisList(), model.getAxisList() );
        final IAxis[] srcAxes = mapping.getSourceAxes();

        final IAxis dateAxis = ObservationUtilities.findAxisByClass( srcAxes, Date.class );

        for( int index = 0; index < model.getCount(); index++ )
        {
          final Date current = (Date) model.getElement( index, dateAxis );
          if( current.before( lastDate ) )
            continue;

          Object[] data;
          if( AxisUtils.findDataSourceAxis( srcAxes ) != null )
          {
            data = new Object[srcAxes.length];

            for( final IAxis srcAxis : srcAxes )
            {
              final Object value = model.getElement( index, srcAxis );
              final int baseIndex = mapping.getBaseIndex( srcAxis );
              data[baseIndex] = value;
            }
          }
          else
          {
            data = new Object[srcAxes.length + 1];

            for( final IAxis srcAxis : srcAxes )
            {
              final Object value = model.getElement( index, srcAxis );
              final int baseIndex = mapping.getBaseIndex( srcAxis );
              data[baseIndex] = value;
            }

            final IAxis dataSourceAxis = mapping.getDataSourceAxis();
            final int baseIndex = mapping.getBaseIndex( dataSourceAxis );
            data[baseIndex] = ArrayUtils.indexOf( m_models, model ); // hack
          }

          m_result.addTupple( data );
          lastDate = current;
        }

      }
      catch( final Throwable t )
      {
        statis.add( StatusUtilities.createWarningStatus( "Adding tuple failed.", t ) );
      }
    }

    return StatusUtilities.createStatus( statis, "Combing tuple models." );
  }

  public ITupleModel getCombinedModel( )
  {
    return m_result;
  }

}
