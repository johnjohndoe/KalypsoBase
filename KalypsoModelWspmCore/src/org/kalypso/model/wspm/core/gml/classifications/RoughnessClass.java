/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.IWspmPointProperties;

/**
 * @author Dirk Kuch
 */
public class RoughnessClass extends AbstractClassificationClass implements IRoughnessClass
{
  public RoughnessClass( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  @Override
  public String toString( )
  {
    return String.format( "Rauheit: %s\nks: %.3f, kst: %.3f", getName(), getKsValue(), getKstValue() );
  }

  @Override
  public BigDecimal getKstValue( )
  {
    return getProperty( PROPERTY_KST_VALUE, BigDecimal.class );
  }

  @Override
  public void setKstValue( final BigDecimal value )
  {
    setProperty( PROPERTY_KST_VALUE, value );
  }

  @Override
  public BigDecimal getKsValue( )
  {
    return getProperty( PROPERTY_KS_VALUE, BigDecimal.class );
  }

  @Override
  public void setKsValue( final BigDecimal value )
  {
    setProperty( PROPERTY_KS_VALUE, value );
  }

  @Override
  public BigDecimal getValue( final String component )
  {
    if( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS.equals( component ) )
      return getKsValue();
    else if( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST.equals( component ) )
      return getKstValue();

    throw new UnsupportedOperationException();
  }
}