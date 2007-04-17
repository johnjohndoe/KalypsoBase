package de.openali.diagram.ext.base.axis.provider;


import de.openali.diagram.factory.configuration.parameters.IParameterContainer;
import de.openali.diagram.factory.configuration.xsd.AxisType;
import de.openali.diagram.factory.provider.IAxisProvider;
import de.openali.diagram.factory.util.DiagramFactoryUtilities;

public abstract class AbstractAxisProvider implements IAxisProvider {


	protected AxisType m_at;
	protected IParameterContainer m_pc;

	/**
	   * @see de.openali.diagram.factory.provider.IAxisProvider#init(org.ksp.chart.configuration.AxisType)
	   */
	  public void init( AxisType at )
	  {
	    m_at=at;
	    m_pc=DiagramFactoryUtilities.createParameterContainer(at.getProvider(), m_at.getId());
	  }

}
