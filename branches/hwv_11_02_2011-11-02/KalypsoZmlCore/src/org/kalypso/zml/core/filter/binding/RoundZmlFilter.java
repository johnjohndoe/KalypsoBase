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
 * The round zml filter.
 * 
 * @author Holger Albert
 */
public class RoundZmlFilter extends Feature_Impl implements IZmlFilter
{
  /**
   * The qname of the factor.
   */
  public static final QName QNAME_FACTOR = new QName( KalypsoZmlCoreConstants.NS_ZML_FILTER, "factor" );

  /**
   * The qname of the mode.
   */
  public static final QName QNAME_MODE = new QName( KalypsoZmlCoreConstants.NS_ZML_FILTER, "mode" );

  /**
   * The qname of the axis type.
   */
  public static final QName QNAME_AXIS_TYPE = new QName( KalypsoZmlCoreConstants.NS_ZML_FILTER, "axisType" );

  public static final QName QNAME_ROUND_ZML_FILTER = new QName( KalypsoZmlCoreConstants.NS_ZML_FILTER, "RoundZmlFilter" );

  /**
   * The constructor.
   * 
   * @param parent
   * @param parentRelation
   * @param ft
   * @param id
   * @param propValues
   */
  public RoundZmlFilter( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  /**
   * This function returns the factor.
   * 
   * @return The factor.
   */
  public Integer getFactor( )
  {
    return getProperty( QNAME_FACTOR, Integer.class );
  }

  /**
   * This function returns the mode.
   * 
   * @return The mode.
   */
  public String getMode( )
  {
    return getProperty( QNAME_MODE, String.class );
  }

  /**
   * This function returns the axis type.
   * 
   * @return The axis type.
   */
  public String getAxisType( )
  {
    return getProperty( QNAME_AXIS_TYPE, String.class );
  }
}