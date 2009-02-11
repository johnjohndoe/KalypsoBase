package org.kalypso.chart.ext.base.style;

import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.kalypso.chart.framework.util.ChartUtilities;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * TODO: use separate alpha for border and filling.
 * 
 * @author burtscher
 */
public class StyledPoint extends AbstractStyledElement implements IStyledElement
{
  private int m_width = 5;

  private int m_height = 7;

  private final RGB m_borderColor;

  private final RGB m_fillColor;

  private final int m_borderWidth;

  private final int m_alpha;

  public StyledPoint( final String id, final int width, final int height, final RGB fillColor, final int borderWidth, final RGB borderColor, final int alpha )
  {
    super( id );
    m_alpha = alpha;
    m_borderColor = borderColor;
    m_fillColor = fillColor;
    m_borderWidth = borderWidth;
    m_width = width;
    m_height = height;
  }

  /**
   * Wandelt einen Point, der das Zentrum des Elements angibt, in einen Point um, der die linke obere Ecke des Elements
   * angibt
   */
  private Point centerToLeftTop( final Point p )
  {
    return new Point( p.x - ((int) (0.5 * m_width)), p.y - ((int) (0.5 * m_height)) );
  }

  /**
   * @see org.kalypso.chart.framework.styles.IStyledElement#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( final GCWrapper gc )
  {
    final Device dev = gc.getDevice();
    ChartUtilities.resetGC( gc.m_gc );
    gc.setAlpha( m_alpha );
    final List<Point> path = getPath();

    final Color borderColor = new Color( dev, m_borderColor );
    final Color fillColor = new Color( dev, m_fillColor );
    if( path != null )
    {
      for( int i = 0; i < path.size(); i++ )
      {
        final Point p = centerToLeftTop( path.get( i ) );
        gc.setForeground( borderColor );
        gc.setBackground( fillColor );
        gc.fillOval( p.x, p.y, m_width, m_height );
        gc.setLineWidth( m_borderWidth );
        gc.drawOval( p.x, p.y, m_width, m_height );
      }
    }
    borderColor.dispose();
    fillColor.dispose();
  }

  /**
   * @see org.kalypso.chart.framework.styles.IStyledElement#getType()
   */
  public SE_TYPE getType( )
  {
    return SE_TYPE.POINT;
  }

  public static StyledPoint getDefault( )
  {
    final String defaultId = "defaultPointStyle_" + System.currentTimeMillis() + Math.random() * 10000;
    return new StyledPoint( defaultId, 3, 3, new RGB( 230, 230, 230 ), 1, new RGB( 0, 0, 0 ), 255 );
  }

}
