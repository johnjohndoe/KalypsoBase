package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

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

  private int m_height = 1;

  private DIRECTION m_dir = DIRECTION.POSITIVE;

  private IAxisAdjustment m_preferredAdjustment = null;

  private final Class< ? > m_dataClass;

  private IAxisRenderer m_renderer;

  private IDataRange<Number> m_numericRange = new DataRange<Number>( null, null );

  public AbstractAxis( final String id, final POSITION pos, final Class< ? > dataClass )
  {
    this( id, pos, dataClass, null );
  }

  public AbstractAxis( final String id, final POSITION pos, final Class< ? > dataClass, final IAxisRenderer renderer )
  {
    super( id );
    m_id = id;
    m_pos = pos;
    m_dataClass = dataClass;
    setRenderer( renderer );
  }

  @Override
  public Class< ? > getDataClass( )
  {
    return m_dataClass;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getDirection()
   */
  @Override
  public DIRECTION getDirection( )
  {
    return m_dir;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getLabel()
   */
  @Override
  public String getLabel( )
  {
    return m_label;
  }

  /**
   * @see org.kalypso.chart.framework.model.mapper.IAxis#getNumericRange()
   */
  @Override
  public IDataRange<Number> getNumericRange( )
  {
    return m_numericRange;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getPosition()
   */
  @Override
  public POSITION getPosition( )
  {
    return m_pos;
  }

  @Override
  public IAxisAdjustment getPreferredAdjustment( )
  {
    return m_preferredAdjustment;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxis#getRenderer()
   */
  @Override
  public IAxisRenderer getRenderer( )
  {
    return m_renderer;
  }

  @Override
  public int getScreenHeight( )
  {
    return m_height;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#isInverted()
   */
  @Override
  public boolean isInverted( )
  {
    return getDirection() == DIRECTION.NEGATIVE;
  }

  @Override
  public boolean isVisible( )
  {
    return m_visible;
  }

  @Override
  public void setDirection( final DIRECTION dir )
  {
    m_dir = dir;
  }

  @Override
  public void setLabel( final String label )
  {
    if( !getLabel().equals( label ) )
    {
      m_label = label;
      fireMapperChanged( this );
    }
  }

  @Override
  public void setNumericRange( final IDataRange<Number> range )
  {
    if( range.getMax() == m_numericRange.getMax() && range.getMin() == m_numericRange.getMin() )
      return;
    m_numericRange = range;
    fireMapperChanged( this );
  }

  @Override
  public void setPreferredAdjustment( final IAxisAdjustment adj )
  {
    m_preferredAdjustment = adj;
    fireMapperChanged( this );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxis#setRenderer(IAxisRenderer )
   */
  @Override
  public void setRenderer( final IAxisRenderer renderer )
  {
    if( renderer != null && renderer.equals( m_renderer ) )
      return;
    m_renderer = renderer;
  }

  @Override
  public void setScreenHeight( final int height )
  {
    if( m_height == height )
      return;
    m_height = height;
    fireMapperChanged( this );
  }

  @Override
  public void setVisible( final boolean visible )
  {
    if( visible == m_visible )
      return;
    m_visible = visible;

    fireMapperChanged( this );
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_label + " " + "{" + m_id + " " + m_pos + " " + m_dir + "}";
  }
}
