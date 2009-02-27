package de.belger.swtchart.util;

import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Point;

/**
 * @author gernot
 */
public enum SwitchDelegate
{
  HORIZONTAL( false ), VERTICAL( true );
  
  private final boolean m_switsch;

  private SwitchDelegate( final boolean switsch )
  {
    m_switsch = switsch;
  }
  public int toInt()
  {
    return m_switsch ? 1 : -1;
  }
  public double getX( final Point2D point )
  {
    return m_switsch ? point.getY() : point.getX();
  }

  public double getY( final Point2D point )
  {
    return m_switsch ? point.getX() : point.getY();
  }
  
  public int getX( final Point point )
  {
    return m_switsch ? point.y : point.x;
  }
  
  public int getY( final Point point )
  {
    return m_switsch ? point.x : point.y;
  }
}
