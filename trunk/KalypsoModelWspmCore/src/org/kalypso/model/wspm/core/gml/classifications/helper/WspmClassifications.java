/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.model.wspm.core.gml.classifications.helper;

import java.math.BigDecimal;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.gml.WspmProject;
import org.kalypso.model.wspm.core.gml.classifications.IRoughnessClass;
import org.kalypso.model.wspm.core.gml.classifications.IVegetationClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Dirk Kuch
 */
public final class WspmClassifications
{
  private WspmClassifications( )
  {
  }

  /**
   * Get the complete roughness value from the record. Resolves the class if necessary, and also applies a roughness
   * factor if present.
   *
   * @param preferClasses
   *          If <code>true</code>, class values have higher priority if direct values and class values are both
   *          present. If <code>false</code>, direct values have the higher priority.
   */
  public static BigDecimal getRoughnessValue( final IProfileRecord point, final String roughnessComponent, final boolean preferClasses )
  {
    final Double factor = point.getRoughnessFactor();

    if( preferClasses )
    {
      final BigDecimal classValue = findRoughnessClassValue( point, roughnessComponent );
      if( classValue != null )
        return classValue.multiply( new BigDecimal( factor ) );

      final BigDecimal value = findRoughnessValue( point, roughnessComponent );
      if( value != null )
        return value.multiply( new BigDecimal( factor ) );

      return null;
    }
    else
    {
      final BigDecimal value = findRoughnessValue( point, roughnessComponent );
      if( value != null )
        return value.multiply( new BigDecimal( factor ) );

      final BigDecimal classValue = findRoughnessClassValue( point, roughnessComponent );
      if( classValue != null )
        return classValue.multiply( new BigDecimal( factor ) );

      return null;
    }
  }

  private static BigDecimal findRoughnessValue( final IProfileRecord point, final String roughnessComponent )
  {
    final int componentIndex = point.indexOfComponent( roughnessComponent );
    if( componentIndex == -1 )
      return null;

    final Object value = point.getValue( componentIndex );
    if( value instanceof BigDecimal )
      return (BigDecimal) value;

    if( value instanceof Number )
    {
      final double dbl = ((Number) value).doubleValue();
      if( Double.isNaN( dbl ) )
        return null;

      return new BigDecimal( dbl );
    }

    // should never happen
    return null;
  }

  private static BigDecimal findRoughnessClassValue( final IProfileRecord point, final String roughnessComponent )
  {
    final IRoughnessClass clazz = findRoughnessClass( point );
    if( Objects.isNull( clazz ) )
      return null;

    return clazz.getValue( roughnessComponent );
  }

  private static IRoughnessClass findRoughnessClass( final IProfileRecord point )
  {
    final IProfile profile = point.getProfile();
    final int componentRoughnessClass = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS );
    if( componentRoughnessClass == -1 )
      return null;

    final IWspmClassification classification = getClassification( profile );
    if( Objects.isNull( classification ) )
      return null;

    final String clazzName = (String) point.getValue( componentRoughnessClass );
    if( Strings.isEmpty( clazzName ) )
      return null;

    return classification.findRoughnessClass( clazzName );
  }

  /**
   * Gets the resolved vegetation value for the given component.<br/>
   * Vegetation classes are resolved if necessary, depending on <code>preferClasses</code>.
   *
   * @param preferClasses
   *          If <code>true</code>, class values have higher priority if direct values and class values are both
   *          present. If <code>false</code>, direct values have the higher priority.
   */
  public static BigDecimal getVegetationValue( final IProfileRecord point, final String vegetationComponent, final boolean preferClasses )
  {
    if( preferClasses )
    {
      final BigDecimal classValue = findVegetationClassValue( point, vegetationComponent );
      if( classValue != null )
        return classValue;

      return findVegetationValue( point, vegetationComponent );
    }
    else
    {
      final BigDecimal value = findVegetationValue( point, vegetationComponent );
      if( value != null )
        return value;

      return findVegetationClassValue( point, vegetationComponent );
    }
  }

  private static BigDecimal findVegetationValue( final IProfileRecord point, final String vegetationComponent )
  {
    final IProfile profile = point.getProfile();

    final int componentIndex = profile.indexOfProperty( vegetationComponent );
    if( componentIndex == -1 )
      return null;

    final Object value = point.getValue( componentIndex );
    if( value instanceof BigDecimal )
      return (BigDecimal) value;

    if( value instanceof Number )
    {
      final double dbl = ((Number) value).doubleValue();
      if( Double.isNaN( dbl ) )
        return null;

      return new BigDecimal( dbl );
    }

    // should never happen
    return null;
  }

  private static BigDecimal findVegetationClassValue( final IProfileRecord point, final String vegetationComponent )
  {
    final IVegetationClass clazz = findVegetationClass( point );
    if( Objects.isNull( clazz ) )
      return null;

    return clazz.getValue( vegetationComponent );
  }

  private static IVegetationClass findVegetationClass( final IProfileRecord point )
  {
    final IProfile profile = point.getProfile();
    final int componentVegetationClass = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS );
    if( componentVegetationClass == -1 )
      return null;

    final IWspmClassification classification = getClassification( profile );
    if( Objects.isNull( classification ) )
      return null;

    final String clazzName = (String) point.getValue( componentVegetationClass );
    if( Strings.isEmpty( clazzName ) )
      return null;

    return classification.findVegetationClass( clazzName );
  }

  public static double getAx( final IProfileRecord point )
  {
    final Double bewuchs = point.getBewuchsAx();
    if( Objects.isNotNull( bewuchs ) )
      return bewuchs.doubleValue();

    final IVegetationClass clazz = findVegetationClass( point );
    if( Objects.isNotNull( clazz ) )
    {
      final BigDecimal decimal = clazz.getAx();
      if( Objects.isNotNull( decimal ) )
        return decimal.doubleValue();
    }

    return Double.NaN;
  }

  public static double getAy( final IProfileRecord point )
  {
    final Double bewuchs = point.getBewuchsAy();
    if( Objects.isNotNull( bewuchs ) )
      return bewuchs.doubleValue();

    final IVegetationClass clazz = findVegetationClass( point );
    if( Objects.isNotNull( clazz ) )
    {
      final BigDecimal decimal = clazz.getAy();
      if( Objects.isNotNull( decimal ) )
        return decimal.doubleValue();
    }

    return Double.NaN;
  }

  public static IWspmClassification getClassification( final IProfile profile )
  {
    final Object source = profile.getSource();
    if( !(source instanceof Feature) )
      return null;

    final Feature feature = (Feature) source;
    final GMLWorkspace workspace = feature.getWorkspace();
    final Feature root = workspace.getRootFeature();
    if( !(root instanceof WspmProject) )
      return null;

    final WspmProject project = (WspmProject) root;

    return project.getClassificationMember();
  }

  public static double getDp( final IProfileRecord point )
  {
    final Double bewuchs = point.getBewuchsDp();
    if( Objects.isNotNull( bewuchs ) )
      return bewuchs.doubleValue();

    final IVegetationClass clazz = findVegetationClass( point );
    if( Objects.isNotNull( clazz ) )
    {
      final BigDecimal decimal = clazz.getDp();
      if( Objects.isNotNull( decimal ) )
        return decimal.doubleValue();
    }

    return Double.NaN;
  }

  public static boolean hasRoughnessClass( final IProfile profile )
  {
    return Objects.isNotNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS ) );
  }

  public static boolean hasRoughnessProperties( final IProfile profile )
  {
    if( Objects.isNotNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS ) ) )
      return true;
    else if( Objects.isNotNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST ) ) )
      return true;

    return false;
  }

  public static boolean hasVegetationClass( final IProfile profile )
  {
    return Objects.isNotNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS ) );
  }

  public static boolean hasVegetationProperties( final IProfile profile )
  {
    if( Objects.isNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AX ) ) )
      return false;
    else if( Objects.isNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AY ) ) )
      return false;
    else if( Objects.isNull( profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_DP ) ) )
      return false;

    return true;
  }

  public static Double getRoughnessValue( final IProfileRecord point, final String property )
  {
    final Double factor = point.getRoughnessFactor();

    final int componentIndex = point.indexOfComponent( property );
    if( componentIndex != -1 )
    {
      final Object value = point.getValue( componentIndex );
      if( value instanceof Number )
      {
        final Number number = (Number) value;
        return number.doubleValue() * factor;
      }
    }

    final IRoughnessClass clazz = WspmClassifications.findRoughnessClass( point );
    if( Objects.isNull( clazz ) )
      return null;

    switch( property )
    {
      case IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS:
        final BigDecimal clazzKsValue = clazz.getKsValue();
        if( Objects.isNotNull( clazzKsValue ) )
          return clazzKsValue.doubleValue() * factor;

      case IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST:
        final BigDecimal clazzKstValue = clazz.getKstValue();
        if( Objects.isNotNull( clazzKstValue ) )
          return clazzKstValue.doubleValue() * factor;

      default:
        return null;
    }
  }
}