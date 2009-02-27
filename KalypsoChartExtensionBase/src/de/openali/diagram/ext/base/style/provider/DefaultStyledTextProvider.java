package de.openali.diagram.ext.base.style.provider;

import org.eclipse.swt.graphics.RGB;

import de.openali.diagram.ext.base.style.StyledText;
import de.openali.diagram.factory.configuration.exception.StyledElementProviderException;
import de.openali.diagram.factory.configuration.parameters.IParameterContainer;
import de.openali.diagram.factory.configuration.parameters.impl.FontStyleParser;
import de.openali.diagram.factory.configuration.parameters.impl.DoubleParser;
import de.openali.diagram.factory.configuration.parameters.impl.RGBParser;
import de.openali.diagram.factory.configuration.xsd.StyleType;
import de.openali.diagram.factory.provider.IStyledElementProvider;
import de.openali.diagram.factory.util.DiagramFactoryUtilities;
import de.openali.diagram.framework.model.styles.IStyledElement;

public class DefaultStyledTextProvider implements IStyledElementProvider {

	private StyleType m_st;

	public IStyledElement getStyledElement()
			throws StyledElementProviderException {
		String id = m_st.getId();
		IParameterContainer ph = DiagramFactoryUtilities
				.createParameterContainer(m_st.getProvider(), id);

		String fontName = ph.getParameterValue("fontName", "arial", id);
		int fontStyle = ph.getParsedParameterValue("fontName", "NORMAL", id,
				new FontStyleParser());
		int fontSize = ph.getParsedParameterValue("fontSize", "10", id,
				new DoubleParser()).intValue();
		RGB foregroundColor = ph.getParsedParameterValue("textColor",
				"#ffffff", id, new RGBParser());
		RGB backgroundColor = ph.getParsedParameterValue("backgroundColor",
				"#000000", id, new RGBParser());
		int alpha = ph.getParsedParameterValue("alpha", "255", id,
				new DoubleParser()).intValue();
		StyledText st = new StyledText(foregroundColor, backgroundColor,
				fontName, fontStyle, fontSize, alpha);
		return st;
	}

	public void init(StyleType st) {
		m_st = st;
	}

}
