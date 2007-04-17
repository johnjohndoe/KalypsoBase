package de.openali.diagram.ext.base.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.openali.diagram.ext.base.style.StyledLine;
import de.openali.diagram.framework.logging.Logger;
import de.openali.diagram.framework.model.data.IDataRange;
import de.openali.diagram.framework.model.legend.ILegendItem;
import de.openali.diagram.framework.model.legend.LegendItem;
import de.openali.diagram.framework.model.mapper.IAxis;
import de.openali.diagram.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.diagram.framework.model.styles.IStyledElement;
import de.openali.diagram.framework.model.styles.IStyleConstants.SE_TYPE;

/**
 * @author alibu
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
public class GridLayer<T_domain extends Comparable, T_value extends Comparable> extends AbstractChartLayer
{

  public enum GridOrientation
  {
    HORIZONTAL,
    VERTICAL,
    BOTH
  };
  
  private GridOrientation m_orientation;

  public GridLayer( IAxis<T_domain> domAxis, IAxis<T_value> valAxis, GridOrientation orientation )
  {
	  m_domainAxis=domAxis;
	  m_targetAxis=valAxis;
	  m_orientation=orientation;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
@SuppressWarnings("unchecked")
public void paint( final GCWrapper gc )
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
       vAxis=getTargetAxis();
    }
    else
    {
      hAxis=getTargetAxis();
      vAxis=getDomainAxis();
    }


    //von links nach rechts zeichnen
    if (m_orientation==GridOrientation.BOTH || m_orientation==GridOrientation.HORIZONTAL)
    {
        Comparable[] vTicks = vAxis.getRegistry().getRenderer(vAxis).getGridTicks( vAxis );
        IDataRange hRange = hAxis.getDataRange();
        int xfrom=hAxis.logicalToScreen(hRange.getMin());
        int xto=hAxis.logicalToScreen(hRange.getMax());
        if (vTicks!=null)
        {
          for( Comparable vTick : vTicks )
          {
            path.clear();
            path.add( new Point(xfrom, vAxis.logicalToScreen( vTick )) );
            path.add( new Point(xto, vAxis.logicalToScreen( vTick )) );
            sl.setPath( path );
            sl.paint( gc);
          }
        }
    }
    //von unten nach oben zeichnen
    if (m_orientation==GridOrientation.BOTH || m_orientation==GridOrientation.VERTICAL)
    {
      Comparable[] hTicks = hAxis.getRegistry().getRenderer(hAxis).getGridTicks( hAxis );
      IDataRange vRange = vAxis.getDataRange();
      int yfrom= vAxis.logicalToScreen(vRange.getMin() ) ;
      int yto=vAxis.logicalToScreen(vRange.getMax() );
      if (hTicks!=null)
      {
        for( Comparable hTick : hTicks )
        {
          path.clear();
          path.add( new Point(hAxis.logicalToScreen( hTick ),yfrom) );
          path.add( new Point(hAxis.logicalToScreen( hTick ), yto ) );
          sl.setPath( path );
          sl.paint( gc);
        }
      }
    }
    sl.setPath( path );
  }


  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getLegendItem()
   */
  public ILegendItem getLegendItem()
  {
	ILegendItem l=null;
    Image img=new Image(Display.getCurrent(), 20, 20);
    drawIcon(img, 20, 20);
    ImageData id=img.getImageData();
    img.dispose();
    l=new LegendItem(null, getId(), id);
    return l;
  }


  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainRange()
   */
  public IDataRange getDomainRange( )
  {
    return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTargetRange()
   */
  public IDataRange getTargetRange( )
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
      line.paint( gcw);
      points.clear();
      points.add( new Point( 0, (int) (height * 0.7) ) );
      points.add( new Point( width, (int) (height * 0.7) ) );
      line.setPath( points );
      line.paint( gcw );
      points.clear();
      points.add( new Point( (int) (width*0.3), 0 ) );
      points.add( new Point( (int) (width*0.3), height ) );
      line.setPath( points );
      line.paint( gcw);
      points.clear();
      points.add( new Point((int) (width*0.7), 0 ) );
      points.add( new Point( (int) (width*0.7),height ) );
      line.setPath( points );
      line.paint( gcw );
      points.clear();
    }

    gc.dispose();
    gcw.dispose();
  }
}
