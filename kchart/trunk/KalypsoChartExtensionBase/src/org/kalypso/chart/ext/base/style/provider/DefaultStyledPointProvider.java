package org.kalypso.chart.ext.base.style.provider;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.style.StyledPoint;
import org.kalypso.chart.factory.configuration.exception.StyledElementProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.DoubleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractStyledElementProvider;
import org.kalypso.chart.framework.model.styles.IStyledElement;


public class DefaultStyledPointProvider extends AbstractStyledElementProvider{

	public IStyledElement getStyledElement()
			throws StyledElementProviderException {


		int pointWidth = m_pc.getParsedParameterValue("pointWidth", "5", 
				new DoubleParser()).intValue();
		int pointHeight = m_pc.getParsedParameterValue("pointHeight", "5", 
				new DoubleParser()).intValue();
		int borderWidth = m_pc.getParsedParameterValue("borderWidth", "1", 
				new DoubleParser()).intValue();
		RGB fillColor = m_pc.getParsedParameterValue("fillColor", "#ffffff", 
				new RGBParser());
		RGB borderColor = m_pc.getParsedParameterValue("borderColor", "#000000",
				new RGBParser());
		int alpha = m_pc.getParsedParameterValue("alpha", "255", 
				new DoubleParser()).intValue();
		StyledPoint sp = new StyledPoint(pointWidth, pointHeight, fillColor,
				borderWidth, borderColor, alpha);
		return sp;
	}

}
