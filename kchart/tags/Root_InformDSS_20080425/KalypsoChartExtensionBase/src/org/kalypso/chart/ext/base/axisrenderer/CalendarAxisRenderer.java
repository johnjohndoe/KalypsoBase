package org.kalypso.chart.ext.base.axisrenderer;

import java.awt.Insets;
import java.text.Format;
import java.util.Calendar;
import java.util.Collection;
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
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.util.InsetsHelper;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author burtscher
 */
public class CalendarAxisRenderer extends AbstractAxisRenderer<Calendar>
{

  private Collection<Calendar> m_ticks;

  // private final SimpleDateFormat m_dateFormat;

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
  public CalendarAxisRenderer( final String id, final RGB foregroundRGB, final RGB backgroundRGB, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick )
  {
    super( id, foregroundRGB, backgroundRGB, lineWidth, tickLength, tickLabelInsets, labelInsets, gap, fdLabel, fdTick );
  }

  public Point calcTickLabelSize( GCWrapper gc, final IAxis<Calendar> axis )
  {
    final IDataRange<Calendar> range = axis.getLogicalRange();
    Format format = axis.getDataOperator().getFormat( axis.getLogicalRange() );
    final String logicalfrom = format.format( range.getMin().getTimeInMillis() );
    final String logicalto = format.format( range.getMax().getTimeInMillis() );
    final Point fromTextExtent = getTextExtent( gc, logicalfrom, getFontDataTickLabel() );
    final Point toTextExtent = getTextExtent( gc, logicalto, getFontDataTickLabel() );
    final Point tickLabelSize = new Point( Math.max( fromTextExtent.x, toTextExtent.x ), Math.max( fromTextExtent.y, toTextExtent.y ) );

    final Insets ihelper = getConvertedInsets( axis, getTickLabelInsets() );

    final int x = tickLabelSize.x + ihelper.left + ihelper.right;
    final int y = tickLabelSize.y + ihelper.top + ihelper.bottom;

    return new Point( x, y );
  }

  protected void drawTicks( GCWrapper gc, IAxis<Calendar> axis, int startX, int startY, Collection<Calendar> ticks )
  {
    gc.setLineWidth( getLineWidth() );
    gc.setLineStyle( SWT.LINE_SOLID );

    int textXDate = 0;
    int textYDate = 0;

    final Calendar cal = Calendar.getInstance();
    cal.setTimeZone( TimeZone.getTimeZone( "GMT+0000" ) );

    Format format = axis.getDataOperator().getFormat( axis.getLogicalRange() );

    if( axis.getPosition() == POSITION.BOTTOM )
    {
      final int y1 = startY;
      final int y2 = y1 + getTickLength();

      for( final Calendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String labelDate = format.format( new Date( value.getTimeInMillis() ) );
        final Point tickExtentDate = getTextExtent( gc, labelDate, getFontDataTickLabel() );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textXDate = tickPos - tickExtentDate.x / 2;
        textYDate = y2 + getTickLabelInsets().top;
        drawText( gc, labelDate, textXDate, textYDate, getFontDataTickLabel() );
      }
    }
    else if( axis.getPosition() == POSITION.TOP )
    {
      final int y1 = startY;
      final int y2 = y1 - getTickLength();

      for( final Calendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = format.format( new Date( value.getTimeInMillis() ) );
        final Point tickExtent = getTextExtent( gc, label, getFontDataTickLabel() );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textXDate = tickPos - tickExtent.x / 2;
        textYDate = y2 - getTickLabelInsets().top - tickExtent.y;
        drawText( gc, label, textXDate, textYDate, getFontDataTickLabel() );
      }
    }
    else if( axis.getPosition() == POSITION.LEFT )
    {
      final int x1 = startX;
      final int x2 = x1 - getTickLength();

      for( final Calendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = format.format( new Date( value.getTimeInMillis() ) );
        final Point tickExtent = getTextExtent( gc, label, getFontDataTickLabel() );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textXDate = x1 - tickExtent.x - getTickLabelInsets().right - getTickLength();
        textYDate = tickPos - tickExtent.y / 2;
        drawText( gc, label, textXDate, textYDate, getFontDataTickLabel() );
      }
    }
    else if( axis.getPosition() == POSITION.RIGHT )
    {
      final int x1 = startX;
      final int x2 = x1 + getTickLength();

      for( final Calendar value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = format.format( new Date( value.getTimeInMillis() ) );
        final Point tickExtent = getTextExtent( gc, label, getFontDataTickLabel() );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textXDate = tickPos - x2 + getTickLabelInsets().top;
        textXDate = x1 + getTickLabelInsets().left + getTickLength();
        textYDate = tickPos - tickExtent.y / 2;
        drawText( gc, label, textXDate, textYDate, getFontDataTickLabel() );
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
  public Collection<Calendar> calcTicks( GCWrapper gc, final IAxis<Calendar> axis )
  {

    // größtes Tick<-Label berechnen
    final Point ticklabelSize = calcTickLabelSize( gc, axis );

    // TickLabelGröße + 2 wegen Rundungsfehlern beim positionieren
    final int minScreenInterval;
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      minScreenInterval = 2 + ticklabelSize.x;
    else
      minScreenInterval = 2 + ticklabelSize.y;

    // Mini- und maximalen ANZEIGBAREN Wert ermitteln anhand der Größe der Labels
    int screenMin, screenMax;
    final IDataRange<Calendar> range = axis.getLogicalRange();

    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        screenMin = (int) (axis.logicalToScreen( range.getMin() ) + Math.ceil( 0.5 * ticklabelSize.x ));
        screenMax = (int) (axis.logicalToScreen( range.getMax() ) - Math.ceil( 0.5 * ticklabelSize.x ));
      }
      else
      {

        screenMin = (int) (axis.logicalToScreen( range.getMin() ) - Math.ceil( 0.5 * ticklabelSize.x ));
        screenMax = (int) (axis.logicalToScreen( range.getMax() ) + Math.ceil( 0.5 * ticklabelSize.x ));
      }
    }
    else
    {
      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        screenMin = (int) (axis.logicalToScreen( range.getMin() ) - Math.ceil( 0.5 * ticklabelSize.y ));
        screenMax = (int) (axis.logicalToScreen( range.getMax() ) + Math.ceil( 0.5 * ticklabelSize.y ));
      }
      else
      {
        screenMin = (int) (axis.logicalToScreen( range.getMin() ) + Math.ceil( 0.5 * ticklabelSize.y ));
        screenMax = (int) (axis.logicalToScreen( range.getMax() ) - Math.ceil( 0.5 * ticklabelSize.y ));
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

    Format format = axis.getDataOperator().getFormat( axis.getLogicalRange() );

    for( long i = normmin; i <= normmax; i += goodInterval )
    {
      if( count > 0 )
      {
        findBetweens( oldi, i, minLogInterval, ticks, format );
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
    setTickMapElement( axis, realticks.toArray( new Calendar[] {} ) );

    return realticks;
  }

  /**
   * recursive function which divides an interval into a number of "divisions" and adds the values to a linked list the
   * divisions have to have a larger range than the param interval
   */
  private void findBetweens( long from, long to, long minInterval, HashSet<Long> ticks, Format format )
  {

    if( from == to || minInterval == 0 )
      return;

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
              findBetweens( oldi, i, minInterval, ticks, format );
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
  protected Point getTextExtent( GCWrapper gc, final Calendar value, FontData fd, Format format )
  {
    final String label = format.format( new Date( value.getTimeInMillis() ) );
    final Point p = getTextExtent( gc, label, fd );
    return p;
  }

  protected int[] createAxisSegment( final IAxis<Calendar> axis, final Rectangle screen )
  {
    int startX;
    int startY;
    int endX;
    int endY;

    final int gap = getGap();

    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      startX = screen.x;
      endX = screen.x + screen.width;

      if( axis.getPosition() == POSITION.BOTTOM )
        startY = screen.y + gap + 1;
      else
        startY = screen.y + screen.height - gap - 1;
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
        startX = screen.x + gap;
      else
        startX = screen.x + screen.width - gap;
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

  public void paint( GCWrapper gc, final IAxis<Calendar> axis, final Rectangle screen )
  {
    final Device dev = gc.getDevice();

    final Color foreground = new Color( dev, getRgbForeground() );
    final Color background = new Color( dev, getRgbBackground() );
    gc.setForeground( foreground );
    gc.setBackground( background );

    // draw axis line
    final int[] coords = createAxisSegment( axis, screen );
    assert coords != null && coords.length == 4;
    drawAxisLine( gc, coords[0], coords[1], coords[2], coords[3] );

    m_ticks = calcTicks( gc, axis );
    drawTicks( gc, axis, coords[0], coords[1], m_ticks );
    drawAxisLabel( gc, axis, coords[0], coords[1], coords[2], coords[3] );

    foreground.dispose();
    background.dispose();
  }

  protected void drawAxisLabel( GCWrapper gc, IAxis<Calendar> axis, int startX, int startY, int endX, int endY )
  {
    if( axis.getLabel() != null )
    {
      final Point textExtent = getTextExtent( gc, axis.getLabel(), getFontDataLabel() );

      final Point tickLabelExtent = calcTickLabelSize( gc, axis );

      int x = 0;
      int y = 0;

      final Insets labelInsets = getLabelInsets();
      final int tickLength = getTickLength();
      final int lineWidth = getLineWidth();

      final Transform tr = new Transform( gc.getDevice() );
      try
      {
        if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          x = Math.abs( endX - startX ) / 2 - textExtent.x / 2;
          y = startY;

          if( axis.getPosition() == POSITION.TOP )
            y -= tickLength + lineWidth + tickLabelExtent.y + labelInsets.top;
          else
            y += tickLength + lineWidth + tickLabelExtent.y + labelInsets.top;
        }
        else
        {
          x = startX;
          y = Math.abs( endY - startY ) / 2;

          int rotation = 0;

          if( axis.getPosition() == POSITION.LEFT )
          {
            rotation = -90;
            x -= lineWidth + tickLength + tickLabelExtent.x + labelInsets.bottom + textExtent.y;
            y += textExtent.x / 2;
          }
          else
          {
            rotation = 90;
            x += lineWidth + tickLength + tickLabelExtent.x + labelInsets.bottom + textExtent.y;
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
        drawText( gc, axis.getLabel(), x, y, getFontDataLabel() );
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
    gc.setLineWidth( getLineWidth() );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.drawLine( x1, y1, x2, y2 );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer#getTicks(org.kalypso.chart.framework.model.mapper.IAxis)
   */
  public Calendar[] getTicks( IAxis<Calendar> axis )
  {
    Calendar[] tickMapElement = getTickMapElement( axis );
    // wenn null, dann muss es neu erzeugt werden
    if( tickMapElement == null )
    {
      Image img = new Image( Display.getDefault(), 1, 1 );
      GC gc = new GC( img );
      GCWrapper gcw = new GCWrapper( gc );

      calcTicks( gcw, axis );
      tickMapElement = getTickMapElement( axis );

      gcw.dispose();
      gc.dispose();
      img.dispose();
    }
    return tickMapElement;
  }

  /**
   * Gibt die Breite bzw. Tiefe einer Achse zurück. Dadurch wird die Grösse der AxisComponent bestimmt
   */
  public int getAxisWidth( IAxis<Calendar> axis )
  {
    int width = 0;
    int label = 0;
    final int gap = getGap();
    final int lineWidth = getLineWidth();
    final int tickLength = getTickLength();

    final Insets labelInsets = getLabelInsets();

    // Testutensilien erzeugen
    final Display dev = Display.getCurrent();
    final Image img = new Image( dev, 1, 1 );
    final GC gc = new GC( img );
    final GCWrapper gcw = new GCWrapper( gc );

    final Point tls = calcTickLabelSize( gcw, axis );

    // Ticks+Labels
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      width += labelInsets.top + labelInsets.bottom;
      label = tls.y;
    }
    else
    {
      width += labelInsets.left + labelInsets.right;
      label = tls.x;
    }
    width += label;
    width += gap;
    width += lineWidth;
    width += tickLength;

    // Jetzt noch den Text
    final Point labelExtent = getTextExtent( gcw, axis.getLabel(), getFontDataLabel() );
    width += labelExtent.y;

    gcw.dispose();
    gc.dispose();
    img.dispose();

    return width;
  }

}
