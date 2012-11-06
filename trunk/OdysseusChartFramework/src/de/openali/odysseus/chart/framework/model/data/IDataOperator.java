package de.openali.odysseus.chart.framework.model.data;

import java.text.Format;
import java.util.Comparator;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;

public interface IDataOperator<T_logical> extends IStringDataConverter<T_logical>
{
  Double logicalToNumeric( T_logical logVal );

  T_logical numericToLogical( Number numVal );

  @Override
  // TODO: rename to logicalToXmlString, check IStringParser Interface first
  String logicalToString( T_logical logVal );

  @Override
  // TODO: rename to logicalFromXmlString , check IStringParser Interface first
  T_logical stringToLogical( String toParse ) throws MalformedValueException;

  Comparator<T_logical> getComparator( );

  /**
   * returns a format to format data values as strings; the format shall try to adapt itself to the given range
   */
  Format getFormat( IDataRange<Number> range );
}