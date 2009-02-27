package org.kalypso.swtchart.chart.axis.renderer;

import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.chart.axis.component.IAxisComponent;
import org.kalypso.swtchart.logging.Logger;


/**
 * @author burtscher
 */
public class CalendarIntervalAxisRenderer extends AbstractAxisRenderer<Calendar>
{

  private SimpleDateFormat m_dateFormat;
  private TreeMap<Long, ArrayList<Calendar>> m_durationTickMap;
  private TreeMap<Long, Integer> m_durationMap=null;


  private final int[] m_durationHierarchy=new int[]
  {
      Calendar.MILLISECOND,
      Calendar.SECOND,
      Calendar.MINUTE,
      Calendar.HOUR_OF_DAY,
      Calendar.DAY_OF_MONTH,
      Calendar.WEEK_OF_YEAR,
      Calendar.MONTH,
      Calendar.YEAR
  };




  /**
   * @param foreground
   *          Color of axis, ticks and text
   * @param lineWidth
   *          Width of ticks and axis line
   * @param tickLength
   * @param tickLabelInsets
   *          <ul>
   *          <li>top: distance between ticklabel and axis</li>
   *          <li>bottom: distance between ticklabel and outside</li>
   *          <li>left: distance between ticklabel and previous ticklabel</li>
   *          <li>right: distance between ticklabel and next ticklabel</li>
   *          </ul>
   * @param maxDigits
   * @param labelInsets
   * @param gap
   *          space between axis and component border
   */
  public CalendarIntervalAxisRenderer( final RGB foregroundRGB, final RGB backgroundRGB, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick, final SimpleDateFormat df )
  {
    super(foregroundRGB, backgroundRGB, lineWidth, tickLength, tickLabelInsets, labelInsets, gap, fdLabel, fdTick);
    m_dateFormat = df;
    initDurationMap();
  }

  private void initDurationMap()
  {
    m_durationMap=new TreeMap<Long, Integer>();

    long second=1000;
    long minute=60*second;
    long hour=60*minute;
    long day=24*hour;
    long week=4*day;
    long month=4*week;
    long year=12*month;

    m_durationMap.put( second, Calendar.SECOND);
    m_durationMap.put( minute, Calendar.MINUTE);
    m_durationMap.put( hour, Calendar.HOUR_OF_DAY);
    m_durationMap.put( day, Calendar.DATE);
    m_durationMap.put( week, Calendar.WEEK_OF_YEAR);
    m_durationMap.put( month, Calendar.MONTH);
    m_durationMap.put( year, Calendar.YEAR);
  }




  private synchronized TreeMap<Long, ArrayList<Calendar>> getTickMap( IAxis<Calendar> axis)
  {
    if (m_durationTickMap==null || m_durationTickMap.size()==0)
        calcTickMap( axis );
    return m_durationTickMap;
  }


  private void calcTickMap(IAxis<Calendar> axis)
  {
    m_durationTickMap=new TreeMap<Long, ArrayList<Calendar>>();

    int col=0;

    ArrayList<Calendar> curTicks=null;

    int minScreenIntervalSize=5;
    int maxColCount=3;





    Set<Long> durKeys = m_durationMap.keySet();
    for( Long durKey : durKeys )
    {
      Logger.logInfo( Logger.TOPIC_LOG_AXIS, "running through durationMap with duration "+durKey );
      
      int screenSize=calcScreenIntervalSize( durKey.longValue(), axis );
      Logger.logInfo( Logger.TOPIC_LOG_AXIS, "screenSize = "+screenSize);

      //Das gezeichnete Intervall muss eine Mindestbreite besitzen
      if (screenSize >= minScreenIntervalSize)
      {
        //.. nach der gewünschten Anzahl von Zeilen abbrechen
        if (col<maxColCount)
        {
          Logger.logInfo( Logger.TOPIC_LOG_AXIS, "Intervall der Länge "+durKey);
          curTicks=calcDurationTicks( axis, m_durationMap.get(durKey).intValue());
          m_durationTickMap.put( durKey, curTicks );

          //Counter nur erhöhen, wenn auch gezeichnet wurde
          col++;
        }
        else
          break;
      }
      else
      {
          Rectangle r=axis.getRegistry().getComponent( axis ).getBounds();
          Logger.logInfo( Logger.TOPIC_LOG_AXIS, " screenSize < minScreenInterval. Component size: "+r.width+"|"+r.height);
      }
    }
  }


  /**
   *   @return offset to use with next AxisElement
   */
  private int drawAxisCols(GCWrapper gc, IAxis<Calendar> axis)
  {
    TreeMap<Long, ArrayList<Calendar>> tickMap = getTickMap( axis );
    Set<Entry<Long, ArrayList<Calendar>>> durKeys = tickMap.entrySet();

    int offset=0;

    for( Entry<Long, ArrayList<Calendar>> entry : durKeys )
    {
      offset=drawCol( gc, axis, entry.getValue(), offset, m_durationMap.get(entry.getKey()).intValue());
    }
    return offset;
  }

  /**
   * Checks the screen distance of a time interval
   * @param millis duration of interval in milliseconds
   */
  private int calcScreenIntervalSize(long millis, IAxis<Calendar> axis)
  {
    //als Startwert axis.getFrom() verwenden, um nich zu große werte zu produzieren
    Calendar startCal=axis.getFrom();
    Calendar endCal=Calendar.getInstance();
    endCal.setTimeZone( startCal.getTimeZone() );
    endCal.setTimeInMillis( startCal.getTimeInMillis()+millis );

    int screenStart=axis.logicalToScreen( startCal ) ;
    int screenEnd=axis.logicalToScreen( endCal ) ;
    Logger.logInfo( Logger.TOPIC_LOG_AXIS, "ScreenStart = "+screenStart+ " screenEnd = "+screenEnd);

    return Math.abs( screenEnd- screenStart);
  }

  /**
   * Draws a column of ticks
   * @param offset number of row
   * @return int value to be used as offset for the next column
   */
  private int drawCol(GCWrapper gc, IAxis<Calendar> axis, ArrayList<Calendar> ticks, int offset, int duration)
  {
    Color dark=new Color(gc.getDevice(), new RGB(220, 220, 220));
    Color light=new Color(gc.getDevice(), new RGB(240, 240, 240));

    Font font=new Font(gc.getDevice(), m_fontDataTick);

    int width=0;

    int axisFrom=axis.logicalToScreen( axis.getFrom() );
    int axisTo=axis.logicalToScreen( axis.getTo() );
    /**
     * kleinster Wert, den eine sichtbare Screen-Position der Achse annehmen kann
     */
    int axisMin=0;
    /**
     * größter Wert, den eine sichtbare Screen-Position der Achse annehmen kann
     */
    int axisMax=0;
    if (
        ( axis.getDirection()==DIRECTION.POSITIVE && !(axis.getPosition().getOrientation()==ORIENTATION.VERTICAL) )
       ||  ( axis.getDirection()==DIRECTION.NEGATIVE && (axis.getPosition().getOrientation()==ORIENTATION.VERTICAL) )

        )
    {
      axisMin=axisFrom;
      axisMax=axisTo;
    }
    else
    {
      axisMin=axisTo;
      axisMax=axisFrom;
    }

    IAxisComponent ac = axis.getRegistry().getComponent( axis );
    int compHeight=ac.getBounds().height;
    int compWidth=ac.getBounds().width;

    Point maxLabelExtent = calcTickLabelSize( gc, ticks, duration);

    for( int i=0;i<ticks.size();i++ )
    {

      //    Farben abwechseln
      if (i%2==0)
        gc.setBackground( light );
      else
        gc.setBackground( dark );

      if (i>0)
      {
        Calendar startDate=ticks.get( i-1 );
        startDate.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
        Calendar endDate=ticks.get( i );
        endDate.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );

        int startScreenPos=axis.logicalToScreen( startDate );
        int endScreenPos=axis.logicalToScreen( endDate );

        //text des labels
        String text=getTickLabel( duration, startDate );

        if (axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL)
        {
           //Breite (hier: Höhe weil horizontal) der Spalte
          width=maxLabelExtent.y+m_tickLabelInsets.top+m_tickLabelInsets.bottom;


          //Startwert ist kleinere Achsenposition
          int x1=Math.min( startScreenPos, endScreenPos);
          //Endwert ist größere Achsenposition
          int x2=Math.max( startScreenPos, endScreenPos);
          int y1=0;
          int y2=0;
          if (axis.getPosition()==POSITION.BOTTOM)
          {
            y1=offset;
            y2=offset+width;
          }
          else
          {
            y1=compHeight-offset;
            y2=compHeight-offset-width;
          }
          //nur zeichnen, wenn Startpunkt < rechtes Ende und Endpunkt > linkes Ende
          if (x1<=axisMax && x2>=axisMin)
          {
            int rectwidth=Math.abs(x2-x1);
            int rectheight=Math.abs(y2-y1);
            Rectangle rect=new Rectangle(x1, y1, rectwidth, rectheight) ;
            gc.fillRectangle( rect );

            //Textposition berechnen
            //Wenn Start UND Endposition über die AxisComponent hinausgehen, wird auf die tatsächliche Breite skaliert
            if ( x1<=axisMin && x2>=axisMax )
            {
              x1=axisMin;
              x2=axisMax;
            }

            //Text zeichnen
            int textX=(int) (x1+( (x2-x1)/2)  -(0.5*maxLabelExtent.x));
            int textY=(int) (y1+( (y2-y1)/2)  -(0.5*maxLabelExtent.y));
            Logger.logInfo( Logger.TOPIC_LOG_AXIS, "Drawing Text "+text+ " at position "+textX+"|"+textY);
            gc.setFont( font );
            gc.drawText( text,textX , textY );

            //und jetzt erst das Rechteck - sonst wird es vom Schrifthintergrund übermalt
            gc.drawRectangle( rect );
           }

        }
        else if (axis.getPosition().getOrientation() == ORIENTATION.VERTICAL)
        {
          //Breite der Spalte
          width=maxLabelExtent.x+m_tickLabelInsets.left+m_tickLabelInsets.right+m_gap;


          //Startwert ist kleinere Achsenposition
          int y1=Math.min( startScreenPos, endScreenPos);
          //Endwert ist größere Achsenposition
          int y2=Math.max( startScreenPos, endScreenPos);
          int x1=0;
          int x2=0;
          if (axis.getPosition()==POSITION.LEFT)
          {
            x1=compWidth-offset-width;
            x2=compWidth-offset;
          }
          else
          {
            x1=offset;
            x2=offset+width;
          }

          //nur zeichnen, wenn Startpunkt < unteres Ende und Endpunkt > oberes Ende
          if (( y1<=axisMax && y2>=axisMin) )
          {
            int rectwidth=Math.abs(x2-x1);
            int rectheight=Math.abs(y2-y1);
            Rectangle rect=new Rectangle(x1, y1, rectwidth, rectheight) ;
            gc.fillRectangle( rect );

            //Text zeichnen

            //Textposition berechnen
            //Wenn Start UND Endposition über die AxisComponent hinausgehen, wird auf die tatsächliche Breite skaliert
            if ( y1<=axisMin && y2>=axisMax )
            {
               y1=axisMin;
               y2=axisMax;
            }
            int textY=(int) (y1+( (y2-y1)/2)  -(0.5*maxLabelExtent.y));
            int textX=(int) (x1+( (x2-x1)/2) - (0.5*maxLabelExtent.x));
            Logger.logInfo( Logger.TOPIC_LOG_AXIS, "Drawing Text "+text+ " at position "+textX+"|"+textY);
            gc.setFont( font );
            gc.drawText( text,textX , textY );

            //und jetzt erst das Rechteck - sonst wird es vom Schrifthintergrund übermalt
            gc.drawRectangle( rect );
          }

        }
      }
    }
    font.dispose();
    dark.dispose();
    light.dispose();

    return offset+width;
  }

  private String getTickLabel(int duration, Calendar tickDate)
  {
    String label="";
    if (duration==Calendar.WEEK_OF_YEAR)
      label="KW"+" "+(new Integer(tickDate.get( duration ))).toString();
    else if (duration==Calendar.DATE)
    {
      SimpleDateFormat sdf=new SimpleDateFormat("dd.");
      sdf.setCalendar( tickDate );
      label=sdf.format( tickDate.getTimeInMillis() );
    }
    else if (duration==Calendar.HOUR_OF_DAY)
    {
      SimpleDateFormat sdf=new SimpleDateFormat("HH'h'");
      sdf.setCalendar( tickDate );
      label=sdf.format( tickDate.getTimeInMillis() );
    }
    else if (duration==Calendar.MINUTE)
    {
      SimpleDateFormat sdf=new SimpleDateFormat("mm");
      label=sdf.format( tickDate.getTimeInMillis() );
    }
    else if (duration==Calendar.SECOND)
    {
      SimpleDateFormat sdf=new SimpleDateFormat("ss");
      label=sdf.format( tickDate.getTimeInMillis() );
    }
    else if (duration==Calendar.MILLISECOND)
    {
      SimpleDateFormat sdf=new SimpleDateFormat("SSS");
      label=sdf.format( tickDate.getTimeInMillis() );
    }
    else if (duration==Calendar.YEAR)
      label=(new Integer(tickDate.get( duration ))).toString();
    else if (duration==Calendar.MONTH)
    {
      SimpleDateFormat sdf=new SimpleDateFormat("MMMMMMMMMMMMMM");
      label=sdf.format( tickDate.getTimeInMillis() );
    }
    return label;
  }

/**
 * @return Collection of calendars; one for every duration between start and end time of axis
 */
  public ArrayList<Calendar> calcDurationTicks(final IAxis<Calendar> axis, final int duration)
  {
    ArrayList<Calendar> ticks=new ArrayList<Calendar>();

      //ermitteln, an welcher Stelle dir übergebene Duration in der Hierarchie steht
      int durationPlace=0;
      for (int i=0;i<m_durationHierarchy.length;i++)
      {
          if (duration==m_durationHierarchy[i])
          {
            durationPlace=i;
            break;
          }
      }

      //ermittele volle Starteinheit (abrunden) und Endeinheit (aufrunden)
      //wenn hier nicht geklont wird, dann kommt bei jedem Aufruf etwas anderes raus
      Calendar startCal=(Calendar) axis.getFrom().clone();
      startCal.setTimeZone( TimeZone.getTimeZone("GMT+0000") );
      Calendar endCal=(Calendar) axis.getTo().clone();
      endCal.setTimeZone( TimeZone.getTimeZone("GMT+0000") );

      Logger.logInfo( Logger.TOPIC_LOG_AXIS, "AxisFrom: "+m_dateFormat.format( new Date(startCal.getTimeInMillis()) ));
      Logger.logInfo( Logger.TOPIC_LOG_AXIS, "AxisTo: "+m_dateFormat.format( new Date(endCal.getTimeInMillis()) ));


      for (int i=0;i<m_durationHierarchy.length;i++)
      {
        if (i < durationPlace )
        {
          //Sonderfall WEEK_OF_YEAR ausschliessen - sonst werden auch die Tage verschoben
          if (m_durationHierarchy[i]!=Calendar.WEEK_OF_YEAR)
          {
            startCal.set( m_durationHierarchy[i], startCal.getMinimum( m_durationHierarchy[i] ) );
            //endCal.set( durationHierarchy[i], 0 );
          }
        }

        else
        {
          startCal.add( m_durationHierarchy[i], -1 );
          //hier werden sicherheitshalber mal 2 addiert
          endCal.add( m_durationHierarchy[i], 1 );
          break;
        }
      }
      Logger.logInfo( Logger.TOPIC_LOG_AXIS, "Start: "+m_dateFormat.format( new Date(startCal.getTimeInMillis()) ));
      Logger.logInfo( Logger.TOPIC_LOG_AXIS, "End: "+m_dateFormat.format( new Date(endCal.getTimeInMillis()) ));

      Calendar curCal=(Calendar) startCal.clone();
      curCal.setTimeZone( TimeZone.getTimeZone("GMT+0000") );
      while (curCal.compareTo( endCal )<=0)
      {
        Calendar tmpCal=(Calendar) curCal.clone();
        curCal.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );
        ticks.add( tmpCal );
        m_dateFormat.setCalendar( curCal );
        Logger.logInfo( Logger.TOPIC_LOG_AXIS, m_dateFormat.format( new Date(curCal.getTimeInMillis()) ));
        curCal.add( duration, 1);
      }


      return ticks;
  }


  @Override
  protected Point getTextExtent( GCWrapper gc, final Calendar value, FontData fd )
  {
    final String label = m_dateFormat.format( new Date( value.getTimeInMillis() ) );
    Point p = getTextExtent( gc, label, fd );
    return p;
  }

  protected int[] createAxisSegment( final IAxis<Calendar> axis, final Rectangle screen )
  {
    int startX;
    int startY;
    int endX;
    int endY;

    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      startX = screen.x;
      endX = screen.x + screen.width;

      if( axis.getPosition() == POSITION.BOTTOM )
        startY = screen.y + m_gap;
      else
        startY = screen.y + screen.height - 1 - m_gap;
      endY = startY;

      if( axis.getDirection() == DIRECTION.NEGATIVE )
      {
        int tmp = startX;
        startX = endX;
        endX = tmp;
      }
    }
    else
    {
      startY = screen.y;
      endY = screen.y + screen.height;

      if( axis.getPosition() == POSITION.RIGHT )
        startX = screen.x + m_gap;
      else
        startX = screen.x + screen.width - m_gap -1;
      endX = startX;

      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        int tmp = startY;
        startY = endY;
        endY = tmp;
      }
    }

    return new int[] { startX, startY, endX, endY };
  }

  public void paint( GCWrapper gc, final IAxis<Calendar> axis, final Rectangle screen )
  {
    Image img = paintBuffered( gc, axis, screen );
    gc.drawImage( img, 0, 0 );
    img.dispose();
  }

  public Image paintBuffered( GCWrapper gc, final IAxis<Calendar> axis, final Rectangle screen )
  {
    Device dev=gc.getDevice();
    Image bufImg = new Image( dev, dev.getBounds() );
    GC bufGc = new GC( bufImg );
    GCWrapper bufGcw = new GCWrapper( bufGc );

    Color foreground=new Color(dev, m_rgbForeground);
    Color background=new Color(dev, m_rgbBackground);
    bufGcw.setForeground( foreground );
    bufGcw.setBackground( background );

    // draw axis line
    int[] coords = createAxisSegment( axis, screen );
    assert coords != null && coords.length == 4;

    int labelOffset=drawAxisCols( bufGcw, axis);
    drawAxisLabel( bufGcw, axis,  labelOffset );
    drawAxisLine( bufGcw, coords[0], coords[1], coords[2], coords[3] );
    //Label als letztes zeichnen, da die transformation sonst stört
    //TODO: tranformation entfernen



    bufGc.dispose();
    bufGcw.dispose();
    foreground.dispose();
    background.dispose();

    return bufImg;
  }


  protected void drawAxisLine( GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  {
    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.drawLine( x1, y1, x2, y2 );
  }


  public Point calcTickLabelSize(GCWrapper gc, Collection<Calendar> ticks, int duration)
  {
      int maxWidth=0;
      int maxHeight=0;
      if (ticks!=null)
      {
        for( Calendar tick : ticks )
        {
          String tickLabel=getTickLabel( duration, tick );
          Point labelExtent = getTextExtent( gc, tickLabel, m_fontDataTick );
          if (labelExtent.x>maxWidth)
            maxWidth=labelExtent.x;
          if (labelExtent.y>maxHeight)
            maxHeight=labelExtent.y;
        }
      }
      return new Point(maxWidth, maxHeight);
  }



  /**
   * Gibt die Breite bzw. Tiefe einer Achse zurück. Dadurch wird die Grösse der AxisComponent bestimmt
   */
  public int getAxisWidth( IAxis<Calendar> axis )
  {
    //Gesamtbreite
    int width = 0;

    // Testutensilien erzeugen
    Display dev = Display.getCurrent();
    Image img = new Image( dev, 1, 1 );
    GC gc = new GC( img );
    GCWrapper gcw = new GCWrapper( gc );

    //zum speichern der Tickbreite der einzelnen Spalten
    ArrayList<Point> tickExtends=new ArrayList<Point>();
    TreeMap<Long, ArrayList<Calendar>> tickMap = getTickMap( axis );

    Set<Long> tickKeys = tickMap.keySet();
    for( Long tickKey : tickKeys )
    {
      tickExtends.add( calcTickLabelSize( gcw, m_tickMap.get( tickKey ), m_durationMap.get(tickKey)) );
    }

    for( Point tickExtend : tickExtends)
    {
      if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      {
        width += m_tickLabelInsets.top + m_tickLabelInsets.bottom;
        width += m_labelInsets.top + m_labelInsets.bottom;
        width = tickExtend.y;
      }
      else
      {
        width += m_tickLabelInsets.left + m_tickLabelInsets.right;
        width += m_labelInsets.left + m_labelInsets.right;
        width = tickExtend.x;
      }

    }

    width += m_lineWidth;



    // Höhe des Labels
    Point labelExtent = getTextExtent( gcw, axis.getLabel(), m_fontDataLabel );
    width += labelExtent.y;
    width += m_labelInsets.bottom;
    width += m_labelInsets.top;

    gcw.dispose();
    gc.dispose();
    img.dispose();
    //return width;
    return 200;
  }

  /**
   * @see org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer#getTicks(org.kalypso.swtchart.chart.axis.IAxis)
   */
  public Collection<Calendar> getGridTicks( IAxis<Calendar> axis )
  {
     TreeMap<Long, ArrayList<Calendar>> tickMap=getTickMap( axis );
     if (tickMap.size()>0)
       //im ersten "Fach" sind die Ticks mit den kleinsten Intervallen
       return m_durationTickMap.get( m_durationTickMap.firstKey() );
     else
       return null;
  }

  protected void drawAxisLabel( GCWrapper gc, IAxis<Calendar> axis, int offset )
  {
    if( axis.getLabel() != null )
    {
      final Point textExtent = getTextExtent( gc, axis.getLabel(), m_fontDataLabel );

      int x = 0;
      int y = 0;

      Rectangle r=axis.getRegistry().getComponent( axis ).getBounds();
      int compWidth=r.width;
      int compHeight=r.height;

      Transform tr = new Transform( gc.getDevice() );
      Color bg=null;
      Color fg=null;

      int rotation = 0;
      try
      {
        if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          x = compWidth / 2 - textExtent.x / 2;

          if( axis.getPosition() == POSITION.TOP )
          {
            y=compHeight-offset-textExtent.y-m_labelInsets.bottom;
          }
          else
          {
            y=offset+m_labelInsets.top;
          }
        }
        else
        {
          y = compHeight / 2;


          if( axis.getPosition() == POSITION.LEFT )
          {
            rotation = -90;
            x=compWidth-offset-textExtent.y-m_labelInsets.bottom;
            y += textExtent.x / 2;
          }
          else
          {
            rotation = 90;
            y -= textExtent.x / 2;
            x = offset+textExtent.y+m_labelInsets.bottom;
          }
          tr.translate( x, y );
          tr.rotate( rotation );
          tr.translate( -x, -y );

        }
        if( tr != null )
        {
          gc.setTransform( tr );
        }

        fg=new Color(gc.getDevice(), m_rgbForeground);
        bg=new Color(gc.getDevice(), m_rgbBackground);
        gc.setForeground( fg );
        gc.setBackground( bg );

        drawText( gc, axis.getLabel(), x, y, m_fontDataLabel );


        tr.translate( x, y );
        tr.rotate( -rotation );
        tr.translate( -x, -y );
        gc.setTransform( tr );
      }



      finally
      {
        if( tr != null )
          tr.dispose();
        if (fg!=null)
          fg.dispose();
        if (bg!=null)
          bg.dispose();
      }
    }
  }

}
