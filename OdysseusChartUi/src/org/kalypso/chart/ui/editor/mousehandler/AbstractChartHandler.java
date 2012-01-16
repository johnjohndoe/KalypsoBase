package org.kalypso.chart.ui.editor.mousehandler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;

import de.openali.odysseus.chart.framework.model.figure.IPaintable;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.util.img.ChartTooltipPainter;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartHandler;

/**
 * @author kimwerner
 */
public abstract class AbstractChartHandler implements IChartHandler
{
  private final IChartComposite m_chart;

  private int m_cursor = -1;

  private EditInfo m_tooltip;

  private final ChartTooltipPainter m_tooltipPainter = new ChartTooltipPainter();

  public AbstractChartHandler( final IChartComposite chart )
  {
    m_chart = chart;
  }

  protected ChartTooltipPainter getTooltipPainter( )
  {
    return m_tooltipPainter;
  }

  protected void setToolInfo( final EditInfo editInfo )
  {
    if( Objects.equal( m_tooltip, editInfo ) )
      return;

    m_tooltip = editInfo;

    forceRedrawEvent();
  }

  protected void forceRedrawEvent( )
  {
    final UIJob job = new UIJob( "Redrawing chart tooltip" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        final Composite composite = (Composite) getChart();
        composite.redraw();

        return Status.OK_STATUS;
      }
    };

    job.setUser( false );
    job.setSystem( true );

    job.schedule();
  }

  public IChartComposite getChart( )
  {
    return m_chart;
  }

  @Override
  public void keyPressed( final KeyEvent e )
  {
  }

  @Override
  public void keyReleased( final KeyEvent e )
  {
  }

  @Override
  public void mouseDoubleClick( final MouseEvent e )
  {
  }

  @Override
  public void mouseDown( final MouseEvent e )
  {
  }

  @Override
  public void mouseMove( final MouseEvent e )
  {
    if( getChart() instanceof Canvas )
    {
      final Canvas plot = (Canvas) getChart();
      final Cursor swtCursor = m_cursor == -1 ? null : plot.getDisplay().getSystemCursor( m_cursor );
      if( plot.getCursor() != plot.getDisplay().getSystemCursor( m_cursor ) )
        plot.setCursor( swtCursor );
    }

  }

  @Override
  public void mouseUp( final MouseEvent e )
  {
  }

  protected void setCursor( final int cursor )
  {
    if( cursor == m_cursor )
      return;

    m_cursor = cursor;
  }

  /**
   * default implementation - paints only tool tip of current chart handler
   */
  @Override
  public void paintControl( final PaintEvent e )
  {
    paintTooltipInfo( e.gc );
  }

  protected final void paintTooltipInfo( final GC gc )
  {
    if( Objects.isNull( m_tooltip ) )
      return;

    final IPaintable hoverFigure = m_tooltip.getHoverFigure();
    if( Objects.isNotNull( hoverFigure ) )
      hoverFigure.paint( gc );

    final Point position = m_tooltip.getPosition();
    if( Objects.isNotNull( position ) )
    {
      m_tooltipPainter.setTooltip( m_tooltip.getText() );
      m_tooltipPainter.paint( gc, position );
    }
  }

  @Override
  public CHART_HANDLER_TYPE getType( )
  {
    return CHART_HANDLER_TYPE.eToggle;
  }
}
