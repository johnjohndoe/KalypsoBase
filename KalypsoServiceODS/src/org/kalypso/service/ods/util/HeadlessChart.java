package org.kalypso.service.ods.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.chart.factory.configuration.ChartConfigurationLoader;
import org.kalypso.chart.factory.configuration.ChartFactory;
import org.kalypso.chart.factory.configuration.exception.ConfigChartNotFoundException;
import org.kalypso.chart.factory.configuration.exception.ConfigurationException;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.impl.ChartModel;
import org.kalypso.chart.framework.view.ChartComposite;
import org.ksp.chart.factory.ChartConfigurationType;

/**
 * @author alibu this class contains all objects (e.g. shell) needed to keep a chart in the cache
 */
public class HeadlessChart
{

  private final Shell m_shell;

  private ChartComposite m_chart;

  private boolean m_isDisposed = false;

  public HeadlessChart( String sceneId, String chartId, RGB bgRGB )
  {
    final Display d = DisplayHelper.getInstance().getDisplay();
    m_shell = new Shell( d );

    try
    {

      final ODSConfigurationLoader ocl = ODSConfigurationLoader.getInstance();
      final ODSScene scene = ocl.getSceneById( sceneId );
      final ChartConfigurationType chartConfiguration = scene.getChartConfiguration();
      final ChartConfigurationLoader cl = new ChartConfigurationLoader( chartConfiguration );
      final IChartModel model = new ChartModel();
      ChartFactory.configureChartModel( model, cl, chartId, null );
      m_chart = new ChartComposite( m_shell, SWT.NONE, model, bgRGB );
    }
    catch( final ConfigChartNotFoundException e )
    {
      Logger.trace( e.getMessage() );
      e.printStackTrace();
    }
    catch( final ConfigurationException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void dispose( )
  {
    if( m_shell != null && !m_shell.isDisposed() )
      m_shell.dispose();
    if( m_chart != null && !m_chart.isDisposed() )
      m_chart.dispose();
    m_isDisposed = true;
  }

  public boolean isDisposed( )
  {
    return m_isDisposed;
  }

  public ChartComposite getChart( )
  {
    return m_chart;
  }

}
