package org.kalypso.gmlschema;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.kalypso.gmlschema.property.IValuePropertyType;

/**
 * mapping between xml-typenames and java-classnames for GML-geometry types and XMLSCHEMA-simple types
 * 
 * @author doemming
 */
public class Mapper
{
  private static final SimpleDateFormat XML_DATETIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );

  private static final SimpleDateFormat XML_DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );

  public static String mapJavaValueToXml( final Object value )
  {
    if( value == null )
      return "";

    if( value instanceof DateWithoutTime )
      return XML_DATE_FORMAT.format( (DateWithoutTime) value );

    if( value instanceof Date )
      return XML_DATETIME_FORMAT.format( (Date) value );

    if( value instanceof Number )
      // TODO: use a special (xml-conform) formatting?
      return value.toString();

    return value.toString();
  }

  /**
   * @param value
   * @throws Exception
   */
  public static Object mapXMLValueToJava( final String value, final Class clazz ) throws Exception
  {
    if( clazz == String.class )
      return value;
    if( clazz == Float.class )
      return new Float( value );
    if( clazz == Double.class )
      return new Double( value );
    if( clazz == Integer.class )
    {
      // shapefiles give string like "10.0"
      double doubleValue = Double.parseDouble( value );
      Integer integer = new Integer( (int) doubleValue );
      if( integer.intValue() != doubleValue )
        throw new Exception( "no valid int value :" + value );
      return integer;
    }
    if( clazz == Long.class )
    {
      // shapefiles give strings like "10.0"
      double doubleValue = Double.parseDouble( value );
      final Long longValue = new Long( (long) doubleValue );
      if( longValue.longValue() != doubleValue )
        throw new Exception( "no valid long value :" + value );
      return longValue;
    }
    if( clazz == Boolean.class )
    {
      if( "true".equals( value ) || "1".equals( value ) )
        return new Boolean( true );
      return new Boolean( false );
    }
    if( clazz == Date.class )
      return XML_DATETIME_FORMAT.parseObject( value );

    if( clazz == DateWithoutTime.class )
      return XML_DATE_FORMAT.parseObject( value );

    throw new Exception( "unknown XML type: " + clazz + "  for value: " + value );
  }

  public static Object defaultValueforJavaType( final Class type, final boolean createGeometry )
  {
    // TODO try to get default value from schema !
    if( "java.util.Date".equals( type ) )
      return new Date( 0 );
    if( "DateWithoutTime.class.getName()".equals( type ) )
      return new DateWithoutTime();
    if( "java.lang.Boolean".equals( type ) )
      return Boolean.FALSE;
    if( "java.lang.Float".equals( type ) )
      return new Float( 0 );
    if( "java.lang.Integer".equals( type ) )
      return new Integer( 0 );
    if( "java.lang.String".equals( type ) )
      return "";
    if( "java.lang.Double".equals( type ) )
      return new Double( 0.0 );
    if( "java.lang.Long".equals( type ) )
      return new Long( 0 );

    if( !createGeometry )
      return null;

    // TODO create default geo properties

    return null;
  }

  public static Object defaultValueforJavaType( IValuePropertyType ftp, boolean createGeometry )
  {
    return defaultValueforJavaType( ftp.getClass(), createGeometry );
  }

  public static Object mapXMLValueToJava( String value, IValuePropertyType ftp ) throws Exception
  {
    return mapXMLValueToJava( value, ftp.getValueClass() );
  }
}