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
 * @author alibu
 *
 * this class contains all objects (e.g. shell) needed to keep a chart in the cache
 */
public class HeadlessAxis
{

    private Shell m_shell;
    private AxisCanvas m_axisCanvas;
    private SizedComposite m_sizedComposite;
    private boolean m_isDisposed=false;
	private IMapperRegistry m_mapperRegistry;

    
    public HeadlessAxis(String sceneId, String axisId)
    {
    	Display d= DisplayHelper.getInstance().getDisplay();
        m_shell=new Shell(d);

        
	  ODSConfigurationLoader ocl=ODSConfigurationLoader.getInstance();
	  ODSScene scene = ocl.getSceneById(sceneId);
	  ChartConfigurationType chartConfiguration = scene.getChartConfiguration();
      ChartConfigurationLoader cl=new ChartConfigurationLoader(chartConfiguration);
      
      
      	
      m_mapperRegistry=new MapperRegistry();
      AxisType axisType = (AxisType) cl.resolveReference(axisId);
      ChartFactory.addAxis(m_mapperRegistry, cl, axisType);
      IAxis axis=m_mapperRegistry.getAxis(axisId);
      
      
      m_shell=new Shell(DisplayHelper.getInstance().getDisplay());
	
      m_sizedComposite=new SizedComposite(m_shell, SWT.PUSH);
      if (axis.getPosition().getOrientation()==ORIENTATION.VERTICAL)
    	  m_sizedComposite.setLayout( new FillLayout( SWT.VERTICAL ) );
      else
    	  m_sizedComposite.setLayout( new FillLayout( SWT.HORIZONTAL ) );

		FillLayout layout=new FillLayout();
		m_shell.setLayout(layout);
	
      m_axisCanvas=new AxisCanvas(axis, m_sizedComposite, SWT.NONE);
      m_mapperRegistry.setComponent(axis, m_axisCanvas);
    }

    public void dispose()
    {
      if (m_shell!=null && !m_shell.isDisposed())
        m_shell.dispose();
      if (m_sizedComposite!=null && !m_sizedComposite.isDisposed())
        m_sizedComposite.dispose();
      if (m_axisCanvas!=null && !m_axisCanvas.isDisposed())
    	  m_axisCanvas.dispose();
      m_isDisposed=true;
    }

    public boolean isDisposed()
    {
      return m_isDisposed;
    }

    public Shell getAxisShell()
    {
      return m_shell;
    }
    
    public IMapperRegistry getMapperRegistry()
    {
    	return m_mapperRegistry;
    }

}
