package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.TextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chart.framework.util.img.GenericChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.IChartLabelRenderer;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;

public class TextFigure extends AbstractFigure<ITextStyle>
{
  private Rectangle m_rectangle = new Rectangle( 0, 0, -1, -1 );

  private IChartLabelRenderer m_labelRenderer = null;

  public IChartLabelRenderer getLabelRenderer( )
  {
    if( m_labelRenderer == null )
    {
      m_labelRenderer = new GenericChartLabelRenderer();
    }
    return m_labelRenderer;
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

  public TitleTypeBean getTitleType( )
  {
    return getLabelRenderer().getTitleTypeBean();
  }

  @Override
  protected void paintFigure( final GC gc )
  {
// if( m_titleType == null )
// {
// final IStyle style = getStyle();
// if( style == null || m_topLeft == null || m_text == null )
// return;
//
// style.apply( gc );
//
// gc.drawText( m_text, m_topLeft.x, m_topLeft.y );
// }
// else
// {
// final IChartLabelRenderer labelRenderer = new GenericChartLabelRenderer( m_titleType );
// labelRenderer.paint( gc, m_topLeft );
// }
    getLabelRenderer().paint( gc, m_rectangle );
  }

  public void setLabelRenderer( IChartLabelRenderer labelRenderer )
  {
    m_labelRenderer = labelRenderer;
  }

  /**
   * @param points
   *          top-left position of the text
   */
  public void setPoint( final Point point )
  {
    setRectangle( new Rectangle( point.x, point.y, -1, -1 ) );
  }

  public void setRectangle( final Rectangle rect )
  {
    m_rectangle = rect;
  }

  public void setText( final String text )
  {
    getTitleType().setText( text );
  }

  public void setTitleType( final TitleTypeBean titleTypeBean )
  {
    getLabelRenderer().setTitleTypeBean( titleTypeBean );
  }
}
