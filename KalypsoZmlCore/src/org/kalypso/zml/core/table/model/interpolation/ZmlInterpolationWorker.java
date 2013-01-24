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
package org.kalypso.zml.core.table.model.interpolation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.transaction.ITupleModelTransaction;
import org.kalypso.ogc.sensor.transaction.TupleModelTransaction;
import org.kalypso.zml.core.table.model.IZmlModelColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlInterpolationWorker implements ICoreRunnableWithProgress
{

  private final IAxis m_valueAxis;

  private final ITupleModel m_model;

  private final MetadataList m_metadata;

  public ZmlInterpolationWorker( final ITupleModel model, final MetadataList metadata, final IAxis valueAxis )
  {
    m_model = model;
    m_metadata = metadata;
    m_valueAxis = valueAxis;
  }

  public ZmlInterpolationWorker( final IZmlModelColumn column ) throws SensorException
  {
    this( column.getTupleModel(), column.getMetadata(), column.getValueAxis() );
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final Set<IStatus> stati = new LinkedHashSet<IStatus>();

    try
    {
      final ITupleModelTransaction transaction = new TupleModelTransaction( m_model, m_metadata );

      try
      {
        final boolean setLastValidValue = ZmlInterpolation.isSetLastValidValue( m_metadata );
        final Double defaultValue = ZmlInterpolation.getDefaultValue( m_metadata );
        final int size = m_model.size();

        final FindStuetzstellenVisitor visitor = new FindStuetzstellenVisitor( m_metadata );
        m_model.accept( visitor, 1 );

        final Integer[] stuetzstellen = visitor.getStuetzstellen( m_valueAxis );

        if( ArrayUtils.isEmpty( stuetzstellen ) )
        {
          ZmlInterpolation.fillValue( transaction, m_valueAxis, 0, size, defaultValue );

          return Status.OK_STATUS;
        }

        // set all values 0 before first stuetzstelle
        if( stuetzstellen[0] > 0 )
          ZmlInterpolation.fillValue( transaction, m_valueAxis, 0, stuetzstellen[0], defaultValue );

        for( int index = 0; index < stuetzstellen.length - 1; index++ )
        {
          final Integer stuetzstelle1 = stuetzstellen[index];
          final Integer stuetzstelle2 = stuetzstellen[index + 1];
          ZmlInterpolation.interpolate( m_model, transaction, m_valueAxis, stuetzstelle1, stuetzstelle2 );
        }

        // set all values 0 after last stuetzstelle
        final Integer last = stuetzstellen[stuetzstellen.length - 1];
        if( last != size - 1 )
        {
          if( setLastValidValue )
          {
            final Object lastValue = m_model.get( last, m_valueAxis );

            ZmlInterpolation.fillValue( transaction, m_valueAxis, last + 1, size, (Double) lastValue );
          }
          else
            ZmlInterpolation.fillValue( transaction, m_valueAxis, last + 1, size, defaultValue );
        }
      }
      finally
      {
        m_model.execute( transaction );
      }
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
      stati.add( StatusUtilities.createExceptionalErrorStatus( "(Re)Interpolating values failed", e ) );
    }

    return StatusUtilities.createStatus( stati, "ZML Interpolation Worker" );
  }
}