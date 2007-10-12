package org.kalypso.chart.ext.test.axisrenderer;

import java.awt.Insets;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.Map.Entry;

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
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.renderer.AbstractAxisRenderer;
import org.kalypso.chart.framework.util.InsetsHelper;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author burtscher * IAxisRenderer-Implementation for displaying numeric values
 */
public class TmpNumberAxisRenderer extends AbstractAxisRenderer<Number>
{
  private NumberFormat m_nf = NumberFormat.getInstance();

  private TreeMap<Integer, SortedSet<Double>> m_ticks;

  /**
   * minimal interval size for ticks; if set to 0, the minimum interval size is calculated; otherwise, no interval
   * smaller than the given value is displayed
   */
  private final double m_minDisplayInterval;

  private final int m_fixedWidth;

  private final boolean m_hideCut;

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
   * @param minDisplayInterval
   *            minimal interval size between ticks; if set to 0, the minimum interval size is calculated; otherwise, no
   *            interval smaller than the given value is displayed
   */
  public TmpNumberAxisRenderer( final String id, final RGB rgbForeground, final RGB rgbBackground, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick, final double minDisplayInterval, final int fixedWidth, final boolean hideCut )
  {
    super( id, rgbForeground, rgbBackground, lineWidth, tickLength, tickLabelInsets, labelInsets, gap, fdLabel, fdTick );
    m_nf.setMinimumFractionDigits( 0 );
    m_nf.setMaximumFractionDigits( 20 );
    m_minDisplayInterval = minDisplayInterval;
    m_hideCut = hideCut;
    m_fixedWidth = fixedWidth;
  }

  /**
   * calculates the size of the biggest tick label by assuming that the width of all labels is strictly monotonic - this
   * means that either the lowest or the highest axis value has the biggest label
   */
  public Point calcTickLabelSize( GCWrapper gc, final IAxis<Number> axis )
  {
    Point tickLabelSize = new Point( 10, 10 );
    // Vielleicht wurden zuvor schon mal ticks ausgerechnet - dann kann man
    // die verwenden
    final Number[] tickMap = getTickMap().get( axis );

    if( tickMap != null && tickMap.length > 0 )
    {
      TmpNumberTickCalculator.testNumberFormat( tickMap );
      tickLabelSize = getTextExtent( gc, m_nf.format( tickMap[tickMap.length - 1] ), getFontDataTickLabel() );
      Logger.logInfo( Logger.TOPIC_LOG_AXIS, "Biggest Tick: " + m_nf.format( tickMap[0] ) + " " + tickMap[0] );
    }
    else
    {
      final IDataRange<Number> range = axis.getLogicalRange();
      final double logicalMin, logicalMax;
      if( range != null )
      {
        logicalMin = range.getMin().doubleValue();
        logicalMax = range.getMax().doubleValue();
      }
      else
      {
        return new Point( 1, 0 );
      }

      final FontData fontDataTick = getFontDataTickLabel();

      final Point fromTextExtent = getTextExtent( gc, logicalMin, fontDataTick );
      final Point toTextExtent = getTextExtent( gc, logicalMax, fontDataTick );
      tickLabelSize = new Point( Math.max( fromTextExtent.x, toTextExtent.x ), Math.max( fromTextExtent.y, toTextExtent.y ) );

    }

    final Insets ihelper = getConvertedInsets( axis, getTickLabelInsets() );

    final int x = tickLabelSize.x + ihelper.left + ihelper.right;
    final int y = tickLabelSize.y + ihelper.top + ihelper.bottom;
    return new Point( x, y );
  }

  /**
   * doubleBuffered painting
   */
  public void paint( GCWrapper gc, final IAxis<Number> axis, final Rectangle screen )
  {
    final Device dev = gc.getDevice();
    final Color fgColor = new Color( dev, getRgbForeground() );
    final Color bgColor = new Color( dev, getRgbBackground() );
    gc.setForeground( fgColor );
    gc.setBackground( bgColor );

    // paint axis line
    final int[] coords = createAxisSegment( axis, screen );
    assert coords != null && coords.length == 4;
    drawAxisLine( gc, coords[0], coords[1], coords[2], coords[3] );

    // final Collection<Double> ticks = calcTicks( gc, axis );
    m_ticks = calcTicks( gc, axis );
    drawTicks( gc, axis, coords[0], coords[1], m_ticks );

    drawAxisLabel( gc, axis, coords[0], coords[1], coords[2], coords[3] );

    fgColor.dispose();
    bgColor.dispose();
  }

  private TreeMap<Integer, SortedSet<Double>> calcTicks( GCWrapper gc, IAxis<Number> axis )
  {
    return TmpNumberTickCalculator.calcTicks( gc, axis, m_minDisplayInterval );
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

    final int gap = getGap();

    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      startX = screen.x;
      endX = screen.x + screen.width;

      if( axis.getPosition() == POSITION.BOTTOM )
        startY = screen.y + gap;
      else
        startY = screen.y + screen.height - 1 - gap;
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
        startX = screen.x + screen.width - 1 - gap;
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

  /**
   * draws the Axis ticks into the given GCWrapper
   */
  private void drawTicks( GCWrapper gc, IAxis<Number> axis, int startX, int startY, TreeMap<Integer, SortedSet<Double>> m_ticks2 )
  {
    if( gc == null || axis == null || m_ticks2 == null )
    {
      return;
    }

    gc.setLineWidth( getLineWidth() );
    gc.setLineStyle( SWT.LINE_SOLID );

    int textX = 0;
    int textY = 0;

    final int tickLength = getTickLength();
    final FontData fontDataTick = getFontDataTickLabel();
    final Insets tickLabelInsets = getTickLabelInsets();

    final IDataRange<Number> dataRange = axis.getLogicalRange();
    final int axisMin = axis.logicalToScreen( dataRange.getMin() );
    final int axisMax = axis.logicalToScreen( dataRange.getMax() );
    final int screenMin = Math.min( axisMin, axisMax );
    final int screenMax = Math.max( axisMin, axisMax );

    for( final Entry<Integer, SortedSet<Double>> level : m_ticks.entrySet() )
    {
      int y1, y2, x1, x2, tickPos;
      String label;
      for( final Number value : level.getValue() )
      {
        boolean drawTick = true;
        label = m_nf.format( value );
        tickPos = axis.logicalToScreen( value );
        final Point tickExtent = getTextExtent( gc, label, fontDataTick );
        // HORIZONTAL
        if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          x1 = tickPos;
          x2 = tickPos;
          y1 = startY;
          textX = tickPos - tickExtent.x / 2;
          // BOTTOM
          if( axis.getPosition() == POSITION.BOTTOM )
          {
            y2 = y1 + tickLength;
            textY = y2 + tickLabelInsets.top;
          }
          // TOP
          else
          {
            y2 = y1 - tickLength;
            textY = y2 - tickLabelInsets.top - tickExtent.y;
          }

          if( m_hideCut && (textX < screenMin || (textX + tickExtent.x) > screenMax) )
            drawTick = false;
        }
        // VERTICAL
        else
        {
          x1 = startX;
          textY = tickPos - tickExtent.y / 2;
          y1 = tickPos;
          y2 = tickPos;
          // LEFT
          if( axis.getPosition() == POSITION.LEFT )
          {
            x2 = x1 - tickLength;
            textX = x2 - tickExtent.x - tickLabelInsets.top;
          }
          // RIGHT
          else
          {
            x2 = x1 + tickLength;
            textX = x2 + tickLabelInsets.top;
          }
          if( m_hideCut && (textY < screenMin || (textY + tickExtent.y) > screenMax) )
            drawTick = false;
        }

        if( drawTick )
        {
          final Color oldFforeground = gc.getForeground();

          drawText( gc, label, textX, textY, fontDataTick );
          gc.drawLine( x1, y1, x2, y2 );
        }
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
   * draws the axis' main line
   */
  private void drawAxisLine( GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  {
    gc.setLineWidth( getLineWidth() );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.drawLine( x1, y1, x2, y2 );
  }

  /**
   * draws the axis label - if the axis is oriented vertical, the text is also drawn vertical
   */
  private void drawAxisLabel( GCWrapper gc, IAxis<Number> axis, int x1, int y1, int x2, int y2 )
  {
    // ChartUtilities.resetGC( gc.m_gc, dev );

    if( axis.getLabel() != null )
    {
      final Point textExtent = getTextExtent( gc, axis.getLabel(), getFontDataLabel() );

      final Point tickLabelExtent = calcTickLabelSize( gc, axis );

      if( tickLabelExtent == null )
      {
        return;
      }

      int x = 0;
      int y = 0;

      final int tickLength = getTickLength();
      final int lineWidth = getLineWidth();

      final Insets labelInsets = getLabelInsets();
      final Insets tickLabelInsets = getTickLabelInsets();

      final Transform tr = new Transform( gc.getDevice() );
      try
      {
        if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          x = Math.abs( x2 - x1 ) / 2 - textExtent.x / 2;
          y = y1;

          if( axis.getPosition() == POSITION.TOP )
            y -= textExtent.y + labelInsets.bottom + tickLength + lineWidth + tickLabelInsets.top + tickLabelExtent.y + tickLabelInsets.bottom + labelInsets.bottom;// +
          else
            y += tickLength + lineWidth + tickLabelInsets.top + tickLabelExtent.y + tickLabelInsets.bottom + labelInsets.top;// +
        }
        else
        {
          x = x1;
          y = Math.abs( y2 - y1 ) / 2;

          int rotation = 0;

          if( axis.getPosition() == POSITION.LEFT )
          {
            rotation = -90;
            x -= lineWidth + tickLength + tickLabelInsets.right + tickLabelExtent.x + tickLabelInsets.left + labelInsets.bottom + textExtent.y;
            y += textExtent.x / 2;
          }
          else
          {
            rotation = 90;
            x += lineWidth + tickLength + tickLabelInsets.right + tickLabelExtent.x + tickLabelInsets.left + labelInsets.bottom + textExtent.y;
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

  /**
   * calculates the extent of a Double using a certain fontdata
   */
  @Override
  protected Point getTextExtent( GCWrapper gc, Number value, FontData fd )
  {
    String label = "";
    try
    {
      label = m_nf.format( value );
    }
    catch( final java.lang.IllegalArgumentException e )
    {
      e.printStackTrace();
    }
    return getTextExtent( gc, label, fd );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer#getTicks(org.kalypso.chart.framework.model.mapper.IAxis)
   */
  public Number[] getGridTicks( IAxis<Number> axis )
  {
    final HashMap<IAxis<Number>, Number[]> tickMap = getTickMap();
    Number[] ticks = tickMap.get( axis );
    if( ticks == null )
    {
      final GC gc = new GC( Display.getDefault() );
      final GCWrapper gcw = new GCWrapper( gc );
      calcTicks( gcw, axis );
      ticks = tickMap.get( axis );
      gcw.dispose();
      gc.dispose();
    }
    return ticks;
  }

  /**
   * Gibt die Breite bzw. Tiefe einer Achse zur�ck. Dadurch wird die Gr�sse der AxisComponent bestimmt
   */
  public int getAxisWidth( IAxis<Number> axis )
  {
    if( m_fixedWidth > 0 )
      return m_fixedWidth;

    int width = 0;
    int ticklabel = 0;

    // Testutensilien erzeugen
    final Display dev = Display.getCurrent();
    final Image img = new Image( dev, 1, 1 );
    final GC gc = new GC( img );
    final GCWrapper gcw = new GCWrapper( gc );

    final Point tickLabelSize = calcTickLabelSize( gcw, axis );

    if( tickLabelSize == null )
    {
      return 0;
    }

    final Insets tickLabelInsets = getTickLabelInsets();
    final Insets labelInsets = getLabelInsets();

    // TickLabels
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      width += tickLabelInsets.top + tickLabelInsets.bottom;
      ticklabel = tickLabelSize.y;
      if( axis.getPosition() == POSITION.BOTTOM )
        width += labelInsets.top;
      else
        width += labelInsets.bottom;

    }
    else
    {
      width += tickLabelInsets.left + tickLabelInsets.right;
      width += labelInsets.bottom;
      ticklabel = tickLabelSize.x;
    }
    width += ticklabel;
    width += getGap();
    width += getLineWidth();
    width += getTickLength();

    // Jetzt noch den Label-Text
    final Point labelExtent = getTextExtent( gcw, axis.getLabel(), getFontDataLabel() );
    width += labelExtent.y;

    gcw.dispose();
    gc.dispose();
    img.dispose();

    return width;
  }

  public void setNumberFormat( NumberFormat nf )
  {
    m_nf = nf;
  }

  public NumberFormat getNumberFormat( )
  {
    return m_nf;
  }

}
