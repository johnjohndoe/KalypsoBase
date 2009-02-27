package de.belger.swtchart.layer;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.belger.swtchart.axis.AxisRange;

/**
 * @author gernot
 */
public abstract class AbstractChartLayer implements IChartLayer
{
  private final AxisRange m_domainRange;

  private final AxisRange m_valueRange;

  public AbstractChartLayer( final AxisRange domainRange, final AxisRange valueRange )
  {
    m_domainRange = domainRange;
    m_valueRange = valueRange;
  }

  /**
   * @see de.belger.swtchart.layer.IChartLayer#getDomainRange()
   */
  public final AxisRange getDomainRange( )
  {
    return m_domainRange;
  }

  /**
   * @see de.belger.swtchart.layer.IChartLayer#getValueRange()
   */
  public final AxisRange getValueRange( )
  {
    return m_valueRange;
  }

  public final Point logical2screen( final Point2D p2d )
  {
    return new Point( (int)m_domainRange.logical2screen( p2d.getX() ), (int)m_valueRange
        .logical2screen( p2d.getY() ) );
  }

  public final Point2D screen2logical( final Point p )
  {
    return new Point2D.Double( m_domainRange.screen2Logical( p.x ), m_valueRange
        .screen2Logical( p.y ) );
  }

  /**
   * @see de.belger.swtchart.layer.IChartLayer#logical2screen(java.awt.geom.Rectangle2D)
   */
  public final Rectangle logical2screen( final Rectangle2D r2d )
  {
    final double x1 = m_domainRange.logical2screen( r2d.getX() );
    final double y1 = m_valueRange.logical2screen( r2d.getY() );
    final double x2 = m_domainRange.logical2screen( r2d.getX() + r2d.getWidth() );
    final double y2 = m_valueRange.logical2screen( r2d.getY() + r2d.getHeight() );

    return new Rectangle( (int)x1, (int)y1, (int)(x2 - x1), (int)(y2 - y1) );
  }

  public boolean isNotPainting( )
  {
    return false;
  }
}