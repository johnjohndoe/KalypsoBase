package org.kalypso.chart.framework.model.layer;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


/**
 * @author gernot
 */
public class EditInfo
{
  public final Rectangle shape;

  public final IEditableChartLayer< ? , ? > layer;

  public final Object data;

  public Point pos;

  public String text;

  public EditInfo( final IEditableChartLayer< ? , ? > editLayer, final Rectangle editShape, final Object editData, final String editText, final Point mousePos )
  {
    layer = editLayer;
    shape = editShape;
    data = editData;
    text = editText;
    pos = mousePos;
  }
}
