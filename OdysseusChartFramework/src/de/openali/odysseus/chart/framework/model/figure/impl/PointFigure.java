package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.style.IMarker;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.util.FigureUtilities;

/**
 * Use {@link MarkerFigure} instead.
 * 
 * @author Gernot
 */
public class PointFigure extends AbstractFigure<IPointStyle>
{
  private Rectangle[] m_markerRectangles = new Rectangle[] {};

  public PointFigure( )
  {
    this( null );
  }

  public PointFigure( final IPointStyle style )
  {
    setStyle( style );
  }

  /**
   * @param points
   *          center position of the figure
   * @deprecated Use {@link #setCenterPoints(Point[])} instead.
   */
  @Deprecated
  public void setPoints( final Point[] points )
  {
    setCenterPoints( points );
  }

  /**
   * @param points
   *          center position of the figure
   * @return The rectangles occupied by the marker for each given point
   */
  public Rectangle[] setCenterPoints( final Point... points )
  {
    m_markerRectangles = new Rectangle[points.length];

    for( int i = 0; i < points.length; i++ )
      m_markerRectangles[i] = toMarkerRectangle( points[i] );

    return m_markerRectangles;
  }

  public Rectangle setCenterPoint( final int x, final int y )
  {
    return setCenterPoint( new Point( x, y ) );
  }

  public Rectangle setCenterPoint( final Point centerPoint )
  {
    setCenterPoints( new Point[] { centerPoint } );
    return m_markerRectangles[0];
  }

  @Override
  protected void paintFigure( final GC gc )
  {
    final IPointStyle style = getStyle();
    if( style == null )
      return;

    final boolean fillVisible = style.isFillVisible();
    final boolean strokeVisible = style.getStroke().isVisible();

    final IMarker marker = getMarker( style );

    for( final Rectangle markerRect : m_markerRectangles )
      marker.paint( gc, markerRect.x, markerRect.y, markerRect.width, markerRect.height, strokeVisible, fillVisible );
  }

  private IMarker getMarker( final IPointStyle style )
  {
    final IMarker marker = style.getMarker();

    if( marker == null )
      return IDefaultStyles.DEFAULT_MARKER;

    return marker;
  }

  public Rectangle toMarkerRectangle( final Point point )
  {
    final int x = point.x;
    final int y = point.y;

    final IPointStyle style = getStyle();
    if( style == null )
      return new Rectangle( x, y, 0, 0 );

    final int width = style.getWidth();
    final int height = style.getHeight();

    final Point leftTopPoint = FigureUtilities.centerToLeftTop( point, width, height );
    return new Rectangle( leftTopPoint.x, leftTopPoint.y, width, height );
  }

//  /**
//   * returns the Style if it is set correctly; otherwise returns the default point style
//   */
//  @Override
//  public IPointStyle getStyle( )
//  {
//    final IPointStyle style = super.getStyle();
//    if( style != null )
//      return style;
//    else
//      // FIXME: bad
//      return StyleUtils.getDefaultStyle( PointStyle.class );
//  }
}
