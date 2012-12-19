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
package org.kalypsodeegree_impl.filterencoding;

import java.util.List;

import org.kalypsodeegree.filterencoding.Expression;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.model.feature.Feature;

/**
 * A {@link IFunctionExpression} implementation that represents the parent feature of the current feature in context.<br/>
 * This function allows one sub-expression, that will be evaluated against the parent feature.
 *
 * @author Gernot Belger
 */
public class ParentFeatureFunctionExpression extends AbstractFunctionExpression
{
  @Override
  public Object evaluate( final Feature feature, final List<Expression> args ) throws FilterEvaluationException
  {
    if( feature == null )
      throw new FilterEvaluationException( "Missing context feature" ); //$NON-NLS-1$

    final Feature parent = feature.getOwner();
    if( parent == null )
      throw new FilterEvaluationException( "Context feature has no parent feature" ); //$NON-NLS-1$

    if( args.size() != 1 )
    {
      final String message = String.format( "Function '%s' must have exactly one sub-expression. Found: %d", getName(), args.size() ); //$NON-NLS-1$
      throw new FilterEvaluationException( message );
    }

    final Expression expression = args.get( 0 );
    if( expression == null )
      throw new FilterEvaluationException( "Sub-Expression is null." ); //$NON-NLS-1$

    return expression.evaluate( parent );
  }
}