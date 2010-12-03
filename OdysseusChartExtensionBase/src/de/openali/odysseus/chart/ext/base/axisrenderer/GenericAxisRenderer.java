package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.awt.Insets;
import java.text.Format;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.InsetsHelper;

/**
 * @author burtscher
 */
public class GenericAxisRenderer extends AbstractGenericAxisRenderer
{

  private ITickCalculator m_tickCalculator;

  private ILabelCreator m_labelCreator;

  private Number m_minTickInterval;

  private boolean m_hideCut;

  private int m_fixedWidth;

  private ALIGNMENT m_labelPosition;

  public GenericAxisRenderer( final String id, final ILabelCreator labelCreator, final ITickCalculator tickCalculator, final AxisRendererConfig config )
  {
    this( id, config.tickLength, config.tickLabelInsets, config.labelInsets, config.gap, labelCreator, tickCalculator, config.minTickInterval, config.hideCut, config.fixedWidth, config.axisLineStyle, config.labelStyle, config.tickLineStyle, config.tickLabelStyle, config.labelPosition );
  }

  public GenericAxisRenderer( final String id, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final ILabelCreator labelCreator, final ITickCalculator tickCalculator, final Number minTickInterval, final boolean hideCut, final int fixedWidth, final ILineStyle axisLineStyle, final ITextStyle labelStyle, final ILineStyle tickLineStyle, final ITextStyle tickLabelStyle, final ALIGNMENT labelPosition )
  {
    this( id, tickLength, tickLabelInsets, labelInsets, gap, labelCreator, tickCalculator, minTickInterval, hideCut, fixedWidth, axisLineStyle, labelStyle, tickLineStyle, tickLabelStyle, 0, labelPosition );
  }

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
   * @param fixedWidth
   *          if > 0, no actual width is calculated - the getWidth() will always return the value of fixed width
   */
  public GenericAxisRenderer( final String id, final int tickLength, final Insets tickLabelInsets, final Insets labelInsets, final int gap, final ILabelCreator labelCreator, final ITickCalculator tickCalculator, final Number minTickInterval, final boolean hideCut, final int fixedWidth, final ILineStyle axisLineStyle, final ITextStyle labelStyle, final ILineStyle tickLineStyle, final ITextStyle tickLabelStyle, final int borderSize, final ALIGNMENT labelPosition )
  {
    super( id, tickLength, tickLabelInsets, labelInsets, gap, axisLineStyle, labelStyle, tickLineStyle, tickLabelStyle, borderSize );
    setTickCalculator( tickCalculator );
    setLabelCreator( labelCreator );
    setMinTickInterval( minTickInterval );
    setHideCut( hideCut );
    setFixedWidth( fixedWidth );
    setLabelPosition( labelPosition );
  }

  public Point calcTickLabelSize( final GC gc, final IAxis axis )
  {
    final IDataRange<Number> range = axis.getNumericRange();
    if( range.getMin() == null || range.getMax() == null )
      return new Point( 0, 0 );
    final String logicalfrom = getLabelCreator().getLabel( range.getMin(), range );
    final String logicalto = getLabelCreator().getLabel( range.getMax(), range );
    final Point fromTextExtent = getTextExtent( gc, logicalfrom, getTickLabelStyle() );
    final Point toTextExtent = getTextExtent( gc, logicalto, getTickLabelStyle() );
    final Point tickLabelSize = new Point( Math.max( fromTextExtent.x, toTextExtent.x ), Math.max( fromTextExtent.y, toTextExtent.y ) );

    final Insets ihelper = getConvertedInsets( axis, getTickLabelInsets() );

    final int x = tickLabelSize.x + ihelper.left + ihelper.right;
    final int y = tickLabelSize.y + ihelper.top + ihelper.bottom;

    return new Point( x, y );

  }

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

  protected void drawAxisLabel( final GC gc, final IAxis axis, final int startX, final int startY, final int endX, final int endY, final int offset )
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
          x = Math.abs( endX - startX ) / 2 - textExtent.x / 2 + offset;
          y = startY;

          if( axis.getPosition() == POSITION.TOP )
            y -= tickLength + lineWidth + tickLabelExtent.y + labelInsets.top + textExtent.y;
          else
            y += tickLength + lineWidth + tickLabelExtent.y + labelInsets.bottom;
        }
        else
        {
          x = startX;
          y = Math.abs( endY - startY ) / 2 + offset;

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
          gc.setTransform( tr );

        getLabelStyle().apply( gc );
        drawText( gc, axis.getLabel(), x, y, getLabelStyle() );
      }
      finally
      {
        if( tr != null )
          tr.dispose();

        gc.setTransform( null );
      }
    }

  }

  protected void drawAxisLine( final GC gc, final int x1, final int y1, final int x2, final int y2 )
  {
    getLineStyle().apply( gc );
    gc.drawLine( x1, y1, x2, y2 );
  }

  /**
   * draws the Axis ticks into the given GC
   */
  private void drawTicks( final GC gc, final IAxis axis, final int startX, final int startY, final Number[] ticks, final int offset )
  {

    if( (gc == null) || (axis == null) || (ticks == null) )
      return;

    final int tickLength = getTickLength();
    final Insets tickLabelInsets = getTickLabelInsets();

    final IDataRange<Number> range = axis.getNumericRange();
    if( range.getMin() == null || range.getMax() == null )
      return;
    final double numericMin = range.getMin().doubleValue();
    final double numericMax = range.getMax().doubleValue();
    final int axisMin = axis.numericToScreen( numericMin );
    final int axisMax = axis.numericToScreen( numericMax );
    final int screenMin = Math.min( axisMin, axisMax );
    final int screenMax = Math.max( axisMin, axisMax );

    final ITextStyle tickLabelStyle = getTickLabelStyle();
    final ILineStyle tickLineStyle = getTickLineStyle();
    final int tickScreenDistance = (screenMax - screenMin) / (ticks.length - 1);

    for( int i = 0; i < ticks.length; i++ )
    {
      final int y1, y2, x1, x2, tickPos;

      final int textX;
      final int textY;

      final String label = getLabelCreator().getLabel( ticks, i, range );

      boolean drawTick = true;

      tickPos = axis.numericToScreen( ticks[i] );

// if( i < ticks.length - 1 )
// tickScreenDistance = axis.numericToScreen( ticks[i + 1] ) - tickPos;
      final Point labelSize = getTextExtent( gc, label, tickLabelStyle );
      // HORIZONTAL
      if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      {
        x1 = tickPos + offset;
        x2 = x1;
        y1 = startY;
        // textX = tickPos- labelSize.x / 2 + offset;
        textX = tickPos - getLabelPosition( labelSize.x, tickScreenDistance, m_labelPosition ) + offset;
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
          textY = y2 - tickLabelInsets.top - labelSize.y;
        }
        // Nicht zeichnen, wenn 1. Text abgeschnitten & hideCut angegeben oder 2. Tick ausserhalb der AxisRange
        if( (m_hideCut && ((textX < screenMin) || ((textX + labelSize.x) > screenMax))) || ((x1 < screenMin + offset) || (x1 > screenMax + offset)) )
          drawTick = false;
      }
      // VERTICAL
      else
      {
        x1 = startX;
        y1 = tickPos + offset;
        y2 = y1;
        // textY = tickPos - labelSize.y / 2 + offset;
        textY = y1 - getLabelPosition( labelSize.y, tickScreenDistance, m_labelPosition );

        // LEFT
        if( axis.getPosition() == POSITION.LEFT )
        {
          x2 = x1 - tickLength;
          textX = x2 - labelSize.x - tickLabelInsets.top;
        }
        // RIGHT
        else
        {
          x2 = x1 + tickLength;
          textX = x2 + tickLabelInsets.top;
        }
        // Nicht zeichnen, wenn 1. Text abgeschnitten & hideCut angegeben oder 2. Tick ausserhalb der AxisRange
        if( (m_hideCut && ((textY < screenMin) || ((textY + labelSize.y) > screenMax))) || ((y1 < screenMin + offset) || (y1 > screenMax + offset)) )
          drawTick = false;
      }

      tickLineStyle.apply( gc );
      gc.drawLine( x1, y1, x2, y2 );
      if( drawTick )
      {
        tickLabelStyle.apply( gc );
        drawText( gc, label, textX, textY, tickLabelStyle );
      }
    }
  }

  /**
   * Gibt die Breite bzw. Tiefe einer Achse zurück. Dadurch wird die Grösse der AxisComponent bestimmt
   */
  @Override
  public int getAxisWidth( final IAxis axis )
  {
    if( !axis.isVisible() )
      return 0;

    // if width is fixed, return fixed width
    if( getFixedWidth() > 0 )
      return getFixedWidth();

    // Else: Calculate
    // check nullValue first
    final boolean labelEmpty = axis.getLabel() == null ? true : axis.getLabel().trim().equals( "" );

    int width = 0;
    final int gap = getGap();
    final int lineWidth = getLineStyle().getWidth();
    final int tickLength = getTickLength();

    final Insets labelInsets = getLabelInsets();

    // Testutensilien erzeugen

    final Display dev = PlatformUI.getWorkbench().getDisplay();
// final Display dev = Display.getCurrent();
    final Image img = new Image( dev, 1, 1 );
    final GC gc = new GC( img );

    final Point tls = calcTickLabelSize( gc, axis );

    // Ticks+Labels
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      width += tls.y;
      if( !labelEmpty )
        width += labelInsets.top + labelInsets.bottom;
    }
    else
    {
      width += tls.x;
      if( !labelEmpty )
        width += labelInsets.left + labelInsets.top;
    }

    width += gap;
    width += lineWidth;
    width += tickLength;

    // Label: nur mit einrechnen, wenn nicht leer
    if( !labelEmpty )
      width += getTextExtent( gc, axis.getLabel(), getLabelStyle() ).y;

    gc.dispose();
    img.dispose();

    return width;
  }

  private Insets getConvertedInsets( final IAxis axis, final Insets insets )
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

  public int getFixedWidth( )
  {
    return m_fixedWidth;
  }

  public ILabelCreator getLabelCreator( )
  {
    return m_labelCreator;
  }

  public ALIGNMENT getLabelPosition( )
  {
    return m_labelPosition;
  }

  final int getLabelPosition( final int labelWidth, final int tickScreenDistance, final ALIGNMENT labelPosition )
  {
    switch( labelPosition )
    {
      case LEFT:
        return labelWidth;
      case RIGHT:
        return 0;
      case CENTERED_HORIZONTAL:
        return labelWidth / 2;
      case INTERVALL_CENTERED:
        return (labelWidth - tickScreenDistance) / 2;
      case TICK_CENTERED:
        return labelWidth / 2;
    }
    throw new IllegalArgumentException( labelPosition.name() );
  }

  public Number getMinTickInterval( )
  {
    return m_minTickInterval;
  }

  @Override
  protected Point getTextExtent( final GC gc, final Number value, final ITextStyle style, final Format format, final IDataRange<Number> range )
  {
    final String label = getLabelCreator().getLabel( value, range );
    final Point p = getTextExtent( gc, label, style );
    return p;
  }

  public ITickCalculator getTickCalculator( )
  {
    return m_tickCalculator;
  }

  @Override
  public Number[] getTicks( final IAxis axis, final GC gc )
  {
    final Point tickLabelSize = calcTickLabelSize( gc, axis );
    final Number[] ticks = getTickCalculator().calcTicks( gc, axis, getMinTickInterval(), tickLabelSize );
    return ticks;
  }

  public boolean isHideCut( )
  {
    return m_hideCut;
  }

  @Override
  public void paint( final GC gc, final IAxis axis, final Rectangle screen )
  {
    if( (screen.width > 0) && (screen.height > 0) && axis.isVisible() )
    {

      gc.setBackground( gc.getDevice().getSystemColor( SWT.COLOR_GRAY ) );
      // gc.fillRectangle( screen );

      // draw axis line
      final int[] coords = createAxisSegment( axis, screen );
      assert (coords != null) && (coords.length == 4);
      drawAxisLine( gc, coords[0], coords[1], coords[2], coords[3] );

      int offset = 0;
      if( axis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
      {
        if( axis.getDirection().equals( DIRECTION.POSITIVE ) )
          offset = coords[0];
        else
          offset = coords[2];
      }
      else if( axis.getDirection().equals( DIRECTION.POSITIVE ) )
        offset = coords[3];
      else
        offset = coords[1];

      final Number[] ticks = getTicks( axis, gc );

      drawTicks( gc, axis, coords[0], coords[1], ticks, offset );
      drawAxisLabel( gc, axis, coords[0], coords[1], coords[2], coords[3], offset );
    }

  }

  public void setFixedWidth( final int fixedWidth )
  {
    m_fixedWidth = fixedWidth;
  }

  public void setHideCut( final boolean hideCut )
  {
    m_hideCut = hideCut;
  }

  public void setLabelCreator( final ILabelCreator labelCreator )
  {
    m_labelCreator = labelCreator;
  }

  public void setLabelPosition( final ALIGNMENT labelPosition )
  {
    m_labelPosition = labelPosition;
  }

  public void setMinTickInterval( final Number minTickInterval )
  {
    m_minTickInterval = minTickInterval;
  }

  public void setTickCalculator( final ITickCalculator tickCalculator )
  {
    m_tickCalculator = tickCalculator;
  }
}
