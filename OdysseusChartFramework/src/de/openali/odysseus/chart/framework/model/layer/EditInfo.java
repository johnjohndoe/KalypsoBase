package de.openali.odysseus.chart.framework.model.layer;

import org.eclipse.swt.SWT;
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

  private Point m_position;

  private String m_text;

  private int m_mouseButton;

  public EditInfo( final ITooltipChartLayer editLayer, final IPaintable hoverFigure, final IPaintable editFigure, final Object editData, final String editText, final Point mousePosition )
  {
    m_layer = editLayer;
    m_hoverFigure = hoverFigure;
    m_editFigure = editFigure;
    m_data = editData;

    setText( editText );
    setPosition( mousePosition );
    setMouseButton( SWT.NONE );
  }

  /**
   * @see java.lang.Object#clone()
   */
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

  public void setPosition( final Point position )
  {
    m_position = position;
  }

  public Point getPosition( )
  {
    return m_position;
  }

  public void setText( final String text )
  {
    m_text = text;
  }

  public String getText( )
  {
    return m_text;
  }

  public void setMouseButton( final int mouseButton )
  {
    m_mouseButton = mouseButton;
  }

  public int getMouseButton( )
  {
    return m_mouseButton;
  }
}
