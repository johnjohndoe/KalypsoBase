package de.openali.odysseus.chart.framework.view.impl;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * handler to manage tooltips; registers on creation, deregisters on disposation
 * 
 * @author kimwerner
 */
public class TooltipHandler extends MouseAdapter implements MouseListener, MouseMoveListener
{

  private final IChartComposite m_chart;

  public TooltipHandler( final IChartComposite chart )
  {
    m_chart = chart;
    m_chart.getPlot().addMouseListener( this );
    m_chart.getPlot().addMouseMoveListener( this );

  }

  public void dispose( )
  {
    if( !m_chart.getPlot().isDisposed() )
    {
      m_chart.getPlot().removeMouseListener( this );
      m_chart.getPlot().removeMouseMoveListener( this );
    }
  }

  /**
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  @Override
  public void mouseMove( final MouseEvent e )
  {
    if( (e.stateMask & SWT.BUTTON_MASK) == 0 )// no mousebutton pressed
    {
      final IChartModel model = m_chart.getChartModel();
      if( model == null )
        return;
      final Point point = m_chart.screen2plotPoint( new Point( e.x, e.y ) );

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
            m_chart.setTooltipInfo( info );
            return;
          }
        }
      }
    }
    m_chart.setTooltipInfo( null );
  }
}
