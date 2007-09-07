package org.kalypso.chart.ext.base.axisrenderer.provider;

import java.awt.Insets;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.axisrenderer.NumberAxisRenderer;
import org.kalypso.chart.factory.configuration.exception.AxisRendererProviderException;
import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.configuration.parameters.impl.FontDataParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractAxisRendererProvider;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;


public class NumberAxisRendererProvider extends AbstractAxisRendererProvider {


	public IAxisRenderer getAxisRenderer() throws AxisRendererProviderException {
		RGBParser rgbp=new RGBParser();
		IParameterContainer pc = getParameterContainer();
		
	    RGB fgRGB=pc.getParsedParameterValue( "color", "#000000", rgbp );
	    RGB bgRGB=pc.getParsedParameterValue( "background-color", "#ffffff", rgbp );

	    FontDataParser fdp=new FontDataParser();
	    FontStyleParser fsp=new FontStyleParser();
	    FontData fdLabel=pc.getParsedParameterValue( "font-family_label", "Arial", fdp );
	    fdLabel.setHeight( Integer.parseInt( getParameterContainer().getParameterValue( "font-height_label", "10" ) ) );
	    fdLabel.setStyle( pc.getParsedParameterValue( "font-style_label", "NORMAL", fsp ) );
	    FontData fdTick=getParameterContainer().getParsedParameterValue( "font-family_tick", "Arial", fdp );
	    fdTick.setHeight( Integer.parseInt( pc.getParameterValue( "font-height_tick", "8" ) ) );
	    fdTick.setStyle( pc.getParsedParameterValue( "font-style_tick", "NORMAL", fsp ) );
	    
	    //Ticks setzen; inset_tick definiert Abstände für alle Seiten; die können dann für jede Seite einzeln überschrieben werden
	    int insetTick=Integer.parseInt( pc.getParameterValue( "inset_tick", "1" ));
	    String insetTickString=(new Integer(insetTick)).toString();
	    int insetTick_left=Integer.parseInt( pc.getParameterValue( "inset_tick_left", insetTickString ));
	    int insetTick_right=Integer.parseInt( pc.getParameterValue( "inset_tick_right", insetTickString ));
	    int insetTick_bottom=Integer.parseInt( pc.getParameterValue( "inset_tick_bottom", insetTickString ));
	    int insetTick_top=Integer.parseInt( pc.getParameterValue( "inset_tick_top", insetTickString ));
	    Insets insetsTick = new Insets( insetTick_top, insetTick_left, insetTick_bottom, insetTick_right );

	    
	    int insetLabel=Integer.parseInt( pc.getParameterValue( "inset_label", "1" ));
	    String insetLabelString=(new Integer(insetLabel)).toString();
	    int insetLabel_left=Integer.parseInt( pc.getParameterValue( "inset_label_left", insetLabelString ));
        int insetLabel_right=Integer.parseInt( pc.getParameterValue( "inset_label_right", insetLabelString ));
        int insetLabel_bottom=Integer.parseInt( pc.getParameterValue( "inset_label_bottom", insetLabelString ));
        int insetLabel_top=Integer.parseInt( pc.getParameterValue( "inset_label_top", insetLabelString ));
	    Insets insetsLabel = new Insets( insetLabel_top, insetLabel_left, insetLabel_bottom, insetLabel_right );

        int lineWidth=Integer.parseInt( pc.getParameterValue( "line_width", "1"));
        
        int tickLength=Integer.parseInt( pc.getParameterValue( "tick_length", "5"));

        int gap=Integer.parseInt( pc.getParameterValue( "gap", "0"));
	    
	    IAxisRenderer<Number> numberAxisRenderer = new NumberAxisRenderer( fgRGB, bgRGB, lineWidth, tickLength, insetsTick, insetsLabel, gap, fdLabel, fdTick );
	    return numberAxisRenderer;
	}

	public Class<?> getDataClass() {
		return Number.class;
	}



}
