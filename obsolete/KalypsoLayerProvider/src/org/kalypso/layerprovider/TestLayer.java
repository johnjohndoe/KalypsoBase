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

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.math.SampleData;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.IDataRange;
import org.kalypso.swtchart.chart.layer.impl.DataRange;
import org.kalypso.swtchart.chart.legend.ILegendItem;
import org.kalypso.swtchart.chart.styles.ILayerStyle;
import org.kalypso.swtchart.chart.styles.IStyledElement;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;

/**
 * @author alibu
 */
public class TestLayer implements IChartLayer
{
  private final IAxis m_domAxis;

  private final IAxis m_valAxis;

  private final Double[][] m_doubles;

  private ILayerStyle m_style;

  private String m_name;

  private String m_description;

  public TestLayer( IAxis domAxis, IAxis valAxis )
  {
    m_domAxis = domAxis;
    m_valAxis = valAxis;

    m_doubles = SampleData.createSinusPoints( 100 );
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
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getValueAxis()
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
   * Zeichnet die Daetn als Balken ins Diagramm
   * 
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gc, Device dev )
  {

    if( getStyle() != null )
    {

      IStyledElement polygon = m_style.getElement( SE_TYPE.POLYGON, 0 );

      // Rausfinden des Abstands zwischen zwei Datenbpunkten - geht davon aus, dass der Abstand zwischen
      // zwei Punkten stets dem der ersten zwei Punkte entspricht
      int pointDifference = Math.abs( m_domAxis.logicalToScreen( m_doubles[0][0] ) - m_domAxis.logicalToScreen( m_doubles[1][0] ) );
      double stackWidth = pointDifference * 1;
      int halfStackWidth = (int) (0.5 * stackWidth);

      IAxis hAxis, vAxis;
      for( int i = 0; i < m_doubles.length; i++ )
      {

        ArrayList<Point> imgPoints = new ArrayList<Point>();

        int x0 = 0, y0 = 0, x1 = 0, y1 = 0;
        // horizontale und vertikale Achse bestimmen
        if( m_domAxis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          // horizontale Achse setzen
          hAxis = m_domAxis;
          // vertikale Achse setzen
          vAxis = m_valAxis;
          int widthLeft = halfStackWidth;
          int widthRight = halfStackWidth;
          int xNow = hAxis.logicalToScreen( m_doubles[i][0] );

          Point insets = calcInsets( i, 0, hAxis );

          x0 = xNow - insets.x;
          x1 = xNow + insets.y;
          y0 = vAxis.logicalToScreen( 0 );
          y1 = vAxis.logicalToScreen( m_doubles[i][1] );
        }
        else
        {
          vAxis = m_domAxis;
          hAxis = m_valAxis;

          int yNow = vAxis.logicalToScreen( m_doubles[i][0] );

          Point insets = calcInsets( i, 0, vAxis );

          y0 = yNow - insets.x;
          y1 = yNow + insets.y;

          x0 = hAxis.logicalToScreen( 0 );
          x1 = hAxis.logicalToScreen( m_doubles[i][1] );
        }

        imgPoints.add( new Point( x0, y0 ) );
        imgPoints.add( new Point( x1, y0 ) );
        imgPoints.add( new Point( x1, y1 ) );
        imgPoints.add( new Point( x0, y1 ) );
        if( polygon != null )
        {
          polygon.setPath( imgPoints );
          // polygon.paint(gc, dev);
        }
        else
        {
          System.out.println( "Teststacklayer: Polygon == null" );
        }
      }
    }
  }

  /**
   * Berechnet den linken und rechten Abstand eines Datenpunktes zu seinen Balkenseiten
   * 
   * @param position
   *          Position des Werts in der Datenreihe
   * @param index
   *          Spalte (0 f¸r domain, 1 f¸r target)
   * @param axis
   *          die Achse, an der die Werte angetragen werden
   */
  private Point calcInsets( int position, int index, IAxis axis )
  {
    int widthLeft = 0;
    int widthRight = 0;
    int now = axis.logicalToScreen( m_doubles[position][index] );
    // ‹berpr¸fen, in welche Richtung gezeichnet wird, und dann den Abstand zu den tats‰chlichen Nachbars‰ule berechnen

    if( (axis.getDirection() == DIRECTION.POSITIVE && axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL)
        || (axis.getDirection() == DIRECTION.NEGATIVE && axis.getPosition().getOrientation() == ORIENTATION.VERTICAL) )
    {

      if( position > 0 )
      {
        int before = axis.logicalToScreen( m_doubles[position - 1][index] );
        widthLeft = (int) Math.ceil( Math.abs( (double) ((before - now - 1)) / 2 ) );
      }
      if( position < m_doubles.length - 1 )
      {
        int after = axis.logicalToScreen( m_doubles[position + 1][index] );
        widthRight = (int) Math.floor( Math.abs( (double) ((after - now - 1)) / 2 ) );
      }
    }
    else
    {
      if( position < m_doubles.length - 1 )
      {
        int before = axis.logicalToScreen( m_doubles[position + 1][index] );
        widthLeft = (int) Math.ceil( Math.abs( (double) ((before - now - 1)) / 2 ) );
      }
      if( position > 0 )
      {
        int after = axis.logicalToScreen( m_doubles[position - 1][index] );
        widthRight = (int) Math.floor( Math.abs( (double) ((after - now - 1)) / 2 ) );
      }
    }
    /*
     * Der letzte und der erste Punkt haben nun eine linke bzw. rechte Breite von 0; um das zu umgehen, wird einfach
     * angenommen, dass mindestens eine Breite gesetzt ist und der Wert der leeren Seite auf den der anderen gesetzt.
     * Wenn es insgesamt nur einen Datenpunkt gibt (Breite links und rechts = 0), wird einfach ein fester Wert vergeben
     */
    if( widthLeft == 0 && widthRight == 0 )
    {
      // widthLeft=5;
      // widthRight=5;
    }
    else
    {
      if( widthLeft == 0 )
        widthLeft = widthRight;
      if( widthRight == 0 )
        widthRight = widthLeft;
    }
    return new Point( widthLeft, widthRight );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainRange()
   */
  public IDataRange getDomainRange( )
  {
    Double xMin = null;
    Double xMax = null;

    for( int i = 0; i < m_doubles.length; i++ )
    {
      double x = m_doubles[i][0];

      if( xMin == null || x < xMin )
        xMin = x;
      if( xMax == null || x > xMax )
        xMax = x;
    }

    return new DataRange<Number>( xMin, xMax );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getValueRange()
   */
  public IDataRange getValueRange( )
  {
    Double yMin = null;
    Double yMax = null;

    for( int i = 0; i < m_doubles.length; i++ )
    {
      double y = m_doubles[i][1];

      if( yMin == null || y < yMin )
        yMin = y;
      if( yMax == null || y > yMax )
        yMax = y;
    }

    return new DataRange<Number>( yMin, yMax );
  }

  public void drawIcon( Image img, int width, int height )
  {
    // TODO Auto-generated method stub

  }

  public void setName( String name )
  {
    m_name = name;
  }

  public void setDescription( String description )
  {
    m_description = description;
  }

}
