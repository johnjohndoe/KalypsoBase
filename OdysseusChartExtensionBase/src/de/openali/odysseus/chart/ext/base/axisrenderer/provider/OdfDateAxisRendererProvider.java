package de.openali.odysseus.chart.ext.base.axisrenderer.provider;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

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
public class OdfDateAxisRendererProvider extends GenericCalendarAxisRendererProvider
{
  @Override
  public final IAxisRenderer getAxisRenderer( final POSITION position )
  {
    return super.getAxisRenderer( position, true );
  }
}
