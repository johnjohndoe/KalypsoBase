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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
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
  private final Map<IAxis, TreeSet<Integer>> m_stuetzstellen = new HashMap<IAxis, TreeSet<Integer>>();

  private DataSourceHandler m_dataSourceHandler;

  private final MetadataList m_metadata;

  public FindStuetzstellenVisitor( final MetadataList metadata )
  {
    m_metadata = metadata;
  }

  @Override
  public void visit( final ITupleModelValueContainer container )
  {
    try
    {
      final IAxis[] valueAxes = AxisUtils.findValueAxes( container.getAxes(), false );
      for( final IAxis valueAxis : valueAxes )
      {
        final IAxis sourceAxis = AxisUtils.findDataSourceAxis( container.getAxes(), valueAxis );
        String source = null;
        if( Objects.isNotNull( sourceAxis ) )
        {
          final int sourceIndex = getSourceIndex( container, sourceAxis );
          source = getDataSourceHandler().getDataSourceIdentifier( sourceIndex );
        }

        final IAxis statusAxis = AxisUtils.findStatusAxis( container.getAxes(), valueAxis );
        Number status = null;
        if( Objects.isNotNull( statusAxis ) )
        {
          final Object statusObject = container.get( statusAxis );
          status = statusObject instanceof Number ? (Number) statusObject : KalypsoStati.BIT_OK;
        }

        if( ZmlValues.isStuetzstelle( status, source ) )
        {
          TreeSet<Integer> references = m_stuetzstellen.get( valueAxis );
          if( Objects.isNull( references ) )
          {
            references = new TreeSet<Integer>();
            m_stuetzstellen.put( valueAxis, references );
          }

          references.add( container.getIndex() );
        }
      }

    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }
  }

  private Integer getSourceIndex( final ITupleModelValueContainer container, final IAxis sourceAxis ) throws SensorException
  {
    final Object sourceIndex = container.get( sourceAxis );
    if( sourceIndex instanceof Number )
      return ((Number) sourceIndex).intValue();

    return -1;
  }

  public Integer[] getStuetzstellen( final IAxis axis )
  {
    final Set<Integer> stuetzstellen = m_stuetzstellen.get( axis );
    if( Objects.isNull( stuetzstellen ) )
      return new Integer[] {};

    return stuetzstellen.toArray( new Integer[] {} );
  }

  private DataSourceHandler getDataSourceHandler( )
  {
    if( Objects.isNull( m_dataSourceHandler ) )
      m_dataSourceHandler = new DataSourceHandler( m_metadata );

    return m_dataSourceHandler;
  }

}
