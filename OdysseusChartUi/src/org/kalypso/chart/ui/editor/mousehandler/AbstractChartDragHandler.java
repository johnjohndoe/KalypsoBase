package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartDragHandler;

/**
 * @author kimwerner
 */
public abstract class AbstractChartDragHandler implements IChartDragHandler
{
  public IChartComposite getChart( )
  {
    return m_chart;
  }

  private final IChartComposite m_chart;

  private EditInfo m_editInfo = null;

  private final int m_trashOld;

  private EditInfo m_clickInfo = null;

  private int m_deltaSnapX = 0;

  private int m_deltaSnapY = 0;

  private int m_startX = 0;

  private int m_startY = 0;

  private Cursor m_cursor = null;

  public AbstractChartDragHandler( final IChartComposite chart )
  {
    this( chart, 5 );
  }

  public AbstractChartDragHandler( final IChartComposite chart, final int trashold )
  {
    m_chart = chart;
    m_trashOld = trashold;
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
    final Point screen = new Point( e.x, e.y );
    m_clickInfo = getHover( screen );
    if( m_clickInfo == null )
      m_clickInfo = new EditInfo( null, null, null, null, null, getChart().screen2plotPoint( screen ) );

    // offset from cursor relative to the given InfoObject Center
    final Point pos = getChart().plotPoint2screen( m_clickInfo.m_pos );
    m_deltaSnapX = e.x - pos.x;
    m_deltaSnapY = e.y - pos.y;
    m_startX = e.x;
    m_startY = e.y;
  }

  final protected EditInfo getHover( final Point screen )
  {
    final IChartModel model = getChart().getChartModel();
    if( model == null )
      return null;

    final Point plotPoint = getChart().screen2plotPoint( screen );
    final IEditableChartLayer[] layers = model.getLayerManager().getEditableLayers();
    for( int i = layers.length - 1; i >= 0; i-- )

      if( layers[i].isVisible() )
      {
        final EditInfo info = layers[i].getHover( plotPoint );
        if( info != null )
        {
          return info;
        }
      }
    return null;
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
      {
        final Point plotPoint = getChart().screen2plotPoint( new Point( e.x - m_deltaSnapX, e.y - m_deltaSnapY ) );
        doMouseUpAction( plotPoint, m_editInfo );
      }
      else if( m_clickInfo != null )
        doMouseUpAction( null, m_clickInfo );
    }
    catch( final Error err )
    {
      err.printStackTrace();
    }
    finally
    {
      m_clickInfo = null;
      m_editInfo = null;
      m_chart.setEditInfo( null );
    }
  }

  private final void setCursor( final MouseEvent e )
  {
    final Cursor cursor = getCursor( e );
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

  abstract public void doMouseUpAction( final Point end, final EditInfo editInfo );

  abstract public void doMouseMoveAction( final Point end, final EditInfo editInfo );

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {
    setCursor( e );
    if( m_clickInfo == null )
      return;

    if( (m_editInfo == null) && ((Math.abs( e.x - m_startX ) > m_trashOld) || (Math.abs( e.y - m_startY ) > m_trashOld)) )
      m_editInfo = new EditInfo( m_clickInfo );

    final Point plotPoint = getChart().screen2plotPoint( new Point( e.x - m_deltaSnapX, e.y - m_deltaSnapY ) );
    doMouseMoveAction( plotPoint, m_editInfo == null ? m_clickInfo : m_editInfo );

  }

}
