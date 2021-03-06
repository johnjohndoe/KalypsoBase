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
package org.kalypsodeegree_impl.filterencoding;

import org.apache.commons.lang3.ObjectUtils;
import org.kalypsodeegree.filterencoding.Expression;
import org.kalypsodeegree.filterencoding.FilterConstructionException;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.filterencoding.Operation;
import org.kalypsodeegree.filterencoding.visitor.FilterVisitor;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.xml.ElementList;
import org.kalypsodeegree.xml.XMLTools;
import org.w3c.dom.Element;

/**
 * Encapsulates the information of a <PropertyIsCOMP>-element (as defined in Filter DTD). COMP can be one of the
 * following:
 * <ul>
 * <li>EqualTo</li>
 * <li>LessThan</li>
 * <li>GreaterThan</li>
 * <li>LessThanOrEqualTo</li>
 * <li>GreaterThanOrEqualTo</li>
 * </ul>
 * 
 * @author Markus Schneider
 * @version 07.08.2002
 */
public class PropertyIsCOMPOperation extends ComparisonOperation
{
  private Expression m_expr1;

  private Expression m_expr2;

  /**
   * Creates a new PropertyIsCOMPOperation object.
   * 
   * @param id
   * @param expr1
   * @param expr2
   */
  public PropertyIsCOMPOperation( final int id, final Expression expr1, final Expression expr2 )
  {
    super( id );
    m_expr1 = expr1;
    m_expr2 = expr2;
  }

  public void setOperatorId( final int operatorId )
  {
    m_operatorId = operatorId;
  }

  /**
   * Given a DOM-fragment, a corresponding Operation-object is built. This method recursively calls other buildFromDOM
   * () - methods to validate the structure of the DOM-fragment.
   * 
   * @throws FilterConstructionException
   *           if the structure of the DOM-fragment is invalid
   */
  public static Operation buildFromDOM( final Element element ) throws FilterConstructionException
  {
    // check if root element's name is a known operator
    final String name = element.getLocalName();
    final int operatorId = OperationDefines.getIdByName( name );

    switch( operatorId )
    {
      case OperationDefines.PROPERTYISEQUALTO:
      case OperationDefines.PROPERTYISNOTEQUALTO:
      case OperationDefines.PROPERTYISLESSTHAN:
      case OperationDefines.PROPERTYISGREATERTHAN:
      case OperationDefines.PROPERTYISLESSTHANOREQUALTO:
      case OperationDefines.PROPERTYISGREATERTHANOREQUALTO:
        break;
      default:
        throw new FilterConstructionException( "'" + name + "' is not a PropertyIsOperator!" );
    }

    final ElementList children = XMLTools.getChildElements( element );

    if( children.getLength() != 2 )
    {
      throw new FilterConstructionException( "'" + name + "' requires exactly 2 elements!" );
    }

    final Expression expr1 = Expression_Impl.buildFromDOM( children.item( 0 ) );
    final Expression expr2 = Expression_Impl.buildFromDOM( children.item( 1 ) );

    return new PropertyIsCOMPOperation( operatorId, expr1, expr2 );
  }

  /**
   * returns the first <tt>Expression</tt> of the comparison
   */
  public Expression getFirstExpression( )
  {
    return m_expr1;
  }

  public void setFirstExperssion( final Expression expr1 )
  {
    m_expr1 = expr1;
  }

  /**
   * returns the second <tt>Expression</tt> of the comparison
   */
  public Expression getSecondExpression( )
  {
    return m_expr2;
  }

  public void setSecondExperssion( final Expression expr2 )
  {
    m_expr2 = expr2;
  }

  /** Produces an indented XML representation of this object. */
  @Override
  public StringBuffer toXML( )
  {
    final StringBuffer sb = new StringBuffer( 500 );
    sb.append( "<ogc:" ).append( getOperatorName() ).append( ">" );
    sb.append( m_expr1.toXML() );
    sb.append( m_expr2.toXML() );
    sb.append( "</ogc:" ).append( getOperatorName() ).append( ">" );
    return sb;
  }

  /**
   * Calculates the <tt>ComparisonOperation</tt>'s logical value based on the certain property values of the given
   * <tt>Feature</tt>. TODO: Improve datatype handling.
   * 
   * @param feature
   *          that determines the property values
   * @return true, if the <tt>FeatureFilter</tt> evaluates to true, else false
   * @throws FilterEvaluationException
   *           if the expressions to be compared are of different types
   */
  @Override
  public boolean evaluate( final Feature feature ) throws FilterEvaluationException
  {
    Object value1 = m_expr1.evaluate( feature );
    Object value2 = m_expr2.evaluate( feature );

    if( value1 == null || value2 == null )
      return false;

    // Convert to comparable data type

    if( value1 instanceof String && value2 instanceof Boolean || value1 instanceof Boolean && value2 instanceof String )
    {
      // Prefer boolean comparison
      if( value1 instanceof String )
        value1 = Boolean.valueOf( (String) value1 );
      else
        value2 = Boolean.valueOf( (String) value2 );
    }

    if( value1 instanceof String && value2 instanceof Number || value1 instanceof Number && value2 instanceof String )
    {
      if( value1 instanceof String )
      {
        // Prefer numeric comparison
        try
        {
          value1 = Double.valueOf( (String) value1 );
        }
        catch( final NumberFormatException e )
        {
          value2 = value2.toString();
        }
      }
      else
      {
        try
        {
          value2 = Double.valueOf( (String) value2 );
        }
        catch( final NumberFormatException e )
        {
          value1 = value1.toString();
        }
      }
    }

    // compare Strings
    if( value1 instanceof String && value2 instanceof String )
    {
      switch( getOperatorId() )
      {
        case OperationDefines.PROPERTYISEQUALTO:
          return ObjectUtils.equals( value1, value2 );

        case OperationDefines.PROPERTYISNOTEQUALTO:
          return !ObjectUtils.equals( value1, value2 );

        case OperationDefines.PROPERTYISLESSTHAN:
        case OperationDefines.PROPERTYISGREATERTHAN:
        case OperationDefines.PROPERTYISLESSTHANOREQUALTO:
        case OperationDefines.PROPERTYISGREATERTHANOREQUALTO:
          throw new FilterEvaluationException( "'" + getOperatorName() + "' can not be applied to " + "String values!" );
        default:
          throw new FilterEvaluationException( "Unknown comparison operation: '" + getOperatorName() + "'!" );
      }
    }// compare Doubles
    else if( value1 instanceof Number && value2 instanceof Number )
    {
      final double d1 = ((Number) value1).doubleValue();
      final double d2 = ((Number) value2).doubleValue();

      switch( getOperatorId() )
      {
        case OperationDefines.PROPERTYISEQUALTO:
          return ObjectUtils.equals( d1, d2 );

        case OperationDefines.PROPERTYISNOTEQUALTO:
          return !ObjectUtils.equals( d1, d2 );

        case OperationDefines.PROPERTYISLESSTHAN:
          return d1 < d2;
        case OperationDefines.PROPERTYISGREATERTHAN:
          return d1 > d2;
        case OperationDefines.PROPERTYISLESSTHANOREQUALTO:
          return d1 <= d2;
        case OperationDefines.PROPERTYISGREATERTHANOREQUALTO:
          return d1 >= d2;
        default:
          throw new FilterEvaluationException( "Unknown comparison operation: '" + getOperatorName() + "'!" );
      }
    }
    // compare boolean
    else if( value1 instanceof Boolean && value2 instanceof Boolean )
    {
      final Boolean b1 = (Boolean) value1;
      final Boolean b2 = (Boolean) value2;

      switch( getOperatorId() )
      {
        case OperationDefines.PROPERTYISEQUALTO:
          return ObjectUtils.equals( b1, b2 );

        case OperationDefines.PROPERTYISNOTEQUALTO:
          return !ObjectUtils.equals( b1, b2 );

        default:
          final String msg = String.format( "Cannot apply comparison operation '%s' to boolean values.", getOperatorName() );
          throw new FilterEvaluationException( msg );
      }
    }
    else
    {
      throw new FilterEvaluationException( "Can not apply operation '" + getOperatorName() + "' to " + "different datatypes!" );
    }
  }

  /**
   * @see org.kalypsodeegree.filterencoding.Operation#accept(org.kalypsodeegree.filterencoding.visitor.FilterVisitor,
   *      org.kalypsodeegree.filterencoding.Operation, int)
   */
  @Override
  public void accept( final FilterVisitor fv, final Operation operation, final int depth )
  {
    fv.visit( this );
  }
}