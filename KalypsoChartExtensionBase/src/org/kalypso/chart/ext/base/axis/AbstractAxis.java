package org.kalypso.chart.ext.base.axis;

import java.util.Comparator;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.AxisAdjustment;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;

/**
 * @author burtscher Abstract implementation of IAxis - implements some methods which are equal for all concrete
 *         IAxis-classes
 */
public abstract class AbstractAxis<T> extends AbstractMapper<T, Integer> implements IAxis<T>
{
  private final IMapperRegistry m_registry = null;

  private final String m_id;

  private final String m_label;

  private final PROPERTY m_prop;

  private final POSITION m_pos;

  private final DIRECTION m_dir;

  private IDataRange<T> m_dataRange;

  private AxisAdjustment m_preferredAdjustment = null;

  /**
   * Uses a ComparableComparator as dataComparator
   */
  @SuppressWarnings("unchecked")
  public AbstractAxis( final String id, final String label, final PROPERTY prop, final POSITION pos, final DIRECTION dir, final Class< ? > dataClass )
  {
    this( id, label, prop, pos, dir, new ComparableComparator(), dataClass );
  }

  public AbstractAxis( final String id, final String label, final PROPERTY prop, final POSITION pos, final DIRECTION dir, final Comparator<T> dataComparator, final Class< ? > dataClass )
  {
    super( id, dataComparator, dataClass );
    m_id = id;
    m_label = label;
    m_prop = prop;
    m_pos = pos;
    m_dir = dir;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getRenderer()
   */
  @SuppressWarnings("unchecked")
  public IAxisRenderer<T> getRenderer( )
  {
    if( m_registry == null )
    {
      throw new IllegalStateException( "Registry is null" );
    }

    return (IAxisRenderer<T>) m_registry.getRenderer( this );
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getLabel()
   */
  public String getLabel( )
  {
    return m_label;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getProperty()
   */
  public PROPERTY getProperty( )
  {
    return m_prop;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getPosition()
   */
  public POSITION getPosition( )
  {
    return m_pos;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getDirection()
   */
  public DIRECTION getDirection( )
  {
    return m_dir;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#isInverted()
   */
  public boolean isInverted( )
  {
    return getDirection() == DIRECTION.NEGATIVE;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_label + " " + "{" + m_id + " " + m_pos + " " + m_dir + "}";
  }

  public IDataRange<T> getLogicalRange( )
  {
    return m_dataRange;
  }

  public void setLogicalRange( final IDataRange<T> dataRange )
  {
    m_dataRange = dataRange;
    getEventHandler().fireMapperRangeChanged( this );
  }

  public AxisAdjustment getPreferredAdjustment( )
  {
    return m_preferredAdjustment;
  }

  public void setPreferredAdjustment( AxisAdjustment adj )
  {
    m_preferredAdjustment = adj;
  }
}
