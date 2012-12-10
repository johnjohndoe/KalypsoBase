package de.openali.odysseus.chart.framework.model.figure.impl;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.TextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chart.framework.util.img.GenericChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;

public class TextFigure extends AbstractFigure<ITextStyle>
{
  private Point m_topLeft;

  private String m_text = StringUtils.EMPTY;

  private TitleTypeBean m_titleType = null;

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

  @Override
  protected void paintFigure( final GC gc )
  {
    if( m_titleType == null )
    {
      final IStyle style = getStyle();
      if( style == null || m_topLeft == null || m_text == null )
        return;

      style.apply( gc );

      gc.drawText( m_text, m_topLeft.x, m_topLeft.y );
    }
    else
    {
      final IChartLabelRenderer labelRenderer = new GenericChartLabelRenderer( m_titleType );
      labelRenderer.paint( gc, m_topLeft );
    }
  }

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

  public void setTitleType( final TitleTypeBean titleTypeBean )
  {
    m_titleType = titleTypeBean;
  }
}
