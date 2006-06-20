package org.kalypso.swtchart.axis.renderer;

import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.kalypso.commons.java.lang.MathUtils;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.axis.IAxis;
import org.kalypso.swtchart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.axis.IAxisConstants.ORIENTATION;
import org.kalypso.swtchart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.util.InsetsHelper;

/**
 * @author schlienger
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

  private final int m_gap;

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
  public NumberAxisRenderer( final Color foreground, final Color background, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final int maxDigits, final Insets labelInsets, final int gap )
  {
    m_foreground = foreground;
    m_background = background;
    m_tickLength = tickLength;
    m_lineWidth = lineWidth;
    m_tickLabelInsets = tickLabelInsets;
    m_labelInsets = labelInsets;
    m_maxDigits = maxDigits;
    m_gap = gap;

    m_nf.setMinimumFractionDigits( maxDigits );
    m_nf.setMaximumFractionDigits( maxDigits );
  }

  private Point calcTickLabelSize( final GCWrapper gc, final IAxis<Number> axis )
  {
    final double logicalfrom = axis.getFrom().doubleValue();
    final double logicalto = axis.getTo().doubleValue();
    final Point fromTextExtent = getTextExtent( gc, logicalfrom );
    final Point toTextExtent = getTextExtent( gc, logicalto );
    final Point tickLabelSize = new Point( Math.max( fromTextExtent.x, toTextExtent.x ), Math.max( fromTextExtent.y, toTextExtent.y ) );

    final Insets ihelper = getConvertedInsets( axis, m_tickLabelInsets );

    final int x = tickLabelSize.x + ihelper.left + ihelper.right;
    final int y = tickLabelSize.y + ihelper.top + ihelper.bottom;

    return new Point( x, y );
  }

  public void paint( final GCWrapper gc, final Device dev, final IAxis<Number> axis, final Rectangle screen )
  {
    // paint axis line
    int[] coords = createAxisSegment( axis, screen );
    assert coords != null && coords.length == 4;
    drawAxisLine( gc, coords[0], coords[1], coords[2], coords[3] );

    final Collection<Number> ticks = calcTicks( gc, axis );
    drawTicks( gc, axis, coords[0], coords[1], coords[2], coords[3], ticks );

    drawAxisLabel( gc, dev, axis, coords[0], coords[1], coords[2], coords[3] );
  }

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
        startY = screen.y + screen.height - m_gap;
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

  private void drawTicks( GCWrapper gc, IAxis<Number> axis, int startX, int startY, int endX, int endY, Collection<Number> ticks )
  {
    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.setForeground( m_foreground );

    if( axis.getPosition() == POSITION.BOTTOM )
    {
      int y1 = startY;
      int y2 = y1 + m_tickLength;

      for( final Number value : ticks )
      {
        final int tickPos = axis.logicalToScreen( value );
        final String label = m_nf.format( value );
        final Point tickExtent = gc.textExtent( label );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        gc.drawText( label, tickPos - tickExtent.x / 2, y2 + m_tickLabelInsets.top );
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
        final Point tickExtent = gc.textExtent( label );

        gc.drawLine( tickPos, y1, tickPos, y2 );
        gc.drawText( label, tickPos - tickExtent.x / 2, y2 - m_tickLabelInsets.top - tickExtent.y );
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
        final Point tickExtent = gc.textExtent( label );

        gc.drawLine( x1, tickPos, x2, tickPos );
        gc.drawText( label, x2 - tickExtent.x - m_tickLabelInsets.top, tickPos - tickExtent.y / 2 );
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
        final Point tickExtent = gc.textExtent( label );

        gc.drawLine( x1, tickPos, x2, tickPos );
        gc.drawText( label, x2 + m_tickLabelInsets.top, tickPos - tickExtent.y / 2 );
      }
    }
  }

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

  private Collection<Number> calcTicks( final GCWrapper gc, final IAxis<Number> axis )
  {
    final Point ticklabelSize = calcTickLabelSize( gc, axis );

    // + 2 wegen Rundungsfehlern beim positionieren
    final int screenInterval = 2 + ticklabelSize.x;

    double logicalInterval = Math.abs( axis.screenToLogical( screenInterval * 2 ).doubleValue() - axis.screenToLogical( screenInterval ).doubleValue() );
    logicalInterval = MathUtils.round( logicalInterval, MathUtils.RoundMethod.UP );
    System.out.println( "Logical Interval= " + logicalInterval );

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

  private void drawAxisLine( final GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  {
    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.setForeground( m_foreground );

    gc.drawLine( x1, y1, x2, y2 );
  }

  private void drawAxisLabel( GCWrapper gc, Device dev, IAxis<Number> axis, int x1, int y1, int x2, int y2 )
  {
    gc.setForeground( m_foreground );
    // gc.setBackground( m_background );

    if( axis.getLabel() != null )
    {
      final Point textExtent = gc.textExtent( axis.getLabel() );

      int x = 0;
      int y = 0;

      Transform tr = null;
      try
      {
        if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          x = Math.abs( x2 - x1 ) / 2 - textExtent.x / 2;
          y = y1;

          if( axis.getPosition() == POSITION.TOP )
            y -= m_tickLength + m_lineWidth + m_tickLabelInsets.top + m_tickLabelInsets.bottom + m_labelInsets.top + textExtent.y;
          else
            y += m_tickLength + m_lineWidth + m_tickLabelInsets.top + m_tickLabelInsets.bottom + m_labelInsets.top + textExtent.y;
        }
        else
        {
          x = x1;
          y = Math.abs( y2 - y1 ) / 2;

          tr = new Transform( dev );
          if( axis.getPosition() == POSITION.LEFT )
          {
            tr.rotate( -90 );
            x -= m_labelInsets.top + textExtent.y;
            y += textExtent.x / 2;
          }
          else
          {
            tr.rotate( 90 );
            x += m_labelInsets.top + textExtent.y;
            y -= textExtent.x / 2;
          }
        }

        if( tr != null )
        {
          gc.setTransform( tr );

          System.out.println( "Using Transform: " + tr );
        }

        System.out.println( "Axis Label: X=" + x + " Y=" + y + " for " + axis );

        gc.drawText( axis.getLabel(), x, y );
      }
      finally
      {
        if( tr != null )
          tr.dispose();
      }
    }
  }

  // public void drawGridLine( final GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  // {
  // if( m_gridcolor != null )
  // {
  // gc.setLineWidth( 1 );
  // gc.setLineStyle( SWT.LINE_DOT );
  // gc.setForeground( m_gridcolor );
  //
  // gc.drawLine( x1, y1, x2, y2 );
  // }
  // }

  private Point getTextExtent( final GCWrapper gc, final double value )
  {
    final String label = m_nf.format( value );
    return gc.textExtent( label );
  }
}
