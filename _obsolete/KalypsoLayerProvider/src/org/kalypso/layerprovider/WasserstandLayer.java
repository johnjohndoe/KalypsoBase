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
import java.util.List;

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
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.IDataRange;
import org.kalypso.swtchart.chart.layer.impl.DataRange;
import org.kalypso.swtchart.chart.legend.ILegendItem;
import org.kalypso.swtchart.chart.styles.ILayerStyle;
import org.kalypso.swtchart.chart.styles.IStyledElement;
import org.kalypso.swtchart.chart.styles.StyledLine;
import org.kalypso.swtchart.chart.styles.StyledPoint;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;
import org.kalypso.swtchart.configuration.parameters.IParameterContainer;
import org.kalypso.swtchart.configuration.parameters.impl.BooleanParser;
import org.kalypso.swtchart.configuration.parameters.impl.NumberParser;
import org.kalypso.swtchart.logging.Logger;
import org.kalypso.swtchart.math.ChartMath;
import org.ksp.chart.configuration.Parameters;

/**
 * @author schlienger
 * @author burtscher
 *
 *  visualization of gauge data as line chart;
 *
 *  The following configuration parameters are needed for NiederschlagLayer:
 *  dataSource:     URL or relative path leading to observation data
 *  @TODO: dataSource is an independent configuration tag right now - it should be
 *      moved into the parameter section as not every layer provider needs an url
 *
 *
 *
 *  The following styled elements are used:
 *  Line:         used to draw the line connecting the individual data points
 *  Point:        used to draw the individual data points; the points are draw after the line
 *
 *
 */
public class WasserstandLayer implements IChartLayer
{
  private final TupleResult m_result;

  private final IAxis m_valAxis;

  private final IComponent m_domComp;

  private final IComponent m_valComp;

  private final IAxis m_domAxis;

  private ILayerStyle m_style;

  private Parameters m_params;

  private String m_name;

  private String m_description;

  private boolean m_isVisible=true;

  private IParameterContainer m_pc;

  public WasserstandLayer( TupleResult result, IComponent domComp, IComponent valComp, IAxis domAxis, IAxis valAxis, IParameterContainer pc )
  {
    m_result = result;
    m_domComp = domComp;
    m_valComp = valComp;
    m_domAxis = domAxis;
    m_valAxis = valAxis;
    m_pc = pc;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( final GCWrapper gc, final Device dev )
  {
    int size = m_result.size();
    gc.setLineWidth( 5 );
    // gc.drawLine(0,0, 200, 200);
    Logger.trace( "Drawing TupleResultChartLayer" );

    StyledLine sl = (StyledLine) getStyle().getElement( SE_TYPE.LINE, 0 );
    StyledPoint sp = (StyledPoint) getStyle().getElement( SE_TYPE.POINT, 0 );

    List<Point> path = new ArrayList<Point>();

    IAxisRegistry ar=m_domAxis.getRegistry();

    for( int i = 0; i < size; i++ )
    {
      path.add(ar.logicalToScreen( this, m_result.getValue( m_result.get( i ), m_domComp ), m_result.getValue( m_result.get( i ), m_valComp ) ));
    }

    boolean useDouglasPeucker=m_pc.getParsedParameterValue( "useDouglasPeucker", "false", new BooleanParser() );
    if (useDouglasPeucker)
    {
      double epsilon=m_pc.getParsedParameterValue( "epsilon", "1.0", new NumberParser() ).doubleValue();
      Logger.logInfo( Logger.TOPIC_LOG_LAYER, "WasserstandLayer: data path size: "+path.size() );
      List<Point> newPath=ChartMath.douglasPeucker(path, epsilon);
      Logger.logInfo( Logger.TOPIC_LOG_LAYER, "WasserstandLayer: new data path size with e="+epsilon+": "+newPath.size() );
      path= newPath;
    }

    if( sl != null )
    {
      sl.setPath( path );
      sl.paint( gc, dev );
    }
    if( sp != null )
    {
      sp.setPath( path );
    //  sp.paint( gc, dev );
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
      final IRecord rec = m_result.get( i );
      final Object val = rec.getValue( m_domComp );
      if( val != null )
      {
        if( min == null || cc.compare( min, val ) < 0 )
          min = val;
        if( max == null || cc.compare( max, val ) > 0 )
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
      points.add( new Point( 0, (int) height / 2 ) );
      points.add( new Point( width/5, (int) height / 2 ) );
      points.add( new Point( width/5*2, (int) height / 4 ) );
      points.add( new Point( width/5*3, (int) height / 4 *3 ) );
      points.add( new Point( width/5*4, (int) height / 2 ) );
      points.add( new Point( width, (int) height / 2 ) );
      line.setPath( points );
      line.paint( gcw, Display.getCurrent() );
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
