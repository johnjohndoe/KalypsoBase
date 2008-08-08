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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IRetinalMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * 
 * @author burtscher1
 * 
 */
public class BranchLayer extends AbstractLineLayer
{

  private final TupleResult m_data;

  private final String m_domainComponentId;

  private final String m_targetComponentId;

  private final String m_iconComponentId;

  private IComponent m_domainComponent = null;

  private IComponent m_targetComponent = null;

  private IComponent m_iconComponent = null;

  private boolean m_isInited = false;

  private IDataRange<Number> m_domainRange;

  private IDataRange<Number> m_targetRange;

  private HashMap<Object, IPointStyle> m_mapping;

  private final Map<Object, ArrayList<Point>> m_pointMarks = new HashMap<Object, ArrayList<Point>>();

  /**
   * @param iconComponent
   *            the component which shall be mapped to an icon
   */
  public BranchLayer( TupleResult data, String domainComponentId, String targetComponentId, String iconComponentenId, ILineStyle lineStyle, IPointStyle pointStyle )
  {
    super( lineStyle, pointStyle );
    m_data = data;
    m_domainComponentId = domainComponentId;
    m_targetComponentId = targetComponentId;
    m_iconComponentId = iconComponentenId;

  }

  @Override
  @SuppressWarnings( { "unchecked", "deprecation" })
  public void init( )
  {
    // find components
    IComponent[] components = m_data.getComponents();
    for( IComponent component : components )
      if( component.getId().equals( m_domainComponentId ) )
        m_domainComponent = component;
      else if( component.getId().equals( m_targetComponentId ) )
        m_targetComponent = component;
      else if( component.getId().equals( m_iconComponentId ) )
        m_iconComponent = component;

    if( (m_domainComponent != null) && (m_targetComponent != null) && (m_iconComponent != null) )
      m_isInited = true;

    // Icon-Mapping vorbereiten

    m_mapping = new HashMap<Object, IPointStyle>();
    IRetinalMapper mapper = getMapper( "icon" );
    IDataOperator dop = mapper.getDataOperator( String.class );

    for( int i = 0; i < m_data.size(); i++ )
    {
      IRecord record = m_data.get( i );
      Object value = record.getValue( m_iconComponent );
      if( !m_mapping.containsKey( value ) )
        m_mapping.put( value, mapper.numericToScreen( dop.logicalToNumeric( value ), getPointStyle() ) );
    }

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
      if( m_targetRange == null )
        m_targetRange = getRange( m_data, m_targetComponent, getTargetAxis() );
      return m_targetRange;
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

    for( Object o : m_mapping.keySet() )
      m_pointMarks.put( o, new ArrayList<Point>() );

    if( m_isInited )
      for( int i = 0; i < m_data.size(); i++ )
      {
        IRecord record = m_data.get( i );
        Point point = getCoordinateMapper().logicalToScreen( record.getValue( m_domainComponent ), record.getValue( m_targetComponent ) );
        path.add( point );
        // Punkte merken
        m_pointMarks.get( record.getValue( m_iconComponent ) ).add( point );
      }

    PolylineFigure polylineFigure = getPolylineFigure();
    polylineFigure.setPoints( path.toArray( new Point[] {} ) );
    polylineFigure.paint( gc );

    // Punkte zeichnen
    for( Entry<Object, ArrayList<Point>> e : m_pointMarks.entrySet() )
    {
      ArrayList<Point> points = e.getValue();
      PointFigure pointFigure = getPointFigure();
      pointFigure.setStyle( m_mapping.get( e.getKey() ) );
      pointFigure.setPoints( points.toArray( new Point[points.size()] ) );
      pointFigure.paint( gc );
    }

  }

  @SuppressWarnings( { "unchecked", "deprecation" })
  private static IDataRange<Number> getRange( TupleResult data, IComponent comp, IAxis axis )
  {
    int size = data.size();
    Object value = null;
    IDataOperator op = null;
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
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
    }

    return new DataRange<Number>( min, max );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getLegendEntries(org.eclipse.swt.graphics.Point)
   */
  @Override
  public ILegendEntry[] createLegendEntries( )
  {

    ArrayList<ILegendEntry> entries = new ArrayList<ILegendEntry>();
    ILineStyle ls = getPolylineFigure().getStyle();
    if( ls.isVisible() )
    {

      LegendEntry le = new LegendEntry( this, ls.getTitle() )
      {

        @Override
        public void paintSymbol( GC gc, Point size )
        {
          int sizeX = size.x;
          int sizeY = size.y;

          final ArrayList<Point> path = new ArrayList<Point>();
          path.add( new Point( 0, sizeX / 2 ) );
          path.add( new Point( sizeX / 5, sizeY / 2 ) );
          path.add( new Point( sizeX / 5 * 2, sizeY / 4 ) );
          path.add( new Point( sizeX / 5 * 3, sizeY / 4 * 3 ) );
          path.add( new Point( sizeX / 5 * 4, sizeY / 2 ) );
          path.add( new Point( sizeX, sizeY / 2 ) );
          drawLine( gc, path );
        }

      };
      entries.add( le );

    }

    final PointFigure pf = new PointFigure();
    for( Entry<Object, IPointStyle> e : m_mapping.entrySet() )
    {
      final IPointStyle ps = e.getValue();
      if( ps.isVisible() )
      {

        LegendEntry le = new LegendEntry( this, ps.getTitle() + ": " + e.getKey().toString() )
        {
          @Override
          public void paintSymbol( GC gc, Point size )
          {
            final ArrayList<Point> path = new ArrayList<Point>();
            pf.setPoints( new Point[] { new Point( size.x / 2, size.y / 2 ) } );
            pf.setStyle( ps );
            pf.paint( gc );
          }

        };
        entries.add( le );
      }
    }

    return entries.toArray( new ILegendEntry[] {} );
  }

}
