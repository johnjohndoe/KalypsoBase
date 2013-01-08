package de.openali.odysseus.chart.ext.base.axis;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRangeRestriction;
import de.openali.odysseus.chart.framework.model.event.IAxisEventListener;
import de.openali.odysseus.chart.framework.model.impl.AxisVisitorBehavior;
import de.openali.odysseus.chart.framework.model.impl.IAxisVisitorBehavior;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.util.img.ChartLabelRendererFactory;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;

/**
 * @author burtscher Abstract implementation of IAxis - implements some methods which are equal for all concrete
 *         IAxis-classes
 */
public abstract class AbstractAxis<T> implements IAxis<T>
{
  private final Set<IAxisEventListener> m_listeners = new LinkedHashSet<>();

  private final String m_identifier;

  /**
   * Hashmap to store arbitrary key value pairs
   */
  private final Map<String, Object> m_data = new HashMap<>();

  private final IDataOperator<T> m_dataOperator;

  private DIRECTION m_dir = DIRECTION.POSITIVE;

  private int m_height = 1;

  private int m_offset = 0;

  private final List<TitleTypeBean> m_axisLabels = new ArrayList<>();

  private IDataRange<Double> m_numericRange = new DataRange<>( null, null );

  private final POSITION m_pos;

  private IAxisAdjustment m_preferredAdjustment = null;

  private DataRangeRestriction<Number> m_rangeRestriction = null;

  private IAxisRenderer m_renderer;

  private boolean m_visible = true;

  private boolean m_allowZoom = true;

  public AbstractAxis( final String id, final POSITION pos )
  {
    this( id, pos, null, null );
  }

  public AbstractAxis( final String id, final POSITION pos, final IAxisRenderer renderer, final IDataOperator<T> dataOperator )
  {
    m_identifier = id;
    m_pos = pos;
    setRenderer( renderer );
    m_dataOperator = dataOperator;
  }

  @Override
  public void addLabel( final TitleTypeBean title )
  {
    m_axisLabels.add( title );
  }

  @Override
  public void addListener( final IAxisEventListener listener )
  {
    m_listeners.add( listener );
  }

  @Override
  public void clearLabels( )
  {
    m_axisLabels.clear();
  }

  private void fireMapperChanged( final IAxis mapper )
  {
    final IAxisEventListener[] listeners = m_listeners.toArray( new IAxisEventListener[] {} );
    for( final IAxisEventListener listener : listeners )
    {
      listener.onAxisChanged( mapper );
    }
  }

  @Override
  public IAxisVisitorBehavior getAxisVisitorBehavior( )
  {
    return new AxisVisitorBehavior( m_allowZoom, true, true );
  }

  @Override
  public Object getData( final String id )
  {
    return m_data.get( id );
  }

  @Deprecated
  protected IDataOperator<T> getDataOperator( )
  {
    return m_dataOperator;
  }

  @Override
  public DIRECTION getDirection( )
  {
    return m_dir;
  }

  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  @Deprecated
  @Override
  public String getLabel( )
  {
    if( m_axisLabels.size() == 0 )
      return ""; //$NON-NLS-1$
    return m_axisLabels.get( 0 ).getText();
  }

  @Override
  public TitleTypeBean[] getLabels( )
  {
    return m_axisLabels.toArray( new TitleTypeBean[] {} );
  }

  @Override
  public IDataRange<T> getLogicalRange( )
  {
    return new DataRange<>( numericToLogical( m_numericRange.getMin() ), numericToLogical( m_numericRange.getMax() ) );
  }

  @Override
  public IDataRange<Double> getNumericRange( )
  {
    return m_numericRange;
  }

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

  @Override
  public DataRangeRestriction<Number> getRangeRestriction( )
  {
    return m_rangeRestriction;
  }

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

  @Override
  public int getScreenOffset( )
  {
    return m_offset;
  }

  private boolean hasNullValues( final IDataRange<Double> range, final DataRangeRestriction<Number> restriction )
  {
    if( restriction == null || range == null || range.getMin() == null || range.getMax() == null || restriction.getMin() == null || restriction.getMax() == null || restriction.getMinRange() == null
        || restriction.getMaxRange() == null )
      return true;

    return false;
  }

  public boolean isAllowZoom( )
  {
    return m_allowZoom;
  }

  protected boolean isInverted( )
  {
    if( getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      return getDirection() == DIRECTION.NEGATIVE;
    return getDirection() == DIRECTION.POSITIVE;
  }

  @Override
  public boolean isVisible( )
  {
    return m_visible;
  }

  @Override
  public int logicalToScreen( final T value )
  {
    return numericToScreen( logicalToNumeric( value ) );
  }

  @Override
  public String logicalToXMLString( final T value )
  {
    return getDataOperator().logicalToString( value );
  }

  @Override
  public Double normalizedToNumeric( final Double value )
  {
    final IDataRange<Double> dataRange = getNumericRange();

    if( dataRange.getMax() == null || dataRange.getMin() == null )
      return null;
    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();

    return Double.valueOf( value * r + dataRange.getMin().doubleValue() );
  }

  @Override
  public int normalizedToScreen( final Double value )
  {
    final int range = getScreenHeight();
    final double screen = (range * (isInverted() ? 1 - value : value));

    // REMARK: using floor here, so all values are rounded to the same direction

    return getScreenOffset() + (int)Math.floor( screen );
  }

  @Override
  public Double numericToNormalized( final Double value )
  {
    if( value == null )
    {
      return Double.NaN;
    }
    final IDataRange<Double> dataRange = getNumericRange();
    if( dataRange.getMax() == null || dataRange.getMin() == null )
      return Double.NaN;
    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();
    return (value - dataRange.getMin().doubleValue()) / r;
  }

  @Override
  public int numericToScreen( final Double value )
  {
    return normalizedToScreen( numericToNormalized( value ) );
  }

  @Override
  public void removeListener( final IAxisEventListener listener )
  {
    m_listeners.remove( listener );
  }

  @Override
  public T screenToLogical( final int value )
  {
    return numericToLogical( screenToNumeric( value ) );
  }

  @Override
  public Double screenToNormalized( final int value )
  {
    final int range = getScreenHeight();
    if( range == 0 )
      return null;
    final double normValue = (double)(value - getScreenOffset()) / range;
    return isInverted() ? 1 - normValue : normValue;
  }

  @Override
  public Double screenToNumeric( final int value )
  {
    return normalizedToNumeric( screenToNormalized( value ) );
  }

  public void setAllowZoom( final boolean allowZoom )
  {
    m_allowZoom = allowZoom;
  }

  @Override
  public void setData( final String id, final Object data )
  {
    m_data.put( id, data );
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
      m_axisLabels.clear();
      m_axisLabels.add( ChartLabelRendererFactory.getAxisLabelType( getPosition(), label, new Insets( 1, 1, 1, 1 ), null ) );

      fireMapperChanged( this );
    }
  }

  @Override
  public void setLogicalRange( final IDataRange<T> range )
  {
    setNumericRange( new DataRange<>( logicalToNumeric( range.getMin() ), logicalToNumeric( range.getMax() ) ) );
  }

  @Override
  public void setNumericRange( final IDataRange<Double> range )
  {
    final Double rangeMin = range.getMin();
    final Double rangeMax = range.getMax();

    if( rangeMax == m_numericRange.getMax() && rangeMin == m_numericRange.getMin() )
      return;
    if( rangeMin == null || rangeMax == null )
      m_numericRange = new DataRange<>( rangeMin, rangeMax );
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

  @Override
  public void setRangeRestriction( final DataRangeRestriction<Number> range )
  {
    m_rangeRestriction = range;
  }

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
    if( m_height == height )// && m_offset == offset )
      return;
    m_height = height;

    fireMapperChanged( this );
  }

  @Override
  public void setScreenOffset( final int offset, final int axisHeight )
  {
    if( m_offset != offset )// && m_offset == offset )
      m_offset = offset;
    if( m_height != axisHeight )// && m_offset == offset )
      m_height = axisHeight;
  }

  @Override
  public void setVisible( final boolean visible )
  {
    if( visible == m_visible )
      return;

    m_visible = visible;

    fireMapperChanged( this );
  }

//  @Override
//  public void setSelection( final IDataRange<Double> range )
//  {
//    if( ObjectUtils.equals( m_activeRange, range ) )
//      return;
//
//    m_activeRange = range;
//
//    // FIXME: generally repainting the chart is bad; only elements that really paint the range should be informed and
//    // those should trigger a paint event (i.e. the selection layer)
//
//    fireMapperChanged( this );
//  }

  @Override
  public String toString( )
  {
    return String.format( "%s {id=%s, pos=%s, dir=%s, visible=%s }", getLabel(), getIdentifier(), m_pos, m_dir, isVisible() ); //$NON-NLS-1$
  }

  protected IDataRange<Double> validateDataRange( final IDataRange<Double> range, final DataRangeRestriction<Number> restriction )
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
      return new DataRange<>( min, max );
    }
    if( newRangeSize < newRestrictionMinRange )
    {
      final double delta = newRestrictionMinRange - newRangeSize;
      final double min = Math.max( newRangeMin - delta / 2.0, newRestrictionMin );
      final double max = Math.min( min + newRestrictionMinRange, newRestrictionMax );
      return new DataRange<>( min, max );
    }

    return new DataRange<>( newRangeMin, newRangeMax );
  }

  @Override
  public T xmlStringToLogical( final String value ) throws MalformedValueException
  {
    return getDataOperator().stringToLogical( value );
  }
}