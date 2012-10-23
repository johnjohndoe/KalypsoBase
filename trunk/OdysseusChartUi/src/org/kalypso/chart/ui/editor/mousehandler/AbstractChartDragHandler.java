package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.impl.visitors.FindLayerTooltipVisitor;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.EditableChartLayerVisitor;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * FIXME: using the edit info here in order to save the click-state is dubious. Separate editing and dragging (i.e.
 * separate this into different helper classes or remove the edit stuff from this class).
 * 
 * @author kimwerner
 */
public abstract class AbstractChartDragHandler extends AbstractChartHandler
{
  private EditInfo m_editInfo = null;

  private final int m_trashold;

  private EditInfo m_clickInfo = null;

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

    m_trashold = trashHold;
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

    final EditableChartLayerVisitor visitor = new EditableChartLayerVisitor();
    model.accept( visitor );

    final IEditableChartLayer[] layers = visitor.getLayers();

    for( final IEditableChartLayer layer : layers )
    {
      final EditInfo info = layer.getHover( screen );
      if( info != null )
        return info;
    }

    return null;
  }

  protected void mouseDown( final Point down )
  {
    m_clickInfo = getHover( down );

    if( m_clickInfo == null )
      m_clickInfo = new EditInfo( null, null, null, null, null, down );

    m_startX = down.x;
    m_startY = down.y;
  }

  @Override
  public void mouseDown( final MouseEvent e )
  {
    if( (button2Mask( e.button ) & m_observedButtonMask) == 0 )
      return;

    mouseDown( new Point( e.x, e.y ) );
  }

  @Override
  public void mouseMove( final MouseEvent e )
  {
    super.mouseMove( e );

    setToolInfo( null );

    final Point p = new Point( e.x, e.y );
    if( m_clickInfo == null )
      doSetTooltip( p );
    else
      mouseMove( p );
  }

  private void doSetTooltip( final Point point )
  {
    final IChartComposite chart = getChart();
    final IChartModel model = chart.getChartModel();

    final FindLayerTooltipVisitor visitor = new FindLayerTooltipVisitor( chart, point );
    model.accept( visitor );

    setToolInfo( visitor.getEditInfo() );
  }

  protected void mouseMove( final Point move )
  {
    if( m_editInfo == null && (Math.abs( move.x - m_startX ) > m_trashold || Math.abs( move.y - m_startY ) > m_trashold) )
      m_editInfo = m_clickInfo.clone();

    final Point plotPoint = new Point( move.x, move.y );
    doMouseMoveAction( plotPoint, m_editInfo == null ? m_clickInfo : m_editInfo );
  }

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
        final Point position = new Point( up.x, up.y );
        doMouseUpAction( position, m_editInfo );
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
      ((IEditableChartLayer)editInfo.getLayer()).commitDrag( editInfo.getPosition(), editInfo );
  }

}