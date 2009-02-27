package de.belger.swtchart.axis;

import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.belger.swtchart.util.InsetsHelper;
import de.belger.swtchart.util.MathUtil;
import de.belger.swtchart.util.SwitchDelegate;

/**
 * @author gernot
 */
public class TickRenderer implements IAxisRenderer
{
  private final NumberFormat m_nf = NumberFormat.getInstance();

  protected final int m_tickLength;

  protected final Insets m_labelInsets;

  protected final int m_maxDigits;

  protected final int m_lineWidth;

  private final Insets m_tickLableInsets;

  private final Color m_foreground;

  private final Color m_gridcolor;

  private final boolean m_opposite;

  private final Color m_background;

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
   *          <li>bottom: distance between ticklabel and next ticklabel</li>
   *          </ul>
   * @param maxDigits
   * @param labelInsets
   * @param gridcolor
   *          if null, no grid will be drawn
   */
  public TickRenderer( final Color foreground, final Color background, final int lineWidth, final int tickLength, final Insets tickLabelInsets, final int maxDigits, final Insets labelInsets, final Color gridcolor, final boolean opposite )
  {
    m_foreground = foreground;
    m_background = background;
    m_tickLength = tickLength;
    m_lineWidth = lineWidth;
    m_tickLableInsets = tickLabelInsets;
    m_labelInsets = labelInsets;
    m_maxDigits = maxDigits;
    m_gridcolor = gridcolor;
    m_opposite = opposite;

    m_nf.setMinimumFractionDigits( maxDigits );
    m_nf.setMaximumFractionDigits( maxDigits );
  }

  private Point calcSize( final GCWrapper gc, final AxisRange axis )
  {
    final Point tickLabelSize = calcTicklableSize( gc, axis );
    final Point labelSize = calcLableSize( gc, axis );

    final SwitchDelegate switch1 = axis.getSwitch();

    switch( switch1 )
    {
      case HORIZONTAL:
        final int x = 0;
        // Eigentlich: tickLabelSize.x / 2;
        // hängt aber von den anderen Achsen ab
        // KIM
        // return new Point( x, tickLabelSize.y + labelSize.y + m_tickLength );
        return new Point( x + 20, tickLabelSize.y + labelSize.y + m_tickLength );
      // KIM
      case VERTICAL:
      default:
        final int y = 0;
        // siehe x: Eigentlich: tickLabelSize.y / 2;
        // KIM
        // return new Point( tickLabelSize.x + labelSize.x + m_tickLength, y );
        return new Point( tickLabelSize.x + labelSize.x + m_tickLength, y + 20 );
    // KIM
    }
  }

  public Rectangle reduceScreenSize( final GCWrapper gc, final AxisRange axis, final Rectangle screen )
  {
    final Point size = calcSize( gc, axis );

    final int w = screen.width - size.x;
    final int h = screen.height - size.y;

    final int x = m_opposite ? screen.x : screen.x + size.x;
    final int y = m_opposite ? screen.y : screen.y + size.y;

    return new Rectangle( x, y, w, h );
  }

  private Point calcTicklableSize( final GCWrapper gc, final AxisRange axis )
  {
    final double logicalfrom = axis.getLogicalFrom();
    final double logicalto = axis.getLogicalTo();
    final Point fromTextExtent = getTextExtent( gc, logicalfrom );
    final Point toTextExtent = getTextExtent( gc, logicalto );
    final Point tickLabelSize = new Point( Math.max( fromTextExtent.x, toTextExtent.x ), Math.max( fromTextExtent.y, toTextExtent.y ) );

    final Insets ihelper = getConvertedInsets( axis, m_tickLableInsets );

    final int x = tickLabelSize.x + ihelper.left + ihelper.right;
    final int y = tickLabelSize.y + ihelper.top + ihelper.bottom;

    return new Point( x, y );
  }

  private Point calcLableSize( final GCWrapper gc, final AxisRange axis )
  {
    final String lable = axis.getLable();
    if( lable == null )
      return new Point( 0, 0 );

    final Point textExtent = getTextExtent( gc, lable );

    final Insets ihelper = getConvertedInsets( axis, m_labelInsets );

    return new Point( textExtent.x + ihelper.left + ihelper.right, textExtent.y + ihelper.top + ihelper.bottom );
  }

  public void paint( final GCWrapper gc, final AxisRange axis, final Rectangle screen )
  {
    final SwitchDelegate crdSwitch = axis.getSwitch();

    // paint axis
    final Point topleft = new Point( screen.x, screen.y );
    final Point bottomright = new Point( screen.x + screen.width, screen.y + screen.height );

    final Point topleftS = new Point( crdSwitch.getX( topleft ), crdSwitch.getY( topleft ) );
    final Point bottomrightS = new Point( crdSwitch.getX( bottomright ), crdSwitch.getY( bottomright ) );

    // final Point axistl = new Point( axis.getScreenFrom(), m_opposite ? bottomrightS.y : topleftS.y );
    // final Point axisbr = new Point( axis.getScreenTo(), m_opposite ? bottomrightS.y : topleftS.y );
    // drawTickLine( gc, crdSwitch.getX( axistl ), crdSwitch.getY( axistl), crdSwitch.getX( axisbr ), crdSwitch.getY(
    // axisbr ) );

    // KIM
    final int gapSpace = axis.getGapSpace() * crdSwitch.toInt();
    final Point axistl = new Point( axis.getScreenFrom() + gapSpace, m_opposite ? bottomrightS.y : topleftS.y );
    final Point axisbr = new Point( axis.getScreenTo() - gapSpace, m_opposite ? bottomrightS.y : topleftS.y );

    drawTickLine( gc, crdSwitch.getX( axistl ), crdSwitch.getY( axistl ), crdSwitch.getX( axisbr ), crdSwitch.getY( axisbr ) );

    // KIM
    // paint ticks
    final Collection<Double> ticks = calcTicks( gc, axis );
    for( final Double value : ticks )
    {
      final double pos = value.doubleValue();
      drawTick( gc, axis, screen, pos );
    }

    // paint label
    final int screenmid = (axisbr.x + axistl.x) / 2;
    final Point tickLabelSize = calcTicklableSize( gc, axis );
    final Point lableSize = calcLableSize( gc, axis );

    if( axis.getLable() != null )
    {
      final int distance = m_opposite ? (m_tickLength + crdSwitch.getY( tickLabelSize ) + m_labelInsets.top)
          : (-m_tickLength - crdSwitch.getY( tickLabelSize ) - crdSwitch.getY( lableSize ) + m_labelInsets.bottom);
      final Point labelPos = new Point( screenmid, axistl.y + distance );
      drawString( gc, axis.getLable(), crdSwitch.getX( labelPos ), crdSwitch.getY( labelPos ) );
    }
  }

  private void drawTick( final GCWrapper gc, final AxisRange axis, final Rectangle screen, final double pos )
  {
    final SwitchDelegate crdSwitch = axis.getSwitch();

    final Point topleft = new Point( screen.x, screen.y );
    final Point bottomright = new Point( screen.x + screen.width, screen.y + screen.height );

    final Point topleftS = new Point( crdSwitch.getX( topleft ), crdSwitch.getY( topleft ) );
    final Point bottomrightS = new Point( crdSwitch.getX( bottomright ), crdSwitch.getY( bottomright ) );

    final int screenpos = (int) axis.logical2screen( pos );

    final Point ticktl = new Point( screenpos, m_opposite ? bottomrightS.y : topleftS.y );
    final Point tickbr = new Point( screenpos, m_opposite ? (bottomrightS.y + m_tickLength) : (topleftS.y - m_tickLength) );

    final Point gridtl = new Point( screenpos, m_opposite ? bottomrightS.y : topleftS.y );
    final Point gridbr = new Point( screenpos, m_opposite ? topleftS.y : bottomrightS.y );

    drawGridLine( gc, crdSwitch.getX( gridtl ), crdSwitch.getY( gridtl ), crdSwitch.getX( gridbr ), crdSwitch.getY( gridbr ) );

    drawTickLine( gc, crdSwitch.getX( ticktl ), crdSwitch.getY( ticktl ), crdSwitch.getX( tickbr ), crdSwitch.getY( tickbr ) );

    final String label = m_nf.format( pos );
    final Point tickExtent = getTextExtent( gc, label );

    final Insets preInsets = getConvertedInsets( axis, m_tickLableInsets );

    final Point insets = m_opposite ? new Point( -preInsets.left, -preInsets.bottom ) : new Point( preInsets.right, preInsets.top );

    final int tickLabelX = tickbr.x - crdSwitch.getX( tickExtent ) / 2;
    final int tickLabelY = tickbr.y - (m_opposite ? 0 : crdSwitch.getY( tickExtent )) - crdSwitch.getY( insets );

    final Point tickLabelPos = new Point( tickLabelX, tickLabelY );

    drawString( gc, label, crdSwitch.getX( tickLabelPos ), crdSwitch.getY( tickLabelPos ) );
  }

  private Insets getConvertedInsets( final AxisRange axis, final Insets insets )
  {
    InsetsHelper ihelper = new InsetsHelper( insets );

    if( axis.isInverted() )
      ihelper = ihelper.invert();
    if( m_opposite )
      ihelper = ihelper.opposite();
    if( axis.getSwitch() == SwitchDelegate.VERTICAL )
      ihelper = ihelper.hor2vert();

    return ihelper;
  }

  private Collection<Double> calcTicks( final GCWrapper gc, final AxisRange axis )
  {
    final SwitchDelegate crdSwitch = axis.getSwitch();

    final Point ticklabelSize = calcTicklableSize( gc, axis );

    // + 2 wegen Rundungsfehlern beim positionieren
    final int screenInterval = 2 + crdSwitch.getX( ticklabelSize );

    final double logicalInterval = MathUtil.round( axis.screenLength2Logical( screenInterval ), MathUtil.RoundMethod.UP );

    final int digits = Math.min( MathUtil.scale( logicalInterval ), m_maxDigits );

    final double ticklength = Math.abs( MathUtil.setScale( logicalInterval, digits, MathUtil.RoundMethod.UP ) );

    final Collection<Double> ticks = new LinkedList<Double>();

    if( ticklength > 0.0 )
    {
      for( double tickValue = MathUtil.setScale( axis.getLogicalFrom(), digits, MathUtil.RoundMethod.UP ); tickValue <= axis.getLogicalTo(); tickValue += ticklength )
        ticks.add( new Double( tickValue ) );
    }
  
    return ticks;
  }

  public void drawTickLine( final GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  {
    gc.setLineWidth( m_lineWidth );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.setForeground( m_foreground );

    gc.drawLine( x1, y1, x2, y2 );
  }

  public void drawGridLine( final GCWrapper gc, final int x1, final int y1, final int x2, final int y2 )
  {
    if( m_gridcolor != null )
    {
      gc.setLineWidth( 1 );
      gc.setLineStyle( SWT.LINE_DOT );
      gc.setForeground( m_gridcolor );

      gc.drawLine( x1, y1, x2, y2 );
    }
  }

  private void drawString( final GCWrapper gc, final String string, final int x, final int y )
  {
    gc.setForeground( m_foreground );
    gc.setBackground( m_background );
    gc.drawString( string, x, y );
  }

  private Point getTextExtent( final GCWrapper gc, final double value )
  {
    final String label = m_nf.format( value );
    return getTextExtent( gc, label );
  }

  private Point getTextExtent( final GCWrapper gc, final String value )
  {
    return gc.textExtent( value );
  }
}
