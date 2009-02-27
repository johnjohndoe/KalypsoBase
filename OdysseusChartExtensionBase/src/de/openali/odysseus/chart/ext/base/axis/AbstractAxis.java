package de.openali.odysseus.chart.ext.base.axis;

import java.util.Comparator;

import org.apache.commons.collections.comparators.ComparableComparator;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

/**
 * @author burtscher Abstract implementation of IAxis - implements some methods which are equal for all concrete
 *         IAxis-classes
 */
public abstract class AbstractAxis extends AbstractMapper<Number, Integer> implements IAxis
{
  private final IMapperRegistry m_registry = null;

  private final String m_id;

  private String m_label = "";

  private final POSITION m_pos;

  private DIRECTION m_dir = DIRECTION.POSITIVE;

  private IDataRange<Number> m_dataRange;

  private IAxisAdjustment m_preferredAdjustment = null;

  /**
   * Uses a ComparableComparator as dataComparator
   */
  @SuppressWarnings("unchecked")
  public AbstractAxis( final String id, final POSITION pos, final Class< ? > dataClass )
  {
    this( id, pos, new ComparableComparator(), dataClass );
  }

  public AbstractAxis( final String id, final POSITION pos, final Comparator<Number> dataComparator, final Class< ? > dataClass )
  {
    super( id, dataComparator, dataClass );
    m_id = id;
    m_pos = pos;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getRenderer()
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  public IAxisRenderer getRenderer( )
  {
    if( m_registry == null )
    {
      throw new IllegalStateException( "Registry is null" );
    }

    return m_registry.getRenderer( this );
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getLabel()
   */
  public String getLabel( )
  {
    return m_label;
  }

  public void setLabel( String label )
  {
    m_label = label;
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

  public void setDirection( DIRECTION dir )
  {
    m_dir = dir;
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

  public IDataRange<Number> getLogicalRange( )
  {
    return m_dataRange;
  }

  public void setLogicalRange( final IDataRange<Number> dataRange )
  {
    m_dataRange = dataRange;
    getEventHandler().fireMapperRangeChanged( this );
  }

  public IAxisAdjustment getPreferredAdjustment( )
  {
    return m_preferredAdjustment;
  }

  public void setPreferredAdjustment( IAxisAdjustment adj )
  {
    m_preferredAdjustment = adj;
  }
}
