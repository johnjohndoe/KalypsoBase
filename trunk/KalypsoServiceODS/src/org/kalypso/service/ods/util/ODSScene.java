package org.kalypso.service.ods.util;

import net.opengis.ows.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.ServiceProviderDocument.ServiceProvider;

import org.ksp.chart.factory.ChartConfigurationType;
import org.ksp.service.odsimpl.ODSSceneDocument;
import org.ksp.service.odsimpl.ODSSceneType;

public class ODSScene
{

  private final ServiceIdentification m_serviceIdentification;

  private final ServiceProvider m_serviceProvider;

  private final ChartConfigurationType m_chartConfiguration;

  private final ODSSceneDocument m_osd;

  public ODSScene( ServiceIdentification serviceIdentification, ServiceProvider serviceProvider, ChartConfigurationType chartConfiguration )
  {
    m_serviceIdentification = serviceIdentification;
    m_serviceProvider = serviceProvider;
    m_chartConfiguration = chartConfiguration;
    m_osd = ODSSceneDocument.Factory.newInstance();
    final ODSSceneType sceneType = m_osd.addNewODSScene();
    sceneType.setChartConfiguration( m_chartConfiguration );
    sceneType.setServiceIdentification( m_serviceIdentification );
    sceneType.setServiceProvider( m_serviceProvider );

  }

  public ServiceIdentification getServiceIdentification( )
  {
    return m_serviceIdentification;
  }

  public ServiceProvider getServiceProvider( )
  {
    return m_serviceProvider;
  }

  public ChartConfigurationType getChartConfiguration( )
  {
    return m_chartConfiguration;
  }

  public ODSSceneDocument getODSSceneDocument( )
  {
    return m_osd;
  }

}
