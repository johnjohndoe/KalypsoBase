package org.kalypso.swtchart.chart.axis.renderer;

import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;

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
import org.kalypso.commons.java.lang.MathUtils;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.ChartUtilities;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.chart.util.InsetsHelper;

/**
 * @author schlienger
 * @author burtscher
 * 
 *  * IAxisRenderer-Implementation for displaying numeric values
 */
public class NumberAxisRenderer implements IAxisRenderer<Number>
{
  private final NumberFormat m_nf = NumberFormat.getInstance();

  protected final int m_tickLength;

  protected final Insets m_labelInsets;

  protected final int m_maxDigits;

  protected final int m_lineWidth;

  private final Insets m_tickLabelInsets;

  private final Color m_foreground;

  private final Color m_background;

  private Collection<Number> m_ticks;

  private final int m_gap;

  private FontData m_fontDataLabel;

  private FontData m_fontDataTick;

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
  public NumberAxisRenderer( final Color foreground, final Color background, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final int maxDigits, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick )
  {
    m_foreground = foreground;
    m_background = background;
    m_tickLength = tickLength;
    m_lineWidth = lineWidth;
    m_tickLabelInsets = tickLabelInsets;
    m_labelInsets = labelInsets;
    m_maxDigits = maxDigits;
    m_gap = gap;
    m_fontDataLabel = fdLabel;
    m_fontDataTick = fdTick;

    m_nf.setMinimumFractionDigits( maxDigits );
    m_nf.setMaximumFractionDigits( maxDigits );
  }

  /**
   * calculates the size of the biggest tick label by assiuming that the width
   * of all labels is strictly monotonic - this means that either the lowest
   * or the highest axis value has the biggest label
   */
  private Point calcTickLabelSize( GCWrapper gc, Device dev, final IAxis<Number> axis )
  {
    final double logicalfrom = axis.getFrom().doubleValue();
    final double logicalto = axis.getTo().doubleValue();
    final Point fromTextExtent = getTextExtent( gc, dev, logicalfrom, m_fontDataTick );
    final Point toTextExtent = getTextExtent( gc, dev, logicalto, m_fontDataTick );
    final Point tickLabelSize = new Point( Math.max( fromTextExtent.x, toTextExtent.x ), Math.max( fromTextExtent.y, toTextExtent.y ) );

    final Insets ihelper = getConvertedInsets( axis, m_tickLabelInsets );

    final int x = tickLabelSize.x + ihelper.left + ihelper.right;
    final int y = tickLabelSize.y + ihelper.top + ihelper.bottom;

    return new Point( x, y );
  }

  /**
   * doubleBuffered painting
   */
  public void paint( GCWrapper gc, final Device dev, final IAxis<Number> axis, final Rectangle screen )
  {
    Image img = paintBuffered( dev, axis, screen );
    gc.drawImage( img, 0, 0 );
    img.dispose();
  }

  /**
   * @return Image containing the rendered Axis
   */
  public Image paintBuffered( final Device dev, final IAxis<Number> axis, final Rectangle screen )
  {
    Image bufImg = new Image( dev, dev.getBounds() );
    GC bufGc = new GC( bufImg );
    GCWrapper bufGcw = new GCWrapper( bufGc );
    // paint axis line
    int[] coords = createAxisSegment( axis, screen );
    assert coords != null && coords.length == 4;
    drawAxisLine( bufGcw, coords[0], coords[1], coords[2], coords[3] );

    // final Collection<Number> ticks = calcTicks( gc, axis );
    m_ticks = calcTicks( bufGcw, dev, axis );
    drawTicks( bufGcw, dev, axis, coords[0], coords[1], coords[2], coords[3], m_ticks );

    drawAxisLabel( bufGcw, dev, axis, coords[0], coords[1], coords[2], coords[3] );

    bufGc.dispose();
    bufGcw.dispose();
    return bufImg;
  }

  /**
   * @return Array of 4 int-Values: startX, startY, endX, endY - where startX/Y are the start coordinates
   * for the axis line and endX/Y its end coordinates within the given Rectangle
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
  private void drawTicks( GCWrapper gc, Device dev, IAxis<Number> axis, int startX, int startY, int endX, int endY, Collection<Number> ticks )
  {
    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.setForeground( m_foreground );

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
        final Point tickExtent = getTextExtent( gc, dev, label, m_fontDataTick );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textX = tickPos - tickExtent.x / 2;
        textY = y2 + m_tickLabelInsets.top;
        drawText( gc, dev, label, textX, textY, m_fontDataTick );
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
        final Point tickExtent = getTextExtent( gc, dev, label, m_fontDataTick );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textX = tickPos - tickExtent.x / 2;
        textY = y2 - m_tickLabelInsets.top - tickExtent.y;
        drawText( gc, dev, label, textX, textY, m_fontDataTick );
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
        final Point tickExtent = getTextExtent( gc, dev, label, m_fontDataTick );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textX = x2 - tickExtent.x - m_tickLabelInsets.top;
        textY = tickPos - tickExtent.y / 2;
        drawText( gc, dev, label, textX, textY, m_fontDataTick );
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
        final Point tickExtent = getTextExtent( gc, dev, label, m_fontDataTick );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textX = x2 + m_tickLabelInsets.top;
        textY = tickPos - tickExtent.y / 2;
        drawText( gc, dev, label, textX, textY, m_fontDataTick );
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
  public Collection<Number> calcTicks( GCWrapper gc, Device dev, final IAxis<Number> axis )
  {

    // größtes Tick<-Label berechnen
    final Point ticklabelSize = calcTickLabelSize( gc, dev, axis );

    // TickLabelGröße + 2 wegen Rundungsfehlern beim positionieren
    final int minScreenInterval;
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      minScreenInterval = 2 + ticklabelSize.x;
    else
      minScreenInterval = 2 + ticklabelSize.y;

    // logischen Abstand anhand des Screen-Abstands berechnen
    double logicalInterval = Math.abs( axis.screenToLogical( minScreenInterval * 2 ).doubleValue() - axis.screenToLogical( minScreenInterval ).doubleValue() );
    // Und jetzt nochmal auf einen mehr oder weniger sinnvollen Wert runden
    logicalInterval = MathUtils.round( logicalInterval, MathUtils.RoundMethod.UP );

    // System.out.println( "Logical Interval= " + logicalInterval );

    // Anzahl der "relevanten" Nachkommastelle herausfinden - das ist die ersten Stelle der Zahl, die ungleich 0 ist
    final int digits = Math.min( MathUtils.scale( logicalInterval ), m_maxDigits );

    final double ticklength = Math.abs( MathUtils.setScale( logicalInterval, digits, MathUtils.RoundMethod.UP ) );

    final Collection<Number> ticks = new LinkedList<Number>();

    if( ticklength > 0.0 )
    {
      for( double tickValue = MathUtils.setScale( axis.getFrom().doubleValue(), digits, MathUtils.RoundMethod.UP ); tickValue <= axis.getTo().doubleValue(); tickValue += ticklength )
        ticks.add( new Double( tickValue ) );
    }

    return ticks;
  }

  /**
   *  draws the axis' main line
   */
  private void drawAxisLine( GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  {
    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.setForeground( m_foreground );

    gc.drawLine( x1, y1, x2, y2 );
  }

  /**
   * draws a given text at the position using special fontData
   */
  private void drawText( GCWrapper gc, Device dev, String text, int x, int y, FontData fd )
  {
    Font f = new Font( dev, fd );
    gc.m_gc.setTextAntialias( SWT.ON );
    gc.setFont( f );
    gc.drawText( text, x, y );
    gc.setFont( dev.getSystemFont() );
    f.dispose();
  }

  /**
   * draws the axis label - if the axis is oriented vertical, the text is also drawn vertical
   */
  private void drawAxisLabel( GCWrapper gc, Device dev, IAxis<Number> axis, int x1, int y1, int x2, int y2 )
  {
    ChartUtilities.resetGC( gc.m_gc, dev );

    gc.setForeground( m_foreground );
    gc.setBackground( m_background );

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
            y -= m_tickLength + m_lineWidth + m_tickLabelInsets.top + tickLabelExtent.y + m_tickLabelInsets.bottom + m_labelInsets.bottom;// +
                                                                                                                                          // m_labelInsets.bottom
                                                                                                                                          // +
                                                                                                                                          // 2*textExtent.y;
          else
            y += m_tickLength + m_lineWidth + m_tickLabelInsets.top + tickLabelExtent.y + m_tickLabelInsets.bottom + m_labelInsets.top;// +
                                                                                                                                        // m_labelInsets.bottom
                                                                                                                                        // +
                                                                                                                                        // textExtent.y;
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
          // System.out.println( "Using Transform: " + tr );
        }
        drawText( gc, dev, axis.getLabel(), x, y, m_fontDataLabel );

        // System.out.println( "Axis Label: X=" + x + " Y=" + y + " for " + axis );
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
  private Point getTextExtent( GCWrapper gc, Device dev, Number value, FontData fd )
  {
    String label = "";
    try
    {
      label = m_nf.format( value );
    }
    catch( java.lang.IllegalArgumentException e )
    {
      System.out.println( value.toString() );
      e.printStackTrace();
    }
    return getTextExtent( gc, dev, label, fd );
  }
  
  /**
   * calculates the extent of text using certain fontdata
   * TODO: should be extracted into a general helper Method
   */
  private Point getTextExtent( GCWrapper gc, Device dev, String label, FontData fd )
  {
    Font f = new Font( dev, fd );
    gc.setFont( f );
    Point point = gc.textExtent( label );
    gc.setFont( dev.getSystemFont() );
    f.dispose();
    return point;
  }

  /**
   * @see org.kalypso.swtchart.axis.renderer.IAxisRenderer#getAxisWidth(org.kalypso.swtchart.axis.IAxis)
   */
  public int getAxisWidth( IAxis<Number> axis )
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
    width += m_lineWidth;
    width += m_tickLength;
    width += m_gap;

    // Jetzt noch den Text
    Point labelExtent = getTextExtent( gcw, d, axis.getLabel(), m_fontDataLabel );
    width += labelExtent.y;

    gcw.dispose();
    gc.dispose();
    img.dispose();

    return width;
  }

}
