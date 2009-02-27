package org.kalypso.chart.ext.base.axisrenderer.provider;

import java.awt.Insets;

import org.kalypso.chart.ext.base.axisrenderer.DateLabelCreator;
import org.kalypso.chart.ext.base.axisrenderer.GenericAxisRenderer;
import org.kalypso.chart.ext.base.axisrenderer.GenericDateTickCalculator;

import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.config.parameters.impl.BooleanParser;
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
  public IAxisRenderer getAxisRenderer( ) throws ConfigurationException
  {

    String dateFormat = getParameterContainer().getParameterValue( "date-format", "dd.MM.yy\nHH:mm:ss" );
    dateFormat = dateFormat.replace( "\\n", "\n" );

    final int insetTick = Integer.parseInt( getParameterContainer().getParameterValue( "inset_tick", "1" ) );
    final Insets insetsTick = new Insets( insetTick, insetTick, insetTick, insetTick );
    final int insetLabel = Integer.parseInt( getParameterContainer().getParameterValue( "inset_label", "1" ) );
    final Insets insetsLabel = new Insets( insetLabel, insetLabel, insetLabel, insetLabel );

    final int tickLength = Integer.parseInt( getParameterContainer().getParameterValue( "tick_length", "5" ) );

    BooleanParser bp = new BooleanParser();
    final boolean hideCut = getParameterContainer().getParsedParameterValue( "hide_cut", "false", bp );

    final int fixedWidth = Integer.parseInt( getParameterContainer().getParameterValue( "fixed_width", "0" ) );
    final int gap = Integer.parseInt( getParameterContainer().getParameterValue( "gap", "0" ) );

    final IAxisRenderer calendarAxisRenderer = new GenericAxisRenderer( getId(), tickLength, insetsTick, insetsLabel, gap, new DateLabelCreator( dateFormat ), new GenericDateTickCalculator(), null, hideCut, fixedWidth );
    return calendarAxisRenderer;
  }

}
