package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;

/**
 * @author burtscher Abstract implementation of IAxis - implements some methods which are equal for all concrete
 *         IAxis-classes
 */
public abstract class AbstractAxis extends AbstractMapper implements IAxis
{
  private final String m_id;

  private String m_label = "";

  private final POSITION m_pos;

  private boolean m_visible = true;

  private DIRECTION m_dir = DIRECTION.POSITIVE;

  private IDataRange<Number> m_dataRange;

  private IAxisAdjustment m_preferredAdjustment = null;

  private final Class< ? > m_dataClass;

  public AbstractAxis( final String id, final POSITION pos, final Class< ? > dataClass )
  {
    super( id );
    m_id = id;
    m_pos = pos;
    m_dataClass = dataClass;
  }

  public boolean isVisible( )
  {
    return m_visible;
  }

  public void setVisible( boolean visible )
  {
    if( visible == m_visible )
      return;
    m_visible = visible;
    getEventHandler().fireMapperChanged( this );
  }

// /**
// * @see org.kalypso.chart.framework.axis.IAxis#getRenderer()
// */
// @Deprecated
// public IAxisRenderer getRenderer( )
// {
// if( m_registry == null )
// throw new IllegalStateException( "Registry is null" );
//
// return m_registry.getRenderer( this );
// }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getLabel()
   */
  public String getLabel( )
  {
    return m_label;
  }

  public void setLabel( String label )
  {
    if( !getLabel().equals( label ) )
    {
      m_label = label;
      getEventHandler().fireMapperChanged( this );
    }
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
    getEventHandler().fireMapperChanged( this );
  }

  public IAxisAdjustment getPreferredAdjustment( )
  {
    return m_preferredAdjustment;
  }

  public void setPreferredAdjustment( IAxisAdjustment adj )
  {
    m_preferredAdjustment = adj;
  }

  public Class< ? > getDataClass( )
  {
    return m_dataClass;
  }
}
