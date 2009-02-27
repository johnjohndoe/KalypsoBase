package de.openali.diagram.ext.base.style.provider;

import org.eclipse.swt.graphics.RGB;

import de.openali.diagram.ext.base.style.StyledPoint;
import de.openali.diagram.factory.configuration.exception.StyledElementProviderException;
import de.openali.diagram.factory.configuration.parameters.IParameterContainer;
import de.openali.diagram.factory.configuration.parameters.impl.DoubleParser;
import de.openali.diagram.factory.configuration.parameters.impl.RGBParser;
import de.openali.diagram.factory.configuration.xsd.StyleType;
import de.openali.diagram.factory.provider.IStyledElementProvider;
import de.openali.diagram.factory.util.DiagramFactoryUtilities;
import de.openali.diagram.framework.model.styles.IStyledElement;

public class DefaultStyledPointProvider implements IStyledElementProvider {

	private StyleType m_st;

	public IStyledElement getStyledElement()
			throws StyledElementProviderException {
		String id = m_st.getId();
		IParameterContainer ph = DiagramFactoryUtilities
				.createParameterContainer(m_st.getProvider(), id);

		int pointWidth = ph.getParsedParameterValue("pointWidth", "5", id,
				new DoubleParser()).intValue();
		int pointHeight = ph.getParsedParameterValue("pointHeight", "5", id,
				new DoubleParser()).intValue();
		int borderWidth = ph.getParsedParameterValue("borderWidth", "1", id,
				new DoubleParser()).intValue();
		RGB fillColor = ph.getParsedParameterValue("fillColor", "#ffffff", id,
				new RGBParser());
		RGB borderColor = ph.getParsedParameterValue("borderColor", "#000000",
				id, new RGBParser());
		int alpha = ph.getParsedParameterValue("alpha", "255", id,
				new DoubleParser()).intValue();
		StyledPoint sp = new StyledPoint(pointWidth, pointHeight, fillColor,
				borderWidth, borderColor, alpha);
		return sp;
	}

	public void init(StyleType st) {
		m_st = st;
	}

}
