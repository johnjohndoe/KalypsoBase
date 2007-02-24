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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.chart.layer.AbstractChartLayer;
import org.kalypso.swtchart.chart.layer.IDataRange;
import org.kalypso.swtchart.chart.legend.ILegendItem;
import org.kalypso.swtchart.chart.styles.IStyledElement;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;

/**
 * @author alibu
 */
public class TestBarLayer extends AbstractChartLayer
{
  private IComponent m_domComp;

  private IComponent m_valComp;

  private TupleResult m_result;

  public TestBarLayer( TupleResult result, IComponent domComp, IComponent valComp, IAxis domAxis, IAxis valAxis )
  {
    super( null, null, domAxis, valAxis );
    m_domComp = domComp;
    m_valComp = valComp;
    m_result = result;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  public void drawIcon( Image img, int width, int height )
  {

  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainRange()
   */
  public IDataRange getDomainRange( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getLegendItem()
   */
  public ILegendItem getLegendItem( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getValueRange()
   */
  public IDataRange getValueRange( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gc, Device dev )
  {
    HashMap<Calendar, Integer> dailyLevel = new HashMap<Calendar, Integer>();

    for( int i = 0; i < m_result.size(); i++ )
    {
      ArrayList<Point> path = new ArrayList<Point>( 4 );
      IRecord record = m_result.get( i );
      Calendar domValue = (Calendar) record.getValue( m_domComp );
      Number valValue = (Number) record.getValue( m_valComp );

      Calendar startCal = domValue;
      startCal.set( Calendar.HOUR_OF_DAY, 0 );
      startCal.set( Calendar.MINUTE, 0 );
      startCal.set( Calendar.SECOND, 0 );
      startCal.set( Calendar.MILLISECOND, 0 );

      Integer curVal = null;
      if( (curVal = dailyLevel.get( startCal )) == null )
        dailyLevel.put( startCal, valValue.intValue() );
      else
      {
        dailyLevel.put( startCal, curVal.intValue() + valValue.intValue() );
      }
    }

    long dayInMillis = 1000 * 60 * 60 * 24;
    ArrayList<Point> path = new ArrayList<Point>();
    IStyledElement poly = getStyle().getElement( SE_TYPE.POLYGON, 1 );
    Set<Calendar> cals = dailyLevel.keySet();
    for( Calendar startCal : cals )
    {
      Number valValue = dailyLevel.get( startCal );
      long startTime = startCal.getTimeInMillis();

      Calendar endCal = Calendar.getInstance();

      endCal.setTimeInMillis( startTime + dayInMillis );

      final IAxis domainAxis = getDomainAxis();
      final IAxis valueAxis = getValueAxis();
      if( domainAxis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      {
        path.add( new Point( domainAxis.logicalToScreen( startCal ), valueAxis.logicalToScreen( 0 ) ) );
        path.add( new Point( domainAxis.logicalToScreen( startCal ), valueAxis.logicalToScreen( valValue ) ) );
        path.add( new Point( domainAxis.logicalToScreen( endCal ), valueAxis.logicalToScreen( valValue ) ) );
        path.add( new Point( domainAxis.logicalToScreen( endCal ), valueAxis.logicalToScreen( 0 ) ) );

      }
      else
      {
        path.add( new Point( valueAxis.logicalToScreen( 0 ), domainAxis.logicalToScreen( startCal ) ) );
        path.add( new Point( valueAxis.logicalToScreen( valValue ), domainAxis.logicalToScreen( startCal ) ) );
        path.add( new Point( valueAxis.logicalToScreen( valValue ), domainAxis.logicalToScreen( endCal ) ) );
        path.add( new Point( valueAxis.logicalToScreen( 0 ), domainAxis.logicalToScreen( endCal ) ) );
      }
      poly.setPath( path );
      poly.paint( gc, dev );
    }
  }

}
