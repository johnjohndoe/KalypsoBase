package org.kalypso.chart.ext.base.style.provider;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.style.StyledText;
import org.kalypso.chart.factory.configuration.exception.StyledElementProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.DoubleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractStyledElementProvider;
import org.kalypso.chart.framework.model.styles.IStyledElement;


public class DefaultStyledTextProvider extends AbstractStyledElementProvider{


	public IStyledElement getStyledElement() throws StyledElementProviderException {
		
		String fontName = m_pc.getParameterValue("fontName", "arial");
		int fontStyle = m_pc.getParsedParameterValue("fontName", "NORMAL",
				new FontStyleParser());
		int fontSize = m_pc.getParsedParameterValue("fontSize", "10",
				new DoubleParser()).intValue();
		RGB foregroundColor = m_pc.getParsedParameterValue("textColor",
				"#ffffff",  new RGBParser());
		RGB backgroundColor = m_pc.getParsedParameterValue("backgroundColor",
				"#000000",  new RGBParser());
		int alpha = m_pc.getParsedParameterValue("alpha", "255", new DoubleParser()).intValue();
		StyledText st = new StyledText(foregroundColor, backgroundColor,
				fontName, fontStyle, fontSize, alpha);
		return st;
	}


}
