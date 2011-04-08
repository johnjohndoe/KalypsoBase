package de.openali.odysseus.chart.framework.model.figure.impl;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.TextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

public class TextFigure extends AbstractFigure<ITextStyle>
{
  private Point m_topLeft;

  private String m_text = StringUtils.EMPTY;

  /**
   * @param points
   *          top-left position of the text
   */
  public void setPoint( final Point point )
  {
    m_topLeft = point;
  }

  public void setText( final String text )
  {
    m_text = text;
  }

  @Override
  protected void paintFigure( final GC gc )
  {
    final IStyle style = getStyle();
    if( style == null || m_topLeft == null || m_text == null )
      return;

    style.apply( gc );

    gc.drawText( m_text, m_topLeft.x, m_topLeft.y );
  }

  /**
   * returns the Style if it is set correctly; otherwise returns the default point style
   */
  @Override
  public ITextStyle getStyle( )
  {
    final ITextStyle style = super.getStyle();
    if( style == null )
      return StyleUtils.getDefaultStyle( TextStyle.class );

    return style;
  }
}
