package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Canvas;

import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartDragHandler;

/**
 * @author kimwerner
 */
public abstract class AbstractChartHandler implements IChartDragHandler
{
  private final IChartComposite m_chart;

  private int m_cursor = -1;

  public AbstractChartHandler( final IChartComposite chart )
  {
    m_chart = chart;
  }

  public IChartComposite getChart( )
  {
    return m_chart;
  }

  /**
   * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
   */
  @Override
  public void keyPressed( final KeyEvent e )
  {
  }

  /**
   * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
   */
  @Override
  public void keyReleased( final KeyEvent e )
  {
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDoubleClick( final MouseEvent e )
  {
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDown( final MouseEvent e )
  {
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( final MouseEvent e )
  {
  }

  protected void setCursor( final int cursor )
  {
    if( cursor == m_cursor )
      return;

    m_cursor = cursor;

    final Canvas plot = getChart().getPlot();
    final Cursor swtCursor = cursor == -1 ? null : plot.getDisplay().getSystemCursor( cursor );
    plot.setCursor( swtCursor );
  }
}
