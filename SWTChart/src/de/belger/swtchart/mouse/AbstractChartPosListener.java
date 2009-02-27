package de.belger.swtchart.mouse;

import java.awt.geom.Point2D;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;

import de.belger.swtchart.axis.AxisRange;

/**
 * Keeps track of Mouse-Position of Chart in logical Koordinates
 * 
 * @author gernot
 */
public abstract class AbstractChartPosListener implements MouseMoveListener, IChartPosListener
{
  private final AxisRange m_domainRange;
  private final AxisRange m_valueRange;

  public AbstractChartPosListener( final AxisRange domainRange, final AxisRange valueRange )
  {
    m_domainRange = domainRange;
    m_valueRange = valueRange;
  }
  
  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public final void mouseMove( final MouseEvent e )
  {
    final double logx = m_domainRange.screen2Logical( e.x );
    final double logy = m_valueRange.screen2Logical( e.y );
    
    final boolean inRange = m_domainRange.isInScreen( e.x ) && m_valueRange.isInScreen( e.y );
    
    final Point2D logpoint = new Point2D.Double( logx, logy );
    
    onPosChanged( logpoint, inRange );
  }
}
