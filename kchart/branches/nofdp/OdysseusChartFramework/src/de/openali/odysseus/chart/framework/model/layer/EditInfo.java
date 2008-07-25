package de.openali.odysseus.chart.framework.model.layer;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.figure.IPaintable;

/**
 * @author Gernot Belger
 * @author burtscher1
 */
public class EditInfo
{
  public final IPaintable m_hoverFigure;

  public final IPaintable m_editFigure;

  public final ITooltipChartLayer m_layer;

  public final Object m_data;

  public Point m_pos;

  public String m_text;

  public EditInfo( final ITooltipChartLayer editLayer, final IPaintable hoverFigure, final IPaintable editFigure, final Object editData, final String editText, final Point mousePos )
  {
    m_layer = editLayer;
    m_hoverFigure = hoverFigure;
    m_editFigure = editFigure;
    m_data = editData;
    m_text = editText;
    m_pos = mousePos;
  }
}
