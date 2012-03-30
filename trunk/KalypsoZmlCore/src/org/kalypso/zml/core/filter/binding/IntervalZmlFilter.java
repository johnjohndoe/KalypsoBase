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
package org.kalypso.zml.core.filter.binding;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.zml.core.KalypsoZmlCoreConstants;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * The interpolation zml filter.
 * 
 * @author Holger Albert
 */
public class IntervalZmlFilter extends Feature_Impl implements IZmlFilter
{
  /**
   * The qname of the mode.
   */
  public static final QName PROPERTY_MODE = new QName( KalypsoZmlCoreConstants.NS_ZML_FILTER, "mode" ); //$NON-NLS-1$

  /**
   * The qname of the calendar amount.
   */
  public static final QName PROPERTY_CALENDAR_AMOUNT = new QName( KalypsoZmlCoreConstants.NS_ZML_FILTER, "calendarAmount" ); //$NON-NLS-1$

  /**
   * The qname of the calendar field.
   */
  public static final QName PROPERTY_CALENDAR_FIELD = new QName( KalypsoZmlCoreConstants.NS_ZML_FILTER, "calendarField" ); //$NON-NLS-1$

  /**
   * The qname of the default status.
   */
  public static final QName PROPERTY_DEFAULT_STATUS = new QName( KalypsoZmlCoreConstants.NS_ZML_FILTER, "defaultStatus" ); //$NON-NLS-1$

  /**
   * The qname of the default value.
   */
  public static final QName PROPERTY_DEFAULT_VALUE = new QName( KalypsoZmlCoreConstants.NS_ZML_FILTER, "defaultValue" ); //$NON-NLS-1$

  public static final QName FEATURE_INTERVAL_ZML_FILTER = new QName( KalypsoZmlCoreConstants.NS_ZML_FILTER, "IntervalZmlFilter" ); //$NON-NLS-1$

  public IntervalZmlFilter( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  /**
   * This function returns the calendar amount.
   * 
   * @return The calendar amount.
   */
  public Integer getCalendarAmount( )
  {
    return getProperty( PROPERTY_CALENDAR_AMOUNT, Integer.class );
  }

  /**
   * This function returns the calendar field.
   * 
   * @return The calendar field.
   */
  public String getCalendarField( )
  {
    return getProperty( PROPERTY_CALENDAR_FIELD, String.class );
  }

  /**
   * This function returns the default status.
   * 
   * @return The default status.
   */
  public Integer getDefaultStatus( )
  {
    return getProperty( PROPERTY_DEFAULT_STATUS, Integer.class );
  }

  /**
   * This function returns the default value.
   * 
   * @return The default value.
   */
  public double getDefaultValue( )
  {
    return getDoubleProperty( PROPERTY_DEFAULT_VALUE, 0.0 );
  }
}