package de.openali.odysseus.chart.framework.model.data.impl;

import java.text.Format;
import java.util.Comparator;

import de.openali.odysseus.chart.framework.model.data.IDataRange;

public class BooleanDataOperator extends AbstractDataOperator<Boolean>
{

  public BooleanDataOperator( final Comparator<Boolean> comparator )
  {
    super( comparator );
  }

  @Override
  public Double logicalToNumeric( final Boolean logVal )
  {
    if( Boolean.TRUE.equals( logVal ) )
      return 1.0;

    return 0.0;
  }

  @Override
  public Boolean numericToLogical( final Number numVal )
  {
    if( numVal.intValue() == 1 )
      return Boolean.TRUE;

    return Boolean.FALSE;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IDataOperator#getFormat(de.openali.odysseus.chart.framework.model.data.IDataRange)
   */
  @Override
  public Format getFormat( final IDataRange<Number> range )
  {
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IStringDataConverter#logicalToString(java.lang.Object)
   */
  @Override
  public String logicalToString( final Boolean value )
  {
    return value.toString();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IStringParser#stringToLogical(java.lang.String)
   */
  @Override
  public Boolean stringToLogical( final String value )
  {
    return Boolean.valueOf( value );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IStringParser#getFormatHint()
   */
  @Override
  public String getFormatHint( )
  {
    return "%s";
  }
}
