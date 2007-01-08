package org.kalypso.swtchart.chart.axis.renderer;

import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

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
import org.kalypso.swtchart.logging.Logger;

/**
 * @author schlienger
 * @author burtscher * IAxisRenderer-Implementation for displaying numeric values
 */
public class NumberAxisRenderer extends AbstractAxisRenderer<Number>
{
  private final NumberFormat m_nf = NumberFormat.getInstance();


  protected final int m_maxDigits;
  private Collection<Number> m_ticks;



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
  public NumberAxisRenderer( final RGB rgbForeground, final RGB rgbBackground, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final int maxDigits, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick )
  {
    super(rgbForeground, rgbBackground, lineWidth, tickLength, tickLabelInsets, labelInsets, gap, fdLabel, fdTick);
    m_maxDigits = maxDigits;
    m_nf.setMinimumFractionDigits( 0 );
    m_nf.setMaximumFractionDigits( 20 );

  }

  /**
   * calculates the size of the biggest tick label by assuming that the width of all labels is strictly monotonic -
   * this means that either the lowest or the highest axis value has the biggest label
   */
  public Point calcTickLabelSize( GCWrapper gc, final IAxis<Number> axis )
  {
    final double logicalfrom = axis.getFrom().doubleValue();
    final double logicalto = axis.getTo().doubleValue();

    // Herausfinden, in welchem 10erPotenz-Bereich sich die Range befinden
    int rangepow = 0;
    // Normalisierte Range
    double normrange = Math.abs( logicalfrom - logicalto );
    if( normrange != 0 )
    {
      while( normrange >= 10 )
      {
        normrange /= 10;
        rangepow--;
      }
      while( normrange < 1 )
      {
        normrange *= 10;
        rangepow++;
      }
    }

    // Für das Number-Format wird nun noch die Anzahl der Nachkommastellen aus der Potenz ermittelt
    if( rangepow >= 0 )
    {
      m_nf.setMaximumFractionDigits( rangepow + 2 );
      m_nf.setMinimumFractionDigits( rangepow + 1 );
    }

    final Point fromTextExtent = getTextExtent( gc, logicalfrom, m_fontDataTick );
    final Point toTextExtent = getTextExtent( gc, logicalto, m_fontDataTick );
    final Point tickLabelSize = new Point( Math.max( fromTextExtent.x, toTextExtent.x ), Math.max( fromTextExtent.y, toTextExtent.y ) );

    final Insets ihelper = getConvertedInsets( axis, m_tickLabelInsets );

    final int x = tickLabelSize.x + ihelper.left + ihelper.right;
    final int y = tickLabelSize.y + ihelper.top + ihelper.bottom;
    return new Point( x, y );
  }

  /**
   * doubleBuffered painting
   */
  public void paint( GCWrapper gc, final IAxis<Number> axis, final Rectangle screen )
  {
    Image img = paintBuffered( gc, axis, screen );
    gc.drawImage( img, 0, 0 );
    img.dispose();
  }

  /**
   * @return Image containing the rendered Axis
   */
  public Image paintBuffered( GCWrapper gc, final IAxis<Number> axis, final Rectangle screen )
  {
    Device dev=gc.getDevice();
    Image bufImg = new Image( dev, dev.getBounds() );
    GC bufGc = new GC( bufImg );
    GCWrapper bufGcw = new GCWrapper( bufGc );
    Color fgColor=new Color(dev, m_rgbForeground);
    Color bgColor=new Color(dev, m_rgbBackground);
    bufGcw.setForeground( fgColor );
    bufGcw.setBackground( bgColor );

    // paint axis line
    int[] coords = createAxisSegment( axis, screen );
    assert coords != null && coords.length == 4;
    drawAxisLine( bufGcw, coords[0], coords[1], coords[2], coords[3] );

    // final Collection<Number> ticks = calcTicks( gc, axis );
    m_ticks = calcTicks( bufGcw, axis );
    drawTicks( bufGcw, axis, coords[0], coords[1], m_ticks );

    drawAxisLabel( bufGcw, axis, coords[0], coords[1], coords[2], coords[3] );

    bufGc.dispose();
    bufGcw.dispose();
    fgColor.dispose();
    bgColor.dispose();
    return bufImg;
  }

  /**
   * @return Array of 4 int-Values: startX, startY, endX, endY - where startX/Y are the start coordinates for the axis
   *         line and endX/Y its end coordinates within the given Rectangle
   */
  private int[] createAxisSegment( final IAxis<Number> axis, final Rectangle screen )
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
        startX = screen.x + screen.width - 1 - m_gap;
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

  /**
   * draws the Axis ticks into the given GCWrapper
   */
  private void drawTicks( GCWrapper gc, IAxis<Number> axis, int startX, int startY, Collection<Number> ticks )
  {
    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );


    int textX = 0;
    int textY = 0;

    if( axis.getPosition() == POSITION.BOTTOM )
    {
      int y1 = startY;
      int y2 = y1 + m_tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, m_fontDataTick );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textX = tickPos - tickExtent.x / 2;
        textY = y2 + m_tickLabelInsets.top;
        drawText( gc, label, textX, textY, m_fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.TOP )
    {
      int y1 = startY;
      int y2 = y1 - m_tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, m_fontDataTick );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textX = tickPos - tickExtent.x / 2;
        textY = y2 - m_tickLabelInsets.top - tickExtent.y;
        drawText( gc, label, textX, textY, m_fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.LEFT )
    {
      int x1 = startX;
      int x2 = x1 - m_tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, m_fontDataTick );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textX = x2 - tickExtent.x - m_tickLabelInsets.top;
        textY = tickPos - tickExtent.y / 2;
        drawText( gc, label, textX, textY, m_fontDataTick );
      }
    }
    else if( axis.getPosition() == POSITION.RIGHT )
    {
      int x1 = startX;
      int x2 = x1 + m_tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = getTextExtent( gc, label, m_fontDataTick );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textX = x2 + m_tickLabelInsets.top;
        textY = tickPos - tickExtent.y / 2;
        drawText( gc, label, textX, textY, m_fontDataTick );
      }
    }
  }

  /**
   * @return Insets-object, transformed in accordance to axis position
   */
  private Insets getConvertedInsets( final IAxis<Number> axis, final Insets insets )
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
  public Collection<Number> calcTicks( GCWrapper gc, final IAxis<Number> axis )
  {

    // größtes Tick<-Label berechnen
    final Point ticklabelSize = calcTickLabelSize( gc, axis );

    // TickLabelGröße + 2 wegen Rundungsfehlern beim positionieren
    final int minScreenInterval;
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      minScreenInterval = 3 + ticklabelSize.x;
    else
      minScreenInterval = 3 + ticklabelSize.y;

    // Collection für Ticks
    final HashSet<Number> ticks = new HashSet<Number>();

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
     Logger.trace(axis.getIdentifier()+" ScreenMin: "+screenMin+" ScreenMax:"+screenMax+" ( "+axis.logicalToScreen( axis.getFrom())+" : "+axis.logicalToScreen( axis.getTo())+") ");
    // logischen Mini- und Maximalen wert
    double logicalMin = axis.screenToLogical( screenMin ).doubleValue();
    double logicalMax = axis.screenToLogical( screenMax ).doubleValue();

    double logicalRange = Math.abs( logicalMax - logicalMin );

    // der minimale logische Abstand; TODO: überprüfen, ob das auch mit negativen Werten geht
    double minLogInterval = Math.abs( axis.screenToLogical( minScreenInterval ).doubleValue() - axis.screenToLogical( 0 ).doubleValue() );

    // Herausfinden, in welchem 10erPotenz-Bereich sich die Range befinden
    int rangepow = 0;
    // Normalisierte Range
    double normrange = logicalRange;
    if( normrange != 0 )
    {
      while( normrange >= 10 )
      {
        normrange /= 10;
        rangepow--;
      }
      while( normrange < 1 )
      {
        normrange *= 10;
        rangepow++;
      }
    }

    Logger.trace(axis.getIdentifier()+" logicalRange: "+logicalRange);


     Logger.trace(axis.getIdentifier()+" Normrange: "+normrange);
     Logger.trace(axis.getIdentifier()+" Rangepow: "+rangepow);

    // Das Minimum normalisieren und abrunden
    double normmin = Math.floor( axis.getFrom().doubleValue() * Math.pow( 10, rangepow ) );
    // Das Minimum wieder zurückrechnen
    normmin = normmin * Math.pow( 10, rangepow * (-1) );
     Logger.trace(axis.getIdentifier()+" Normmin: "+normmin);

    // Das Maximum normalisieren und aufrunden
    double normmax = Math.ceil( axis.getTo().doubleValue() * Math.pow( 10, rangepow ) );
    // Das Maximum wieder zurückrechnen
    normmax = normmax * Math.pow( 10, rangepow * (-1) );
     Logger.trace(axis.getIdentifier()+" Normmax: "+normmax);

    // Das Intervall verwendet zunächst nur 10er Schritte
    double interval = Math.pow( 10, rangepow * (-1) );
     Logger.trace(axis.getIdentifier()+" Interval ("+axis.getIdentifier()+"): "+interval);

    // hier werden alle Zahlen gespeichert, die als gute Divisoren eines Intervalls gelten
    // 3 würde z.B. schnell krumme werte erzeugen
    LinkedList<Integer> goodDivisors = new LinkedList<Integer>();

    goodDivisors.add( 10 );
    goodDivisors.add( 5 );
    goodDivisors.add( 4 );
    goodDivisors.add( 2 );

    int count = 0;
    double oldi = 0;
    for( double i = normmin; i <= normmax * 1.1; i += interval )
    {
      if( count > 0 )
      {
        findBetweens( oldi, i, minLogInterval, goodDivisors, ticks );
      }
      ticks.add( (new Double( i )).intValue() );

      count++;
      oldi = i;
    }

    // in realticks sind jetzt nur die wirklich zu zeichnenden werte drin
    // wenn man das nicht über neues objekt, sondern ticks.remove macht, gibts eine concurrentmodificationexception
    LinkedList<Number> realticks = new LinkedList<Number>();
    for( Number tick : ticks )
    {
      // "Unnötige" Zahlen abschneiden - dadurch werden Werte wie -0.0 (von -0.00000000000000001) und unnötige
      // Nachkommastellen entfernt
      // unnötig ist alles, was mehr als 2 Stellen länger als Rangepow ist
      double tickdv = tick.doubleValue();
      int tickNorm = (int) (tickdv * Math.pow( 10, (rangepow + 2) ));
      tickdv = tickNorm * Math.pow( 10, -(rangepow + 2) );

      if( (tickdv >= logicalMin) && (tickdv <= logicalMax) )
      {
        realticks.add( new Double( tickdv ) );
      }
    }

    // Für das Number-Format wird nun noch die Anzahl der Nachkommastellen aus der Potenz ermittelt
    if( rangepow >= 0 )
    {
      m_nf.setMaximumFractionDigits( rangepow + 2);
      m_nf.setMinimumFractionDigits( rangepow + 1 );
    }

    m_tickMap.put( axis, realticks );

    return realticks;
  }

  /**
   * recursive function which divides in interval into a number of "divisions" and adds the values to a linked list the
   * divisions have to have a larger range than the param interval
   */
  private void findBetweens( double from, double to, double interval, LinkedList<Integer> divisors, HashSet<Number> ticks )
  {

    for( Integer divisor : divisors )
    {
      double betweenrange = Math.abs( from - to ) / divisor.intValue();

      // TODO: this prevents an endles loop below, if from == to
      // @Alex:check if this is ok
      if( betweenrange < 0.0001 )
        return;

      if( interval < betweenrange )
      {
        // vorher prüfen
        int count = 0;
        double oldi = 0;
        for( double i = from; i <= to; i += betweenrange )
        {
          ticks.add( new Double( i ) );
          if( count >= 1 )
          {
            findBetweens( oldi, i, interval, divisors, ticks );
          }
          // Tick setzen
          count++;
          oldi = i;
        }
        break;
      }
    }
  }

  /**
   * draws the axis' main line
   */
  private void drawAxisLine( GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  {
    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.drawLine( x1, y1, x2, y2 );
  }



  /**
   * draws the axis label - if the axis is oriented vertical, the text is also drawn vertical
   */
  private void drawAxisLabel( GCWrapper gc, IAxis<Number> axis, int x1, int y1, int x2, int y2 )
  {
    //ChartUtilities.resetGC( gc.m_gc, dev );


    if( axis.getLabel() != null )
    {
      final Point textExtent = getTextExtent( gc, axis.getLabel(), m_fontDataLabel );

      final Point tickLabelExtent = calcTickLabelSize( gc, axis );

      int x = 0;
      int y = 0;

      Transform tr = new Transform( gc.getDevice() );
      try
      {
        if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          x = Math.abs( x2 - x1 ) / 2 - textExtent.x / 2;
          y = y1;

          if( axis.getPosition() == POSITION.TOP )
            y -= m_tickLength + m_lineWidth + m_tickLabelInsets.top + tickLabelExtent.y + m_tickLabelInsets.bottom + m_labelInsets.bottom;// +
          else
            y += m_tickLength + m_lineWidth + m_tickLabelInsets.top + tickLabelExtent.y + m_tickLabelInsets.bottom + m_labelInsets.top;// +
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

  /**
   * calculates the extent of a number using a certain fontdata
   */
  @Override
  protected Point getTextExtent( GCWrapper gc, Number value, FontData fd )
  {
    String label = "";
    try
    {
      label = m_nf.format( value );
    }
    catch( java.lang.IllegalArgumentException e )
    {
      e.printStackTrace();
    }
    return getTextExtent( gc, label, fd );
  }

  /**
   * @see org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer#getTicks(org.kalypso.swtchart.chart.axis.IAxis)
   */
  public Collection<Number> getGridTicks( IAxis<Number> axis )
  {
    Collection<Number> ticks= m_tickMap.get( axis );
    if (ticks==null)
    {
        GC gc=new GC(Display.getDefault());
        GCWrapper gcw=new GCWrapper(gc);
        calcTicks( gcw, axis );
        ticks= m_tickMap.get( axis );
        gcw.dispose();
        gc.dispose();
    }
     return ticks;
  }

  /**
   * Gibt die Breite bzw. Tiefe einer Achse zurück. Dadurch wird die Grösse der AxisComponent bestimmt
   */
  public int getAxisWidth( IAxis<Number> axis )
  {
    int width = 0;
    int label = 0;

    // Testutensilien erzeugen
    Display dev = Display.getCurrent();
    Image img = new Image( dev, 1, 1 );
    GC gc = new GC( img );
    GCWrapper gcw = new GCWrapper( gc );

    Point tls = calcTickLabelSize( gcw, axis );

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
    Point labelExtent = getTextExtent( gcw, axis.getLabel(), m_fontDataLabel );
    width += labelExtent.y;

    gcw.dispose();
    gc.dispose();
    img.dispose();

    return width;
  }





}
