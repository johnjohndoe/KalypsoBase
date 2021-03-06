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
package org.kalypso.model.wspm.core.gml.classifications;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.IWspmPointProperties;

/**
 * @author Dirk Kuch
 */
public class VegetationClass extends AbstractClassificationClass implements IVegetationClass
{
  public VegetationClass( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  @Override
  public BigDecimal getAx( )
  {
    return getProperty( PROPERTY_AX, BigDecimal.class );
  }

  @Override
  public BigDecimal getAy( )
  {
    return getProperty( PROPERTY_AY, BigDecimal.class );
  }

  @Override
  public BigDecimal getDp( )
  {
    return getProperty( PROPERTY_DP, BigDecimal.class );
  }

  @Override
  public void setAx( final BigDecimal value )
  {
    setProperty( PROPERTY_AX, value );
  }

  @Override
  public void setAy( final BigDecimal value )
  {
    setProperty( PROPERTY_AY, value );
  }

  @Override
  public void setDp( final BigDecimal value )
  {
    setProperty( PROPERTY_DP, value );
  }

  @Override
  public BigDecimal getValue( final String identifier )
  {
    if( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AX.equals( identifier ) )
      return getAx();
    else if( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AY.equals( identifier ) )
      return getAy();
    else if( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_DP.equals( identifier ) )
      return getDp();

    throw new UnsupportedOperationException();
  }

  @Override
  public String getLabelWithValues( )
  {
    final String ax = formatValue( StringUtils.EMPTY, "%.2f", getAx() ); //$NON-NLS-1$
    final String ay = formatValue( StringUtils.EMPTY, "%.2f", getAy() ); //$NON-NLS-1$
    final String dp = formatValue( StringUtils.EMPTY, "%.2f", getDp() ); //$NON-NLS-1$

    return String.format( "%s (%s, %s, %s)", getDescription(), ax, ay, dp ); //$NON-NLS-1$
  }
}