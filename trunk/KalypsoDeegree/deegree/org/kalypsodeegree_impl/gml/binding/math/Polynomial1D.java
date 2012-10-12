/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.gml.binding.math;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * Default imlementation of the IPolynom1D interface
 *
 * @author Patrice Congo
 */
public class Polynomial1D extends Feature_Impl implements IPolynomial1D
{
  public Polynomial1D( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  @Override
  public double computeResult( final double input )
  {
    // computation based on Hornerschema
    final double coefs[] = getCoefficients();

    int i = coefs.length;
    // the empty sum is always 0
    if( i == 0 )
      return 0.0;

    i--;// last element
    double result = coefs[i];
    i--;
    for( ; i >= 0; i-- )
      result = result * input + coefs[i];

    return result;
  }

  @Override
  public double[] getCoefficients( )
  {
    final List<Double> coefs = (List<Double>) getProperty( QNAME_PROP_COEFFICIENTS );
    if( coefs == null )
      return new double[0];

    final Double[] objects = coefs.toArray( new Double[coefs.size()] );
    return ArrayUtils.toPrimitive( objects, Double.NaN );
  }

  @Override
  public void setCoefficients( final double[] coefficients ) throws IllegalArgumentException
  {
    final List<Double> list = new ArrayList<>();

    if( coefficients != null )
    {
      for( final double coefficient : coefficients )
        list.add( coefficient );
    }

    setProperty( QNAME_PROP_COEFFICIENTS, list );
  }

  // FIXME 1) overwriting equals of Feature's is forbidden -> break integrity of feature lists etc.
  // FIXME 2) !! hashCode not overwritten as well !!
// @Override
// public boolean equals( final Object obj )
// {
// if( this == obj )
// {
// return true;
// }
// else if( obj instanceof IPolynomial1D )
// {
// final double thisCoefs[] = getCoefficients();
// final double compCoefs[] = ((IPolynomial1D) obj).getCoefficients();
// int i = thisCoefs.length;
// if( i != compCoefs.length )
// {
// return false;
// }
// i--;// goto last element
// for( ; i >= 0; i-- )
// {
// if( thisCoefs[i] != compCoefs[i] )
// {
// return false;
// }
// }
// return true;
//
// }
// else
// {
// return false;// super.equals(obj);
// }
// }

  @Override
  public String toString( )
  {
    final StringBuffer buf = new StringBuffer( 64 );
    buf.append( "Polynomial1D" );
    final String id = getId();
    if( id != null )
    {
      buf.append( '.' );
      buf.append( id );
    }
    // coefs
    buf.append( "[ coefs=" );
    for( final double coef : getCoefficients() )
    {
      buf.append( String.valueOf( coef ) );
      buf.append( ' ' );
    }
    buf.append( ']' );
    return buf.toString();
  }

  @Override
  public PolynomialConfigState checkConsistency( )
  {
    // how should it be ever inconsistent
    return PolynomialConfigState.CONSISTENCY_OK;
  }

  @Override
  public double getRangeMin( )
  {
    final Double property = (Double) getProperty( QNAME_PROP_MINRANGE );
    if( property == null )
      return Double.NEGATIVE_INFINITY;

    return property;
  }

  @Override
  public double getRangeMax( )
  {
    final Double property = (Double) getProperty( QNAME_PROP_MAXRANGE );
    if( property == null )
      return Double.POSITIVE_INFINITY;

    return property;
  }

  @Override
  public void setRange( final double from, final double to )
  {
    setProperty( QNAME_PROP_MINRANGE, Double.isNaN( from ) ? null : from );
    setProperty( QNAME_PROP_MAXRANGE, Double.isNaN( to ) ? null : to );
  }

  @Override
  public void setDomainPhenomenon( final String domainId )
  {
    final Feature ref = refForId( domainId );

    setProperty( QNAME_PROP_DOMAIN_PHENOMENON, ref );
  }

  @Override
  public void setRangePhenomenon( final String rangeId )
  {
    final Feature ref = refForId( rangeId );

    setProperty( QNAME_PROP_RANGE_PHENOMENON, ref );
  }

  private Feature refForId( final String domainId )
  {
    final IRelationType relation = (IRelationType) getFeatureType().getProperty( QNAME_PROP_DOMAIN_PHENOMENON );
    final IFeatureType featureType = GMLSchemaUtilities.getFeatureTypeQuiet( new QName( NS.SWE, "Phenomenon" ) );

    return FeatureFactory.createXLink( this, relation, featureType, domainId );
  }

  @Override
  public String getDomainPhenomenon( )
  {
    final Feature feature = this;
    final Object property = feature.getProperty( QNAME_PROP_DOMAIN_PHENOMENON );
    return propertyToId( property );
  }

  @Override
  public String getRangePhenomenon( )
  {
    final Feature feature = this;
    final Object property = feature.getProperty( QNAME_PROP_RANGE_PHENOMENON );
    return propertyToId( property );
  }

  private String propertyToId( final Object property )
  {
    if( property == null )
      return null;

    if( property instanceof IXLinkedFeature )
      return ((IXLinkedFeature) property).getHref();

    if( property instanceof Feature )
      return ((Feature) property).getId();

    return property.toString();
  }
}