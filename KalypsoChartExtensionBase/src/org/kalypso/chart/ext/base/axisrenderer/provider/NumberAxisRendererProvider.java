package org.kalypso.chart.ext.base.axisrenderer.provider;

import java.awt.Insets;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.axisrenderer.NumberAxisRenderer;
import org.kalypso.chart.factory.configuration.exception.AxisRendererProviderException;
import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.configuration.parameters.impl.BooleanParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontDataParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractAxisRendererProvider;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;

public class NumberAxisRendererProvider extends AbstractAxisRendererProvider
{

  @SuppressWarnings("unused")
  public IAxisRenderer< ? > getAxisRenderer( ) throws AxisRendererProviderException
  {
    final RGBParser rgbp = new RGBParser();
    final IParameterContainer pc = getParameterContainer();

    final RGB fgRGB = pc.getParsedParameterValue( "color", "#000000", rgbp );
    final RGB bgRGB = pc.getParsedParameterValue( "background-color", "#ffffff", rgbp );

    final String tick_label_formater = pc.getParameterValue( "tick_label_formater", "%s" );

    final FontDataParser fdp = new FontDataParser();
    final FontStyleParser fsp = new FontStyleParser();
    final FontData fdLabel = pc.getParsedParameterValue( "font-family_label", "Arial", fdp );
    fdLabel.setHeight( Integer.parseInt( getParameterContainer().getParameterValue( "font-height_label", "10" ) ) );
    fdLabel.setStyle( pc.getParsedParameterValue( "font-style_label", "NORMAL", fsp ) );
    final FontData fdTick = getParameterContainer().getParsedParameterValue( "font-family_tick", "Arial", fdp );
    fdTick.setHeight( Integer.parseInt( pc.getParameterValue( "font-height_tick", "8" ) ) );
    fdTick.setStyle( pc.getParsedParameterValue( "font-style_tick", "NORMAL", fsp ) );

    // Ticks setzen; inset_tick definiert Abstände für alle Seiten; die können dann für jede Seite einzeln überschrieben
    // werden
    final int insetTick = Integer.parseInt( pc.getParameterValue( "inset_tick", "1" ) );
    final String insetTickString = (new Integer( insetTick )).toString();
    final int insetTick_left = Integer.parseInt( pc.getParameterValue( "inset_tick_left", insetTickString ) );
    final int insetTick_right = Integer.parseInt( pc.getParameterValue( "inset_tick_right", insetTickString ) );
    final int insetTick_bottom = Integer.parseInt( pc.getParameterValue( "inset_tick_bottom", insetTickString ) );
    final int insetTick_top = Integer.parseInt( pc.getParameterValue( "inset_tick_top", insetTickString ) );
    final Insets insetsTick = new Insets( insetTick_top, insetTick_left, insetTick_bottom, insetTick_right );

    final int insetLabel = Integer.parseInt( pc.getParameterValue( "inset_label", "1" ) );
    final String insetLabelString = (new Integer( insetLabel )).toString();
    final int insetLabel_left = Integer.parseInt( pc.getParameterValue( "inset_label_left", insetLabelString ) );
    final int insetLabel_right = Integer.parseInt( pc.getParameterValue( "inset_label_right", insetLabelString ) );
    final int insetLabel_bottom = Integer.parseInt( pc.getParameterValue( "inset_label_bottom", insetLabelString ) );
    final int insetLabel_top = Integer.parseInt( pc.getParameterValue( "inset_label_top", insetLabelString ) );
    final Insets insetsLabel = new Insets( insetLabel_top, insetLabel_left, insetLabel_bottom, insetLabel_right );

    final int lineWidth = Integer.parseInt( pc.getParameterValue( "line_width", "1" ) );
    final int tickLength = Integer.parseInt( pc.getParameterValue( "tick_length", "5" ) );
    final int gap = Integer.parseInt( pc.getParameterValue( "gap", "0" ) );

    final int minTickInteval = Integer.parseInt( pc.getParameterValue( "min_tick_interval", "0" ) );
    final int fixedWidth = Integer.parseInt( pc.getParameterValue( "fixed_width", "0" ) );
    final BooleanParser bp = new BooleanParser();
    final boolean hideCut = pc.getParsedParameterValue( "hide_cut", "false", bp );

    final IAxisRenderer<Number> tmpNumberAxisRenderer = new NumberAxisRenderer( getAxisRendererType().getId(), fgRGB, bgRGB, lineWidth, tickLength, insetsTick, insetsLabel, gap, fdLabel, fdTick, minTickInteval, fixedWidth, hideCut, tick_label_formater );
    return tmpNumberAxisRenderer;
  }

  public Class< ? > getDataClass( )
  {
    return Number.class;
  }

}
