package org.kalypso.swtchart.chart.axis.renderer;

import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.ChartUtilities;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.chart.util.InsetsHelper;
import org.kalypso.swtchart.logging.Logger;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author burtscher
 */
public class XMLGregorianCalendarAxisRenderer implements IAxisRenderer<XMLGregorianCalendar>
{
  private final SimpleDateFormat m_df;

  protected final int m_tickLength;

  protected final Insets m_labelInsets;

  protected final int m_maxDigits;

  protected final int m_lineWidth;

  private final Insets m_tickLabelInsets;

  private final Color m_foreground;

  private final Color m_background;

  private Collection<XMLGregorianCalendar> m_ticks;

  private final int m_gap;

  private FontData m_fontDataLabel = new FontData( "Arial", 11, SWT.BOLD );;

  private FontData m_fontDataTick = new FontData( "Arial", 8, SWT.NONE );

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
  public XMLGregorianCalendarAxisRenderer( final Color foreground, final Color background, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final int maxDigits, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick, final SimpleDateFormat df )
  {
    m_foreground = foreground;
    m_background = background;
    m_tickLength = tickLength;
    m_lineWidth = lineWidth;
    m_tickLabelInsets = tickLabelInsets;
    m_labelInsets = labelInsets;
    m_maxDigits = maxDigits;
    m_gap = gap;
    m_df = df;
  }

  public Point calcTickLabelSize( GCWrapper gc, final Device dev, final IAxis<XMLGregorianCalendar> axis )
  {
    final XMLGregorianCalendar logicalfrom = axis.getFrom();
    final XMLGregorianCalendar logicalto = axis.getTo();
    final Point fromTextExtent = getTextExtent( gc, dev, logicalfrom, m_fontDataTick );
    final Point toTextExtent = getTextExtent( gc, dev, logicalto, m_fontDataTick );
    final Point tickLabelSize = new Point( Math.max( fromTextExtent.x, toTextExtent.x ), Math.max( fromTextExtent.y, toTextExtent.y ) );

    final Insets ihelper = getConvertedInsets( axis, m_tickLabelInsets );

    final int x = tickLabelSize.x + ihelper.left + ihelper.right;
    final int y = 2*tickLabelSize.y + 2*ihelper.top + 2*ihelper.bottom;

    // Logger.trace("XMLGregCalAxRend: TickLabelSize: "+x+":"+y);
    return new Point( x, y );
  }

  protected void drawTicks( GCWrapper gc, Device dev, IAxis<XMLGregorianCalendar> axis, int startX, int startY, int endX, int endY, Collection<XMLGregorianCalendar> ticks )
  {

    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.setForeground( m_foreground );

    int textXDate = 0;
    int textYDate = 0;
    int textXTime = 0;
    int textYTime = 0;



    SimpleDateFormat dfDate=new SimpleDateFormat( "dd.MM.yy" );
    SimpleDateFormat dfTime=new SimpleDateFormat( "HH:mm:ss" );

    Calendar cal=Calendar.getInstance();
    cal.setTimeZone( TimeZone.getTimeZone( "GMT+00:00" ) );

    dfDate.setCalendar( cal );
    dfTime.setCalendar( cal );

    if( axis.getPosition() == POSITION.BOTTOM )
    {
      int y1 = startY;
      int y2 = y1 + m_tickLength;

      for( final XMLGregorianCalendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String labelDate = dfDate.format( value.toGregorianCalendar().getTimeInMillis() );
        final String labelTime = dfTime.format( value.toGregorianCalendar().getTimeInMillis() );
        final Point tickExtentDate = getTextExtent( gc, dev, labelDate, m_fontDataTick );
        final Point tickExtentTime = getTextExtent( gc, dev, labelTime, m_fontDataTick );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textXDate = tickPos - tickExtentDate.x / 2;
        textYDate = y2 + m_tickLabelInsets.top;
        textXTime = tickPos - tickExtentTime.x / 2;
        textYTime = y2 + tickExtentDate.y + m_tickLabelInsets.top;
        drawText( gc, dev, labelDate, textXDate, textYDate, m_fontDataTick );
        drawText( gc, dev, labelTime, textXTime, textYTime, m_fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.TOP )
    {
      int y1 = startY;
      int y2 = y1 - m_tickLength;

      for( final XMLGregorianCalendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_df.format( new Date( value.toGregorianCalendar().getTimeInMillis() ) );
        final Point tickExtent = getTextExtent( gc, dev, label, m_fontDataTick );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textXDate = tickPos - tickExtent.x / 2;
        textYDate = y2 - m_tickLabelInsets.top - tickExtent.y;
        drawText( gc, dev, label, textXDate, textYDate, m_fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.LEFT )
    {
      int x1 = startX;
      int x2 = x1 - m_tickLength;

      for( final XMLGregorianCalendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_df.format( new Date( value.toGregorianCalendar().getTimeInMillis() ) );
        final Point tickExtent = getTextExtent( gc, dev, label, m_fontDataTick );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textXDate = tickPos - x2 - tickExtent.x - m_tickLabelInsets.top;
        textYDate = tickPos - tickExtent.y / 2;
        drawText( gc, dev, label, textXDate, textYDate, m_fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.RIGHT )
    {
      int x1 = startX;
      int x2 = x1 + m_tickLength;

      for( final XMLGregorianCalendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_df.format( new Date( value.toGregorianCalendar().getTimeInMillis() ) );
        final Point tickExtent = getTextExtent( gc, dev, label, m_fontDataTick );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textXDate = x2 + m_tickLabelInsets.top;
        textYDate = tickPos - tickExtent.y / 2;
        drawText( gc, dev, label, textXDate, textYDate, m_fontDataTick );
      }
    }

  }

  private Insets getConvertedInsets( final IAxis<XMLGregorianCalendar> axis, final Insets insets )
  {
    // POSITION BOTTOM is the default order for the insets
    InsetsHelper ihelper = new InsetsHelper( insets );

    if( axis.getPosition() == POSITION.TOP )
      ihelper = ihelper.mirrorTopBottom();
    if( axis.getPosition() == POSITION.LEFT )
      ihelper = ihelper.hor2vert();
    if( axis.getPosition() == POSITION.RIGHT )
      ihelper = ihelper.hor2vert().mirrorLeftRight();

    return ihelper;
  }

  /**
   * berechnet die Ticks
   */
  /*
  public Collection<XMLGregorianCalendar> calcTicks( GCWrapper gc, final Device dev, final IAxis<XMLGregorianCalendar> axis )
  {
    final Point ticklabelSize = calcTickLabelSize( gc, dev, axis );

    // + 2 wegen Rundungsfehlern beim positionieren
    final int screenInterval = 2 + ticklabelSize.x;

    // optimales logisches Intervall - soll sicherstellen, dass pro Tag höchstens ein Tick gezeichnet wird
    XMLGregorianCalendar from = axis.getFrom();
    XMLGregorianCalendar fromPlusOne = ((XMLGregorianCalendar) from.clone());
    fromPlusOne.setDay( from.getDay() + 1 );
    int optLogicalInterval = Math.abs( axis.logicalToScreen( from ) - axis.logicalToScreen( fromPlusOne ) );
    int interval = screenInterval;
    if( optLogicalInterval > screenInterval )
      interval = optLogicalInterval;

    long logicalInterval = Math.abs( axis.screenToLogical( interval * 2 ).toGregorianCalendar().getTimeInMillis() - axis.screenToLogical( interval ).toGregorianCalendar().getTimeInMillis() );
    logicalInterval = (long) (MathUtils.round( logicalInterval, MathUtils.RoundMethod.UP ));
    // Logger.trace( "Logical Interval= " + logicalInterval );

    final int digits = Math.min( MathUtils.scale( logicalInterval ), m_maxDigits );

    final long ticklength = (long) Math.abs( MathUtils.setScale( logicalInterval, digits, MathUtils.RoundMethod.UP ) );

    final Collection<XMLGregorianCalendar> ticks = new LinkedList<XMLGregorianCalendar>();

    ticks.add( new XMLGregorianCalendarImpl( axis.getFrom().toGregorianCalendar() ) );
    if( ticklength > 0.0 )
    {
      for( long tickValue = (long) MathUtils.setScale( axis.getFrom().toGregorianCalendar().getTimeInMillis()+logicalInterval, digits, MathUtils.RoundMethod.UP ); tickValue <= axis.getTo().toGregorianCalendar().getTimeInMillis()-logicalInterval; tickValue += ticklength )
      {
        GregorianCalendar gregCal = new GregorianCalendar();
        Date d = new Date( tickValue );
        gregCal.setTime( d );
        // Recalc.gr
        ticks.add( new XMLGregorianCalendarImpl( gregCal ) );
        // Logger.trace("XMLGregorianCalendarAxisTick: "+m_df.format(tickValue));
      }
    }
    ticks.add( new XMLGregorianCalendarImpl( axis.getTo().toGregorianCalendar() ) );
    return ticks;
  }
*/


  /**
   * Calculates the ticks shown for the given Axis
   */
  public Collection<XMLGregorianCalendar> calcTicks( GCWrapper gc, Device dev, final IAxis<XMLGregorianCalendar> axis )
  {

    // größtes Tick<-Label berechnen
    final Point ticklabelSize = calcTickLabelSize( gc, dev, axis );

    // TickLabelGröße + 2 wegen Rundungsfehlern beim positionieren
    final int minScreenInterval;
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      minScreenInterval = 2 + ticklabelSize.x;
    else
      minScreenInterval = 2 + ticklabelSize.y;

    //Mini- und maximalen ANZEIGBAREN Wert ermitteln anhand der Größe der Labels
    int screenMin, screenMax;
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      if (axis.getDirection()==DIRECTION.POSITIVE)
      {
        screenMin=(int)(axis.logicalToScreen( axis.getFrom()) + Math.ceil(0.5*ticklabelSize.x));
        screenMax=(int)(axis.logicalToScreen(axis.getTo())-Math.ceil( 0.5*ticklabelSize.x));
      }
      else
      {

        screenMin=(int)(axis.logicalToScreen( axis.getFrom()) - Math.ceil(0.5*ticklabelSize.x));
        screenMax=(int)(axis.logicalToScreen(axis.getTo()) + Math.ceil( 0.5*ticklabelSize.x));
      }
    }
    else
    {
      if (axis.getDirection()==DIRECTION.POSITIVE)
      {
        screenMin=(int)(axis.logicalToScreen( axis.getFrom()) - Math.ceil(0.5*ticklabelSize.y));
        screenMax=(int)(axis.logicalToScreen(axis.getTo())+ Math.ceil(0.5*ticklabelSize.y));
      }
      else
      {
        screenMin=(int)(axis.logicalToScreen( axis.getFrom()) + Math.ceil(0.5*ticklabelSize.y));
        screenMax=(int)(axis.logicalToScreen(axis.getTo()) - Math.ceil(0.5*ticklabelSize.y));
      }

    }

    //Ab jetzt wird nur noch mit long gerechnet


    //Debug-Ausgabe
    Logger.trace("From: " + longToDateString( axis.getFrom().toGregorianCalendar().getTimeInMillis() ));
    Logger.trace("To: " + longToDateString( axis.getTo().toGregorianCalendar().getTimeInMillis() ));


    //logischen mini- und maximalen Wert ermitteln
    long logicalMin=axis.screenToLogical( screenMin ).toGregorianCalendar().getTimeInMillis();
    long logicalMax=axis.screenToLogical( screenMax ).toGregorianCalendar().getTimeInMillis();;
    Logger.trace("Logical Min: " + longToDateString( logicalMin ));
    Logger.trace("Logical Max: " + longToDateString( logicalMax ));

    double logicalRange=Math.abs( logicalMax-logicalMin) ;

    //der minimale logische Abstand
    long minLogInterval=Math.abs(axis.screenToLogical( minScreenInterval ).toGregorianCalendar().getTimeInMillis() -axis.screenToLogical( 0 ).toGregorianCalendar().getTimeInMillis() );
    //ein paar Größen
    long secondInMillis=1000;
    long minuteInMillis=secondInMillis*60;
    long hourInMillis=minuteInMillis*60;
    long dayInMillis=hourInMillis*24;



    //letzten Tagesbeginn VOR dem Startdatum
    long normmin= ( ( logicalMin/dayInMillis) -1) * dayInMillis;
    //erster Tagesbeginn NACH dem Startdatum
    long normmax= ( ( logicalMax/dayInMillis) +1) * dayInMillis;
    Logger.trace("Normmin: " + longToDateString( normmin )+ " sind millis "+normmin);
    Logger.trace("normmin / day: "+(normmin/dayInMillis)+" normmin % day: "+(normmin%dayInMillis  ));

    Logger.trace("Normmax: " + longToDateString( normmax )+ " sind millis "+normmax);
    Logger.trace("normmax / day: "+(normmax/dayInMillis)+" normmax % day: "+(normmax%dayInMillis  ));

    //Collection für Ticks
    HashSet<Long> ticks=new HashSet<Long>();


   // findBetweens( normmin, normmax, minLogInterval, ticks );

    int count=0;
    long oldi=0;

    long goodInterval=dayInMillis;
    while(goodInterval < minLogInterval)
    {
      goodInterval+=dayInMillis;
    }


    for (long i=normmin;i<=normmax;i+=goodInterval)
    {
      if (count>0)
      {
        findBetweens(oldi, i, minLogInterval, ticks);
      }
      ticks.add( i );

      count++;
      oldi=i;
    }




    LinkedList<XMLGregorianCalendar> realticks=new LinkedList<XMLGregorianCalendar>();
    for (Long tick:ticks)
    {
      long ticklv=tick.longValue();
      if (ticklv>=logicalMin && ticklv<=logicalMax)
      {
          Logger.trace("Date tick: "+ticklv);
          GregorianCalendar cal=(GregorianCalendar) GregorianCalendar.getInstance();
          cal.setTimeInMillis( ticklv );
          realticks.add(new XMLGregorianCalendarImpl(cal));
      }
    }







    return realticks;
  }

  private String longToDateString(long millis)
  {
      Calendar cal=Calendar.getInstance();
      cal.setTimeZone( TimeZone.getTimeZone( "GMT+00:00" ) );
      cal.setTimeInMillis( millis );
      SimpleDateFormat df=new SimpleDateFormat("yy-MM-dd HH:mm");
      df.setCalendar( cal );
      String date=df.format( millis );

      return date;
  }

    /**
     * recursive function which divides in interval into a number of "divisions" and adds the values to a linked list
     * the divisions have to have a larger range than the param interval
     */
    private void findBetweens(long from, long to, long minInterval, HashSet<Long> ticks)
    {

      Logger.trace("find Betweens: from: "+from+" to: "+to+" interval: "+minInterval);
      /*
      //ein paar grössen
      final long secondInMillis=1000;
      final long minuteInMillis=secondInMillis*60;
      final long hourInMillis=minuteInMillis*60;
      final long dayInMillis=hourInMillis*24;

      final long range=Math.abs(to-from);



      long myInterval=0;


      //Dauer größer als ein Tag =>
      if (range>dayInMillis)
      {
          Logger.trace("range > day ");
          myInterval=dayInMillis;
      }
      else if (range>hourInMillis)
      {
        Logger.trace("range > hour ");
          myInterval=hourInMillis;
      }
      else if (range>minuteInMillis)
      {
          Logger.trace("range > minute ");
          myInterval=minuteInMillis;
      }
      else if (range>secondInMillis)
      {
        Logger.trace("range > seconds ");
        myInterval=secondInMillis;
      }
      else
      {
        Logger.trace("Keine gültige Range in DateAxis: "+range);
        return;
      }


      if (myInterval>0)
      {
        long optInterval=myInterval;
        while (optInterval<interval)
        {
            optInterval+=myInterval;
        }

        long oldi=0;
        int count=0;
        for (long i=from;i<=to;i+=optInterval)
        {
          if (count>1)
          {
            long newrange=Math.abs( i - oldi);
            if (newrange >= interval)
            {
              findBetweens( oldi, i, interval, ticks );
            }
          }
          ticks.add( i );
          Logger.trace("added Tick: "+longToDateString( i ));
          count++;
          oldi=i;
        }
      }
      */


      LinkedList<Integer> divisors=new LinkedList<Integer>();
      divisors.add(6);
      divisors.add(4);
      divisors.add(3);
      divisors.add(2);

      for (Integer divisor:divisors)
      {
        long betweenrange=Math.abs(from-to)/divisor.intValue();

        if (minInterval < betweenrange )
        {
            //vorher prüfen
            int count=0;
            long oldi=0;
            for (long i =from;i<=to;i+=betweenrange)
            {
              ticks.add( new Long(i) );
              if (count>=1)
              {
                findBetweens(oldi, i, minInterval, ticks);
              }
              //Tick setzen
              count++;
              oldi=i;
            }
            break;
        }
      }

    }















  private Point getTextExtent( GCWrapper gc, final Device dev, final XMLGregorianCalendar value, FontData fd )
  {
    final String label = m_df.format( new Date( value.toGregorianCalendar().getTimeInMillis() ) );
    Point p = getTextExtent( gc, dev, label, fd );
    return p;
  }

  /**
   * Gibt die Breite bzw. Tiefe einer Achse zurück. Dadurch wird die Grösse der AxisComponent bestimmt
   */
  public int getAxisWidth( IAxis<XMLGregorianCalendar> axis )
  {
    int width = 0;
    int label = 0;

    // Testutensilien erzeugen
    Display d = Display.getCurrent();
    Image img = new Image( d, 1, 1 );
    GC gc = new GC( img );
    GCWrapper gcw = new GCWrapper( gc );

    Point tls = calcTickLabelSize( gcw, d, axis );

    // Ticks+Labels
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      width += m_tickLabelInsets.top + m_tickLabelInsets.bottom;
      width += m_labelInsets.top + m_labelInsets.bottom;
      label = tls.y;
      // Toleranzwert
      width += 5;
    }
    else
    {
      width += m_tickLabelInsets.left + m_tickLabelInsets.right;
      width += m_labelInsets.left + m_labelInsets.right;
      label = tls.x;
    }
    width += label;
    width += m_gap;
    width += m_lineWidth;
    width += m_tickLength;

    // Jetzt noch den Text
    Point labelExtent = getTextExtent( gcw, d, axis.getLabel(), m_fontDataLabel );
    width += labelExtent.y;

    gcw.dispose();
    gc.dispose();
    img.dispose();

    return width;
  }

  protected void drawText( GCWrapper gc, Device dev, String text, int x, int y, FontData fd )
  {
    Font f = new Font( dev, fd );
    gc.setFont( f );
    gc.drawText( text, x, y );
    gc.setFont( dev.getSystemFont() );
    f.dispose();
  }

  protected int[] createAxisSegment( final IAxis<XMLGregorianCalendar> axis, final Rectangle screen )
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
        startX = screen.x + screen.width - m_gap;
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

  public void paint( GCWrapper gc, final Device dev, final IAxis<XMLGregorianCalendar> axis, final Rectangle screen )
  {
    Image img = paintBuffered( null, dev, axis, screen );
    gc.drawImage( img, 0, 0 );
    img.dispose();
  }

  public Image paintBuffered( GCWrapper gc, final Device dev, final IAxis<XMLGregorianCalendar> axis, final Rectangle screen )
  {
    Image bufImg = new Image( dev, dev.getBounds() );
    GC bufGc = new GC( bufImg );
    GCWrapper bufGcw = new GCWrapper( bufGc );
    // paint axis line
    int[] coords = createAxisSegment( axis, screen );
    assert coords != null && coords.length == 4;
    drawAxisLine( bufGcw, coords[0], coords[1], coords[2], coords[3] );

    m_ticks = calcTicks( bufGcw, dev, axis );
    drawTicks( bufGcw, dev, axis, coords[0], coords[1], coords[2], coords[3], m_ticks );

    drawAxisLabel( bufGcw, dev, axis, coords[0], coords[1], coords[2], coords[3] );

    bufGc.dispose();
    bufGcw.dispose();

    return bufImg;
  }

  protected void drawAxisLabel( GCWrapper gc, Device dev, IAxis<XMLGregorianCalendar> axis, int x1, int y1, int x2, int y2 )
  {
    ChartUtilities.resetGC( gc.m_gc, dev );

    gc.setForeground( m_foreground );
    // gc.setBackground( m_background );

    if( axis.getLabel() != null )
    {
      final Point textExtent = getTextExtent( gc, dev, axis.getLabel(), m_fontDataLabel );

      final Point tickLabelExtent = calcTickLabelSize( gc, dev, axis );

      int x = 0;
      int y = 0;

      Transform tr = new Transform( dev );
      try
      {
        if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          x = Math.abs( x2 - x1 ) / 2 - textExtent.x / 2;
          y = y1;

          if( axis.getPosition() == POSITION.TOP )
            y -= m_tickLength + m_lineWidth + m_tickLabelInsets.top + tickLabelExtent.y + m_tickLabelInsets.bottom + m_labelInsets.top;
          else
            y += m_tickLength + m_lineWidth + m_tickLabelInsets.top + tickLabelExtent.y + m_tickLabelInsets.bottom + m_labelInsets.top;
        }
        else
        {
          x = x1;
          y = Math.abs( y2 - y1 ) / 2;

          int rotation = 0;

          if( axis.getPosition() == POSITION.LEFT )
          {
            rotation = -90;
            x -= m_lineWidth + m_tickLength + m_tickLabelInsets.right + tickLabelExtent.x + m_tickLabelInsets.left + m_labelInsets.bottom + textExtent.y;
            y += textExtent.x / 2;
          }
          else
          {
            rotation = 90;
            x += m_lineWidth + m_tickLength + m_tickLabelInsets.right + tickLabelExtent.x + m_tickLabelInsets.left + m_labelInsets.bottom + textExtent.y;
            y -= textExtent.x / 2;
          }
          tr.translate( x, y );
          tr.rotate( rotation );
          tr.translate( -x, -y );

        }
        if( tr != null )
        {
          gc.setTransform( tr );
          // Logger.trace( "Using Transform: " + tr );
        }
        drawText( gc, dev, axis.getLabel(), x, y, m_fontDataLabel );

        // Logger.trace( "Axis Label: X=" + x + " Y=" + y + " for " + axis );
      }
      finally
      {
        if( tr != null )
          tr.dispose();
      }
    }
  }

  protected void drawAxisLine( GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  {
    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.setForeground( m_foreground );

    gc.drawLine( x1, y1, x2, y2 );
  }

  protected Point getTextExtent( GCWrapper gc, final Device dev, final String value, FontData fd )
  {
    Font f = new Font( dev, fd );
    gc.setFont( f );
    Point point = gc.textExtent( value );
    gc.setFont( dev.getSystemFont() );
    f.dispose();
    return point;
  }

}
