package de.openali.odysseus.chart.factory.provider;

import java.net.URL;

import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.util.ChartFactoryUtilities;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chartconfig.x010.LayerType;

public abstract class AbstractLayerProvider implements ILayerProvider
{
  private LayerType m_lt;

  private IChartModel m_model;

  private IParameterContainer m_pc;

  private URL m_context;

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#init(org.kalypso.swtchart.chart.ChartView,
   *      org.ksp.chart.configuration.LayerType)
   */
  public void init( final IChartModel model, final LayerType lt, final URL context )
  {
    m_lt = lt;
    m_model = model;
    m_pc = ChartFactoryUtilities.createXmlbeansParameterContainer( m_lt.getId(), m_lt.getProvider() );
    m_context = context;
  }

  /**
   * default behaviour: return original xml type; implement, if changes shall be saved
   * 
   * @see org.kalypso.chart.factory.provider.ILayerProvider#getLayerType()
   */
  public LayerType getXMLType( IChartLayer layer )
  {
    LayerType lt = (LayerType) m_lt.copy();
    lt.setVisible( layer.isVisible() );
    return lt;
  }

  public IChartModel getChartModel( )
  {
    return m_model;
  }

  public IParameterContainer getParameterContainer( )
  {
    return m_pc;
  }

  private LayerType getLayerType( )
  {
    return m_lt;
  }

  protected URL getContext( )
  {
    return m_context;
  }

  protected String getId( )
  {
    return m_lt.getId();
  }

  protected String getDomainAxisId( )
  {
    return getLayerType().getMappers().getDomainAxisRef().getRef();
  }

  protected String getTargetAxisId( )
  {
    return getLayerType().getMappers().getTargetAxisRef().getRef();
  }

}
