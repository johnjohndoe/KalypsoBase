package de.openali.odysseus.chart.framework.model.layer;

import java.awt.event.MouseEvent;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.figure.IPaintable;

/**
 * @author Gernot Belger
 * @author burtscher1
 */
public class EditInfo implements Cloneable
{
  private final IPaintable m_hoverFigure;

  private final IPaintable m_editFigure;

  private final ITooltipChartLayer m_layer;

  private final Object m_data;

  private final Point m_position;

  private final String m_text;

  private final int m_mouseButton;

  public EditInfo( final ITooltipChartLayer editLayer, final IPaintable hoverFigure, final IPaintable editFigure, final Object editData, final String editText, final Point mousePosition )
  {
    m_layer = editLayer;
    m_hoverFigure = hoverFigure;
    m_editFigure = editFigure;
    m_data = editData;
    m_text = editText;
    m_position = mousePosition;
    m_mouseButton = MouseEvent.NOBUTTON;
  }

  public EditInfo( final EditInfo info )
  {
    m_layer = info.getLayer();
    m_hoverFigure = info.getHoverFigure();
    m_editFigure = info.getEditFigure();
    m_data = info.getData();
    m_text = info.getText();
    m_position = info.getPosition();
    m_mouseButton = info.getMouseButton();
  }

  @Override
  public EditInfo clone( )
  {
    return new EditInfo( getLayer(), getHoverFigure(), getEditFigure(), getData(), getText(), getPosition() );
  }

  public IPaintable getHoverFigure( )
  {
    return m_hoverFigure;
  }

  public IPaintable getEditFigure( )
  {
    return m_editFigure;
  }

  public ITooltipChartLayer getLayer( )
  {
    return m_layer;
  }

  public Object getData( )
  {
    return m_data;
  }

  public Point getPosition( )
  {
    return m_position;
  }

  public String getText( )
  {
    return m_text;
  }

  public int getMouseButton( )
  {
    return m_mouseButton;
  }
}