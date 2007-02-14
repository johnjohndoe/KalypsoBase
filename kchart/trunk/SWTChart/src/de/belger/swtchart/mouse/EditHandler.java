package de.belger.swtchart.mouse;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;

import de.belger.swtchart.ChartCanvas;
import de.belger.swtchart.EditInfo;
import de.belger.swtchart.layer.IChartLayer;

/**
 * Registers itself upon creation and deregisters at disposal.
 * 
 * @author gernot
 */
public class EditHandler implements MouseListener, MouseMoveListener
{
  private final ChartCanvas m_chart;

  private boolean m_editingAllowed = false;

  private int m_trashHold = 3;

  private Point m_clickPoint;

  public EditHandler( final ChartCanvas chart )
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

  /**
   * If true, editing is allowed, else no editing can happen. Hover info however is always displayed.
   */
  public void setEditingAllowed( boolean editingAllowed )
  {
    m_editingAllowed = editingAllowed;
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
    final EditInfo editInfo = m_chart.getHoverInfo();
    m_clickPoint = new Point( e.x, e.y );
    if( m_editingAllowed && e.button == 1 && editInfo != null )
    {
      m_chart.setHoverInfo( null );
      final Point pos = new Point( e.x, e.y );
      editInfo.pos = pos;
      m_chart.setEditInfo( editInfo );
      m_chart.setCapture( true );
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp( final MouseEvent e )
  {
    if( e.button == 1 )
    {
      final EditInfo editInfo = m_chart.getEditInfo();
      if( editInfo != null )
      {
        try
        {
          if( (Math.abs( e.x - m_clickPoint.x ) > m_trashHold) || (Math.abs( e.y - m_clickPoint.y ) > m_trashHold) )
          {
            editInfo.layer.edit( new Point( e.x, e.y ), editInfo.data );
          }
          else
          {
            editInfo.layer.setActivePoint( editInfo.data );
          }
        }
        finally
        {
          m_clickPoint = null;
          m_chart.setEditInfo( null );
        }
      }
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove( final MouseEvent e )
  {

    if( m_chart.isEditing() )
    {

      m_chart.getEditInfo().pos = new Point( e.x, e.y );
      m_chart.redraw();

    }
    else if( !m_chart.isDragging() )
    {
      // to prevent holding the capture
      m_chart.setCapture( false );

      final Point point = new Point( e.x, e.y );

      for( final IChartLayer layer : m_chart.getLayers() )
      {
        if( m_chart.isVisible( layer ) )
        {
          final EditInfo info = layer.getHoverInfo( point );
          if( info != null )
          {
            info.pos = point;
            m_chart.setHoverInfo( info );
            return;
          }
        }
      }

      m_chart.setHoverInfo( null );
    }
  }
}
