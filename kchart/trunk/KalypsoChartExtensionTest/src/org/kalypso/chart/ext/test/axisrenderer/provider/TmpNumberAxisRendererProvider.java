package org.kalypso.chart.ext.test.axisrenderer.provider;

import java.awt.Insets;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.test.axisrenderer.TmpNumberAxisRenderer;
import org.kalypso.chart.factory.configuration.exception.AxisRendererProviderException;
import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.configuration.parameters.impl.BooleanParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontDataParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractAxisRendererProvider;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;

public class TmpNumberAxisRendererProvider extends AbstractAxisRendererProvider
{

  public IAxisRenderer getAxisRenderer( ) throws AxisRendererProviderException
  {
    final RGBParser rgbp = new RGBParser();
    final IParameterContainer pc = getParameterContainer();

    final RGB fgRGB = pc.getParsedParameterValue( "color", "#000000", rgbp );
    final RGB bgRGB = pc.getParsedParameterValue( "background-color", "#ffffff", rgbp );

    final FontDataParser fdp = new FontDataParser();
    final FontStyleParser fsp = new FontStyleParser();
    final FontData fdLabel = pc.getParsedParameterValue( "font-family_label", "Arial", fdp );
    fdLabel.setHeight( Integer.parseInt( getParameterContainer().getParameterValue( "font-height_label", "10" ) ) );
    fdLabel.setStyle( pc.getParsedParameterValue( "font-style_label", "NORMAL", fsp ) );
    final FontData fdTick = getParameterContainer().getParsedParameterValue( "font-family_tick", "Arial", fdp );
    fdTick.setHeight( Integer.parseInt( pc.getParameterValue( "font-height_tick", "8" ) ) );
    fdTick.setStyle( pc.getParsedParameterValue( "font-style_tick", "NORMAL", fsp ) );

    final int insetTick = Integer.parseInt( pc.getParameterValue( "inset_tick", "1" ) );
    final Insets insetsTick = new Insets( insetTick, insetTick, insetTick, insetTick );
    final int insetLabel = Integer.parseInt( pc.getParameterValue( "inset_label", "1" ) );
    final Insets insetsLabel = new Insets( insetLabel, insetLabel, insetLabel, insetLabel );

    final int minTickInteval = Integer.parseInt( pc.getParameterValue( "min_tick_inteval", "0" ) );
    final int fixedWidth = Integer.parseInt( pc.getParameterValue( "fixed_width", "0" ) );
    final BooleanParser bp = new BooleanParser();
    final boolean hideCut = pc.getParsedParameterValue( "hide_cut", "false", bp );

    final IAxisRenderer<Number> tmpNumberAxisRenderer = new TmpNumberAxisRenderer( "id", fgRGB, bgRGB, 1, 5, insetsTick, insetsLabel, 0, fdLabel, fdTick, minTickInteval, fixedWidth, hideCut );
    return tmpNumberAxisRenderer;
  }

  public Class< ? > getDataClass( )
  {
    return Number.class;
  }

}
