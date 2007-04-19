package org.kalypso.chart.factory.provider;

import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.util.ChartFactoryUtilities;
import org.kalypso.chart.framework.model.IChartModel;
import org.ksp.chart.factory.LayerType;

public abstract class AbstractLayerProvider implements ILayerProvider {

	protected LayerType m_lt;
	protected IChartModel m_model;
	protected IParameterContainer m_pc;


	  /**
	   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#init(org.kalypso.swtchart.chart.ChartView, org.ksp.chart.configuration.LayerType)
	   */
	  public void init( final IChartModel model, final LayerType lt)
	  {
	    m_lt = lt;
	    m_model = model;
	    m_pc=ChartFactoryUtilities.createXmlbeansParameterContainer(m_lt.getId(), m_lt.getProvider());
	    
	  }

}
