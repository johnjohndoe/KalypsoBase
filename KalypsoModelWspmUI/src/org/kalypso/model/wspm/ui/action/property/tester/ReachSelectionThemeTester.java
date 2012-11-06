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
package org.kalypso.model.wspm.ui.action.property.tester;

import java.util.Iterator;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.model.wspm.core.gml.WspmReach;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.outline.nodes.FeatureThemeNode;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;

/**
 * @author Dirk Kuch
 */
public class ReachSelectionThemeTester extends PropertyTester
{
  private static final String PROPERTY_HAS_REACH_SELECTION = "hasReachSelection"; //$NON-NLS-1$

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    try
    {
      if( PROPERTY_HAS_REACH_SELECTION.equals( property ) )
        return testHasReachSelection( receiver );

      return false;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return false;
    }
  }

  private boolean testHasReachSelection( final Object receiver )
  {
    if( !(receiver instanceof IStructuredSelection) )
      return false;

    final IStructuredSelection selection = (IStructuredSelection)receiver;
    final Iterator< ? > itr = selection.iterator();
    while( itr.hasNext() )
    {
      final Object next = itr.next();
      if( next instanceof FeatureThemeNode )
      {
        final FeatureThemeNode theme = (FeatureThemeNode)next;
        final IKalypsoFeatureTheme element = theme.getElement();
        final FeatureList featureList = element.getFeatureList();

        final Feature owner = featureList == null ? null : featureList.getOwner();
        if( owner instanceof WspmReach )
          return true;
      }

      if( next instanceof WspmReach )
        return true;
    }

    return false;
  }
}