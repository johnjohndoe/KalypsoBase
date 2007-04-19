package org.kalypso.chart.ext.base.style.provider;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.style.StyledPolygon;
import org.kalypso.chart.factory.configuration.exception.StyledElementProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.DoubleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractStyledElementProvider;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.ksp.chart.factory.StyleType;


public class DefaultStyledPolygonProvider extends AbstractStyledElementProvider{


	public IStyledElement getStyledElement()
			throws StyledElementProviderException {
	    
	    int borderWidth = m_pc.getParsedParameterValue( "borderWidth", "0", new DoubleParser() ).intValue();
	    RGB borderColor = m_pc.getParsedParameterValue( "borderColor", "#000000", new RGBParser());
	    RGB fillColor = m_pc.getParsedParameterValue( "fillColor", "#ffffff", new RGBParser());
	    int alpha = m_pc.getParsedParameterValue( "alpha", "255", new DoubleParser() ).intValue();
	    StyledPolygon sp = new StyledPolygon( fillColor, borderWidth, borderColor, alpha );
	      
	    return sp;
	}


}
