package de.openali.diagram.framework.model.mapper;

import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Point;

/**
 * @author burtscher
 * 
 * some enumerations that describe axis characteristics
 */
public interface IAxisConstants
{
  /**
   * The position of the axis in the chart
   */
  public enum POSITION
  {
    TOP(ORIENTATION.HORIZONTAL),
    LEFT(ORIENTATION.VERTICAL),
    BOTTOM(ORIENTATION.HORIZONTAL),
    RIGHT(ORIENTATION.VERTICAL);

    private final ORIENTATION m_orientation;

    POSITION( ORIENTATION orientation )
    {
      m_orientation = orientation;
    }

    public ORIENTATION getOrientation( )
    {
      return m_orientation;
    }
  }

  public enum ORIENTATION implements ICoordinateSwitcher
  {
    VERTICAL(true),
    HORIZONTAL(false);

    private final boolean m_switsch;

    private ORIENTATION( final boolean switsch )
    {
      m_switsch = switsch;
    }

    public int toInt( )
    {
      return m_switsch ? -1 : 1;
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

  /**
   * The property of the data.
   */
  public enum PROPERTY
  {
    CONTINUOUS,
    DISCRETE;
  }

  /**
   * An axis direction can be one of:
   * <ul>
   * <li>POSITIVE: from bottom to top, or from left to right
   * <li>NEGATIVE: from top to bottom, or from right to left
   * </ul>
   * 
   * @author alibu
   */
  public enum DIRECTION
  {
    POSITIVE,
    NEGATIVE;
  }
}
