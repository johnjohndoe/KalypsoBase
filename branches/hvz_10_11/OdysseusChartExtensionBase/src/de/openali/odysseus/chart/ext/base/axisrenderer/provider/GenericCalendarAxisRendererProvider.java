package de.openali.odysseus.chart.ext.base.axisrenderer.provider;

import de.openali.odysseus.chart.ext.base.axisrenderer.DateLabelCreator;
import de.openali.odysseus.chart.ext.base.axisrenderer.GenericDateTickCalculator;
import de.openali.odysseus.chart.ext.base.axisrenderer.ILabelCreator;
import de.openali.odysseus.chart.ext.base.axisrenderer.ITickCalculator;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.LABEL_POSITION;

/**
 * Parameters:
 * <table>
 * <tr style='color:blue; font-weight:bold'>
 * <td>ID</td>
 * <td>Description</td>
 * <td>Examples</td>
 * <td>default
 * <td>
 * </tr>
 * <tr>
 * <td>color</td>
 * <td>Color of axis foreground</td>
 * <td>#FF22CC</td>
 * <td>#000000</td>
 * </tr>
 * <tr>
 * <td>background-color</td>
 * <td>Color of axis background</td>
 * <td></td>
 * <td></td>
 * </tr>
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
 * @author alibu
 */
public class GenericCalendarAxisRendererProvider extends AbstractGenericAxisRendererProvider
{

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.provider.AbstractGenericAxisRendererProvider#getLabelCreator()
   */
  @Override
  public ILabelCreator getLabelCreator( )
  {
    String dateFormat = getParameterContainer().getParameterValue( "date-format", "dd.MM.yy\nHH:mm:ss" );
    final LABEL_POSITION position = getPosition();
    dateFormat = dateFormat.replaceAll( "\\n", "\n" );
    
    return new DateLabelCreator( dateFormat, position );
  }

  private LABEL_POSITION getPosition( )
  {
    final String parameter = getParameterContainer().getParameterValue( "label_position", LABEL_POSITION.TICK_CENTERED.name() );
    
    return LABEL_POSITION.valueOf( parameter );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.provider.AbstractGenericAxisRendererProvider#getTickCalculator()
   */
  @Override
  public ITickCalculator getTickCalculator( )
  {
    return new GenericDateTickCalculator();
  }

}
