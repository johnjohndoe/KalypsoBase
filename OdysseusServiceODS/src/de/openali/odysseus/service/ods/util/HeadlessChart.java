package de.openali.odysseus.service.ods.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.impl.ChartImageComposite;

/**
 * @author alibu this class contains all objects (e.g. shell) needed to keep a chart in the cache
 */
public class HeadlessChart
{
  private final Shell m_shell;

  private final IChartComposite m_chart;

  public HeadlessChart( final IChartModel chartModel, final RGB bgRGB )
  {
    final Display d = DisplayHelper.getInstance().getDisplay();
    m_shell = new Shell( d );
    m_chart = new ChartImageComposite( m_shell, SWT.NONE, chartModel, bgRGB );
  }

  public void dispose( )
  {
    if( m_shell != null && !m_shell.isDisposed() )
    {
      m_shell.dispose();
    }
    if( m_chart != null && !m_chart.getPlot().isDisposed() )
    {
      m_chart.getPlot().dispose();
    }
  }

  public IChartComposite getChart( )
  {
    return m_chart;
  }

}