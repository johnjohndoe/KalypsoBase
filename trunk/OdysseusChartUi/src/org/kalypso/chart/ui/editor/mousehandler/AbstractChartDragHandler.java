package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.EditableChartLayerVisitor;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartDragHandler;

/**
 * @author kimwerner
 */
public abstract class AbstractChartDragHandler implements IChartDragHandler
{
  private final IChartComposite m_chart;

  private EditInfo m_editInfo = null;

  private final int m_trashOld;

  private EditInfo m_clickInfo = null;

  private int m_deltaSnapX = 0;

  private int m_deltaSnapY = 0;

  private int m_startX = 0;

  private int m_startY = 0;

  private Cursor m_cursor = null;

  private final int m_SWTCursor;

  private final int m_observedButtonMask;

  public AbstractChartDragHandler( final IChartComposite chart )
  {
    this( chart, 5 );
  }

  public AbstractChartDragHandler( final IChartComposite chart, final int trashold )
  {
    this( chart, trashold, SWT.BUTTON1 | SWT.BUTTON2 | SWT.BUTTON3, SWT.CURSOR_ARROW );
  }

  public AbstractChartDragHandler( final IChartComposite chart, final int trashold, final int observedButtonMask, final int cursor )
  {
    m_chart = chart;
    m_trashOld = trashold;
    m_observedButtonMask = observedButtonMask;
    m_SWTCursor = cursor;
  }

  private final int button2Mask( final int button )
  {
    return 1 << (18 + button);
  }

  abstract public void doMouseMoveAction( final Point end, final EditInfo editInfo );

  abstract public void doMouseUpAction( final Point end, final EditInfo editInfo );

  public IChartComposite getChart( )
  {
    return m_chart;
  }

  public Cursor getCursor( final MouseEvent e )
  {
    return e.display.getSystemCursor( m_SWTCursor );
  }

  final protected EditInfo getHover( final Point screen )
  {
    final IChartModel model = getChart().getChartModel();
    if( model == null )
      return null;

    final Point plotPoint = getChart().screen2plotPoint( screen );

    final ILayerManager layerManager = model.getLayerManager();
    final EditableChartLayerVisitor visitor = new EditableChartLayerVisitor();
    layerManager.accept( visitor );

    final IEditableChartLayer[] layers = visitor.getLayers();
    ArrayUtils.reverse( layers );

    for( final IEditableChartLayer layer : layers )
    {
      if( layer.isVisible() )
      {
        final EditInfo info = layer.getHover( plotPoint );
        if( info != null )
          return info;
      }
    }

    return null;
  }

  /**
   * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
   */
  @Override
  public void keyPressed( final KeyEvent e )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
   */
  @Override
  public void keyReleased( final KeyEvent e )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDoubleClick( final MouseEvent e )
  {
  }

  protected void mouseDown( final Point down )
  {
    m_clickInfo = getHover( down );
    if( m_clickInfo == null )
      m_clickInfo = new EditInfo( null, null, null, null, null, getChart().screen2plotPoint( down ) );

    // offset from cursor relative to the given InfoObject Center
    final Point pos = getChart().plotPoint2screen( m_clickInfo.m_pos );
    m_deltaSnapX = down.x - pos.x;
    m_deltaSnapY = down.y - pos.y;
    m_startX = down.x;
    m_startY = down.y;
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDown( final MouseEvent e )
  {
    if( (button2Mask( e.button ) & m_observedButtonMask) == 0 )
      return;
    mouseDown( new Point( e.x, e.y ) );

  }

  protected void mouseMove( final Point move )
  {
    if( (m_editInfo == null) && ((Math.abs( move.x - m_startX ) > m_trashOld) || (Math.abs( move.y - m_startY ) > m_trashOld)) )
      m_editInfo = new EditInfo( m_clickInfo );

    final Point plotPoint = getChart().screen2plotPoint( new Point( move.x - m_deltaSnapX, move.y - m_deltaSnapY ) );
    doMouseMoveAction( plotPoint, m_editInfo == null ? m_clickInfo : m_editInfo );
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {
    setCursor( e );
    if( m_clickInfo == null )
      return;
    mouseMove( new Point( e.x, e.y ) );
  }

  protected void mouseUp( final Point up )
  {
    try
    {
      if( m_editInfo != null )
      {
        final Point plotPoint = getChart().screen2plotPoint( new Point( up.x - m_deltaSnapX, up.y - m_deltaSnapY ) );
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

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( final MouseEvent e )
  {
    mouseUp( new Point( e.x, e.y ) );
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

}
