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

import java.math.BigInteger;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * @author Patrice Congo
 */
public class Polynomial2D extends Feature_Impl implements IPolynomial2D
{
  public Polynomial2D( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  public static final QName QNAME = new QName( NS.COMMON_MATH, "Polynomial2D" );

  public static final QName QNAME_PROP_DEGREEX = new QName( NS.COMMON_MATH, "degreeX" );

  public static final QName QNAME_PROP_DEGREEY = new QName( NS.COMMON_MATH, "degreeY" );

  public static final QName QNAME_PROP_COEFFICIENTS = new QName( NS.COMMON_MATH, "coefficients" );

  @Override
  public PolynomialConfigState checkConsistency( )
  {
    return null;
  }

  public static final PolynomialConfigState checkConsistency( final int degreeX, final int degreeY, final double[] coefficients )
  {
    if( coefficients == null )
    {
      throw new IllegalArgumentException( "argument coefs must not be null" );
    }

    if( degreeX < 0 )
    {
      return PolynomialConfigState.NEGATIVE_DEGREEX;
    }

    if( degreeY < 0 )
    {
      return PolynomialConfigState.NEGATIVE_DEGREEY;
    }

    if( (degreeY + 1) * (degreeX + 1) != coefficients.length )
    {
      return PolynomialConfigState.ORDER_COEF_MISMATCH;
    }
    // TODO check last x and last y
    if( coefficients[coefficients.length - 1] == 0.0 )
    {
      return PolynomialConfigState.ZERO_MOST_SIGNIFICANT_COEFS;
    }
    return PolynomialConfigState.CONSISTENCY_OK;
  }

  @Override
  public double evaluate( final double inputX, final double inputY )
  {
    throw new RuntimeException( "not supported" );
  }

  @Override
  public double[] getCoefficients( ) throws IllegalFeatureState
  {
    final Object coefs = getProperty( QNAME_PROP_COEFFICIENTS );
    if( coefs instanceof String )
    {
      final String[] subStrings = ((String) coefs).split( " " );// "/s+");
      final double doubles[] = new double[subStrings.length];
      for( int i = 0; i < subStrings.length; i++ )
      {
        doubles[i] = Double.parseDouble( subStrings[i] );
      }
      return doubles;
    }
    else
    {
      throw new IllegalFeatureState( this, QNAME_PROP_COEFFICIENTS, coefs );
    }
  }

  @Override
  public void setCefficients( final double[] coefficients ) throws IllegalArgumentException
  {

  }

  @Override
  public int getDegreeX( ) throws IllegalFeatureState
  {
    return getDegree( QNAME_PROP_DEGREEX );
  }

  @Override
  public void setDegreeX( final int degreeX )
  {
    setDegree( degreeX, QNAME_PROP_DEGREEX );
  }

  @Override
  public int getDegreeY( ) throws IllegalFeatureState
  {
    return getDegree( QNAME_PROP_DEGREEY );
  }

  @Override
  public void setDegreeY( final int degreeY )
  {
    setDegree( degreeY, QNAME_PROP_DEGREEY );
  }

  private final void setDegree( final int degree, final QName degreeQName ) throws IllegalArgumentException
  {

    if( degree <= 0 )
    {
      throw new IllegalArgumentException();
    }

    setProperty( degreeQName, new Integer( degree ) );
  }

  private final int getDegree( final QName degreeQName ) throws IllegalFeatureState
  {
    final Object dx = getProperty( degreeQName );
    if( dx instanceof BigInteger )
    {
      return ((BigInteger) dx).intValue();
    }
    else
    {
      final StringBuffer buf = new StringBuffer( 128 );
      buf.append( degreeQName );
      buf.append( " must be and integer but got:" );
      buf.append( dx == null ? null : dx.getClass() );
      buf.append( "with the value:" );
      buf.append( dx );
      throw new IllegalFeatureState( buf.toString(), this, degreeQName, dx );

    }

  }

  @Override
  public void setPolynomParameters( final int degreeX, final int degreeY, final double[] coefficients ) throws IllegalArgumentException
  {
    if( PolynomialConfigState.CONSISTENCY_OK != checkConsistency( degreeX, degreeY, coefficients ) )
    {
      throw new IllegalArgumentException();
    }

    final StringBuffer buf = new StringBuffer( 128 );
    for( final double coef : coefficients )
    {
      buf.append( coef );
      buf.append( ' ' );
    }
    setProperty( QNAME_PROP_COEFFICIENTS, buf.toString() );
  }

}
