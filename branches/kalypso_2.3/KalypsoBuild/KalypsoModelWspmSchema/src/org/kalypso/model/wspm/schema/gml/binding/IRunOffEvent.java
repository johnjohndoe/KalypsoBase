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
package org.kalypso.model.wspm.schema.gml.binding;

import java.math.BigDecimal;
import java.util.SortedMap;

import javax.xml.namespace.QName;

import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.gml.WspmWaterBody;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.TupleResult;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Dirk Kuch
 */
public interface IRunOffEvent extends Feature
{
// {org.kalypso.model.wspmrunoff}RunOffEvent
// - {http://www.opengis.net/gml}description
// - {http://www.opengis.net/gml}name
// - {http://www.opengis.net/gml}boundedBy
// - {http://www.opengis.net/gml}location
// - {http://www.opengis.net/om}time
// - {http://www.opengis.net/om}location
// - {http://www.opengis.net/om}precedingEvent
// - {http://www.opengis.net/om}followingEvent
// - {http://www.opengis.net/om}procedure
// - {http://www.opengis.net/om}countParameter
// - {http://www.opengis.net/om}measureParameter
// - {http://www.opengis.net/om}termParameter
// - {http://www.opengis.net/om}observedProperty
// - {http://www.opengis.net/om}featureOfInterest
// - {http://www.opengis.net/om}resultDefinition
// - {http://www.opengis.net/om}result
// - {org.kalypso.model.wspmrunoff}returnPeriod

  public QName QN_TYPE = new QName( IWspmConstants.NS_WSPMRUNOFF, "RunOffEvent" );

  public QName QN_PROP_ANNUALITY = new QName( IWspmConstants.NS_WSPMRUNOFF, "returnPeriod" );

  Integer getAnnuality( );

  Double getDischarge( double station );

  SortedMap<BigDecimal, BigDecimal> getDischargeTable( );

  @Override
  WspmWaterBody getParent( );

  boolean isDirectionUpstream( );

  IObservation<TupleResult> toObservation( );
}
