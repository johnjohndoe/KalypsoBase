package org.kalypso.service.ods.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.chart.factory.configuration.ChartConfigurationLoader;
import org.kalypso.chart.factory.configuration.ChartFactory;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.model.mapper.registry.impl.MapperRegistry;
import org.kalypso.chart.framework.view.AxisCanvas;
import org.kalypso.contribs.eclipse.swt.widgets.SizedComposite;
import org.ksp.chart.factory.AxisType;
import org.ksp.chart.factory.ChartConfigurationType;

/**
 * @author alibu this class contains all objects (e.g. shell) needed to keep a chart in the cache
 */
public class HeadlessAxis
{

  private Shell m_shell;

  private final AxisCanvas m_axisCanvas;

  private final SizedComposite m_sizedComposite;

  private boolean m_isDisposed = false;

  private final IMapperRegistry m_mapperRegistry;

  public HeadlessAxis( String sceneId, String axisId )
  {
    final Display d = DisplayHelper.getInstance().getDisplay();
    m_shell = new Shell( d );

    final ODSConfigurationLoader ocl = ODSConfigurationLoader.getInstance();
    final ODSScene scene = ocl.getSceneById( sceneId );
    final ChartConfigurationType chartConfiguration = scene.getChartConfiguration();
    final ChartConfigurationLoader cl = new ChartConfigurationLoader( chartConfiguration );

    m_mapperRegistry = new MapperRegistry();
    final AxisType axisType = (AxisType) cl.resolveReference( axisId );
    ChartFactory.addAxis( m_mapperRegistry, cl, axisType );
    final IAxis axis = m_mapperRegistry.getAxis( axisId );

    m_shell = new Shell( DisplayHelper.getInstance().getDisplay() );

    m_sizedComposite = new SizedComposite( m_shell, SWT.PUSH );
    if( axis.getPosition().getOrientation() == ORIENTATION.VERTICAL )
      m_sizedComposite.setLayout( new FillLayout( SWT.VERTICAL ) );
    else
      m_sizedComposite.setLayout( new FillLayout( SWT.HORIZONTAL ) );

    final FillLayout layout = new FillLayout();
    m_shell.setLayout( layout );

    m_axisCanvas = new AxisCanvas( axis, m_sizedComposite, SWT.NONE );
    m_mapperRegistry.setComponent( axis, m_axisCanvas );
  }

  public void dispose( )
  {
    if( m_shell != null && !m_shell.isDisposed() )
      m_shell.dispose();
    if( m_sizedComposite != null && !m_sizedComposite.isDisposed() )
      m_sizedComposite.dispose();
    if( m_axisCanvas != null && !m_axisCanvas.isDisposed() )
      m_axisCanvas.dispose();
    m_isDisposed = true;
  }

  public boolean isDisposed( )
  {
    return m_isDisposed;
  }

  public Shell getAxisShell( )
  {
    return m_shell;
  }

  public IMapperRegistry getMapperRegistry( )
  {
    return m_mapperRegistry;
  }

}
