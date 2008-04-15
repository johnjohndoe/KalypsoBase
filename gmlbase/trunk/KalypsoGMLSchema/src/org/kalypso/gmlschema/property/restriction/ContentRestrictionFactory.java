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
package org.kalypso.gmlschema.property.restriction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.Facet;
import org.apache.xmlbeans.impl.xb.xsdschema.NoFixedFacet;
import org.apache.xmlbeans.impl.xb.xsdschema.NumFacet;
import org.apache.xmlbeans.impl.xb.xsdschema.PatternDocument.Pattern;
import org.apache.xmlbeans.impl.xb.xsdschema.RestrictionDocument.Restriction;
import org.kalypso.gmlschema.annotation.AnnotationAdapterFactory;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.annotation.ILanguageAnnontationProvider;
import org.kalypso.gmlschema.swe.ObservationLanguageAnnontationProvider;

/**
 * represents restriction on a simple content
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
    if( enumerationArray.length > 0 )
    {
      final Map<Object, ILanguageAnnontationProvider> map = new LinkedHashMap<Object, ILanguageAnnontationProvider>();

      for( final NoFixedFacet facet : enumerationArray )
      {
        final String stringValue = facet.getValue().getStringValue();

        final String platformLang = AnnotationAdapterFactory.getPlatformLang();
        final IAnnotation annotation = AnnotationAdapterFactory.annotationForElement( platformLang, facet.getAnnotation(), stringValue );

        map.put( stringValue, new ObservationLanguageAnnontationProvider( new IAnnotation[] { annotation }, annotation ) );
      }

      result.add( new EnumerationRestriction( map ) );
    }

    final Pattern[] patternArray = restriction.getPatternArray();
    if( patternArray.length > 0 )
    {
      final List<String> list = new ArrayList<String>();
      for( final Pattern pattern : patternArray )
      {
        final String stringValue = pattern.getValue().getStringValue();
        list.add( stringValue );
      }
      result.add( new RegExpRestriction( list.toArray( new String[list.size()] ) ) );
    }

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

    // fraction digits
    if( restriction.getFractionDigitsArray().length > 0 )
      result.add( new FractionDigitRestriction( getMaxAsInt( restriction.getFractionDigitsArray() ) ) );

    return result.toArray( new IRestriction[result.size()] );
  }

  private static int getMinAsInt( final NumFacet[] array )
  {
    int result = Integer.MAX_VALUE;
    for( final NumFacet element : array )
    {
      final int value = intValue( element );
      if( value < result )
        result = value;
    }
    return result;
  }

  private static int getMaxAsInt( final NumFacet[] array )
  {
    int result = Integer.MIN_VALUE;
    for( final NumFacet element : array )
    {
      final int value = intValue( element );
      if( value > result )
        result = value;
    }
    return result;
  }

  private static double getMinAsDouble( final Facet[] facets )
  {
    double result = Double.MAX_VALUE;
    for( final Facet element : facets )
    {
      final double value = doubleValue( element );
      if( value < result )
        result = value;
    }
    return result;
  }

  private static double getMaxAsDouble( final Facet[] facets )
  {
    double result = -Double.MAX_VALUE;
    for( final Facet element : facets )
    {
      final double value = doubleValue( element );
      if( value > result )
        result = value;
    }
    return result;
  }

  /**
   * @param facet
   */
  private static int intValue( final NumFacet facet )
  {
    final XmlAnySimpleType value = facet.getValue();
    return Integer.parseInt( value.getStringValue() );
  }

  /**
   * TODO: change to big decimal
   * 
   * @param facet
   */
  private static double doubleValue( final Facet facet )
  {
    return Double.parseDouble( facet.getValue().getStringValue() );
  }

}
