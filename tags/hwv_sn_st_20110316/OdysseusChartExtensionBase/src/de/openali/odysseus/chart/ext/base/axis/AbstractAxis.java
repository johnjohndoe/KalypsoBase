package de.openali.odysseus.chart.ext.base.axis;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRangeRestriction;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

/**
 * @author burtscher Abstract implementation of IAxis - implements some methods which are equal for all concrete
 *         IAxis-classes
 */
public abstract class AbstractAxis extends AbstractMapper implements IAxis
{
  private final Class< ? > m_dataClass;

  private DIRECTION m_dir = DIRECTION.POSITIVE;

  private int m_height = 1;

  private final String m_id;

  private String m_label = "";

  private IDataRange<Number> m_numericRange = new DataRange<Number>( null, null );

  private final POSITION m_pos;

  private IAxisAdjustment m_preferredAdjustment = null;

  private DataRangeRestriction<Number> m_rangeRestriction = null;

  private IAxisRenderer m_renderer;

  private boolean m_visible = true;

  private Object m_selection;

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
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxis#getRangeRestriction()
   */
  @Override
  public DataRangeRestriction<Number> getRangeRestriction( )
  {
    return m_rangeRestriction;
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

  private boolean hasNullValues( final IDataRange<Number> range, final DataRangeRestriction<Number> restriction )
  {
    if( restriction == null || range == null || range.getMin() == null || range.getMax() == null || restriction.getMin() == null || restriction.getMax() == null || restriction.getMinRange() == null
        || restriction.getMaxRange() == null )
      return true;

    return false;
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
    final Number rangeMin = range.getMin();
    final Number rangeMax = range.getMax();

    if( rangeMax == m_numericRange.getMax() && rangeMin == m_numericRange.getMin() )
      return;
    if( rangeMin == null || rangeMax == null )
      m_numericRange = new DataRange<Number>( rangeMin, rangeMax );
    else
      m_numericRange = validateDataRange( range, getRangeRestriction() );

    fireMapperChanged( this );
  }

  @Override
  public void setPreferredAdjustment( final IAxisAdjustment adj )
  {
    m_preferredAdjustment = adj;

    fireMapperChanged( this );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxis#setRangeRestriction(de.openali.odysseus.chart.framework.model.data.IDataRange)
   */
  @Override
  public void setRangeRestriction( final DataRangeRestriction<Number> range )
  {
    m_rangeRestriction = range;
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
    return String.format( "%s {%s, %s, %s }", m_label, m_id, m_pos, m_dir ); //$NON-NLS-1$
  }

  protected IDataRange<Number> validateDataRange( final IDataRange<Number> range, final DataRangeRestriction<Number> restriction )
  {
    if( hasNullValues( range, restriction ) )
      return range;

    final double restrictionMin = restriction.getMin().doubleValue();
    final double restrictionMax = restriction.getMax().doubleValue();
    final double restrictionMinRange = restriction.getMinRange().doubleValue();
    final double restrictionMaxRange = restriction.getMaxRange().doubleValue();
    final double rangeMin = range.getMin().doubleValue();
    final double rangeMax = range.getMax().doubleValue();
    final Double rangeSize = rangeMax - rangeMin;

    final IAxisAdjustment adj = getPreferredAdjustment();
    final double adjAfter = adj == null ? 0.0 : adj.getAfter();
    final double adjBefore = adj == null ? 0.0 : adj.getBefore();
    final double adjRange = adj == null ? 100.0 : adj.getRange();
    final double adjSum = adjAfter + adjBefore + adjRange;
    final double adjAfterPercent = adjAfter / adjSum;
    final double adjBeforePercent = adjBefore / adjSum;
    final double adjRangePercent = adjRange / adjSum;

    final double newRestrictionMin = Double.isInfinite( restrictionMin ) ? restrictionMin : restrictionMin - rangeSize * adjBeforePercent;
    final double newRestrictionMax = Double.isInfinite( restrictionMax ) ? restrictionMax : restrictionMax + rangeSize * adjAfterPercent;
    final double newRestrictionMinRange = restrictionMinRange / adjRangePercent;
    final double newRestrictionMaxRange = restrictionMaxRange / adjRangePercent;

    double newRangeMin = rangeMin;
    double newRangeMax = rangeMax;
    if( newRestrictionMin > rangeMin || restriction.isFixMinValue() )
    {
      newRangeMin = newRestrictionMin;
      newRangeMax = restriction.isFixMaxValue() ? newRestrictionMax : Math.min( newRestrictionMin + rangeSize, newRestrictionMax );
    }
    if( newRestrictionMax < rangeMax || restriction.isFixMaxValue() )
    {
      newRangeMax = newRestrictionMax;
      newRangeMin = restriction.isFixMinValue() ? newRestrictionMin : Math.max( newRestrictionMax - rangeSize, newRestrictionMin );
    }

    final double newRangeSize = newRangeMax - newRangeMin;

    if( newRangeSize > newRestrictionMaxRange )
    {
      final double delta = newRangeSize - newRestrictionMaxRange;
      final double min = Math.max( newRangeMin + delta / 2.0, newRestrictionMin );
      final double max = Math.min( min + newRestrictionMaxRange, newRestrictionMax );
      return new DataRange<Number>( min, max );
    }
    if( newRangeSize < newRestrictionMinRange )
    {
      final double delta = newRestrictionMinRange - newRangeSize;
      final double min = Math.max( newRangeMin - delta / 2.0, newRestrictionMin );
      final double max = Math.min( min + newRestrictionMinRange, newRestrictionMax );
      return new DataRange<Number>( min, max );
    }

    return new DataRange<Number>( newRangeMin, newRangeMax );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxis#getSelection()
   */
  @Override
  public Object getSelection( )
  {
    return m_selection;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.mapper.IAxis#setSelection(java.lang.Object)
   */
  @Override
  public void setSelection( final Point screen )
  {
    if( screen == null )
      m_selection = null;

    final int screenValue = getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) ? screen.x : screen.y;
    m_selection = screenToNumeric( screenValue );

    fireMapperChanged( this );
  }
}
