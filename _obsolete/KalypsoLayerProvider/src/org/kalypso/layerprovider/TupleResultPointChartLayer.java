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
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.IDataRange;
import org.kalypso.swtchart.chart.layer.impl.DataRange;
import org.kalypso.swtchart.chart.legend.ILegendItem;
import org.kalypso.swtchart.chart.styles.ILayerStyle;
import org.kalypso.swtchart.chart.styles.IStyledElement;
import org.kalypso.swtchart.chart.styles.StyledPoint;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;

/**
 * TODO: what is the difference to the TupleResultLineChartLayer, why do we need both?
 * 
 * @author schlienger
 * @author burtscher
 *
 * Visualization of TupleResult data as line chart
 *
 *  The following styled elements are used:
 *  Line:         used to draw the line connecting the individual data points
 *  Point:        used to draw the individual data points; the points are draw after the line
 */
public class TupleResultPointChartLayer implements IChartLayer
{
  private final TupleResult m_result;

  private final IAxis m_valAxis;

  private final IComponent m_domComp;

  private final IComponent m_valComp;

  private final IAxis m_domAxis;

  private ILayerStyle m_style;


  private String m_name = "No name given";

  private String m_description = "No description given";

  private boolean m_isVisible=true;

  public TupleResultPointChartLayer( TupleResult result, IComponent domComp, IComponent valComp, IAxis domAxis, IAxis valAxis)
  {
    m_result = result;
    m_domComp = domComp;
    m_valComp = valComp;
    m_domAxis = domAxis;
    m_valAxis = valAxis;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gc, final Device dev )
  {
    int size = m_result.size();
    StyledPoint sp = (StyledPoint) getStyle().getElement( SE_TYPE.POINT, 0 );

    ArrayList<Point> path = new ArrayList<Point>();
    for( int i = 0; i < size; i++ )
    {
      final IRecord r1 = m_result.get( i );

      Object x1 = m_result.getValue( r1, m_domComp );
      Object y1 = m_result.getValue( r1, m_valComp );

      int ix1 = m_domAxis.logicalToScreen( x1 );
      int iy1 = m_valAxis.logicalToScreen( y1 );
      path.add( new Point( ix1, iy1 ) );
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
  public void setStyle( ILayerStyle style )
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
    ComparableComparator cc=new ComparableComparator();
    for( int i = 0; i < m_result.size(); i++ )
    {
      IRecord rec = m_result.get( i );
      Object val = rec.getValue( m_domComp );

      if( val == null )
      {
//        Logger.trace( rec );
      }
      else
      {
        if( min == null || cc.compare( min, val ) > 0 )
          min = val;
        if( max == null || cc.compare( max, val ) < 0 )
          max = val;
      }
    }
    return new DataRange( min, max );

  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getValueRange()
   */
  public IDataRange getValueRange( )
  {
    Object min = null;
    Object max = null;
    ComparableComparator cc = new ComparableComparator();
    for( int i = 0; i < m_result.size(); i++ )
    {
      IRecord rec = m_result.get( i );
      Object val = rec.getValue( m_valComp );
      if( min == null || cc.compare( min, val ) > 0 )
        min = val;
      if( max == null || cc.compare( max, val ) < 0 )
        max = val;
    }
    return new DataRange( min, max );
  }

  public void drawIcon( Image img, int width, int height )
  {
    GC gc = new GC( img );
    GCWrapper gcw = new GCWrapper( gc );

    IStyledElement line = m_style.getElement( SE_TYPE.LINE, 0 );
    IStyledElement point = m_style.getElement( SE_TYPE.POINT, 0 );

    if( line != null )
    {
      ArrayList<Point> points = new ArrayList<Point>();
      // Linie von Links nach rechts
      points.add( new Point( 0, height / 2 ) );
      points.add( new Point( width, height / 2 ) );
      line.setPath( points );
      line.paint( gcw, Display.getCurrent() );
    }

    if( point != null )
    {
      ArrayList<Point> points = new ArrayList<Point>();
      // Linie von Links nach rechts
      points.add( new Point( width / 2, height / 2 ) );
      point.setPath( points );
      point.paint( gcw, Display.getCurrent() );
    }

    gc.dispose();
    gcw.dispose();
  }

  public void setName( String name )
  {
    m_name = name;
  }

  public void setDescription( String description )
  {
    m_description = description;
  }

  public void setVisibility(boolean isVisible)
  {
    m_isVisible=isVisible;
  }

  public boolean getVisibility()
  {
    return m_isVisible;
  }
}
