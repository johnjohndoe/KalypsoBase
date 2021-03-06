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
 * Encapsulates the information of a <PropertyIsNull>-element (as defined in Filter DTD). The DTD defines the properties
 * type to be tested as PropertyName or Literal.
 * 
 * @author Markus Schneider
 * @version 07.08.2002
 */
public class PropertyIsNullOperation extends ComparisonOperation
{
  private static ExpressionDefines EXPRESSION_DEFINES = new ExpressionDefines();

  // PropertyName / Literal
  private Expression m_expression;

  public PropertyIsNullOperation( final Expression expression )
  {
    super( OperationDefines.PROPERTYISNULL );
    m_expression = expression;
  }

  public Expression getExpression( )
  {
    return m_expression;
  }

  public void setExpression( final Expression expr )
  {
    m_expression = expr;
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

    // check if root element's name equals 'PropertyIsNull'
    if( !element.getLocalName().equals( "PropertyIsNull" ) )
      throw new FilterConstructionException( "Name of element does not equal 'PropertyIsNull'!" );

    final ElementList children = XMLTools.getChildElements( element );
    if( children.getLength() != 1 )
      throw new FilterConstructionException( "'PropertyIsNull' requires exactly 1 element!" );

    final Element child = children.item( 0 );
    Expression expr = null;

    switch( EXPRESSION_DEFINES.getIdByName( child.getLocalName() ) )
    {
      case ExpressionDefines.PROPERTYNAME:
      {
        expr = PropertyName.buildFromDOM( child );
        break;
      }
      case ExpressionDefines.LITERAL:
      {
        expr = Literal.buildFromDOM( child );
        break;
      }
      default:
      {
        throw new FilterConstructionException( "Name of element does not equal 'PropertyIsNull'!" );
      }
    }

    return new PropertyIsNullOperation( expr );
  }

  /** Produces an indented XML representation of this object. */
  @Override
  public StringBuffer toXML( )
  {
    final StringBuffer sb = new StringBuffer( 500 );
    sb.append( "<ogc:" ).append( getOperatorName() ).append( ">" );
    sb.append( m_expression.toXML() );
    sb.append( "</ogc:" ).append( getOperatorName() ).append( ">" );
    return sb;
  }

  /**
   * Calculates the <tt>PropertyIsNull</tt> -Operation's logical value based on the certain property values of the given
   * <tt>Feature</tt>.
   * 
   * @param feature
   *          that determines the property values
   * @return true, if the <tt>PropertyIsNull</tt> -Operation evaluates to true, else false
   * @throws FilterEvaluationException
   *           if the evaluation fails
   */
  @Override
  public boolean evaluate( final Feature feature ) throws FilterEvaluationException
  {
    final Object value = m_expression.evaluate( feature );
    if( value == null )
      return true;
    return false;
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