package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;

import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartDragHandler;

/**
 * @author kimwerner
 */
public abstract class AbstractChartHandler implements IChartDragHandler
{
  private final IChartComposite m_chart;

  private Cursor m_cursor = null;

  private final int m_swtCursor;

  public AbstractChartHandler( final IChartComposite chart, final int cursor )
  {
    m_chart = chart;
    m_swtCursor = cursor;
  }

  public IChartComposite getChart( )
  {
    return m_chart;
  }

  public Cursor getCursor( final MouseEvent e )
  {
    return e.display.getSystemCursor( m_swtCursor );
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
    setCursor( e );
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( final MouseEvent e )
  {
  }

  private final void setCursor( final MouseEvent e )
  {
    final Cursor cursor = getCursor( e );
    if( cursor == null )
      return;

    // FIXME: why not set cursor on m_chart.getPlot() ?

    if( e.getSource() instanceof Control )
    {
      if( cursor == m_cursor )
        return;

      m_cursor = cursor;
      ((Control) e.getSource()).setCursor( cursor );
    }
  }
}
