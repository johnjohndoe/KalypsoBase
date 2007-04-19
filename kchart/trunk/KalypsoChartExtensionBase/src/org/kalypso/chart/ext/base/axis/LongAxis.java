package org.kalypso.chart.ext.base.axis;

import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;

/**
 * @author burtscher
 * 
 * Concrete IAxis implementation - to be used for long data 
 */
public class LongAxis extends AbstractAxis<Long>
{
  public LongAxis( String id, String label, PROPERTY prop, POSITION pos, DIRECTION dir)
  {
    super( id, label, prop, pos, dir, Long.class );
  }

  public long logicalToNormalized( final Long value )
  {
	IDataRange<Long> dataRange = getDataRange();  
	final long r = dataRange.getMax().longValue() - dataRange.getMin().longValue();
    final long norm = (value.longValue() - dataRange.getMin().longValue()) / r;
    return norm;
  }

  public Long normalizedToLogical( final long value )
  {
	IDataRange<Long> dataRange = getDataRange();  
	final long r = dataRange.getMax().longValue() - dataRange.getMin().longValue();
	final long logical = value * r + dataRange.getMin().longValue();

    return logical;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#logicalToScreen(T)
   */
  public int logicalToScreen( final Long value )
  {
    if( m_registry == null )
      return 0;

    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
      return 0;

    return comp.normalizedToScreen( logicalToNormalized( value ) );
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#screenToLogical(int)
   */
  public Long screenToLogical( final int value )
  {
    if( m_registry == null )
      return null;

    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
    	return null;

    return normalizedToLogical( (long) comp.screenToNormalized( value ) );
  }
  
  public int zeroToScreen()
  {
	  return logicalToScreen((long) 0);
  }

}
