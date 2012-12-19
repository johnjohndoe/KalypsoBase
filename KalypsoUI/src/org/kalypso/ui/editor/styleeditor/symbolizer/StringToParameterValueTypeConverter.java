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
package org.kalypso.ui.editor.styleeditor.symbolizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.databinding.conversion.TypedConverter;
import org.kalypsodeegree.filterencoding.Expression;
import org.kalypsodeegree.graphics.sld.ParameterValueType;
import org.kalypsodeegree_impl.filterencoding.ExpressionDefines;
import org.kalypsodeegree_impl.filterencoding.Expression_Impl;
import org.kalypsodeegree_impl.filterencoding.Literal;
import org.kalypsodeegree_impl.filterencoding.PropertyName;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Gernot Belger
 */
public class StringToParameterValueTypeConverter extends TypedConverter<String, ParameterValueType>
{
  public StringToParameterValueTypeConverter( )
  {
    super( String.class, ParameterValueType.class );
  }

  @Override
  public ParameterValueType convertTyped( final String text )
  {
    if( StringUtils.isBlank( text ) )
      return null;

    final Collection<Object> components = new ArrayList<>();

    final Pattern pattern = Pattern.compile( "\\<(.*?):(.*?)\\>" ); //$NON-NLS-1$
    final Matcher matcher = pattern.matcher( text );

    while( matcher.find() )
    {
      final String normalText = matchCurrentText( matcher );
      if( !StringUtils.isEmpty( normalText ) )
        components.add( normalText );

      final String expressionName = matcher.group( 1 );
      final String expressionValue = matcher.group( 2 );
      final Expression expr = parseExpression( expressionName, expressionValue );
      if( expr != null )
        components.add( expr );
    }

    final String normalText = matchTail( matcher );
    if( !StringUtils.isEmpty( normalText ) )
      components.add( normalText );

    return StyleFactory.createParameterValueType( components.toArray( new Object[components.size()] ) );
  }

  private Expression parseExpression( final String expressionName, final String expressionValue )
  {
    final int id = Expression_Impl.EXPRESSION_DEFINES.getIdByName( expressionName );
    switch( id )
    {
      case ExpressionDefines.EXPRESSION:
        return null;

      case ExpressionDefines.PROPERTYNAME:
        return new PropertyName( expressionValue, null );

      case ExpressionDefines.LITERAL:
        return new Literal( expressionValue );

// case ExpressionDefines.FUNCTION:
// return Function.buildFromDOM( element );
// case ExpressionDefines.ADD:
// case ExpressionDefines.SUB:
// case ExpressionDefines.MUL:
// case ExpressionDefines.DIV:
// return ArithmeticExpression.buildFromDOM( element );

      default:
      {
        // TODO: implement other types of epxressions
        return null;
// throw new FilterConstructionException( "Unknown expression '" + expressionName + "'!" );
      }
    }
  }

  private String matchTail( final Matcher matcher )
  {
    final StringBuffer buf = new StringBuffer();
    matcher.appendTail( buf );
    return buf.toString();
  }

  private String matchCurrentText( final Matcher matcher )
  {
    final StringBuffer buf = new StringBuffer();
    matcher.appendReplacement( buf, StringUtils.EMPTY );
    return buf.toString();
  }
}