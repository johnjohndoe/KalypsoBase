package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.style.IMarker;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

public class MarkerFigure extends AbstractFigure<IPointStyle>
{
  private Rectangle m_markerRect;

  public MarkerFigure( final IPointStyle style )
  {
    setStyle( style );
  }

  public Rectangle setCenterPoint( final int x, final int y )
  {
    final IPointStyle style = getStyle();
    if( style == null )
      m_markerRect = new Rectangle( x, y, 0, 0 );
    else
    {
      final int width = style.getWidth();
      final int height = style.getHeight();

      final int left = x - (int) (width / 2.0f);
      final int top = y - (int) (height / 2.0f);

      m_markerRect = new Rectangle( left, top, width, height );
    }

    return m_markerRect;
  }

  @Override
  protected void paintFigure( final GC gc )
  {
    if( m_markerRect == null )
      return;

    final IPointStyle style = getStyle();
    final IMarker marker = getMarker( style );

    final boolean fillVisible = style.isFillVisible();
    final boolean strokeVisible = style.getStroke().isVisible();

    final int width = m_markerRect.width;
    final int height = m_markerRect.height;
    final int x = m_markerRect.x;
    final int y = m_markerRect.y;

    marker.paint( gc, x, y, width, height, strokeVisible, fillVisible );
  }

  private IMarker getMarker( final IPointStyle style )
  {
    if( style == null )
      return IDefaultStyles.DEFAULT_MARKER;

    final IMarker marker = style.getMarker();
    if( marker != null )
      return marker;

    return IDefaultStyles.DEFAULT_MARKER;
  }
}