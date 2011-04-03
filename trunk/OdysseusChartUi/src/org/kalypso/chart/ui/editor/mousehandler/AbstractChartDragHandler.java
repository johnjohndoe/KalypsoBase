package org.kalypso.chart.ui.editor.mousehandler;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.EditableChartLayerVisitor;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * FIXME: using the eidt info here in order to save the click-state is dubious. Separate editing and dragging (i.e.
 * separate this into different helper classes or remove the edit stuff from this class).
 * 
 * @author kimwerner
 */
public abstract class AbstractChartDragHandler extends AbstractChartHandler
{
  private EditInfo m_editInfo = null;

  private final int m_trashHold;

  private EditInfo m_clickInfo = null;

  private int m_deltaSnapX = 0;

  private int m_deltaSnapY = 0;

  private int m_startX = 0;

  private int m_startY = 0;

  private final int m_observedButtonMask;

  public AbstractChartDragHandler( final IChartComposite chart )
  {
    this( chart, 5 );
  }

  public AbstractChartDragHandler( final IChartComposite chart, final int trashHold )
  {
    this( chart, trashHold, SWT.BUTTON1 | SWT.BUTTON2 | SWT.BUTTON3 );
  }

  public AbstractChartDragHandler( final IChartComposite chart, final int trashHold, final int observedButtonMask )
  {
    super( chart );

    m_trashHold = trashHold;
    m_observedButtonMask = observedButtonMask;
  }

  protected final int button2Mask( final int button )
  {
    return 1 << 18 + button;
  }

  protected abstract void doMouseMoveAction( final Point end, final EditInfo editInfo );

  protected abstract void doMouseUpAction( final Point end, final EditInfo editInfo );

  protected final EditInfo getHover( final Point screen )
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
      final EditInfo info = layer.getHover( plotPoint );
      if( info != null )
        return info;
    }

    return null;
  }

  protected void mouseDown( final Point down )
  {
    m_clickInfo = getHover( down );
    if( m_clickInfo == null )
      m_clickInfo = new EditInfo( null, null, null, null, null, getChart().screen2plotPoint( down ) );

    // offset from cursor relative to the given InfoObject Center
    final Point pos = getChart().plotPoint2screen( m_clickInfo.getPosition() );
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

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {
    super.mouseMove( e );

    if( m_clickInfo == null )
      return;

    mouseMove( new Point( e.x, e.y ) );
  }

  protected void mouseMove( final Point move )
  {
    if( m_editInfo == null && (Math.abs( move.x - m_startX ) > m_trashHold || Math.abs( move.y - m_startY ) > m_trashHold) )
      m_editInfo = m_clickInfo.clone();

    final Point plotPoint = getChart().screen2plotPoint( new Point( move.x - m_deltaSnapX, move.y - m_deltaSnapY ) );
    doMouseMoveAction( plotPoint, m_editInfo == null ? m_clickInfo : m_editInfo );
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( final MouseEvent e )
  {
    final Point pos = new Point( e.x, e.y );
    mouseUp( pos );
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
      final IChartComposite chart = getChart();
      chart.setEditInfo( null );
      chart.setDragArea( null );
    }
  }

  protected void updateSelection( final EditInfo editInfo )
  {
    if( editInfo != null && editInfo.getLayer() != null )
      ((IEditableChartLayer) editInfo.getLayer()).commitDrag( editInfo.getPosition(), editInfo );
  }
}