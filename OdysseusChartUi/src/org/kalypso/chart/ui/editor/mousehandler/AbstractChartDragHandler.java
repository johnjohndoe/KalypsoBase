package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.view.IChartDragHandler;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author kimwerner
 */
public abstract class AbstractChartDragHandler implements IChartDragHandler
{
  public ChartComposite getChart( )
  {
    return m_chart;
  }

  private final ChartComposite m_chart;

  private EditInfo m_editInfo = null;

  private final int m_trashHold;

  private EditInfo m_clickInfo = null;

  private int m_deltaSnapX = 0;

  private int m_deltaSnapY = 0;

  private int m_startX = 0;

  private int m_startY = 0;

  private Cursor m_cursor = null;

  public AbstractChartDragHandler( final ChartComposite chart )
  {
    this( chart, 5 );
  }

  public AbstractChartDragHandler( final ChartComposite chart, final int trashhold )
  {
    m_chart = chart;
    m_trashHold = trashhold;
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
    m_clickInfo = m_chart.getChartInfo();
    if( m_clickInfo == null )
      m_clickInfo = new EditInfo( null, null, null, null, null, new Point( e.x, e.y ) );

    m_deltaSnapX = e.x - m_clickInfo.m_pos.x;
    m_deltaSnapY = e.y - m_clickInfo.m_pos.y;
    m_startX = e.x;
    m_startY = e.y;
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( final MouseEvent e )
  {
    try
    {
      if( m_editInfo != null )
        doMouseUpAction( new Point( e.x - m_deltaSnapX, e.y - m_deltaSnapY ), m_editInfo );
      else if( m_clickInfo != null )
        doMouseUpAction( null, m_clickInfo );
    }
    catch( Error err )
    {
      err.printStackTrace();
    }
    finally
    {
      m_clickInfo = null;
      m_editInfo = null;
      m_chart.setChartInfo( null );
    }
  }

  private final void setCursor( final MouseEvent e )
  {
    final Cursor cursor = getCursor(e);
    if( cursor == null )
      return;
    if( e.getSource() instanceof Control )
    {

      if( cursor == m_cursor )
        return;
      m_cursor = cursor;
      ((Control) e.getSource()).setCursor( cursor );
    }
  }

  abstract public void doMouseUpAction( final Point start, final EditInfo editInfo );

  abstract public void doMouseMoveAction( final Point start, final EditInfo editInfo );

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {
    setCursor( e );
    if( m_clickInfo == null )
      return;
    if( (m_editInfo == null) && ((Math.abs( e.x - m_startX ) > m_trashHold) || (Math.abs( e.y - m_startY ) > m_trashHold)) )
      m_editInfo = new EditInfo( m_clickInfo );

    doMouseMoveAction( new Point( e.x - m_deltaSnapX, e.y - m_deltaSnapY ), m_editInfo == null ? m_clickInfo : m_editInfo );

  }
}
