package org.kalypso.chart.ext.base.style;

import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.framework.impl.util.ChartUtilities;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.eclipse.swt.graphics.GC;

/**
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

  public StyledPoint( String id, int width, int height, RGB fillColor, int borderWidth, RGB borderColor, int alpha )
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
  private Point centerToLeftTop( Point p )
  {
    return new Point( p.x - ((int) (0.5 * m_width)), p.y - ((int) (0.5 * m_height)) );
  }

  /**
   * @see org.kalypso.chart.framework.styles.IStyledElement#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GC gc )
  {
    final Device dev = gc.getDevice();
    ChartUtilities.resetGC( gc );
    gc.setAlpha( m_alpha );
    List<Point> path = getPath();

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
    String defaultId = "defaultPointStyle_" + System.currentTimeMillis() + Math.random() * 10000;
    return new StyledPoint( defaultId, 3, 3, new RGB( 230, 230, 230 ), 1, new RGB( 0, 0, 0 ), 255 );
  }

}
