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
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.IDataRange;
import org.kalypso.swtchart.chart.legend.ILegendItem;
import org.kalypso.swtchart.chart.styles.ILayerStyle;
import org.kalypso.swtchart.chart.styles.IStyledElement;
import org.kalypso.swtchart.chart.styles.StyledLine;
import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;
import org.kalypso.swtchart.logging.Logger;
import org.ksp.chart.configuration.ParameterType;
import org.ksp.chart.configuration.Parameters;

/**
 * @author schlienger
 * @author burtscher
 *
 *  visualization of precipitation data as bar chart;
 *
 *  The following configuration parameters are needed for NiederschlagLayer:
 *  fixedPoint:     start time (e.g. 2006-07-01T00:00:00Z) of any possible bar within the chart;
 *                  typically, bars start at 00:00 and end at 23:59, but to get more flexibility,
 *                  it's possible to make them "last" more or less than one day (see next parameter)
 *                  and start them at any desired time / date.
 *  barWidth:       width of the chart bars in milliseconds (e.g. 86400000 for one day)
 *
 *  The following styled elements are used:
 *  Polygon:        used to draw the individual bars
 *
 *
 *
 */
public class GridLayer implements IChartLayer
{

  private final IAxis m_valAxis;

  private final IAxis m_domAxis;

  private ILayerStyle m_style;

  public enum GridOrientation
  {
    HORIZONTAL,
    VERTICAL,
    BOTH
  };

  private String m_name;

  private String m_description;

  private boolean m_isVisible=true;

  private GridOrientation m_orientation;

  public GridLayer( IAxis domAxis, IAxis valAxis, GridOrientation orientation )
  {
    m_domAxis = domAxis;
    m_valAxis = valAxis;
    m_orientation=orientation;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( final GCWrapper gc, final Device dev )
  {

    gc.setLineWidth( 5 );
    // gc.drawLine(0,0, 200, 200);
    Logger.trace( "Drawing GridLayer" );

    StyledLine sl = (StyledLine) getStyle().getElement( SE_TYPE.LINE, 0 );

    ArrayList<Point> path = new ArrayList<Point>();

    IAxis hAxis = null;
    IAxis vAxis = null;

    //Welche ist die horizontale, welche die horizontale Achse?
    if (getDomainAxis().getPosition().getOrientation()==ORIENTATION.HORIZONTAL)
    {
       hAxis=getDomainAxis();
       vAxis=getValueAxis();
    }
    else
    {
      hAxis=getValueAxis();
      vAxis=getDomainAxis();
    }


    //von links nach rechts zeichnen
    if (m_orientation==GridOrientation.BOTH || m_orientation==GridOrientation.HORIZONTAL)
    {
        Collection vTicks = vAxis.getRenderer().getGridTicks( vAxis );
        int xfrom=hAxis.logicalToScreen(hAxis.getFrom());
        int xto=hAxis.logicalToScreen(hAxis.getTo());
        if (vTicks!=null)
        {
          for( Object vTick : vTicks )
          {
            path.clear();
            path.add( new Point(xfrom, vAxis.logicalToScreen( vTick )) );
            path.add( new Point(xto, vAxis.logicalToScreen( vTick )) );
            sl.setPath( path );
            sl.paint( gc, dev );
          }
        }
    }
    //von unten nach oben zeichnen
    if (m_orientation==GridOrientation.BOTH || m_orientation==GridOrientation.VERTICAL)
    {
      Collection hTicks = hAxis.getRenderer().getGridTicks( hAxis );
      int yfrom= vAxis.logicalToScreen(vAxis.getFrom() ) ;
      int yto=vAxis.logicalToScreen(vAxis.getTo() );
      if (hTicks!=null)
      {
        for( Object hTick : hTicks )
        {
          path.clear();
          path.add( new Point(hAxis.logicalToScreen( hTick ),yfrom) );
          path.add( new Point(hAxis.logicalToScreen( hTick ), yto ) );
          sl.setPath( path );
          sl.paint( gc, dev );
        }
      }
    }



    sl.setPath( path );

  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getName()
   */
  public String getName( )
  {
    return "Raster";
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
    return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getValueRange()
   */
  public IDataRange getValueRange( )
  {
   return null;
  }

  public void drawIcon( Image img, int width, int height )
  {
    GC gc = new GC( img );
    GCWrapper gcw = new GCWrapper( gc );

    IStyledElement line = m_style.getElement( SE_TYPE.LINE, 0 );

    if( line != null )
    {
      ArrayList<Point> points = new ArrayList<Point>();
      // Linie von links nach rechts
      points.add( new Point( 0, (int) (height * 0.3) ) );
      points.add( new Point( width, (int) (height * 0.3) ) );
      line.setPath( points );
      line.paint( gcw, Display.getCurrent() );
      points.clear();
      points.add( new Point( 0, (int) (height * 0.7) ) );
      points.add( new Point( width, (int) (height * 0.7) ) );
      line.setPath( points );
      line.paint( gcw, Display.getCurrent() );
      points.clear();
      points.add( new Point( (int) (width*0.3), 0 ) );
      points.add( new Point( (int) (width*0.3), height ) );
      line.setPath( points );
      line.paint( gcw, Display.getCurrent() );
      points.clear();
      points.add( new Point((int) (width*0.7), 0 ) );
      points.add( new Point( (int) (width*0.7),height ) );
      line.setPath( points );
      line.paint( gcw, Display.getCurrent() );
      points.clear();
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
