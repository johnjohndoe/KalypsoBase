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
package org.kalypso.chart.ext.observation.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * 
 * Equal to TupleResultLineLayer in data visualization, but works directly with TupleResult data instead of using a
 * TupleResultDataContainer (which copies the TupleResult data). This class is intended to be used if the original data
 * can change during the layer life cycle.<br>
 * <br>
 * 
 * <B>ATTENTION:</B> This has not been tested yet. (Don't use it and blame me if it responds sluggish.) Also, it will
 * crash if the TupleResult contains Components which are not subclasses of the types Number or Calendar.
 * 
 * @author burtscher1
 * 
 */
public class RealTupleResultLineLayer extends AbstractLineLayer
{

  private final TupleResult m_data;

  private final String m_domainComponentId;

  private final String m_targetComponentId;

  private IComponent m_domainComponent = null;

  private IComponent m_targetComponent = null;

  private boolean m_isInited = false;

  private IDataRange<Number> m_domainRange;

  public RealTupleResultLineLayer( TupleResult data, String domainComponentId, String targetComponentId, ILineStyle lineStyle, IPointStyle pointStyle )
  {
    super( lineStyle, pointStyle );
    m_data = data;
    m_domainComponentId = domainComponentId;
    m_targetComponentId = targetComponentId;

    // find components
    IComponent[] components = m_data.getComponents();
    for( IComponent component : components )
      if( component.getId().equals( m_domainComponentId ) )
        m_domainComponent = component;
      else if( component.getId().equals( m_targetComponentId ) )
        m_targetComponent = component;

    if( (m_domainComponent != null) && (m_targetComponent != null) )
      m_isInited = true;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  public IDataRange<Number> getDomainRange( )
  {
    if( m_isInited )
    {
      if( m_domainRange == null )
        m_domainRange = getRange( m_data, m_domainComponent, getDomainAxis() );
      return m_domainRange;
    }
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  public IDataRange<Number> getTargetRange( )
  {
    if( m_isInited )
    {
      if( m_domainRange == null )
        m_domainRange = getRange( m_data, m_targetComponent, getTargetAxis() );
      return m_domainRange;
    }
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @SuppressWarnings("deprecation")
  public void paint( GC gc )
  {
    List<Point> path = new ArrayList<Point>();

    if( m_isInited )
      for( int i = 0; i < m_data.size(); i++ )
      {
        IRecord record = m_data.get( i );
        path.add( getCoordinateMapper().logicalToScreen( record.getValue( m_domainComponent ), record.getValue( m_targetComponent ) ) );
      }

    PolylineFigure polylineFigure = getPolylineFigure();
    polylineFigure.setPoints( path.toArray( new Point[] {} ) );
    polylineFigure.paint( gc );

    PointFigure pointFigure = getPointFigure();
    pointFigure.setPoints( path.toArray( new Point[] {} ) );
    pointFigure.paint( gc );

  }

  @SuppressWarnings( { "unchecked", "deprecation" })
  private static IDataRange<Number> getRange( TupleResult data, IComponent comp, IAxis axis )
  {
    int size = data.size();
    Object value = null;
    IDataOperator op = null;
    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;
    for( int i = 0; i < size; i++ )
    {
      IRecord record = data.get( i );
      value = record.getValue( comp );

      // Beim ersten Mal: abfragen
      if( op == null )
        op = axis.getDataOperator( value.getClass() );
      // ‹berpr¸fen, ob vorhanden
      if( op != null )
      {
        if( op.logicalToNumeric( value ).doubleValue() < min )
          min = op.logicalToNumeric( value ).doubleValue();
        if( op.logicalToNumeric( value ).doubleValue() > max )
          max = op.logicalToNumeric( value ).doubleValue();
      }
      else
      {
        Logger.logFatal( Logger.TOPIC_LOG_LAYER, "There's no data operator for class '" + value.getClass() + "'" );
        return null;
      }
      return new DataRange<Number>( min, max );
    }
    return null;
  }

}
