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
package org.kalypso.layerprovider;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
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
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.IDataRange;
import org.kalypso.swtchart.chart.layer.impl.DataRange;
import org.kalypso.swtchart.chart.legend.ILegendItem;
import org.kalypso.swtchart.chart.styles.ILayerStyle;
import org.kalypso.swtchart.chart.styles.IStyledElement;
import org.kalypso.swtchart.chart.styles.StyledPolygon;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;
import org.kalypso.swtchart.chart.util.AliComparator;
import org.ksp.chart.viewerconfiguration.ParameterType;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author schlienger
 */
public class NiederschlagLayer implements IChartLayer
{
  private final TupleResult m_result;

  private final IAxis m_valAxis;

  private final IComponent m_domComp;

  private final IComponent m_valComp;

  private final IAxis m_domAxis;

  private ILayerStyle m_style;

  private final List<ParameterType> m_parameters;

  private String m_name;

  private String m_description;

  public NiederschlagLayer( TupleResult result, IComponent domComp, IComponent valComp, IAxis domAxis, IAxis valAxis, List<ParameterType> parameters )
  {
    m_result = result;
    m_domComp = domComp;
    m_valComp = valComp;
    m_domAxis = domAxis;
    m_valAxis = valAxis;
    m_parameters = parameters;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( final GCWrapper gc, final Device dev )
  {

    gc.setLineWidth( 5 );
    // gc.drawLine(0,0, 200, 200);
    System.out.println( "Drawing TupleResultChartLayer" );

    StyledPolygon sp = (StyledPolygon) getStyle().getElement( SE_TYPE.POLYGON, 0 );

    // Config-Parameter auslesen
    double barInterval = 0;
    XMLGregorianCalendar fixedPoint = null;
    if( m_parameters != null )
    {
      for( ParameterType param : m_parameters )
      {
        // parsen des Config-Werts in XMLGregCalendar
        if( param.getName().compareTo( "fixedPoint" ) == 0 && param.getValue().length() > 0 )
        {
          try
          {
            DatatypeFactory df = DatatypeFactory.newInstance();
            fixedPoint = df.newXMLGregorianCalendar( param.getValue() );
          }
          catch( DatatypeConfigurationException e )
          {
            e.printStackTrace();
          }
        }
        if( param.getName().compareTo( "barInterval" ) == 0 && param.getValue().length() > 0 )
        {
          barInterval = Double.parseDouble( param.getValue() );
          System.out.println( "Parsed BarInterval: " + barInterval );
        }
      }
    }
    // Falls noch nicht initialisiert werden nun Standardwerte gesetzt
    if( barInterval == 0 )
    {
      // Intervall: ein Tag in Millisekunden
      barInterval = 24 * 60 * 60 * 1000;
    }
    if( fixedPoint == null )
    {
      // Fixpunkt für Intervall setzten: Hauptsache 0 Uhr
      GregorianCalendar fixedGregCal = new GregorianCalendar();
      fixedGregCal.set( 2000, 0, 1, 0, 0 );
      fixedPoint = new XMLGregorianCalendarImpl( fixedGregCal );
    }
    System.out.println( "Used BarInterval: " + barInterval );

    // Daten zum Zeichnen durchlaufen
    int size = m_result.size();
    for( int i = 0; i < size; i++ )
    {
      final IRecord r1 = m_result.get( i );

      Object domVal = m_result.getValue( r1, m_domComp );
      Object valVal = m_result.getValue( r1, m_valComp );
      int x1, y1, x2, y2;
      if( m_domAxis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      {
        Point xs = m_domAxis.logicalToScreenInterval( domVal, fixedPoint, barInterval );
        x1 = Math.min( xs.x, xs.y );
        x2 = Math.max( xs.x, xs.y );
        y1 = m_valAxis.logicalToScreen( 0 );
        y2 = m_valAxis.logicalToScreen( valVal );
      }
      else
      {
        Point ys = m_domAxis.logicalToScreenInterval( domVal, fixedPoint, barInterval );
        y1 = Math.min( ys.x, ys.y );
        y2 = Math.max( ys.x, ys.y );
        x1 = m_valAxis.logicalToScreen( m_valAxis.getFrom() );
        x2 = m_valAxis.logicalToScreen( valVal );
      }
      ArrayList<Point> path = new ArrayList<Point>();
      path.add( new Point( x1, y1 ) );
      path.add( new Point( x2, y1 ) );
      path.add( new Point( x2, y2 ) );
      path.add( new Point( x1, y2 ) );

      if( sp != null )
      {
        sp.setPath( path );
        System.out.println( "Drawing Bar: (" + x1 + ":" + y1 + "):(" + x2 + ":" + y2 + ")" );
        sp.paint( gc, dev );
      }
    }

  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getName()
   */
  public String getName( )
  {
    return "Niederschlag";
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDescription()
   */
  public String getDescription( )
  {
    return "";
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
    AliComparator ac = new AliComparator();
    for( int i = 0; i < m_result.size(); i++ )
    {
      IRecord rec = m_result.get( i );
      Object val = rec.getValue( m_domComp );
      if( min == null || ac.compare( min, val ) < 0 )
        min = val;
      if( max == null || ac.compare( max, val ) > 0 )
        max = val;
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

    IStyledElement poly = m_style.getElement( SE_TYPE.POLYGON, 0 );

    if( poly != null )
    {
      ArrayList<Point> points = new ArrayList<Point>();
      // Linie von Links nach rechts
      points.add( new Point( (int) (width * 0.2), (int) (height * 0.2) ) );
      points.add( new Point( (int) (width * 0.2), (int) (height * 0.8) ) );
      points.add( new Point( (int) (width * 0.8), (int) (height * 0.8) ) );
      points.add( new Point( (int) (width * 0.8), (int) (height * 0.2) ) );
      poly.setPath( points );
      poly.paint( gcw, Display.getCurrent() );
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
}
