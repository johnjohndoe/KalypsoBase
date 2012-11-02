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

import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.databinding.conversion.TypedConverter;
import org.kalypsodeegree.filterencoding.Expression;
import org.kalypsodeegree.graphics.sld.ParameterValueType;
import org.kalypsodeegree_impl.filterencoding.PropertyName;

/**
 * @author Gernot Belger
 */
public class ParameterValueTypeToString extends TypedConverter<ParameterValueType, String>
{
  public ParameterValueTypeToString( )
  {
    super( ParameterValueType.class, String.class );
  }

  @Override
  public String convertTyped( final ParameterValueType value )
  {
    if( value == null )
      return StringUtils.EMPTY;

    final StringBuilder builder = new StringBuilder();

    final Object[] components = value.getComponents();
    for( final Object component : components )
    {
      final String componentFormat = formatComponent( component );
      builder.append( componentFormat );
    }
    return builder.toString();
  }

  private String formatComponent( final Object component )
  {
    if( component instanceof String )
      return (String)component;

    if( !(component instanceof Expression) )
      throw new UnsupportedOperationException();

    final Expression expr = (Expression)component;

    final StringBuilder sb = new StringBuilder();
    sb.append( '<' );
    sb.append( expr.getExpressionName() );
    sb.append( ':' );

    if( component instanceof PropertyName )
      sb.append( ((PropertyName)component).getValue() );
    else
    {
      // TODO: need to implement other expression types
      sb.append( "TODO" ); //$NON-NLS-1$
    }

    sb.append( '>' );
    return sb.toString();
  }
}