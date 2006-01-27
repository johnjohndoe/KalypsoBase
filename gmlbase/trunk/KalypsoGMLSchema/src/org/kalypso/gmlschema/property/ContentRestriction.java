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

/**
 * 
 * represents restiction on a simple content
 * 
 * @author doemming
 */
public class ContentRestriction implements IRestriction
{
  private final boolean m_hasEnum;

  final String[] m_enum;

  private boolean m_hasMaxLength;

  private final int m_maxLength;

  private final boolean m_hasMinLength;

  private final int m_minLength;

  private final boolean m_hasMaxInclusive;

  private final double m_maxInclusive;

  private final boolean m_hasMinInclusive;

  private final double m_minInclusive;

  private final boolean m_hasMaxExclusive;

  private final double m_maxExclusive;

  private final boolean m_hasMinExclusive;

  private final double m_minExclusive;

  public ContentRestriction( final Restriction restriction )
  {
    // enumeration
    final NoFixedFacet[] enumerationArray = restriction.getEnumerationArray();
    final List enumList = new ArrayList();
    for( int i = 0; i < enumerationArray.length; i++ )
    {
      final NoFixedFacet facet = enumerationArray[i];
      enumList.add( facet.getValue().getStringValue() );
    }
    m_enum = (String[])enumList.toArray( new String[enumList.size()] );
    m_hasEnum = m_enum.length > 0;

    m_maxLength = getMinAsInt( restriction.getMaxLengthArray() );
    m_hasMaxLength = restriction.getMaxLengthArray().length > 0;

    m_minLength = getMaxAsInt( restriction.getMinLengthArray() );
    m_hasMinLength = restriction.getMinLengthArray().length > 0;

    m_maxInclusive = getMinAsDouble( restriction.getMaxInclusiveArray() );
    m_hasMaxInclusive = restriction.getMaxInclusiveArray().length > 0;

    m_maxExclusive = getMinAsDouble( restriction.getMaxExclusiveArray() );
    m_hasMaxExclusive = restriction.getMaxExclusiveArray().length > 0;

    m_minInclusive = getMaxAsDouble( restriction.getMinInclusiveArray() );
    m_hasMinInclusive = restriction.getMinInclusiveArray().length > 0;

    m_minExclusive = getMaxAsDouble( restriction.getMinExclusiveArray() );
    m_hasMinExclusive = restriction.getMinExclusiveArray().length > 0;
  }

  public String[] getEnumeration()
  {
    return m_enum;
  }

  public boolean hasEnumeration()
  {
    return m_hasEnum;
  }

  public boolean hasMaxExclusive()
  {
    return m_hasMaxExclusive;
  }

  public boolean hasMaxInclusive()
  {
    return m_hasMaxInclusive;
  }

  public boolean hasMaxLength()
  {
    return m_hasMaxLength;
  }

  public boolean hasMinExclusive()
  {
    return m_hasMinExclusive;
  }

  public boolean hasMinInclusive()
  {
    return m_hasMinInclusive;
  }

  public boolean hasMinLength()
  {
    return m_hasMinLength;
  }

  private int getMinAsInt( NumFacet[] array )
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

  private int getMaxAsInt( NumFacet[] array )
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

  private double getMinAsDouble( Facet[] facets )
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

  private double getMaxAsDouble( Facet[] facets )
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
  private int intValue( NumFacet facet )
  {
    return Integer.parseInt( facet.getValue().toString() );
  }

  /**
   * @param facet
   */
  private double doubleValue( Facet facet )
  {
    return Double.parseDouble( facet.getValue().toString() );
  }

  public double getMaxExclusive()
  {
    return m_maxExclusive;
  }

  public double getMaxInclusive()
  {
    return m_maxInclusive;
  }

  public int getMaxLength()
  {
    return m_maxLength;
  }

  public double getMinExclusive()
  {
    return m_minExclusive;
  }

  public double getMinInclusive()
  {
    return m_minInclusive;
  }

  public int getMinLength()
  {
    return m_minLength;
  }
}
