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
package org.kalypso.chart.ext.test.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.ext.test.data.IListDataContainer;

import de.openali.odysseus.chart.ext.base.axis.GenericLinearAxis;
import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author alibu
 */
public class GenericAxisLineLayer extends AbstractLineLayer
{

  private final IListDataContainer m_data;

  public GenericAxisLineLayer( final IListDataContainer data, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( lineStyle, pointStyle );
    m_data = data;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  @SuppressWarnings("unchecked")
  public void paint( final GC gc )
  {

    final ArrayList<Point> path = new ArrayList<Point>();

    final List<Object> domainValues = getDataContainer().getDomainValues();
    final List<Object> targetValues = getDataContainer().getTargetValues();

    final GenericLinearAxis dAxis = (GenericLinearAxis) getDomainAxis();
    final GenericLinearAxis tAxis = (GenericLinearAxis) getTargetAxis();

    final Object domainFirst = domainValues.get( 0 );
    final Object targetFirst = targetValues.get( 0 );

    final IDataOperator ddo = dAxis.getDataOperator( domainFirst.getClass() );
    final IDataOperator tdo = tAxis.getDataOperator( targetFirst.getClass() );

    final int dataSize = domainValues.size();

    for( int i = 0; i < dataSize; i++ )
    {
      final IDataRange<Number> domRange = dAxis.getNumericRange();

      final Number min = domRange.getMin();
      final Number max = domRange.getMax();

      final Object domVal = domainValues.get( i );
      final Number domValNum = ddo.logicalToNumeric( domVal );
      boolean setPoint = false;
      if( domValNum.doubleValue() >= min.doubleValue() && domValNum.doubleValue() <= max.doubleValue() )
      {
        setPoint = true;
      }
      else
      {
        // kleiner als min: Nachfolger muss >= min sein
        if( domValNum.doubleValue() <= min.doubleValue() && i < domainValues.size() - 1 )
        {

          final Object next = domainValues.get( i + 1 );

          final Number nextNum = ddo.logicalToNumeric( next );
          if( nextNum.doubleValue() >= min.doubleValue() )
          {
            setPoint = true;
          }
        }
        // größer als max: Vorgänger muss <= max sein
        else if( domValNum.doubleValue() >= max.doubleValue() && i > 0 )
        {
          final Object prev = domainValues.get( i - 1 );
          final Number prevNum = ddo.logicalToNumeric( prev );
          if( prevNum.doubleValue() <= max.doubleValue() )
          {
            setPoint = true;
          }
          else
          {
            // jetzt kann man aufhören
            break;
          }
        }
      }

      if( setPoint )
      {

        final Object targetVal = targetValues.get( i );
        final Number targetValNum = tdo.logicalToNumeric( targetVal );

        final int domScreen = dAxis.numericToScreen( domValNum );
        final int valScreen = tAxis.numericToScreen( targetValNum );
        Point p;
        if( getDomainAxis().getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
        {
          p = new Point( domScreen, valScreen );

        }
        else
        {
          p = new Point( valScreen, domScreen );
        }
        path.add( p );
      }
    }

    drawLine( gc, path );
    drawPoints( gc, path );
  }

  public IListDataContainer getDataContainer( )
  {
    return m_data;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( )
  {
    // TODO Auto-generated method stub
    return null;
  }

}
