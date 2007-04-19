package org.kalypso.chart.ext.base.axisrenderer.provider;

import java.awt.Insets;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.axisrenderer.DoubleAxisRenderer;
import org.kalypso.chart.factory.configuration.exception.AxisRendererProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.FontDataParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractAxisRendererProvider;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;


public class DoubleAxisRendererProvider extends AbstractAxisRendererProvider {


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



}
