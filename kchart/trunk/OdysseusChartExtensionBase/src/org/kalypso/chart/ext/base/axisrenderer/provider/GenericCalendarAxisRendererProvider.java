package org.kalypso.chart.ext.base.axisrenderer.provider;

import java.awt.Insets;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.axisrenderer.DateLabelCreator;
import org.kalypso.chart.ext.base.axisrenderer.GenericAxisRenderer;
import org.kalypso.chart.ext.base.axisrenderer.GenericDateTickCalculator;
import org.kalypso.chart.factory.configuration.exception.AxisRendererProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.BooleanParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontDataParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;

import de.openali.odysseus.chart.factory.provider.AbstractAxisRendererProvider;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

/**
 * 
 * Parameters: <table>
 * <tr style='color:blue; font-weight:bold'>
 * <td>ID</td>
 * <td>Description</td>
 * <td>Examples</td>
 * <td>default
 * <td> </tr>
 * <tr>
 * <td>color</td>
 * <td>Color of axis foreground</td>
 * <td>#FF22CC</td>
 * <td>#000000</td>
 * </tr>
 * 
 * <tr>
 * <td>background-color</td>
 * <td>Color of axis background</td>
 * <td></td>
 * <td></td>
 * </tr>
 * 
 * <tr>
 * <td>font-family_label</td>
 * <td>Font-family of label</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>font-height_label</td>
 * <td>Height of label</td>
 * <td></td>
 * <td></td>
 * </tr>
 * </table>
 * 
 * 
 * @author alibu
 * 
 */
public class GenericCalendarAxisRendererProvider extends AbstractAxisRendererProvider
{

  @SuppressWarnings("unused")
  public IAxisRenderer< ? > getAxisRenderer( ) throws AxisRendererProviderException
  {

    String dateFormat = getParameterContainer().getParameterValue( "date-format", "dd.MM.yy\nHH:mm:ss" );
    dateFormat = dateFormat.replace( "\\n", "\n" );

    final RGBParser rgbp = new RGBParser();
    final RGB fgRGB = getParameterContainer().getParsedParameterValue( "color", "#000000", rgbp );
    final RGB bgRGB = getParameterContainer().getParsedParameterValue( "background-color", "#ffffff", rgbp );

    final FontDataParser fdp = new FontDataParser();
    final FontStyleParser fsp = new FontStyleParser();
    final FontData fdLabel = getParameterContainer().getParsedParameterValue( "font-family_label", "Arial", fdp );
    fdLabel.setHeight( Integer.parseInt( getParameterContainer().getParameterValue( "font-height_label", "10" ) ) );
    fdLabel.setStyle( (getParameterContainer().getParsedParameterValue( "font-style_label", "NORMAL", fsp )).intValue() );
    final FontData fdTick = getParameterContainer().getParsedParameterValue( "font-family_tick", "Arial", fdp );
    fdTick.setHeight( Integer.parseInt( getParameterContainer().getParameterValue( "font-height_tick", "8" ) ) );
    fdTick.setStyle( (getParameterContainer().getParsedParameterValue( "font-style_tick", "NORMAL", fsp )).intValue() );

    final int insetTick = Integer.parseInt( getParameterContainer().getParameterValue( "inset_tick", "1" ) );
    final Insets insetsTick = new Insets( insetTick, insetTick, insetTick, insetTick );
    final int insetLabel = Integer.parseInt( getParameterContainer().getParameterValue( "inset_label", "1" ) );
    final Insets insetsLabel = new Insets( insetLabel, insetLabel, insetLabel, insetLabel );

    final int lineWidth = Integer.parseInt( getParameterContainer().getParameterValue( "line_width", "1" ) );
    final int tickLength = Integer.parseInt( getParameterContainer().getParameterValue( "tick_length", "5" ) );

    BooleanParser bp = new BooleanParser();
    final boolean hideCut = getParameterContainer().getParsedParameterValue( "hide_cut", "false", bp );

    // final IAxisRenderer<Number> calendarAxisRenderer = new GenericCalendarAxisRenderer(
    // getAxisRendererType().getId(), fgRGB, bgRGB, 1, 5, insetsTick, insetsLabel, 0, fdLabel, fdTick );
    final IAxisRenderer<Number> calendarAxisRenderer = new GenericAxisRenderer( getAxisRendererType().getId(), fgRGB, bgRGB, lineWidth, tickLength, insetsTick, insetsLabel, 0, fdLabel, fdTick, new DateLabelCreator( dateFormat ), new GenericDateTickCalculator(), null, hideCut );
    return calendarAxisRenderer;
  }

  public Class< ? > getDataClass( )
  {
    return Number.class;
  }

}
