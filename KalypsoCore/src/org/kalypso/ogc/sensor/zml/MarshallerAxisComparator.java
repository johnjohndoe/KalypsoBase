package org.kalypso.ogc.sensor.zml;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.ogc.sensor.IAxis;

/**
 * @author Gernot Belger
 */
final class MarshallerAxisComparator implements Comparator<IAxis>
{
  @Override
  public int compare( final IAxis a1, final IAxis a2 )
  {
    final String type1 = getAxisTypeOrEmpty( a1 );
    final String type2 = getAxisTypeOrEmpty( a2 );

    if( type1.equals( type2 ) )
    {
      final String n1 = getNameOrEmpty( a1 );
      final String n2 = getNameOrEmpty( a2 );

      return n1.compareTo( n2 );
    }

    return type1.compareTo( type2 );
  }

  private String getNameOrEmpty( final IAxis a1 )
  {
    final String name = a1.getName();

    if( name == null )
      return StringUtils.EMPTY;

    return name;
  }

  private String getAxisTypeOrEmpty( final IAxis a1 )
  {
    final String type = a1.getType();

    if( type == null )
      return StringUtils.EMPTY;

    return type;
  }
}