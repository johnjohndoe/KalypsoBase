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
package org.kalypso.model.wspm.core.gml;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * @author Dirk Kuch
 */
public class RunOffEvent extends Feature_Impl implements IRunOffEvent
{
  public RunOffEvent( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  @Override
  public Integer getAnnuality( )
  {
    final Object property = getProperty( PROPERTY_ANNUALITY );
    if( property instanceof Number )
      return ((Number)property).intValue();

    return null;
  }

  @Override
  public IObservation<TupleResult> toObservation( )
  {
    final IObservation<TupleResult> obs = ObservationFeatureFactory.toObservationInternal( this, null );
    final TupleResult result = obs.getResult();

    final String[] components = new String[] { COMPONENT_STATION, COMPONENT_RUNOFF, COMPONENT_COMMENT };
    for( final String component : components )
    {
      final int index = result.indexOfComponent( component );
      if( index == -1 )
      {
        result.addComponent( ComponentUtilities.getFeatureComponent( component ) );
        saveObservation( obs );
      }
    }

    return obs;
  }

  @Override
  public BigDecimal getDischarge( final BigDecimal station )
  {
    final SortedMap<BigDecimal, BigDecimal> table = getDischargeTable();
    final SortedMap<BigDecimal, BigDecimal> headMap = table.headMap( station );

    final BigDecimal lastKey = headMap.lastKey();
    if( lastKey == null )
      return null;

    return headMap.get( lastKey );
  }

  /**
   * @return SortedMap<profile station, discharge value>
   */
  @Override
  public SortedMap<BigDecimal, BigDecimal> getDischargeTable( )
  {
    final IObservation<TupleResult> observation = toObservation();
    final TupleResult result = observation.getResult();

    final int stationComp = result.indexOfComponent( COMPONENT_STATION );
    final int abflussComp = result.indexOfComponent( COMPONENT_RUNOFF );

    final Comparator<BigDecimal> comp = new Comparator<BigDecimal>()
    {
      @Override
      public int compare( final BigDecimal o1, final BigDecimal o2 )
      {
        if( isDirectionUpstream() )
          return o1.compareTo( o2 );
        else
          return o2.compareTo( o1 );
      }
    };

    final SortedMap<BigDecimal, BigDecimal> values = new TreeMap<>( comp );

    for( final IRecord record : result )
    {
      final BigDecimal station = (BigDecimal)record.getValue( stationComp );
      final BigDecimal runOff = (BigDecimal)record.getValue( abflussComp );

      // REMARK: just skip lines with 'holes', avoids confusion if an empty (but hardly visible) line is present.
      if( station != null && runOff != null )
        values.put( station, runOff );
    }

    return values;
  }

  @Override
  public WspmWaterBody getOwner( )
  {
    return (WspmWaterBody)super.getOwner();
  }

  @Override
  public boolean isDirectionUpstream( )
  {
    final WspmWaterBody waterBody = getOwner();

    return waterBody.isDirectionUpstreams();
  }

  @Override
  public void saveObservation( final IObservation<TupleResult> observation )
  {
    ObservationFeatureFactory.toFeature( observation, this );
  }
}
