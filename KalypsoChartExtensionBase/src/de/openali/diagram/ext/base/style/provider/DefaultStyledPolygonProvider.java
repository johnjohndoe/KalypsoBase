package de.openali.diagram.ext.base.style.provider;

import org.eclipse.swt.graphics.RGB;

import de.openali.diagram.ext.base.style.StyledPolygon;
import de.openali.diagram.factory.configuration.exception.StyledElementProviderException;
import de.openali.diagram.factory.configuration.parameters.IParameterContainer;
import de.openali.diagram.factory.configuration.parameters.impl.DoubleParser;
import de.openali.diagram.factory.configuration.parameters.impl.RGBParser;
import de.openali.diagram.factory.configuration.xsd.StyleType;
import de.openali.diagram.factory.provider.IStyledElementProvider;
import de.openali.diagram.factory.util.DiagramFactoryUtilities;
import de.openali.diagram.framework.model.styles.IStyledElement;

public class DefaultStyledPolygonProvider implements IStyledElementProvider {

	private StyleType m_st;

	public IStyledElement getStyledElement()
			throws StyledElementProviderException {
		String id= m_st.getId();
	    IParameterContainer pc=DiagramFactoryUtilities.createParameterContainer(m_st.getProvider(), id);
	    
	    int borderWidth = pc.getParsedParameterValue( "borderWidth", "0", id, new DoubleParser() ).intValue();
	    RGB borderColor = pc.getParsedParameterValue( "borderColor", "#000000", id, new RGBParser());
	    RGB fillColor = pc.getParsedParameterValue( "fillColor", "#ffffff", id, new RGBParser());
	    int alpha = pc.getParsedParameterValue( "alpha", "255", id, new DoubleParser() ).intValue();
	    StyledPolygon sp = new StyledPolygon( fillColor, borderWidth, borderColor, alpha );
	      
	    return sp;
	}

	public void init(StyleType st) {
		m_st=st;
	}

}
