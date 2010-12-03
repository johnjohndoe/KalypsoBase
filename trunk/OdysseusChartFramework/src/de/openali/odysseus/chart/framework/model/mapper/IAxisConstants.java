package de.openali.odysseus.chart.framework.model.mapper;

import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Point;

/**
 * @author burtscher some enumerations that describe axis characteristics
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

    POSITION( final ORIENTATION orientation )
    {
      m_orientation = orientation;
    }

    public ORIENTATION getOrientation( )
    {
      return m_orientation;
    }
  }

  public enum ALIGNMENT
  {
    LEFT(0),
    RIGHT(1),
    TOP(1),
    BOTTOM(0),
    CENTER(0.5),
    CENTERED_HORIZONTAL(0.5),
    CENTERED_VERTICAL(0.5),
    TICK_CENTERED(0.5),
    INTERVALL_CENTERED(0.5);

    private final double m_doubleValue;

    ALIGNMENT( final double doubleValue )
    {
      m_doubleValue = doubleValue;
    }

    public double doubleValue( )
    {
      return m_doubleValue;
    }

  }

  public enum ORIENTATION implements ICoordinateSwitcher
  {
    VERTICAL(true),
    HORIZONTAL(false);

    private final boolean m_applySwitsh;

    private ORIENTATION( final boolean applySwitsch )
    {
      m_applySwitsh = applySwitsch;
    }

    @Override
    public int toInt( )
    {
      return m_applySwitsh ? -1 : 1;
    }

    @Override
    public double getX( final Point2D point )
    {
      return m_applySwitsh ? point.getY() : point.getX();
    }

    @Override
    public double getY( final Point2D point )
    {
      return m_applySwitsh ? point.getX() : point.getY();
    }

    @Override
    public int getX( final Point point )
    {
      return m_applySwitsh ? point.y : point.x;
    }

    @Override
    public int getY( final Point point )
    {
      return m_applySwitsh ? point.x : point.y;
    }
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

  /**
   * Axis Render Data key's
   */
  String BORDER_SIZE = "de.openali.odysseus.chart.framework.model.mapper.IAxisRendere_border_size";

  String TICK_LENGTH = "de.openali.odysseus.chart.framework.model.mapper.IAxisRendere_tick_length";

  String AXIS_GAP = "de.openali.odysseus.chart.framework.model.mapper.IAxisRendere_gap";

  String SCREEN_COORDINATE_AXIS = "de.openali.odysseus.chart.ext.base.axis.ScreenCoordinateAxis";

  String SCREEN_COORDINATE_AXIS_HORIZONTAL = SCREEN_COORDINATE_AXIS + "#HORIZONTAL";

  String SCREEN_COORDINATE_AXIS_VERTICAL = SCREEN_COORDINATE_AXIS + "#VERTICAL";

}
