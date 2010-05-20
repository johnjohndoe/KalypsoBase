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
package org.kalypso.observation.result;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.property.restriction.EnumerationRestriction;
import org.kalypso.gmlschema.property.restriction.FractionDigitRestriction;
import org.kalypso.gmlschema.property.restriction.IRestriction;

/**
 * Utility methods for components
 * 
 * @author schlienger
 */
public final class ComponentUtilities
{
  private ComponentUtilities( )
  {
    // utility class
  }

  /**
   * @return the first component of the given type if found, else null
   */
  public static IComponent findComponent( final IComponent[] comps, final QName typeName )
  {
    for( final IComponent element : comps )
    {
      if( element.getValueTypeName().equals( typeName ) )
        return element;
    }

    return null;
  }

  /**
   * @return the first component found that is not of the given type
   */
  public static IComponent otherComponent( final IComponent[] comps, final QName typeName )
  {
    for( int i = 0; i < comps.length; i++ )
    {
      if( !comps[i].getValueTypeName().equals( typeName ) )
        return comps[i];
    }

    return null;
  }

  /**
   * @return the first component found that equals the given one
   */
  public static IComponent sameComponent( final IComponent[] comps, final IComponent comp )
  {
    for( final IComponent element : comps )
    {
      if( element.equals( comp ) )
        return element;
    }

    return null;
  }

  /**
   * TODO: move into helper class of restrictions!
   * 
   * @author Dirk Kuch
   */
  static public boolean restrictionContainsEnumeration( final IRestriction[] restrictions )
  {
    for( final IRestriction restriction : restrictions )
    {
      if( restriction instanceof EnumerationRestriction )
        return true;
    }

    return false;
  }

  /**
   * Searches for a component by id.
   * 
   * @return <code>null</code>, if no compoentn with the given id is found, else the first found component.
   */
  public static IComponent findComponentByID( final IComponent[] components, final String componentID )
  {
    for( final IComponent component : components )
    {
      if( componentID.equals( component.getId() ) )
        return component;
    }

    return null;
  }

  /**
   * Returns a human readable label of the component.<br>
   * The label is usually of the form '<componentName> [<unit-name>]' (e.g. 'Discharge [m≥/s]'.
   */
  public static String getComponentLabel( final IComponent component )
  {
    final String name = getComponentName( component );
    final String unit = getComponentUnitLabel( component );
    if( unit == null )
      return name;

    return String.format( "%s [%s]", name, unit );
  }

  /**
   * Returns the label of the unit of a {@link IComponent}.<br>
   * The label is returned by the following rules:
   * <ul>
   * <li>TODO: use name/description of referenced unit</li>
   * <li>if no reference is set, the local part of the reference is returned</li>
   * <li>if the local part of the reference is 'none', '-' is returned</li>
   * </ul>
   */
  public static String getComponentUnitLabel( final IComponent component )
  {
    if( component == null )
      return ""; //$NON-NLS-1$

    final String unitReference = component.getUnit();
    if( unitReference == null )
      return null;

    final String localRef = getLocalUnitReference( unitReference );
    if( "none".equals( localRef ) ) //$NON-NLS-1$
      return "-"; //$NON-NLS-1$

    if( localRef.isEmpty() )
      return null;

    return localRef;
  }

  private static String getLocalUnitReference( final String unitReference )
  {
    final int hashIndex = unitReference.indexOf( '#' );
    if( hashIndex == -1 )
      return unitReference;

    return unitReference.substring( hashIndex + 1 );
  }

  private static String getComponentName( final IComponent component )
  {
    // TODO: fixme, use description and or name of phenomenon
    return component.getName();
  }

  /**
   * Returns the scale (number of decimal digits) of this component.<br>
   * This works only for components that contain a {@link FractionDigitRestriction}.<br>
   * 
   * @return -1 If no scale can be determined.
   * @see org.kalypso.observation.result.IComponent#getPrecision()
   */
  public static int getScale( final IComponent component )
  {
    for( final IRestriction restriction : component.getRestrictions() )
    {
      if( restriction instanceof FractionDigitRestriction )
        return ((FractionDigitRestriction) restriction).getFractionDigits();
    }

    return -1;
  }

}
