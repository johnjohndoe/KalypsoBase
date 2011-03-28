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

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.visitor.ITupleModelValueContainer;
import org.kalypso.ogc.sensor.visitor.ITupleModelVisitor;
import org.kalypso.zml.core.table.model.references.ZmlValues;

/**
 * @author Dirk Kuch
 */
public class FindStuetzstellenVisitor implements ITupleModelVisitor
{
  private final Set<Integer> m_references = new LinkedHashSet<Integer>();

  private final DataSourceHandler m_sourceHandler;

  private final IAxis m_sourceAxis;

  private final IAxis m_statusAxis;

  public FindStuetzstellenVisitor( final IObservation observation )
  {
    final MetadataList metadata = observation.getMetadataList();
    m_sourceHandler = new DataSourceHandler( metadata );
    final IAxis[] axes = observation.getAxes();
    m_sourceAxis = AxisUtils.findDataSourceAxis( axes );
    m_statusAxis = AxisUtils.findStatusAxis( axes );
  }

  public Integer[] getStuetzstellen( )
  {
    return m_references.toArray( new Integer[] {} );
  }

  /**
   * @see org.kalypso.ogc.sensor.visitor.ITupleModelVisitor#visit(org.kalypso.ogc.sensor.visitor.ITupleModelValueContainer)
   */
  @Override
  public void visit( final ITupleModelValueContainer container )
  {
    try
    {
      final Object sourceIndexObject = m_sourceAxis == null ? null : container.get( m_sourceAxis );
      final Number sourceIndex = sourceIndexObject instanceof Number ? (Number) sourceIndexObject : -1;
      final String source = m_sourceHandler.getDataSourceIdentifier( sourceIndex.intValue() );

      final Object statusObject = m_statusAxis == null ? null : container.get( m_statusAxis );
      final Number status = statusObject instanceof Number ? (Number) statusObject : KalypsoStati.BIT_OK;

      if( ZmlValues.isStuetzstelle( status, source ) )
        m_references.add( container.getIndex() );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }
  }
}
