package org.kalypso.chart.ext.base.axisrenderer;

import java.awt.Insets;
import java.text.Format;
import java.text.NumberFormat;

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
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.util.InsetsHelper;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author burtscher * IAxisRenderer-Implementation for displaying numeric values
 */
public class NumberAxisRenderer extends AbstractAxisRenderer<Number>
{
  private final NumberFormat m_nf = NumberFormat.getInstance();

  /**
   * minimal interval size for ticks; if set to 0, the minimum interval size is calculated; otherwise, no interval
   * smaller than the given value is displayed
   */
  private final double m_minDisplayInterval;

  private final int m_fixedWidth;

  private final boolean m_hideCut;

  private final String m_tick_label_formater;

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
   * @param tick_label_formater
   *            String.format(string) for formating output values - if format string equals %s, dynamic size ticklabel
   *            calculator will be used
   */
  public NumberAxisRenderer( final String id, final RGB rgbForeground, final RGB rgbBackground, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final FontData fdLabel, final FontData fdTick, final double minDisplayInterval, final int fixedWidth, final boolean hideCut, final String tick_label_formater )
  {
    super( id, rgbForeground, rgbBackground, lineWidth, tickLength, tickLabelInsets, labelInsets, gap, fdLabel, fdTick );

    m_tick_label_formater = tick_label_formater;
    m_nf.setMinimumFractionDigits( 0 );
    m_nf.setMaximumFractionDigits( 20 );
    m_minDisplayInterval = minDisplayInterval;
    m_hideCut = hideCut;
    m_fixedWidth = fixedWidth;
  }

  private String getLabel( final Number value, final Format format )
  {
    if( m_tick_label_formater.trim().equals( "%s" ) )
      return format.format( value );
    else
      return String.format( m_tick_label_formater, value );
  }

  /**
   * calculates the size of the biggest tick label by assuming that the width of all labels is strictly monotonic - this
   * means that either the lowest or the highest axis value has the biggest label
   */
  public Point calcTickLabelSize( final GCWrapper gc, final IAxis<Number> axis )
  {
    final Format format = axis.getDataOperator().getFormat( axis.getLogicalRange() );
    final String testStringMax = format.format( axis.getLogicalRange().getMax() );
    final String testStringMin = format.format( axis.getLogicalRange().getMin() );

    final Font oldfont = gc.getFont();
    gc.setFont( getFont( getFontDataTickLabel(), gc.getDevice() ) );

    final Point tickLabelSizeMax = gc.textExtent( testStringMax );
    final Point tickLabelSizeMin = gc.textExtent( testStringMin );

    gc.setFont( oldfont );

    final Insets ihelper = getConvertedInsets( axis, getTickLabelInsets() );
    final int x = Math.max( tickLabelSizeMax.x, tickLabelSizeMin.x ) + ihelper.left + ihelper.right;
    final int y = Math.max( tickLabelSizeMax.y, tickLabelSizeMin.y ) + ihelper.top + ihelper.bottom;
    return new Point( x, y );
  }

  /**
   * doubleBuffered painting
   */
  public void paint( final GCWrapper gc, final IAxis<Number> axis, final Rectangle screen )
  {
    final Device dev = gc.getDevice();
    final Color fgColor = new Color( dev, getRgbForeground() );
    final Color bgColor = new Color( dev, getRgbBackground() );
    gc.setForeground( fgColor );
    gc.setBackground( bgColor );

    // paint axis line
    final int[] coords = createAxisSegment( axis, screen );
    assert (coords != null) && (coords.length == 4);
    drawAxisLine( gc, coords[0], coords[1], coords[2], coords[3] );

    // final Collection<Double> ticks = calcTicks( gc, axis );
    drawTicks( gc, axis, coords[0], coords[1] );

    drawAxisLabel( gc, axis, coords[0], coords[1], coords[2], coords[3] );

    fgColor.dispose();
    bgColor.dispose();
  }

  public Number[] getTicks( final IAxis<Number> axis )
  {
    Number[] tickMapElement = getTickMapElement( axis );
    // wenn null, dann muss es neu erzeugt werden
    if( tickMapElement == null )
    {
      final Image img = new Image( Display.getDefault(), 1, 1 );
      final GC gc = new GC( img );
      final GCWrapper gcw = new GCWrapper( gc );

      NumberTickCalculator.calcTicks( gcw, axis, m_minDisplayInterval );
      tickMapElement = getTickMapElement( axis );

      gcw.dispose();
      gc.dispose();
      img.dispose();
    }
    return tickMapElement;
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
        startY = screen.y + gap + 1;
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
        startX = screen.x + gap + 1;
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
  private void drawTicks( final GCWrapper gc, final IAxis<Number> axis, final int startX, final int startY )
  {
    final Number[] ticks = getTicks( axis );

    if( (gc == null) || (axis == null) || (ticks == null) )
      return;

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
    for( final Number value : ticks )
    {
      int y1, y2, x1, x2, tickPos;
      String label;
      label = getLabel( value, axis.getDataOperator().getFormat( axis.getLogicalRange() ) );

      boolean drawTick = true;

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

        if( m_hideCut && ((textX < screenMin) || ((textX + tickExtent.x) > screenMax)) )
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
        if( m_hideCut && ((textY < screenMin) || ((textY + tickExtent.y) > screenMax)) )
          drawTick = false;
      }

      if( drawTick )
      {
        drawText( gc, label, textX, textY, fontDataTick );
        gc.drawLine( x1, y1, x2, y2 );
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
  private void drawAxisLine( final GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  {
    gc.setLineWidth( getLineWidth() );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.drawLine( x1, y1, x2, y2 );
  }

  /**
   * draws the axis label - if the axis is oriented vertical, the text is also drawn vertical
   */
  private void drawAxisLabel( final GCWrapper gc, final IAxis<Number> axis, final int x1, final int y1, final int x2, final int y2 )
  {
    // ChartUtilities.resetGC( gc.m_gc, dev );

    if( axis.getLabel() != null )
    {
      final Point textExtent = getTextExtent( gc, axis.getLabel(), getFontDataLabel() );

      final Point tickLabelExtent = calcTickLabelSize( gc, axis );

      if( tickLabelExtent == null )
        return;

      int x = 0;
      int y = 0;

      final int tickLength = getTickLength();
      final int lineWidth = getLineWidth();

      final Insets labelInsets = getLabelInsets();

      final Transform tr = new Transform( gc.getDevice() );
      try
      {
        if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          x = Math.abs( x2 - x1 ) / 2 - textExtent.x / 2;
          y = y1;

          if( axis.getPosition() == POSITION.TOP )
            y -= textExtent.y + labelInsets.bottom + tickLength + lineWidth + tickLabelExtent.y - 2;// +
          else
            y += tickLength + lineWidth + tickLabelExtent.y + labelInsets.top - 2;// +
        }
        else
        {
          x = x1;
          y = Math.abs( y2 - y1 ) / 2;

          int rotation = 0;

          if( axis.getPosition() == POSITION.LEFT )
          {
            rotation = -90;
            x -= lineWidth + tickLength + tickLabelExtent.x + labelInsets.bottom + textExtent.y - 2;
            y += textExtent.x / 2;
          }
          else
          {
            rotation = 90;
            x += lineWidth + tickLength + tickLabelExtent.x + labelInsets.bottom + textExtent.y - 2;
            y -= textExtent.x / 2;
          }
          tr.translate( x, y );
          tr.rotate( rotation );
          tr.translate( -x, -y );

        }
        if( tr != null )
          gc.setTransform( tr );
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
  protected Point getTextExtent( final GCWrapper gc, final Number value, final FontData fd, final Format format )
  {
    String label = "";
    try
    {
      label = getLabel( value, format );
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
  public Number[] getGridTicks( final IAxis<Number> axis )
  {
    final Number[] ticks = getTicks( axis );
    return ticks;
  }

  /**
   * Gibt die Breite bzw. Tiefe einer Achse zur�ck. Dadurch wird die Gr�sse der AxisComponent bestimmt
   */
  public int getAxisWidth( final IAxis<Number> axis )
  {
    if( m_fixedWidth > 0 )
      return m_fixedWidth;

    int width = 0;
    int ticklabelWidth = 0;

    // Testutensilien erzeugen
    final Display dev = Display.getCurrent();
    final Image img = new Image( dev, 1, 1 );
    final GC gc = new GC( img );
    final GCWrapper gcw = new GCWrapper( gc );

    final Point tickLabelSize = calcTickLabelSize( gcw, axis );

    if( tickLabelSize == null )
      return 0;

    final Insets labelInsets = getLabelInsets();

    // TickLabels
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      ticklabelWidth = tickLabelSize.y;
      width += labelInsets.top;
      width += labelInsets.bottom;
    }
    else
    {
      width += labelInsets.bottom;
      width += labelInsets.top;
      ticklabelWidth = tickLabelSize.x;
    }
    width += ticklabelWidth;
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

}
