package org.kalypso.chart.ext.base.axis;


import java.util.Comparator;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;
import org.kalypso.chart.framework.util.math.ComparableNumber;


/**
 * @author burtscher
 * 
 * Concrete IAxis implementation - to be used for numeric data 
 */
public class ComparableNumberAxis extends AbstractAxis<ComparableNumber>
{
  public ComparableNumberAxis( String id, String label, PROPERTY prop, POSITION pos, DIRECTION dir, Comparator<ComparableNumber> comp )
  {
    super( id, label, prop, pos, dir, new ComparableComparator(), ComparableNumber.class );
  }

  public double logicalToNormalized( final ComparableNumber value )
  {
	IDataRange<ComparableNumber> dataRange = getDataRange();  
	
    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();

    final double norm = (value.doubleValue() - dataRange.getMin().doubleValue()) / r;

    return norm;
  }

  public ComparableNumber normalizedToLogical( final double value )
  {

	  IDataRange<ComparableNumber> dataRange = getDataRange();  
		
      final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();


    double logicalDouble= value * r + dataRange.getMin().doubleValue();

    final ComparableNumber logical = new ComparableNumber(logicalDouble); 
    
    return logical;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#logicalToScreen(T)
   */
  public int logicalToScreen( final ComparableNumber value )
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
  public ComparableNumber screenToLogical( final int value )
  {
    if( m_registry == null )
      return null;

    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
      return null; 

    return normalizedToLogical( comp.screenToNormalized( value ) );
  }
  
  public int zeroToScreen()
  {
	  return logicalToScreen(new ComparableNumber(0.0));
  }

}
