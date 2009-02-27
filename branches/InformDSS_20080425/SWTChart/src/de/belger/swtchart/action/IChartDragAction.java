package de.belger.swtchart.action;

import org.eclipse.swt.graphics.Point;

/**
 * @author gernot
 *
 */
public interface IChartDragAction
{
  public void dragFinished( final Point start, final Point stop );

  public void dragTo( final Point start, final Point stop );

  /** One of SWT.CURSOR_ constants */
  public int getCursorType( );
}
