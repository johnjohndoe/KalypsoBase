package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.style.IMarker;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.impl.PointStyle;
import de.openali.odysseus.chart.framework.util.FigureUtilities;
import de.openali.odysseus.chart.framework.util.StyleUtils;

public class PointFigure extends AbstractFigure<IPointStyle>
{

  private Point[] m_centerPoints;

  private Point[] m_leftTopPoints;

  /**
   * 
   * @param points
   *          center position of the figure
   */
  public void setPoints( Point[] points )
  {
    m_centerPoints = points;
    m_leftTopPoints = null;

  }

  @Override
  public void paintFigure( GC gc )
  {
    IPointStyle style = getStyle();
    if( (style != null) && (m_centerPoints != null) )
    {
      if( m_leftTopPoints == null )
      {
        m_leftTopPoints = new Point[m_centerPoints.length];
        for( int i = 0; i < m_centerPoints.length; i++ )
        {
          Point centerPoint = m_centerPoints[i];
          m_leftTopPoints[i] = FigureUtilities.centerToLeftTop( centerPoint, style.getWidth(), style.getHeight() );
        }
      }

      style.apply( gc );
      IMarker marker = style.getMarker();
      if( marker == null )
        marker = IDefaultStyles.DEFAULT_MARKER;
      for( Point p : m_leftTopPoints )
        marker.paint( gc, p, getStyle().getWidth(), getStyle().getHeight(), getStyle().getStroke().isVisible(), getStyle().isFillVisible() );
    }

  }

  /**
   * returns the Style if it is set correctly; otherwise returns the default point style
   */
  @Override
  public IPointStyle getStyle( )
  {
    IPointStyle style = super.getStyle();
    if( style != null )
      return style;
    else
      return StyleUtils.getDefaultStyle( PointStyle.class );
  }

  @Override
  public void setStyle( IPointStyle ps )
  {
    super.setStyle( ps );
    m_leftTopPoints = null;
  }

}
