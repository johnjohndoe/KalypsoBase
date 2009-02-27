package de.openali.diagram.ext.base.axisrenderer.provider;

import java.awt.Insets;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import de.openali.diagram.ext.base.axisrenderer.DoubleAxisRenderer;
import de.openali.diagram.factory.configuration.exception.AxisRendererProviderException;
import de.openali.diagram.factory.configuration.parameters.IParameterContainer;
import de.openali.diagram.factory.configuration.parameters.impl.FontDataParser;
import de.openali.diagram.factory.configuration.parameters.impl.FontStyleParser;
import de.openali.diagram.factory.configuration.parameters.impl.RGBParser;
import de.openali.diagram.factory.configuration.xsd.AxisRendererType;
import de.openali.diagram.factory.provider.IAxisRendererProvider;
import de.openali.diagram.factory.util.DiagramFactoryUtilities;
import de.openali.diagram.framework.model.mapper.renderer.IAxisRenderer;

public class DoubleAxisRendererProvider implements IAxisRendererProvider {

	private IParameterContainer m_pc;
	private AxisRendererType m_art;

	public IAxisRenderer getAxisRenderer() throws AxisRendererProviderException {
		RGBParser rgbp=new RGBParser();
	    RGB fgRGB=m_pc.getParsedParameterValue( "color", "#000000", rgbp );
	    RGB bgRGB=m_pc.getParsedParameterValue( "background-color", "#ffffff", rgbp );

	    FontDataParser fdp=new FontDataParser();
	    FontStyleParser fsp=new FontStyleParser();
	    FontData fdLabel=m_pc.getParsedParameterValue( "font-family_label", "Arial", fdp );
	    fdLabel.setHeight( Integer.parseInt( m_pc.getParameterValue( "font-height_label", "10" ) ) );
	    fdLabel.setStyle( m_pc.getParsedParameterValue( "font-style_label", "NORMAL", fsp ) );
	    FontData fdTick=m_pc.getParsedParameterValue( "font-family_tick", "Arial", fdp );
	    fdTick.setHeight( Integer.parseInt( m_pc.getParameterValue( "font-height_tick", "8" ) ) );
	    fdTick.setStyle( m_pc.getParsedParameterValue( "font-style_tick", "NORMAL", fsp ) );
	    
	    int insetTick=Integer.parseInt( m_pc.getParameterValue( "inset_tick", "1" ));
	    Insets insetsTick = new Insets( insetTick, insetTick, insetTick, insetTick );
	    int insetLabel=Integer.parseInt( m_pc.getParameterValue( "inset_label", "1" ));
	    Insets insetsLabel = new Insets( insetLabel, insetLabel, insetLabel, insetLabel );

	    IAxisRenderer<Double> doubleAxisRenderer = new DoubleAxisRenderer( fgRGB, bgRGB, 1, 5, insetsTick, insetsLabel, 0, fdLabel, fdTick );
	    return doubleAxisRenderer;
	}

	public Class<?> getDataClass() {
		return Number.class;
	}

	public void init(AxisRendererType at) {
		m_art=at;
	    m_pc=DiagramFactoryUtilities.createParameterContainer(at.getProvider(), m_art.getId());
	}

}
