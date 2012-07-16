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
package org.kalypso.zml.ui.chart.layer.themes;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.commons.pair.IKeyValue;
import org.kalypso.commons.pair.KeyValueFactory;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.ClipHelper;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;
import de.openali.odysseus.chart.framework.util.resource.IPair;
import de.openali.odysseus.chart.framework.util.resource.Pair;

/**
 * @author Dirk Kuch
 */
public class ZmlLineLayerRangeHandler
{
  private final IDataOperator<Date> m_dateDataOperator = new DataOperatorHelper().getDataOperator( Date.class );

  private final IDataOperator<Number> m_numberDataOperator = new DataOperatorHelper().getDataOperator( Number.class );

  private final ZmlLineLayer m_layer;

  private IDataRange<Number> m_domainRange = null;

  private IDataRange<Number> m_targetRange = null;

  public ZmlLineLayerRangeHandler( final ZmlLineLayer layer )
  {
    m_layer = layer;
  }

  public synchronized IDataRange<Number> getDomainRange( )
  {
    if( m_domainRange == null )
      recalculateRanges();

    return m_domainRange;
  }

  public synchronized IDataRange<Number> getTargetRange( )
  {
    if( m_targetRange == null )
      recalculateRanges();

    return m_targetRange;
  }

  private void recalculateRanges( )
  {
    try
    {
      final IPair<Number, Number>[] points = getClippedPoints();
      if( points.length == 0 )
        return;

      final IKeyValue<IPair<Number, Number>, IPair<Number, Number>> minMax = calculateMinMax( points );
      final IPair<Number, Number> min = minMax.getKey();
      final IPair<Number, Number> max = minMax.getValue();

      final Number minDomain = min.getDomain();
      final Number maxDomain = max.getDomain();

      final Number minTarget = min.getTarget();
      final Number maxTarget = max.getTarget();

      m_domainRange = DataRange.create( minDomain, maxDomain );
      m_targetRange = DataRange.create( minTarget, maxTarget );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );

      return;
    }
  }

  public IDataOperator<Date> getDateDataOperator( )
  {
    return m_dateDataOperator;
  }

  public IDataOperator<Number> getNumberDataOperator( )
  {
    return m_numberDataOperator;
  }

  private IKeyValue<IPair<Number, Number>, IPair<Number, Number>> calculateMinMax( final IPair<Number, Number>[] points )
  {
    Double minX = Double.POSITIVE_INFINITY;
    Double minY = Double.POSITIVE_INFINITY;
    Double maxY = Double.NEGATIVE_INFINITY;
    Double maxX = Double.NEGATIVE_INFINITY;

    for( final IPair<Number, Number> point : points )
    {
      final double domainValue = point.getDomain().doubleValue();

      minX = Math.min( domainValue, minX );
      maxX = Math.max( domainValue, maxX );

      final double targetValue = point.getTarget().doubleValue();
      minY = Math.min( targetValue, minY );
      maxY = Math.max( targetValue, maxY );
    }

    minX = Double.isInfinite( minX ) ? null : minX;
    minY = Double.isInfinite( minY ) ? null : minY;
    maxX = Double.isInfinite( maxX ) ? null : maxX;
    maxY = Double.isInfinite( maxY ) ? null : maxY;

    final IPair<Number, Number> min = new Pair<Number, Number>( minX, minY );
    final IPair<Number, Number> max = new Pair<Number, Number>( maxX, maxY );
    return KeyValueFactory.createPairEqualsBoth( min, max );
  }

  @SuppressWarnings("unchecked")
  private IPair<Number, Number>[] getClippedPoints( ) throws SensorException
  {
    final IPair<Number, Number>[] filteredPoints = m_layer.getFilteredPoints( null, new NullProgressMonitor() );
    final Rectangle2D clip = m_layer.getClip();
    if( clip == null )
      return filteredPoints;

    // Special case: one single point
    if( filteredPoints.length == 1 )
    {
      final IPair<Number, Number> singleFiltered = filteredPoints[0];
      if( clip.contains( singleFiltered.getDomain().doubleValue(), singleFiltered.getTarget().doubleValue() ) )
        return filteredPoints;

      return new IPair[] {};
    }
    else
    {
      final ClipHelper helper = new ClipHelper( clip );
      final IPair<Number, Number>[][] clippedPoints = helper.clipAsLine( filteredPoints );
      final Collection<IPair<Number, Number>> points = new ArrayList<IPair<Number, Number>>();
      for( final IPair<Number, Number>[] clipPoints : clippedPoints )
        points.addAll( Arrays.asList( clipPoints ) );
      return points.toArray( new IPair[points.size()] );
    }
  }

  public synchronized void clearRanges( )
  {
    m_domainRange = null;
    m_targetRange = null;
  }
}