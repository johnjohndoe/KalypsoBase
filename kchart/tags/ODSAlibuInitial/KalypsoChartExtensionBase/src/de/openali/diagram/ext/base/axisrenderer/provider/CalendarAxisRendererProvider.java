package de.openali.diagram.ext.base.axisrenderer.provider;

import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import de.openali.diagram.ext.base.axisrenderer.CalendarAxisRenderer;
import de.openali.diagram.factory.configuration.exception.AxisRendererProviderException;
import de.openali.diagram.factory.configuration.parameters.IParameterContainer;
import de.openali.diagram.factory.configuration.parameters.impl.FontDataParser;
import de.openali.diagram.factory.configuration.parameters.impl.FontStyleParser;
import de.openali.diagram.factory.configuration.parameters.impl.RGBParser;
import de.openali.diagram.factory.configuration.xsd.AxisRendererType;
import de.openali.diagram.factory.provider.IAxisRendererProvider;
import de.openali.diagram.factory.util.DiagramFactoryUtilities;
import de.openali.diagram.framework.model.mapper.renderer.IAxisRenderer;

public class CalendarAxisRendererProvider implements IAxisRendererProvider {

	private AxisRendererType m_art;
	private IParameterContainer m_pc;

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

	public void init(AxisRendererType art) {
		m_art=art;
	    m_pc=DiagramFactoryUtilities.createParameterContainer(m_art.getProvider(), m_art.getId());
	}

}
