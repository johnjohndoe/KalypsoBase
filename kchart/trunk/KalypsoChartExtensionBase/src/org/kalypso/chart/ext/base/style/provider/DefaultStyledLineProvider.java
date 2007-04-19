package org.kalypso.chart.ext.base.style.provider;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.style.StyledLine;
import org.kalypso.chart.factory.configuration.parameters.impl.DoubleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.LineStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractStyledElementProvider;
import org.kalypso.chart.framework.model.styles.IStyledElement;


public class DefaultStyledLineProvider extends AbstractStyledElementProvider{

	public IStyledElement getStyledElement()
	{
	    int alpha = m_pc.getParsedParameterValue( "alpha", "255", new DoubleParser()).intValue() ;
	    int lineWidth = m_pc.getParsedParameterValue( "lineWidth", "1", new DoubleParser()).intValue() ;
	    RGB lineColor = m_pc.getParsedParameterValue( "lineColor", "#000000",  new RGBParser());
	    int lineStyle = m_pc.getParsedParameterValue( "lineStyle", "SOLID", new LineStyleParser());
	    StyledLine sl = new StyledLine( lineWidth, lineColor, lineStyle, alpha );
	    return sl;
	}

}
