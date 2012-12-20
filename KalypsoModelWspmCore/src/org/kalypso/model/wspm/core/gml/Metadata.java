/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.core.gml;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * @author Holger Albert
 */
public class Metadata extends Feature_Impl
{
  public static final QName FEATURE_METADATA = new QName( IWspmConstants.NS_WSPMPROF, "Metadata" ); //$NON-NLS-1$

  public static final QName PROPERTY_KEY = new QName( IWspmConstants.NS_WSPMPROF, "key" ); //$NON-NLS-1$

  public static final QName PROPERTY_VALUE = new QName( IWspmConstants.NS_WSPMPROF, "value" ); //$NON-NLS-1$

  public Metadata( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  public String getKey( )
  {
    return (String)getProperty( PROPERTY_KEY );
  }

  public String getValue( )
  {
    return (String)getProperty( PROPERTY_VALUE );
  }

  public void setKey( final String key )
  {
    setProperty( PROPERTY_KEY, key );
  }

  public void setValue( final String value )
  {
    setProperty( PROPERTY_VALUE, value );
  }
}