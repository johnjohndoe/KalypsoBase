package de.belger.swtchart.mouse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;

import de.belger.swtchart.ChartCanvas;
import de.belger.swtchart.action.IChartDragAction;

/**
 * Registers itself upon creation and deregisters at disposal.
 * 
 * @author gernot
 */
public class DragHandler implements MouseListener, MouseMoveListener
{
  private final ChartCanvas m_chart;

  private Point m_startPos;

  private IChartDragAction m_action;

  public DragHandler( final ChartCanvas chart )
  {
    m_chart = chart;
    
    chart.addMouseListener( this );
    chart.addMouseMoveListener( this );
  }
  
  public void dispose( )
  {
    if( !m_chart.isDisposed() )
    {
      m_chart.removeMouseListener( this );
      m_chart.removeMouseMoveListener( this );
    }
  }
  
  public void setDragAction( final IChartDragAction action )
  {
    if( m_startPos != null )
      stopDragging( m_startPos.x, m_startPos.y );
    
   m_action = action;
   final int cursortype = m_action == null ? SWT.CURSOR_ARROW : m_action.getCursorType();
   m_chart.setCursor( m_chart.getDisplay().getSystemCursor( cursortype ) );
  }
  
  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDoubleClick( final MouseEvent e )
  {
    // ignore
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDown( final MouseEvent e )
  {
    if( m_chart.isHovering() || m_chart.isEditing() )
      return;
    
    if( e.button == 1 && m_startPos == null )
    {
      m_startPos = new Point( e.x, e.y );
      m_chart.setCapture( true );
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( final MouseEvent e )
  {
    if( e.button == 1 && m_startPos != null )
      stopDragging( e.x, e.y );
  }

  private void stopDragging( final int x, final int y )
  {
    m_chart.setDragArea( null );
    m_chart.setCapture( false );

    if( m_action != null )
      m_action.dragFinished( m_startPos, new Point( x, y ) );

    m_startPos = null;
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( final MouseEvent e )
  {
    if( m_startPos != null && m_action != null )
      m_action.dragTo( m_startPos, new Point( e.x, e.y ) );
    else
      // to prevent holding the capture
      m_chart.setCapture( false );
  }
}
