package org.kalypso.swtchart.chart.axis.renderer;

import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
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
import org.kalypso.swtchart.chart.util.InsetsHelper;

/**
 * @author burtscher
 */
public class CalendarAxisRenderer extends AbstractAxisRenderer<Calendar>
{

  private Collection<Calendar> m_ticks;

  private final SimpleDateFormat m_dateFormat;

  /**
   * @param foreground
   *            Color of axis, ticks and text
   * @param lineWidth
   *            Width of ticks and axis line
   * @param tickLength
   * @param tickLabelInsets
   *            <ul>
   *            <li>top: distance between ticklabel and axis</li>
   *            <li>bottom: distance between ticklabel and outside</li>
   *            <li>left: distance between ticklabel and previous ticklabel</li>
   *            <li>right: distance between ticklabel and next ticklabel</li>
   *            </ul>
   * @param maxDigits
   * @param labelInsets
   * @param gap
   *            space between axis and component border
   */
  public CalendarAxisRenderer( final RGB foregroundRGB, final RGB backgroundRGB, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick, final SimpleDateFormat df )
  {
    super( foregroundRGB, backgroundRGB, lineWidth, tickLength, tickLabelInsets, labelInsets, gap, fdLabel, fdTick );
    m_dateFormat = df;
  }

  public Point calcTickLabelSize( final GCWrapper gc, final IAxis<Calendar> axis )
  {
    final Calendar from = axis.getFrom();
    final Calendar to = axis.getTo();
    if( from == null || to == null )
      return null;

    final String logicalfrom = m_dateFormat.format( from.getTimeInMillis() );
    final String logicalto = m_dateFormat.format( to.getTimeInMillis() );
    final Point fromTextExtent = getTextExtent( gc, logicalfrom, m_fontDataTick );
    final Point toTextExtent = getTextExtent( gc, logicalto, m_fontDataTick );
    final Point tickLabelSize = new Point( Math.max( fromTextExtent.x, toTextExtent.x ), Math.max( fromTextExtent.y, toTextExtent.y ) );

    final Insets ihelper = getConvertedInsets( axis, m_tickLabelInsets );

    final int x = tickLabelSize.x + ihelper.left + ihelper.right;
    final int y = tickLabelSize.y + ihelper.top + ihelper.bottom;

    return new Point( x, y );
  }

  protected void drawTicks( final GCWrapper gc, final IAxis<Calendar> axis, final int startX, final int startY, final Collection<Calendar> ticks )
  {
    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );

    int textXDate = 0;
    int textYDate = 0;

    final Calendar cal = Calendar.getInstance();
    cal.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );

    m_dateFormat.setCalendar( cal );

    if( axis.getPosition() == POSITION.BOTTOM )
    {
      final int y1 = startY;
      final int y2 = y1 + m_tickLength;

      for( final Calendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String labelDate = m_dateFormat.format( value.getTimeInMillis() );
        final Point tickExtentDate = getTextExtent( gc, labelDate, m_fontDataTick );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textXDate = tickPos - tickExtentDate.x / 2;
        textYDate = y2 + m_tickLabelInsets.top;
        drawText( gc, labelDate, textXDate, textYDate, m_fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.TOP )
    {
      final int y1 = startY;
      final int y2 = y1 - m_tickLength;

      for( final Calendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_dateFormat.format( new Date( value.getTimeInMillis() ) );
        final Point tickExtent = getTextExtent( gc, label, m_fontDataTick );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textXDate = tickPos - tickExtent.x / 2;
        textYDate = y2 - m_tickLabelInsets.top - tickExtent.y;
        drawText( gc, label, textXDate, textYDate, m_fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.LEFT )
    {
      final int x1 = startX;
      final int x2 = x1 - m_tickLength;

      for( final Calendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_dateFormat.format( new Date( value.getTimeInMillis() ) );
        final Point tickExtent = getTextExtent( gc, label, m_fontDataTick );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textXDate = x1 - tickExtent.x - m_tickLabelInsets.right - m_tickLength;
        textYDate = tickPos - tickExtent.y / 2;
        drawText( gc, label, textXDate, textYDate, m_fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.RIGHT )
    {
      final int x1 = startX;
      final int x2 = x1 + m_tickLength;

      for( final Calendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_dateFormat.format( new Date( value.getTimeInMillis() ) );
        final Point tickExtent = getTextExtent( gc, label, m_fontDataTick );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textXDate = tickPos - x2 + m_tickLabelInsets.top;
        textXDate = x1 + m_tickLabelInsets.left + m_tickLength;
        textYDate = tickPos - tickExtent.y / 2;
        drawText( gc, label, textXDate, textYDate, m_fontDataTick );
      }
    }

  }

  private Insets getConvertedInsets( final IAxis<Calendar> axis, final Insets insets )
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
   * Calculates the ticks shown for the given Axis
   */
  public Collection<Calendar> calcTicks( final GCWrapper gc, final IAxis<Calendar> axis )
  {
    // größtes Tick<-Label berechnen
    final Point ticklabelSize = calcTickLabelSize( gc, axis );
    if( ticklabelSize == null )
      return Collections.EMPTY_LIST;

    // TickLabelGröße + 2 wegen Rundungsfehlern beim positionieren
    final int minScreenInterval;
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      minScreenInterval = 2 + ticklabelSize.x;
    else
      minScreenInterval = 2 + ticklabelSize.y;

    // Mini- und maximalen ANZEIGBAREN Wert ermitteln anhand der Größe der Labels
    int screenMin, screenMax;
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        screenMin = (int) (axis.logicalToScreen( axis.getFrom() ) + Math.ceil( 0.5 * ticklabelSize.x ));
        screenMax = (int) (axis.logicalToScreen( axis.getTo() ) - Math.ceil( 0.5 * ticklabelSize.x ));
      }
      else
      {

        screenMin = (int) (axis.logicalToScreen( axis.getFrom() ) - Math.ceil( 0.5 * ticklabelSize.x ));
        screenMax = (int) (axis.logicalToScreen( axis.getTo() ) + Math.ceil( 0.5 * ticklabelSize.x ));
      }
    }
    else
    {
      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        screenMin = (int) (axis.logicalToScreen( axis.getFrom() ) - Math.ceil( 0.5 * ticklabelSize.y ));
        screenMax = (int) (axis.logicalToScreen( axis.getTo() ) + Math.ceil( 0.5 * ticklabelSize.y ));
      }
      else
      {
        screenMin = (int) (axis.logicalToScreen( axis.getFrom() ) + Math.ceil( 0.5 * ticklabelSize.y ));
        screenMax = (int) (axis.logicalToScreen( axis.getTo() ) - Math.ceil( 0.5 * ticklabelSize.y ));
      }

    }

    // Ab jetzt wird nur noch mit long gerechnet

    // logischen mini- und maximalen Wert ermitteln
    final long logicalMin = axis.screenToLogical( screenMin ).getTimeInMillis();
    final long logicalMax = axis.screenToLogical( screenMax ).getTimeInMillis();

    // der minimale logische Abstand
    final long minLogInterval = Math.abs( axis.screenToLogical( minScreenInterval ).getTimeInMillis() - axis.screenToLogical( 0 ).getTimeInMillis() );
    // ein paar Größen
    final long secondInMillis = 1000;
    final long minuteInMillis = secondInMillis * 60;
    final long hourInMillis = minuteInMillis * 60;
    final long dayInMillis = hourInMillis * 24;

    // letzten Tagesbeginn VOR dem Startdatum
    final long normmin = ((logicalMin / dayInMillis) - 1) * dayInMillis;
    // erster Tagesbeginn NACH dem Startdatum
    final long normmax = ((logicalMax / dayInMillis) + 1) * dayInMillis;

    // Collection für Ticks
    final HashSet<Long> ticks = new HashSet<Long>();

    int count = 0;
    long oldi = 0;

    long goodInterval = dayInMillis;
    while( goodInterval < minLogInterval )
    {
      goodInterval += dayInMillis;
    }

    for( long i = normmin; i <= normmax; i += goodInterval )
    {
      if( count > 0 )
      {
        findBetweens( oldi, i, minLogInterval, ticks );
      }
      ticks.add( i );

      count++;
      oldi = i;
    }

    final LinkedList<Calendar> realticks = new LinkedList<Calendar>();
    for( final Long tick : ticks )
    {
      final long ticklv = tick.longValue();
      if( ticklv >= logicalMin && ticklv <= logicalMax )
      {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis( ticklv );
        realticks.add( cal );
      }
    }
    m_tickMap.put( axis, realticks );

    return realticks;
  }

  /**
   * recursive function which divides an interval into a number of "divisions" and adds the values to a linked list the
   * divisions have to have a larger range than the param interval
   */
  private void findBetweens( final long from, final long to, final long minInterval, final HashSet<Long> ticks )
  {

    /*
     * TODO: hier muss noch irgendwie überprüft werden, in welchem Bereich sich die DateRange befindet - anhand der
     * DateRange muss dann bestimmt werden, welche divisoren sinnvoll sind; Beispiel: Wenn Stunden geteilt werden, dann
     * sind die Divisoren 6,4,3,2 gut; bei Tagen eher 120, 90, 60, 30, etc.
     */
    final LinkedList<Integer> divisors = new LinkedList<Integer>();

    divisors.add( 60 );
    divisors.add( 24 );
    divisors.add( 18 );
    divisors.add( 12 );
    divisors.add( 6 );
    divisors.add( 2 );

    // Abbruchbedingung: Abstand muss größer 1 sein, sonst kommt immer der 2. Wert raus
    if( to - from > 1 )
    {
      final Calendar tmpcal = Calendar.getInstance();
      m_dateFormat.setCalendar( tmpcal );

      for( final Integer divisor : divisors )
      {
        final long betweenrange = Math.abs( from - to ) / divisor.intValue();
        if( minInterval < betweenrange )
        {
          // vorher prüfen
          int count = 0;
          long oldi = 0;
          for( long i = from; i <= to; i += betweenrange )
          {
            ticks.add( new Long( i ) );
            if( count >= 1 )
            {
              findBetweens( oldi, i, minInterval, ticks );
            }
            // Tick setzen
            count++;
            oldi = i;
          }
          break;
        }
      }
    }

  }

  @Override
  protected Point getTextExtent( final GCWrapper gc, final Calendar value, final FontData fd )
  {
    final String label = m_dateFormat.format( new Date( value.getTimeInMillis() ) );
    final Point p = getTextExtent( gc, label, fd );
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
        final int tmp = startX;
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
        startX = screen.x + screen.width - m_gap - 1;
      endX = startX;

      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        final int tmp = startY;
        startY = endY;
        endY = tmp;
      }
    }

    return new int[] { startX, startY, endX, endY };
  }

  public void paint( final GCWrapper gc, final IAxis<Calendar> axis, final Rectangle screen )
  {
    final Image img = paintBuffered( gc, axis, screen );
    gc.drawImage( img, 0, 0 );
    img.dispose();
  }

  public Image paintBuffered( final GCWrapper gc, final IAxis<Calendar> axis, final Rectangle screen )
  {
    final Device dev = gc.getDevice();
    final Image bufImg = new Image( dev, dev.getBounds() );
    final GC bufGc = new GC( bufImg );
    final GCWrapper bufGcw = new GCWrapper( bufGc );

    final Color foreground = new Color( dev, m_rgbForeground );
    final Color background = new Color( dev, m_rgbBackground );
    bufGcw.setForeground( foreground );
    bufGcw.setBackground( background );

    // draw axis line
    final int[] coords = createAxisSegment( axis, screen );
    assert coords != null && coords.length == 4;
    drawAxisLine( bufGcw, coords[0], coords[1], coords[2], coords[3] );

    m_ticks = calcTicks( bufGcw, axis );
    drawTicks( bufGcw, axis, coords[0], coords[1], m_ticks );
    drawAxisLabel( bufGcw, axis, coords[0], coords[1], coords[2], coords[3] );

    bufGc.dispose();
    bufGcw.dispose();
    foreground.dispose();
    background.dispose();

    return bufImg;
  }

  protected void drawAxisLabel( final GCWrapper gc, final IAxis<Calendar> axis, final int x1, final int y1, final int x2, final int y2 )
  {
    if( axis.getLabel() != null )
    {
      final Point textExtent = getTextExtent( gc, axis.getLabel(), m_fontDataLabel );

      final Point tickLabelExtent = calcTickLabelSize( gc, axis );
      if( tickLabelExtent == null )
        return;

      int x = 0;
      int y = 0;

      final Transform tr = new Transform( gc.getDevice() );
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
        }
        drawText( gc, axis.getLabel(), x, y, m_fontDataLabel );
      }
      finally
      {
        if( tr != null )
          tr.dispose();
      }
    }
  }

  protected void drawAxisLine( final GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  {
    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.drawLine( x1, y1, x2, y2 );
  }

  /**
   * @see org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer#getTicks(org.kalypso.swtchart.chart.axis.IAxis)
   */
  public Collection<Calendar> getGridTicks( final IAxis<Calendar> axis )
  {
    Collection<Calendar> ticks = m_tickMap.get( axis );
    if( ticks == null )
    {
      final GC gc = new GC( Display.getDefault() );
      final GCWrapper gcw = new GCWrapper( gc );
      calcTicks( gcw, axis );
      ticks = m_tickMap.get( axis );
      gcw.dispose();
      gc.dispose();
    }
    return ticks;
  }

  /**
   * Gibt die Breite bzw. Tiefe einer Achse zurück. Dadurch wird die Grösse der AxisComponent bestimmt
   */
  public int getAxisWidth( final IAxis<Calendar> axis )
  {
    int width = 0;
    int label = 0;

    // Testutensilien erzeugen
    final Display dev = Display.getCurrent();
    final Image img = new Image( dev, 1, 1 );
    final GC gc = new GC( img );
    final GCWrapper gcw = new GCWrapper( gc );

    final Point tls = calcTickLabelSize( gcw, axis );
    if( tls == null )
      return 0;

    // Ticks+Labels
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      width += m_tickLabelInsets.top + m_tickLabelInsets.bottom;
      width += m_labelInsets.top + m_labelInsets.bottom;
      label = tls.y;
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
    final Point labelExtent = getTextExtent( gcw, axis.getLabel(), m_fontDataLabel );
    width += labelExtent.y;

    gcw.dispose();
    gc.dispose();
    img.dispose();

    return width;
  }

}
