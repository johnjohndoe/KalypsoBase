package org.kalypso.chart.ext.base.axisrenderer;

import java.awt.Insets;
import java.text.Format;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.impl.util.InsetsHelper;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;

/**
 * @author burtscher
 */
public class GenericAxisRenderer extends AbstractGenericAxisRenderer
{

  private final ITickCalculator m_tickCalculator;

  private final ILabelCreator m_labelCreator;

  private final Number m_minTickInterval;

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
   */
  public GenericAxisRenderer( final String id, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, ILabelCreator labelCreator, ITickCalculator tickCalculator, Number minTickInterval, boolean hideCut )
  {
    super( id, tickLength, tickLabelInsets, labelInsets, gap );
    m_tickCalculator = tickCalculator;
    m_labelCreator = labelCreator;
    m_minTickInterval = minTickInterval;
    m_hideCut = hideCut;
  }

  public Point calcTickLabelSize( GC gc, final IAxis axis )
  {
    final IDataRange<Number> range = axis.getNumericRange();

    final String logicalfrom = m_labelCreator.getLabel( range.getMin(), range );
    final String logicalto = m_labelCreator.getLabel( range.getMax(), range );
    final Point fromTextExtent = getTextExtent( gc, logicalfrom, getStyles().getStyle( "tickLabel", ITextStyle.class ) );
    final Point toTextExtent = getTextExtent( gc, logicalto, getStyles().getStyle( "tickLabel", ITextStyle.class ) );
    final Point tickLabelSize = new Point( Math.max( fromTextExtent.x, toTextExtent.x ), Math.max( fromTextExtent.y, toTextExtent.y ) );

    final Insets ihelper = getConvertedInsets( axis, getTickLabelInsets() );

    final int x = tickLabelSize.x + ihelper.left + ihelper.right;
    final int y = tickLabelSize.y + ihelper.top + ihelper.bottom;

    return new Point( x, y );

  }

  /**
   * draws the Axis ticks into the given GC
   */
  private void drawTicks( final GC gc, final IAxis axis, final int startX, final int startY, Number[] ticks )
  {

    if( (gc == null) || (axis == null) || (ticks == null) )
    {
      return;
    }

    int textX = 0;
    int textY = 0;

    final int tickLength = getTickLength();
    final Insets tickLabelInsets = getTickLabelInsets();

    IDataRange<Number> range = axis.getNumericRange();
    final int axisMin = axis.numericToScreen( range.getMin() );
    final int axisMax = axis.numericToScreen( range.getMax() );
    final int screenMin = Math.min( axisMin, axisMax );
    final int screenMax = Math.max( axisMin, axisMax );

    ITextStyle tickLabelStyle = getTickLabelStyle();
    ILineStyle tickLineStyle = getTickLineStyle();

    for( final Number value : ticks )
    {
      int y1, y2, x1, x2, tickPos;
      String label;
      label = m_labelCreator.getLabel( value, range );

      boolean drawTick = true;

      tickPos = axis.numericToScreen( value );
      final Point tickExtent = getTextExtent( gc, label, tickLabelStyle );
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
        {
          drawTick = false;
        }
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
        {
          drawTick = false;
        }
      }

      if( drawTick )
      {
        drawText( gc, label, textX, textY, tickLabelStyle );
        tickLineStyle.apply( gc );
        gc.drawLine( x1, y1, x2, y2 );
      }
    }
  }

  protected void drawTicks2( GC gc, IAxis axis, int startX, int startY, Number[] ticks )
  {
    ITextStyle tickLabelStyle = getTickLabelStyle();
    ILineStyle tickLineStyle = getTickLineStyle();

    int textXDate = 0;
    int textYDate = 0;

    IDataRange<Number> range = axis.getNumericRange();

    if( axis.getPosition() == POSITION.BOTTOM )
    {
      final int y1 = startY;
      final int y2 = y1 + getTickLength();

      for( final Number value : ticks )
      {
        final int tickPos = axis.numericToScreen( value );
        final String labelDate = m_labelCreator.getLabel( value, range );
        final Point tickExtentDate = getTextExtent( gc, labelDate, tickLabelStyle );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textXDate = tickPos - tickExtentDate.x / 2;
        textYDate = y2 + getTickLabelInsets().top;
        drawText( gc, labelDate, textXDate, textYDate, tickLabelStyle );
      }
    }
    else if( axis.getPosition() == POSITION.TOP )
    {
      final int y1 = startY;
      final int y2 = y1 - getTickLength();

      for( final Number value : ticks )
      {
        final int tickPos = axis.numericToScreen( value );
        final String label = m_labelCreator.getLabel( value, range );
        final Point tickExtent = getTextExtent( gc, label, tickLabelStyle );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        textXDate = tickPos - tickExtent.x / 2;
        textYDate = y2 - getTickLabelInsets().top - tickExtent.y;
        drawText( gc, label, textXDate, textYDate, tickLabelStyle );
      }
    }
    else if( axis.getPosition() == POSITION.LEFT )
    {
      final int x1 = startX;
      final int x2 = x1 - getTickLength();

      for( final Number value : ticks )
      {
        final int tickPos = axis.numericToScreen( value );
        final String label = m_labelCreator.getLabel( value, range );
        final Point tickExtent = getTextExtent( gc, label, tickLabelStyle );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textXDate = x1 - tickExtent.x - getTickLabelInsets().right - getTickLength();
        textYDate = tickPos - tickExtent.y / 2;
        drawText( gc, label, textXDate, textYDate, tickLabelStyle );
      }
    }
    else if( axis.getPosition() == POSITION.RIGHT )
    {
      final int x1 = startX;
      final int x2 = x1 + getTickLength();

      for( final Number value : ticks )
      {
        final int tickPos = axis.numericToScreen( value );
        final String label = m_labelCreator.getLabel( value, range );
        final Point tickExtent = getTextExtent( gc, label, tickLabelStyle );

        gc.drawLine( x1, tickPos, x2, tickPos );
        textXDate = tickPos - x2 + getTickLabelInsets().top;
        textXDate = x1 + getTickLabelInsets().left + getTickLength();
        textYDate = tickPos - tickExtent.y / 2;
        drawText( gc, label, textXDate, textYDate, tickLabelStyle );
      }
    }

  }

  private Insets getConvertedInsets( final IAxis axis, final Insets insets )
  {
    // POSITION BOTTOM is the default order for the insets
    InsetsHelper ihelper = new InsetsHelper( insets );

    if( axis.getPosition() == POSITION.TOP )
    {
      ihelper = ihelper.mirrorTopBottom();
    }
    if( axis.getPosition() == POSITION.LEFT )
    {
      ihelper = ihelper.hor2vert();
    }
    if( axis.getPosition() == POSITION.RIGHT )
    {
      ihelper = ihelper.hor2vert().mirrorLeftRight();
    }

    return ihelper;
  }

  @Override
  protected Point getTextExtent( GC gc, final Number value, ITextStyle style, Format format, IDataRange<Number> range )
  {
    final String label = m_labelCreator.getLabel( value, range );
    final Point p = getTextExtent( gc, label, style );
    return p;
  }

  // protected int[] createAxisSegment( final IAxis<Number> axis, final Rectangle screen )
  // {
  // int startX;
  // int startY;
  // int endX;
  // int endY;
  //
  // final int gap = getGap();
  //
  // if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
  // {
  // startX = screen.x;
  // endX = screen.x + screen.width;
  //
  // if( axis.getPosition() == POSITION.BOTTOM )
  // startY = screen.y + gap + 1;
  // else
  // startY = screen.y + screen.height - gap - 1;
  // endY = startY;
  //
  // if( axis.getDirection() == DIRECTION.NEGATIVE )
  // {
  // final int tmp = startX;
  // startX = endX;
  // endX = tmp;
  // }
  // }
  // else
  // {
  // startY = screen.y;
  // endY = screen.y + screen.height;
  //
  // if( axis.getPosition() == POSITION.RIGHT )
  // startX = screen.x + gap;
  // else
  // startX = screen.x + screen.width - gap;
  // endX = startX;
  //
  // if( axis.getDirection() == DIRECTION.POSITIVE )
  // {
  // final int tmp = startY;
  // startY = endY;
  // endY = tmp;
  // }
  // }
  //
  // return new int[] { startX - 1, startY, endX - 1, endY };
  // }

  /**
   * @return Array of 4 int-Values: startX, startY, endX, endY - where startX/Y are the start coordinates for the axis
   *         line and endX/Y its end coordinates within the given Rectangle
   */
  private int[] createAxisSegment( final IAxis axis, final Rectangle screen )
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
      {
        startY = screen.y + gap + 1;
      }
      else
      {
        startY = screen.y + screen.height - 1 - gap;
      }
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
      {
        startX = screen.x + gap + 1;
      }
      else
      {
        startX = screen.x + screen.width - 1 - gap;
      }
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

  public void paint( GC gc, final IAxis axis, final Rectangle screen )
  {
    final Device dev = gc.getDevice();

    gc.setBackground( gc.getDevice().getSystemColor( SWT.COLOR_GRAY ) );
    // gc.fillRectangle( screen );

    // draw axis line
    final int[] coords = createAxisSegment( axis, screen );
    assert coords != null && coords.length == 4;
    drawAxisLine( gc, coords[0], coords[1], coords[2], coords[3] );

    Number[] ticks = getTicks( axis, gc );
    drawTicks( gc, axis, coords[0], coords[1], ticks );
    drawAxisLabel( gc, axis, coords[0], coords[1], coords[2], coords[3] );

  }

  protected void drawAxisLabel( GC gc, IAxis axis, int startX, int startY, int endX, int endY )
  {
    if( axis.getLabel() != null )
    {
      final Point textExtent = getTextExtent( gc, axis.getLabel(), getLabelStyle() );

      final Point tickLabelExtent = calcTickLabelSize( gc, axis );

      int x = 0;
      int y = 0;

      final Insets labelInsets = getLabelInsets();
      final int tickLength = getTickLength();
      final int lineWidth = getLineStyle().getWidth();

      final Transform tr = new Transform( gc.getDevice() );
      try
      {
        if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          x = Math.abs( endX - startX ) / 2 - textExtent.x / 2;
          y = startY;

          if( axis.getPosition() == POSITION.TOP )
          {
            y -= tickLength + lineWidth + tickLabelExtent.y + labelInsets.top + textExtent.y;
          }
          else
          {
            y += tickLength + lineWidth + tickLabelExtent.y + labelInsets.bottom;
          }
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
        drawText( gc, axis.getLabel(), x, y, getLabelStyle() );
      }
      finally
      {
        if( tr != null )
        {
          tr.dispose();
        }
      }
    }

  }

  protected void drawAxisLine( GC gc, final int x1, final int y1, final int x2, final int y2 )
  {
    getLineStyle().apply( gc );
    gc.drawLine( x1, y1, x2, y2 );
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer#getTicks(org.kalypso.chart.framework.model.mapper.IAxis)
   */
  public Number[] getTicks( IAxis axis )
  {
    Number[] tickMapElement = getTickMapElement( axis );
    // wenn null, dann muss es neu erzeugt werden
    if( tickMapElement == null )
    {
      Image img = new Image( Display.getDefault(), 1, 1 );
      GC gc = new GC( img );

      getTicks( axis, gc );

      gc.dispose();
      img.dispose();
      tickMapElement = getTickMapElement( axis );
    }
    return tickMapElement;
  }

  public Number[] getTicks( IAxis axis, GC gc )
  {
    Number[] tickMapElement = getTickMapElement( axis );
    // wenn null, dann muss es neu erzeugt werden
    if( tickMapElement == null )
    {
      Point tickLabelSize = calcTickLabelSize( gc, axis );
      Number[] ticks = m_tickCalculator.calcTicks( gc, axis, m_minTickInterval, tickLabelSize );
      setTickMapElement( axis, ticks );
      tickMapElement = getTickMapElement( axis );
    }
    return tickMapElement;
  }

  /**
   * Gibt die Breite bzw. Tiefe einer Achse zurück. Dadurch wird die Grösse der AxisComponent bestimmt
   */
  public int getAxisWidth( IAxis axis )
  {
    int width = 0;
    int label = 0;
    final int gap = getGap();
    final int lineWidth = getLineStyle().getWidth();
    final int tickLength = getTickLength();

    final Insets labelInsets = getLabelInsets();

    // Testutensilien erzeugen
    final Display dev = Display.getCurrent();
    final Image img = new Image( dev, 1, 1 );
    final GC gc = new GC( img );

    final Point tls = calcTickLabelSize( gc, axis );

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
    final Point labelExtent = getTextExtent( gc, axis.getLabel(), getLabelStyle() );
    width += labelExtent.y;

    gc.dispose();
    img.dispose();

    return width;
  }

  private Format getFormat( IDataRange<Number> range )
  {
    // TODO: implementiere mich
    return null;
  }

}
