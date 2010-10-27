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
package org.kalypso.model.wspm.ui.profil.wizard.utils;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.outline.nodes.FeatureThemeNode;

/**
 * Helper class for wizards on {@link IKalypsoFeatureTheme}s.
 * 
 * @author Gernot Belger
 */
public final class FeatureThemeWizardUtilitites
{
  private FeatureThemeWizardUtilitites( )
  {
    throw new UnsupportedOperationException( "helper class, don't instantiate" );
  }

  public static IKalypsoFeatureTheme findTheme( final ISelection selection )
  {
    if( !(selection instanceof IStructuredSelection) )
      return null;

    final IStructuredSelection structSel = (IStructuredSelection) selection;

    for( final Iterator< ? > iterator = structSel.iterator(); iterator.hasNext(); )
    {
      final Object element = iterator.next();
      final IKalypsoFeatureTheme theme = searchTheme( element );
      if( theme != null )
        return theme;
    }

    return null;
  }

  private static IKalypsoFeatureTheme searchTheme( final Object selectedObject )
  {
    if( selectedObject instanceof IKalypsoFeatureTheme )
      return (IKalypsoFeatureTheme) selectedObject;

    if( selectedObject instanceof FeatureThemeNode )
    {
      final FeatureThemeNode theme = (FeatureThemeNode) selectedObject;
      return theme.getTheme();
    }

    return null;
  }


}
