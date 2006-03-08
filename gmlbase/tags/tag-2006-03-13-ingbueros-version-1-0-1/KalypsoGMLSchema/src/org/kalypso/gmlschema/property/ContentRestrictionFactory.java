/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.gmlschema.property;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.impl.xb.xsdschema.Facet;
import org.apache.xmlbeans.impl.xb.xsdschema.NoFixedFacet;
import org.apache.xmlbeans.impl.xb.xsdschema.NumFacet;
import org.apache.xmlbeans.impl.xb.xsdschema.RestrictionDocument.Restriction;
import org.kalypso.gmlschema.property.restriction.EnumerationResitriction;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.property.restriction.MaxExclusiveRestriction;
import org.kalypso.gmlschema.property.restriction.MaxInclusiveRestriction;
import org.kalypso.gmlschema.property.restriction.MaxLengthRestriction;
import org.kalypso.gmlschema.property.restriction.MinExclusiveRestriction;
import org.kalypso.gmlschema.property.restriction.MinInclusiveRestriction;
import org.kalypso.gmlschema.property.restriction.MinLengthRestriction;

/**
 * represents restiction on a simple content
 * 
 * @author doemming
 */
public class ContentRestrictionFactory
{

  public static IRestriction[] createRestrictions( final Restriction restriction )
  {
    final List<IRestriction> result = new ArrayList<IRestriction>();

    // enumeration
    final NoFixedFacet[] enumerationArray = restriction.getEnumerationArray();
    final List<String> enumList = new ArrayList<String>();
    for( int i = 0; i < enumerationArray.length; i++ )
    {
      final NoFixedFacet facet = enumerationArray[i];
      enumList.add( facet.getValue().getStringValue() );
    }
    final String[] _enum = enumList.toArray( new String[enumList.size()] );
    if( _enum.length > 0 )
      result.add( new EnumerationResitriction( _enum ) );

    // maxlength

    if( restriction.getMaxLengthArray().length > 0 )
      result.add( new MaxLengthRestriction( getMinAsInt( restriction.getMaxLengthArray() ) ) );

    if( restriction.getMinLengthArray().length > 0 )
      result.add( new MinLengthRestriction( getMaxAsInt( restriction.getMinLengthArray() ) ) );

    if( restriction.getMaxInclusiveArray().length > 0 )
      result.add( new MaxInclusiveRestriction( getMinAsDouble( restriction.getMaxInclusiveArray() ) ) );

    if( restriction.getMaxExclusiveArray().length > 0 )
      result.add( new MaxExclusiveRestriction( getMinAsDouble( restriction.getMaxExclusiveArray() ) ) );

    if( restriction.getMinInclusiveArray().length > 0 )
      result.add( new MinInclusiveRestriction( getMaxAsDouble( restriction.getMinInclusiveArray() ) ) );

    if( restriction.getMinExclusiveArray().length > 0 )
      result.add( new MinExclusiveRestriction( getMaxAsDouble( restriction.getMinExclusiveArray() ) ) );

    return result.toArray( new IRestriction[result.size()] );
  }

  private static int getMinAsInt( NumFacet[] array )
  {
    int result = Integer.MAX_VALUE;
    for( int i = 0; i < array.length; i++ )
    {
      final int value = intValue( array[i] );
      if( value < result )
        result = value;
    }
    return result;
  }

  private static int getMaxAsInt( NumFacet[] array )
  {
    int result = Integer.MIN_VALUE;
    for( int i = 0; i < array.length; i++ )
    {
      final int value = intValue( array[i] );
      if( value > result )
        result = value;
    }
    return result;
  }

  private static double getMinAsDouble( Facet[] facets )
  {
    double result = Double.MAX_VALUE;
    for( int i = 0; i < facets.length; i++ )
    {
      final double value = doubleValue( facets[i] );
      if( value < result )
        result = value;
    }
    return result;
  }

  private static double getMaxAsDouble( Facet[] facets )
  {
    double result = Double.MIN_VALUE;
    for( int i = 0; i < facets.length; i++ )
    {
      final double value = doubleValue( facets[i] );
      if( value > result )
        result = value;
    }
    return result;
  }

  /**
   * @param facet
   */
  private static int intValue( NumFacet facet )
  {
    return Integer.parseInt( facet.getValue().toString() );
  }

  /**
   * @param facet
   */
  private static double doubleValue( Facet facet )
  {
    return Double.parseDouble( facet.getValue().toString() );
  }

}
