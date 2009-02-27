package de.belger.swtchart.mouse;

import java.awt.geom.Point2D;

/**
 * @author gernot
 *
 */
public interface IChartPosListener
{
  public void onPosChanged( final Point2D logpoint, final boolean inScreen );
}
