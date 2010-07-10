package de.openali.odysseus.chart.framework.view;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;
import de.openali.odysseus.chart.framework.view.impl.PlotCanvas;

/**
 * handler to manage tooltips; registers on creation, deregisters on disposation
 * 
 * @author kimwerner
 */
public class TooltipHandler extends MouseAdapter implements MouseListener, MouseMoveListener
{

  /**
   * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseDown( MouseEvent e )
  {
    m_mouseDown = true;

  }

  /**
   * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseUp( MouseEvent e )
  {
    m_mouseDown = false;

    if( m_info != null )
    {
      m_info.m_mouseButton = e.button;
      final ITooltipChartLayer layer = m_info.m_layer;
      if( layer instanceof IEditableChartLayer )
        ((IEditableChartLayer) layer).commitDrag( m_info.m_pos, m_info );
      m_info = null;
    }
  }

  private final PlotCanvas m_plot;

  private final ChartComposite m_chart;

  private EditInfo m_info;

  private boolean m_mouseDown = false;

  public TooltipHandler( final ChartComposite chart, final PlotCanvas plot )
  {
    m_plot = plot;
    m_chart = chart;
    m_plot.addMouseListener( this );
    m_plot.addMouseMoveListener( this );

  }

  public void dispose( )
  {
    if( !m_plot.isDisposed() )
    {
      m_plot.removeMouseListener( this );
      m_plot.removeMouseMoveListener( this );

    }
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {

// TODO: Try MouseEvent.stateMask to indicate mousebuttons
    if( !m_mouseDown )
    {
      final IChartModel model = m_chart.getChartModel();
      if( model == null )
        return;
      final Point point = new Point( e.x, e.y );

      final ITooltipChartLayer[] tooltipLayers = model.getLayerManager().getTooltipLayers();
      // Array umdrehen, damit die oberen Layer zuerst befragt werden
      ArrayUtils.reverse( tooltipLayers );

      for( final ITooltipChartLayer layer : tooltipLayers )
      {
        if( layer.isVisible() )
        {
          final EditInfo info = layer.getHover( point );
          if( info != null )
          {
            m_info = info;
            info.m_pos = point;
            m_plot.setTooltipInfo( info );
            m_plot.redraw();
            return;
          }
        }
      }
    }
    m_plot.setTooltipInfo( null );
    m_plot.redraw();
  }
}
