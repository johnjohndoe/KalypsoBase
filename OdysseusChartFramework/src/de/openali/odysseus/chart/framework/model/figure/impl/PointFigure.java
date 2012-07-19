package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.style.IMarker;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.impl.PointStyle;
import de.openali.odysseus.chart.framework.util.FigureUtilities;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * Use {@link MarkerFigure} instead.
 *
 * @author Gernot
 */
@Deprecated
public class PointFigure extends AbstractFigure<IPointStyle>
{
  private Point[] m_centerPoints;

  /**
   * @param points
   *          center position of the figure
   */
  public void setPoints( final Point[] points )
  {
    m_centerPoints = points;
  }

  @Override
  protected void paintFigure( final GC gc )
  {
    final IPointStyle style = getStyle();
    if( style != null && m_centerPoints != null )
    {
      IMarker marker = style.getMarker();
      if( marker == null )
        marker = IDefaultStyles.DEFAULT_MARKER;

      final int width = style.getWidth();
      final int height = style.getHeight();
      final boolean fillVisible = style.isFillVisible();
      final boolean strokeVisible = style.getStroke().isVisible();

      for( final Point centerPoint : m_centerPoints )
      {
        final Point leftTopPoint = FigureUtilities.centerToLeftTop( centerPoint, width, height );
        marker.paint( gc, leftTopPoint.x, leftTopPoint.y, width, height, strokeVisible, fillVisible );
      }
    }
  }

  /**
   * returns the Style if it is set correctly; otherwise returns the default point style
   */
  @Override
  public IPointStyle getStyle( )
  {
    final IPointStyle style = super.getStyle();
    if( style != null )
      return style;
    else
      return StyleUtils.getDefaultStyle( PointStyle.class );
  }
}
