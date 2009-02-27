package de.belger.swtchart;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.belger.swtchart.layer.IChartLayer;

/**
 * @author gernot
 */
public class EditInfo
{
  public final Rectangle shape;
  public final IChartLayer layer;
  public final Object data;
  public Point pos;
  public String text;

  public EditInfo( final IChartLayer editLayer, final Rectangle editShape, final Object editData, final String editText )
  {
    layer = editLayer;
    shape = editShape;
    data = editData;
    text = editText;
  }
}
