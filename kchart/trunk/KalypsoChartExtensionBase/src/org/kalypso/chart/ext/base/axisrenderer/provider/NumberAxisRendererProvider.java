package org.kalypso.chart.ext.base.axisrenderer.provider;

import java.awt.Insets;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.axisrenderer.NumberAxisRenderer;
import org.kalypso.chart.factory.configuration.exception.AxisRendererProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.FontDataParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractAxisRendererProvider;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;


public class NumberAxisRendererProvider extends AbstractAxisRendererProvider {


	public IAxisRenderer getAxisRenderer() throws AxisRendererProviderException {
		RGBParser rgbp=new RGBParser();
	    RGB fgRGB=getParameterContainer().getParsedParameterValue( "color", "#000000", rgbp );
	    RGB bgRGB=getParameterContainer().getParsedParameterValue( "background-color", "#ffffff", rgbp );

	    FontDataParser fdp=new FontDataParser();
	    FontStyleParser fsp=new FontStyleParser();
	    FontData fdLabel=getParameterContainer().getParsedParameterValue( "font-family_label", "Arial", fdp );
	    fdLabel.setHeight( Integer.parseInt( getParameterContainer().getParameterValue( "font-height_label", "10" ) ) );
	    fdLabel.setStyle( getParameterContainer().getParsedParameterValue( "font-style_label", "NORMAL", fsp ) );
	    FontData fdTick=getParameterContainer().getParsedParameterValue( "font-family_tick", "Arial", fdp );
	    fdTick.setHeight( Integer.parseInt( getParameterContainer().getParameterValue( "font-height_tick", "8" ) ) );
	    fdTick.setStyle( getParameterContainer().getParsedParameterValue( "font-style_tick", "NORMAL", fsp ) );
	    
	    int insetTick=Integer.parseInt( getParameterContainer().getParameterValue( "inset_tick", "1" ));
	    Insets insetsTick = new Insets( insetTick, insetTick, insetTick, insetTick );
	    int insetLabel=Integer.parseInt( getParameterContainer().getParameterValue( "inset_label", "1" ));
	    Insets insetsLabel = new Insets( insetLabel, insetLabel, insetLabel, insetLabel );

	    IAxisRenderer<Number> numberAxisRenderer = new NumberAxisRenderer( fgRGB, bgRGB, 1, 5, insetsTick, insetsLabel, 0, fdLabel, fdTick );
	    return numberAxisRenderer;
	}

	public Class<?> getDataClass() {
		return Number.class;
	}



}
