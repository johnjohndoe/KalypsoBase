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
package org.kalypso.layerprovider;

import java.util.ArrayList;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.observation.result.TupleResultUtilities;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.IDataRange;
import org.kalypso.swtchart.chart.layer.impl.DataRange;
import org.kalypso.swtchart.chart.legend.ILegendItem;
import org.kalypso.swtchart.chart.styles.ILayerStyle;
import org.kalypso.swtchart.chart.styles.IStyledElement;
import org.kalypso.swtchart.chart.styles.StyledLine;
import org.kalypso.swtchart.chart.styles.StyledPoint;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;

/**
 * Visualization of TupleResult data as line chart
 * <p>
 * The following styled elements are used:
 * <ul>
 * <li> Line: used to draw the line connecting the individual data points </li>
 * <li>Point: used to draw the individual data points; the points are drawn after the line</li>
 * </ul>
 * 
 * @author schlienger
 * @author burtscher
 */
public class TupleResultLineChartLayer implements IChartLayer
{
  private TupleResult m_result;

  /* needed for chart update / redraw event */
  private Chart m_chart = null;

  private String m_domainComponentId;

  private String m_valueComponentId;

  private final IAxis m_valAxis;

  private final IAxis m_domAxis;

  private ILayerStyle m_style;

  private String m_name = "No name given";

  private String m_description = "No description given";

  private boolean m_isVisible = true;

  private boolean m_showPoints = true;

  private boolean m_showLines = true;

  public TupleResultLineChartLayer( final Chart chart, final TupleResult result, final String domainComponentId, final String valueComponentId, final IAxis domAxis, final IAxis valAxis )
  {
    this( result, domainComponentId, valueComponentId, domAxis, valAxis );
    m_chart = chart;
  }

  public TupleResultLineChartLayer( final TupleResult result, final String domainComponentId, final String valueComponentId, final IAxis domAxis, final IAxis valAxis )
  {
    m_result = result;
    m_domainComponentId = domainComponentId;
    m_valueComponentId = valueComponentId;
    m_domAxis = domAxis;
    m_valAxis = valAxis;
  }

  public void setShowPoints( final boolean showPoints )
  {
    m_showPoints = showPoints;
  }

  public void setShowLines( final boolean showLines )
  {
    m_showLines = showLines;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( final GCWrapper gc, final Device dev )
  {
    gc.setLineWidth( 1 );

    final StyledLine sl = m_showLines ? (StyledLine) getStyle().getElement( SE_TYPE.LINE, 0 ) : null;
    final StyledPoint sp = m_showPoints ? (StyledPoint) getStyle().getElement( SE_TYPE.POINT, 0 ) : null;

    final ArrayList<Point> path = new ArrayList<Point>();

    final IComponent domainComponent = TupleResultUtilities.findComponentById( m_result, m_domainComponentId );
    final IComponent valueComponent = TupleResultUtilities.findComponentById( m_result, m_valueComponentId );

    if( (domainComponent != null) && (valueComponent != null) )
    {
      for( final IRecord r : m_result )
      {
        final Object xVal = getValue( r, domainComponent );
        final Object yVal = getValue( r, valueComponent );

        final Integer x = m_domAxis.logicalToScreen( xVal );
        final Integer y = m_valAxis.logicalToScreen( yVal );
        final Point point = new Point( x, y );
        path.add( point );
      }
    }

    if( sl != null )
    {
      sl.setPath( path );
      sl.paint( gc, dev );
    }

    if( sp != null )
    {
      sp.setPath( path );
      sp.paint( gc, dev );
    }
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getName()
   */
  public String getName( )
  {
    return m_name;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDescription()
   */
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainAxis()
   */
  public IAxis getDomainAxis( )
  {
    return m_domAxis;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getRangeAxis()
   */
  public IAxis getValueAxis( )
  {
    return m_valAxis;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getLegendItem()
   */
  public ILegendItem getLegendItem( )
  {
    return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#isActive()
   */
  public boolean isActive( )
  {
    return true;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#isVisible()
   */
  public boolean isVisible( )
  {
    return true;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getStyle()
   */
  public ILayerStyle getStyle( )
  {
    return m_style;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setStyle(org.kalypso.swtchart.chart.styles.ILayerStyle)
   */
  public void setStyle( final ILayerStyle style )
  {
    m_style = style;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainRange()
   */
  public IDataRange getDomainRange( )
  {
    Object min = null;
    Object max = null;
    final ComparableComparator cc = new ComparableComparator();

    final IComponent domainComponent = TupleResultUtilities.findComponentById( m_result, m_domainComponentId );
    if( domainComponent == null )
      return null;

    for( final IRecord rec : m_result )
    {
      final Object val = getValue( rec, domainComponent );

      if( val == null )
      {
        // Logger.trace( rec );
      }
      else
      {
        if( (min == null) || (cc.compare( min, val ) > 0) )
        {
          min = val;
        }
        if( (max == null) || (cc.compare( max, val ) < 0) )
        {
          max = val;
        }
      }
    }
    return new DataRange<Object>( min, max );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getValueRange()
   */
  public IDataRange getValueRange( )
  {
    Object min = null;
    Object max = null;
    final ComparableComparator cc = new ComparableComparator();

    final IComponent valueComponent = TupleResultUtilities.findComponentById( m_result, m_valueComponentId );

    if( valueComponent == null )
      return null;

    for( final IRecord rec : m_result )
    {
      final Object val = getValue( rec, valueComponent );

      if( val == null )
      {
        continue;
      }

      if( (min == null) || (cc.compare( min, val ) > 0) )
      {
        min = val;
      }
      if( (max == null) || (cc.compare( max, val ) < 0) )
      {
        max = val;
      }
    }
    return new DataRange<Object>( min, max );
  }

  public void drawIcon( final Image img, final int width, final int height )
  {
    final GC gc = new GC( img );
    final GCWrapper gcw = new GCWrapper( gc );

    final IStyledElement line = m_style.getElement( SE_TYPE.LINE, 0 );
    final IStyledElement point = m_style.getElement( SE_TYPE.POINT, 0 );

    if( line != null )
    {
      final ArrayList<Point> points = new ArrayList<Point>();
      // Linie von Links nach rechts
      points.add( new Point( 0, height / 2 ) );
      points.add( new Point( width, height / 2 ) );
      line.setPath( points );
      line.paint( gcw, Display.getCurrent() );
    }

    if( point != null )
    {
      final ArrayList<Point> points = new ArrayList<Point>();
      // Linie von Links nach rechts
      points.add( new Point( width / 2, height / 2 ) );
      point.setPath( points );
      point.paint( gcw, Display.getCurrent() );
    }

    gc.dispose();
    gcw.dispose();
  }

  public void setName( final String name )
  {
    m_name = name;
  }

  public void setDescription( final String description )
  {
    m_description = description;
  }

  public void setVisibility( final boolean isVisible )
  {
    m_isVisible = isVisible;
  }

  public boolean getVisibility( )
  {
    return m_isVisible;
  }

  public void updateResult( final TupleResult result, final String domainComponentId, final String valueComponentId )
  {
    m_result = result;
    m_domainComponentId = domainComponentId;
    m_valueComponentId = valueComponentId;
    if( m_chart != null )
    {
      m_chart.repaint();
    }
  }

  /**
   * Helper class to avoid handling of XMLGregorianCalendars.
   * <p>
   * HACK: This is q quite crued hack, but what else can be done?
   */
  private static Object getValue( final IRecord record, final IComponent component )
  {
    final Object value = record.getValue( component );
    if( value instanceof XMLGregorianCalendar )
      return ((XMLGregorianCalendar) value).toGregorianCalendar();

    return value;
  }
}
