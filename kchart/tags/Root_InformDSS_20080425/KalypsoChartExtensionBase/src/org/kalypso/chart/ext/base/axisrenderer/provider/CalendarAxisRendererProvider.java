package org.kalypso.chart.ext.base.axisrenderer.provider;

import java.awt.Insets;
import java.util.Calendar;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.axisrenderer.CalendarAxisRenderer;
import org.kalypso.chart.factory.configuration.exception.AxisRendererProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.FontDataParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractAxisRendererProvider;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;

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
public class CalendarAxisRendererProvider extends AbstractAxisRendererProvider
{

  @SuppressWarnings("unused")
  public IAxisRenderer< ? > getAxisRenderer( ) throws AxisRendererProviderException
  {
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

    final IAxisRenderer<Calendar> calendarAxisRenderer = new CalendarAxisRenderer( getAxisRendererType().getId(), fgRGB, bgRGB, 1, 5, insetsTick, insetsLabel, 0, fdLabel, fdTick );
    return calendarAxisRenderer;
  }

  public Class< ? > getDataClass( )
  {
    return Calendar.class;
  }

}
