package org.kalypso.chart.ext.base.axisrenderer.provider;

import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.axisrenderer.CalendarAxisRenderer;
import org.kalypso.chart.factory.configuration.exception.AxisRendererProviderException;
import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.configuration.parameters.impl.FontDataParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.ksp.chart.factory.AxisRendererType;
import org.kalypso.chart.factory.provider.AbstractAxisRendererProvider;
import org.kalypso.chart.factory.provider.IAxisRendererProvider;
import org.kalypso.chart.factory.util.ChartFactoryUtilities;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;


public class CalendarAxisRendererProvider extends AbstractAxisRendererProvider {


	public IAxisRenderer getAxisRenderer() throws AxisRendererProviderException {
		RGBParser rgbp=new RGBParser();
	    RGB fgRGB=(RGB) m_pc.getParsedParameterValue( "color", "#000000", rgbp );
	    RGB bgRGB=(RGB) m_pc.getParsedParameterValue( "background-color", "#ffffff", rgbp );

	    FontDataParser fdp=new FontDataParser();
	    FontStyleParser fsp=new FontStyleParser();
	    FontData fdLabel=(FontData) m_pc.getParsedParameterValue( "font-family_label", "Arial", fdp );
	    fdLabel.setHeight( Integer.parseInt( m_pc.getParameterValue( "font-height_label", "10" ) ) );
	    fdLabel.setStyle( ((Integer) m_pc.getParsedParameterValue( "font-style_label", "NORMAL", fsp )).intValue() );
	    FontData fdTick=(FontData) m_pc.getParsedParameterValue( "font-family_tick", "Arial", fdp );
	    fdTick.setHeight( Integer.parseInt( m_pc.getParameterValue( "font-height_tick", "8" ) ) );
	    fdTick.setStyle(  ((Integer) m_pc.getParsedParameterValue( "font-style_tick", "NORMAL", fsp )).intValue() );

	    int insetTick=Integer.parseInt( m_pc.getParameterValue( "inset_tick", "1" ));
	    Insets insetsTick = new Insets( insetTick, insetTick, insetTick, insetTick );
	    int insetLabel=Integer.parseInt( m_pc.getParameterValue( "inset_label", "1" ));
	    Insets insetsLabel = new Insets( insetLabel, insetLabel, insetLabel, insetLabel );

	    String dateFormatString=m_pc.getParameterValue( "dateFormat", "yyyy-MM-dd\nhh:mm:ss" );
	    //Steuerzeichen aus Config ersetzen
	    dateFormatString=dateFormatString.replace( "\\n", "\n" );
	    IAxisRenderer<Calendar> calendarAxisRenderer = new CalendarAxisRenderer( fgRGB, bgRGB, 1, 5, insetsTick, insetsLabel, 0, fdLabel, fdTick, new SimpleDateFormat( dateFormatString ) );
	    return calendarAxisRenderer;
	}

	public Class<?> getDataClass() {
		return Calendar.class;
	}



}
