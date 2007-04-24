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


		int pointWidth = getParameterContainer().getParsedParameterValue("pointWidth", "5", 
				new DoubleParser()).intValue();
		int pointHeight = getParameterContainer().getParsedParameterValue("pointHeight", "5", 
				new DoubleParser()).intValue();
		int borderWidth = getParameterContainer().getParsedParameterValue("borderWidth", "1", 
				new DoubleParser()).intValue();
		RGB fillColor = getParameterContainer().getParsedParameterValue("fillColor", "#ffffff", 
				new RGBParser());
		RGB borderColor = getParameterContainer().getParsedParameterValue("borderColor", "#000000",
				new RGBParser());
		int alpha = getParameterContainer().getParsedParameterValue("alpha", "255", 
				new DoubleParser()).intValue();
		StyledPoint sp = new StyledPoint(pointWidth, pointHeight, fillColor,
				borderWidth, borderColor, alpha);
		return sp;
	}

}
