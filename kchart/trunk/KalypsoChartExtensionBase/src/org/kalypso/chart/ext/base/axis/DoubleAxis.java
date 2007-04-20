package org.kalypso.chart.ext.base.axis;

import java.util.Comparator;

import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;


/**
 * @author burtscher
 * 
 * Concrete IAxis implementation - to be used for numeric data 
 */
public class DoubleAxis extends AbstractAxis<Double>
{
  public DoubleAxis( String id, String label, PROPERTY prop, POSITION pos, DIRECTION dir, Comparator<Double> comp )
  {
    super( id, label, prop, pos, dir, comp, Double.class );
  }

  public double logicalToNormalized( final Double value )
  {
	IDataRange<Double> dataRange = getDataRange();  
	
    final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();

    final double norm = (value.doubleValue() - dataRange.getMin().doubleValue()) / r;

    return norm;
  }

  public Double normalizedToLogical( final double value )
  {

	  IDataRange<Double> dataRange = getDataRange();  
		
      final double r = dataRange.getMax().doubleValue() - dataRange.getMin().doubleValue();


    final double logical = value * r + dataRange.getMin().doubleValue();

    return logical;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#logicalToScreen(T)
   */
  public int logicalToScreen( final Double value )
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
  public Double screenToLogical( final int value )
  {
    if( m_registry == null )
      return Double.NaN;

    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
      return Double.NaN; 

    return normalizedToLogical( comp.screenToNormalized( value ) );
  }
  
  public int zeroToScreen()
  {
	  return logicalToScreen(0.0);
  }

public Double parseStringValue(String value) throws MalformedValueException {
	try {
		double d = Double.parseDouble(value);
		return d;
	}
	catch (NumberFormatException e) {
		throw new MalformedValueException(e);
	}
}

}
