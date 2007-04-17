package de.openali.diagram.ext.base.style.provider;

import org.eclipse.swt.graphics.RGB;

import de.openali.diagram.ext.base.style.StyledLine;
import de.openali.diagram.factory.configuration.parameters.IParameterContainer;
import de.openali.diagram.factory.configuration.parameters.impl.LineStyleParser;
import de.openali.diagram.factory.configuration.parameters.impl.DoubleParser;
import de.openali.diagram.factory.configuration.parameters.impl.RGBParser;
import de.openali.diagram.factory.configuration.xsd.StyleType;
import de.openali.diagram.factory.provider.IStyledElementProvider;
import de.openali.diagram.factory.util.DiagramFactoryUtilities;
import de.openali.diagram.framework.model.styles.IStyledElement;

public class DefaultStyledLineProvider implements IStyledElementProvider {


	private StyleType m_st;

	public IStyledElement getStyledElement()
	{
		String id= m_st.getId();
	    IParameterContainer pc=DiagramFactoryUtilities.createParameterContainer(m_st.getProvider(), id);
	    
	    
	    int alpha = pc.getParsedParameterValue( "alpha", "255", id, new DoubleParser()).intValue() ;
	    int lineWidth = pc.getParsedParameterValue( "lineWidth", "1", id, new DoubleParser()).intValue() ;
	    RGB lineColor = pc.getParsedParameterValue( "lineColor", "#000000", id, new RGBParser());
	    int lineStyle = pc.getParsedParameterValue( "lineStyle", "SOLID", id, new LineStyleParser());
	
	    StyledLine sl = new StyledLine( lineWidth, lineColor, lineStyle, alpha );
	    return sl;
	}

	public void init(StyleType st) {
		m_st=st;
	}

}
