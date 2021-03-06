/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.observation.result;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.kalypso.commons.math.LinearEquation;
import org.kalypso.commons.math.LinearEquation.SameXValuesException;
import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.core.i18n.Messages;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.phenomenon.IPhenomenon;

/**
 * TODO: Merge most of the stuff with {@link ComponentUtilities}.
 *
 * @author Gernot Belger
 */
public class TupleResultUtilities
{
  private TupleResultUtilities( )
  {
    // helper class, do not instantiate
  }

  /**
   * Get component by name.
   *
   * @return null, if no component with the given name was found.
   */
  public static IComponent findComponentByName( final TupleResult result, final String name )
  {
    final IComponent[] components = result.getComponents();
    for( final IComponent comp : components )
    {
      if( comp.getName().equals( name ) )
      {
        return comp;
      }
    }

    return null;
  }

  /**
   * Find component by id.
   *
   * @return The first component with the given id, null, none was found.
   */
  public static IComponent findComponentById( final IObservation<TupleResult> observation, final String id )
  {
    return findComponentById( observation.getResult(), id );
  }

  /**
   * Find component by id.
   *
   * @return The first component with the given id, null, none was found.
   */
  public static IComponent findComponentById( final TupleResult result, final String id )
  {
    final IComponent[] components = result.getComponents();
    return findComponentById( components, id );
  }

  public static IComponent findComponentById( final IComponent[] components, final String id )
  {
    for( final IComponent comp : components )
    {
      if( comp.getId().equals( id ) )
      {
        return comp;
      }
    }

    return null;
  }

  /**
   * @author thuel2
   * @return returns minimum value for component of a tupleResult.<br>
   *         Works for components of XmlType XS_BOOLEAN, XS_DOUBLE, XS_DATE, XS_STRING. <br>
   *         For all others <code>object.toString</code> will be used for comparison.
   */
  public static Object findComponentMinById( final TupleResult result, final String compID )
  {
    final IComponent comp = TupleResultUtilities.findComponentById( result, compID );
    if( comp == null )
    {
      return null;
    }
    final int iComp = result.indexOfComponent( comp );
    final QName valueTypeName = comp.getValueTypeName();

    if( XmlTypes.XS_BOOLEAN.equals( valueTypeName ) )
    {
      final List<Boolean> values = new ArrayList<>();
      for( final IRecord record : result )
      {
        values.add( (Boolean)record.getValue( iComp ) );
      }
      if( values.size() < 1 )
      {
        return null;
      }
      return Collections.min( values );
    }
    else if( XmlTypes.XS_DOUBLE.equals( valueTypeName ) )
    {
      // TODO think about other numerical types:
      // XmlTypes.XS_BYTE, XmlTypes.XS_DECIMAL, XmlTypes.XS_FLOAT, XmlTypes.XS_INT, XmlTypes.XS_INTEGER,
      // XmlTypes.XS_LONG, XmlTypes.XS_SHORT
      final List<java.lang.Double> values = new ArrayList<>();
      for( final IRecord record : result )
      {
        values.add( (java.lang.Double)record.getValue( iComp ) );
      }
      if( values.size() < 1 )
      {
        return null;
      }
      return Collections.min( values );
    }
    else if( XmlTypes.XS_DATE.equals( valueTypeName ) )
    {
      // TODO think about other date types
      // XmlTypes.XS_DATETIME, XmlTypes.XS_DURATION, XmlTypes.XS_TIME
      final List<Date> values = new ArrayList<>();
      for( final IRecord record : result )
      {
        values.add( (Date)record.getValue( iComp ) );
      }
      if( values.size() < 1 )
      {
        return null;
      }
      return Collections.min( values );
    }
    else if( XmlTypes.XS_STRING.equals( valueTypeName ) )
    {
      final List<String> values = new ArrayList<>();
      for( final IRecord record : result )
      {
        values.add( (String)record.getValue( iComp ) );
      }
      if( values.size() < 1 )
      {
        return null;
      }
      return Collections.min( values );
    }
    else
    {
      final List<String> values = new ArrayList<>();
      for( final IRecord record : result )
      {
        values.add( record.getValue( iComp ).toString() );
      }
      if( values.size() < 1 )
      {
        return null;
      }
      return Collections.min( values );
    }
  }

  /**
   * @author thuel2
   * @return returns maximum value for component of a tupleResult. <br>
   *         Works for components of XmlType XS_BOOLEAN, XS_DOUBLE, XS_DATE, XS_STRING. <br>
   *         For all others <code>object.toString()</code> will be used for comparison.
   */
  public static Object findComponentMaxById( final TupleResult result, final String compID )
  {
    final IComponent comp = TupleResultUtilities.findComponentById( result, compID );
    if( comp == null )
    {
      return null;
    }
    final QName valueTypeName = comp.getValueTypeName();
    final int iComp = result.indexOfComponent( comp );

    if( XmlTypes.XS_BOOLEAN.equals( valueTypeName ) )
    {
      final List<Boolean> values = new ArrayList<>();
      for( final IRecord record : result )
      {
        values.add( (Boolean)record.getValue( iComp ) );
      }
      if( values.size() < 1 )
      {
        return null;
      }
      return Collections.max( values );
    }
    else if( XmlTypes.XS_DOUBLE.equals( valueTypeName ) )
    {
      // TODO think about other numerical types:
      // XmlTypes.XS_BYTE, XmlTypes.XS_DECIMAL, XmlTypes.XS_FLOAT, XmlTypes.XS_INT, XmlTypes.XS_INTEGER,
      // XmlTypes.XS_LONG, XmlTypes.XS_SHORT
      final List<java.lang.Double> values = new ArrayList<>();
      for( final IRecord record : result )
      {
        values.add( (java.lang.Double)record.getValue( iComp ) );
      }
      if( values.size() < 1 )
      {
        return null;
      }
      return Collections.max( values );
    }
    else if( XmlTypes.XS_DATE.equals( valueTypeName ) )
    {
      // TODO think about other date types
      // XmlTypes.XS_DATETIME, XmlTypes.XS_DURATION, XmlTypes.XS_TIME
      final List<Date> values = new ArrayList<>();
      for( final IRecord record : result )
      {
        values.add( (Date)record.getValue( iComp ) );
      }
      if( values.size() < 1 )
      {
        return null;
      }
      return Collections.max( values );
    }
    else if( XmlTypes.XS_STRING.equals( valueTypeName ) )
    {
      final List<String> values = new ArrayList<>();
      for( final IRecord record : result )
      {
        values.add( (String)record.getValue( iComp ) );
      }
      if( values.size() < 1 )
      {
        return null;
      }
      return Collections.max( values );
    }
    else
    {
      final List<String> values = new ArrayList<>();
      for( final IRecord record : result )
      {
        values.add( record.getValue( iComp ).toString() );
      }
      if( values.size() < 1 )
      {
        return null;
      }
      return Collections.max( values );
    }
  }

  /**
   * Copies records from one {@link TupleResult} to another.
   *
   * @param componentMap
   *          Map of component ids.
   * @throws IllegalArgumentException
   *           If for an id from the map no component is found.
   */
  public static void copyValues( final TupleResult sourceResult, final TupleResult targetResult, final Map<String, String> componentMap )
  {
    /* Find Components */
    final IComponent[] sourceComponents = new IComponent[componentMap.size()];
    final IComponent[] targetComponents = new IComponent[componentMap.size()];

    int count = 0;
    for( final Map.Entry<String, String> entry : componentMap.entrySet() )
    {
      final String sourceID = entry.getKey();
      final String targetID = entry.getValue();

      final IComponent sourceComponent = ComponentUtilities.findComponentByID( sourceResult.getComponents(), sourceID );
      if( sourceComponent == null )
        throw new IllegalArgumentException( Messages.getString( "org.kalypso.observation.result.TupleResultUtilities.0" ) + sourceID ); //$NON-NLS-1$

      final IComponent targetComponent = ComponentUtilities.findComponentByID( targetResult.getComponents(), targetID );
      if( targetComponent == null )
        throw new IllegalArgumentException( Messages.getString( "org.kalypso.observation.result.TupleResultUtilities.1" ) + targetID ); //$NON-NLS-1$

      sourceComponents[count] = sourceComponent;
      targetComponents[count] = targetComponent;
      count++;
    }

    /* Copy values */
    for( final IRecord sourceRecord : sourceResult )
    {
      final IRecord targetRecord = targetResult.createRecord();

      for( int i = 0; i < sourceComponents.length; i++ )
      {
        final Object value = sourceRecord.getValue( sourceResult.indexOfComponent( sourceComponents[i] ) );
        targetRecord.setValue( targetResult.indexOfComponent( targetComponents[i] ), value );
      }

      targetResult.add( targetRecord );
    }
  }

  /**
   * Returns the index of the first component with a given id.
   *
   * @return -1, if no such component exists.
   */
  public static int indexOfComponent( final IObservation<TupleResult> observation, final String id )
  {
    return indexOfComponent( observation.getResult(), id );
  }

  /**
   * Returns the index of the first component with a given id.
   *
   * @return -1, if no such component exists.
   */
  public static int indexOfComponent( final TupleResult result, final String id )
  {
    final IComponent[] components = result.getComponents();
    for( int i = 0; i < components.length; i++ )
    {
      final IComponent component = components[i];
      if( component.getId().equals( id ) )
        return i;
    }

    return -1;
  }

  /** Extracts a numerical column of values from an observation. */
  public static Number[] getValuesAsNumbers( final IObservation<TupleResult> observation, final int componentIndex ) throws ClassCastException
  {
    final TupleResult result = observation.getResult();
    final Collection<Number> values = new ArrayList<>( result.size() );

    for( final IRecord record : result )
    {
      final Number value = (Number)record.getValue( componentIndex );
      values.add( value );
    }

    return values.toArray( new Number[values.size()] );
  }

  public static double[] getInterpolatedValues( final IObservation<TupleResult> observation, final int valueIndex, final int interpolateIndex )
  {
    final Number[] domainValues = getValuesAsNumbers( observation, interpolateIndex );
    final Number[] rangeValues = getValuesAsNumbers( observation, valueIndex );
    final double[] interpolatedValues = new double[rangeValues.length];

    int lastIndexNonNull = -1;

    for( int i = 0; i < rangeValues.length; i++ )
    {
      final Number currentValue = rangeValues[i];
      if( currentValue == null )
        interpolatedValues[i] = Double.NaN;
      else
      {
        interpolatedValues[i] = currentValue.doubleValue();

        final Number domain = domainValues[i];
        if( domain != null )
        {
          if( lastIndexNonNull != -1 && lastIndexNonNull != i - 1 )
          {
            final double startDomain = domainValues[lastIndexNonNull].doubleValue();
            final double startValue = rangeValues[lastIndexNonNull].doubleValue();
            final double endDomain = domainValues[i].doubleValue();
            final double endValue = rangeValues[i].doubleValue();
            try
            {
              final LinearEquation linearEquation = new LinearEquation( startDomain, startValue, endDomain, endValue );
              for( int j = lastIndexNonNull + 1; j < i; j++ )
              {
                final Number currentDomain = domainValues[j];
                if( currentDomain != null )
                {
                  final double interpolationValue = currentDomain.doubleValue();
                  interpolatedValues[j] = linearEquation.computeY( interpolationValue );
                }
              }
            }
            catch( final SameXValuesException e )
            {
              // Unable to interpolate between same values, set all to Double.NaN
              for( int j = lastIndexNonNull + 1; j < i; j++ )
                interpolatedValues[j] = Double.NaN;
            }
          }

          lastIndexNonNull = i;
        }
      }
    }

    return interpolatedValues;
  }

  /**
   * Returns the first (index of a) component, that has the given component id.
   *
   * @reutrn -1, if no such component was found.
   */
  public static int indexOfComponentByPhenomenon( final TupleResult result, final String phenomenonID )
  {
    final IComponent[] components = result.getComponents();
    for( int i = 0; i < components.length; i++ )
    {
      final IComponent comp = components[i];
      final IPhenomenon phenomenon = comp.getPhenomenon();
      final String phenID = phenomenon.getID();
      if( ObjectUtils.equals( phenomenonID, phenID ) )
        return i;
    }

    return -1;
  }

  public static void setNumberValue( final IRecord record, final IComponent component, final Number value )
  {
    final QName qname = component.getValueTypeName();
    if( XmlTypes.XS_DECIMAL.equals( qname ) )
      record.setValue( component, BigDecimal.valueOf( value.doubleValue() ) );
    else if( XmlTypes.XS_DOUBLE.equals( qname ) )
      record.setValue( component, Double.valueOf( value.doubleValue() ) );
    else if( XmlTypes.XS_FLOAT.equals( qname ) )
      record.setValue( component, Float.valueOf( value.floatValue() ) );
    else if( XmlTypes.XS_INT.equals( qname ) )
      record.setValue( component, Integer.valueOf( value.intValue() ) );
    else if( XmlTypes.XS_INTEGER.equals( qname ) )
      record.setValue( component, BigInteger.valueOf( value.longValue() ) );
    else if( XmlTypes.XS_LONG.equals( qname ) )
      record.setValue( component, Long.valueOf( value.longValue() ) );
    else if( XmlTypes.XS_SHORT.equals( qname ) )
      record.setValue( component, Short.valueOf( value.shortValue() ) );
    else
      throw new UnsupportedOperationException();

  }

  public static void copyValues( final TupleResult sourceResult, final TupleResult targetResult )
  {
    final IComponent[] components = sourceResult.getComponents();
    for( final IComponent component : components )
      targetResult.addComponent( component );

    for( int index = 0; index < sourceResult.size(); index++ )
    {
      final IRecord target = targetResult.createRecord();
      final IRecord record = sourceResult.get( index );

      for( int component = 0; component < ArrayUtils.getLength( components ); component++ )
        target.setValue( component, record.getValue( component ) );

      targetResult.add( target );
    }
  }

  /**
   * Either gets and existing component, or creates it if it doesn't exist yet.
   *
   * @return The index of the component
   */
  public static int getOrCreateComponent( final TupleResult result, final String componentID )
  {
    final int index = result.indexOfComponent( componentID );
    if( index != -1 )
      return index;

    result.addComponent( ComponentUtilities.getFeatureComponent( componentID ) );
    return result.indexOfComponent( componentID );
  }
}