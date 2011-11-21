package de.openali.odysseus.service.ods.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.impl.ChartImageComposite;

/**
 * This class contains all objects (e.g. shell) needed to keep a chart in the cache.
 * 
 * @author Alexander Burtscher, Holger Albert
 */
public class HeadlessChart
{
  /**
   * The shell.
   */
  private Shell m_shell;

  /**
   * The chart composite.
   */
  private ChartImageComposite m_chartComposite;

  /**
   * The constructor.
   * 
   * @param chartModel
   *          The chart model.
   * @param bgRGB
   *          The background rgb.
   */
  public HeadlessChart( final IChartModel chartModel, final RGB bgRGB )
  {
    m_shell = new Shell( DisplayHelper.getInstance().getDisplay() );
    m_chartComposite = new ChartImageComposite( m_shell, SWT.NONE, chartModel, bgRGB );
  }

  /**
   * This function disposes the headless chart.
   */
  public void dispose( )
  {
    if( m_shell != null && !m_shell.isDisposed() )
      m_shell.dispose();

    if( m_chartComposite != null && !m_chartComposite.isDisposed() )
      m_chartComposite.dispose();

    m_shell = null;
    m_chartComposite = null;
  }

  /**
   * This function returns the chart composite.
   * 
   * @return The chart composite.
   */
  public IChartComposite getChartComposite( )
  {
    return m_chartComposite;
  }
}