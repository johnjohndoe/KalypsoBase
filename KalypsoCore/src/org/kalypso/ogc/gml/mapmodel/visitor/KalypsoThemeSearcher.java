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
package org.kalypso.ogc.gml.mapmodel.visitor;

import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemePredicate;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor;

/**
 * A {@link org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor} that searches for the first theme that matches a given
 * {@link org.kalypso.ogc.gml.mapmodel.IKalypsoThemePredicate}.
 * 
 * @author Gernot Belger
 */
public class KalypsoThemeSearcher implements IKalypsoThemeVisitor
{
  private final IKalypsoThemePredicate m_predicate;

  private IKalypsoTheme m_foundTheme;

  public KalypsoThemeSearcher( final IKalypsoThemePredicate predicate )
  {
    m_predicate = predicate;
  }

  @Override
  public boolean visit( final IKalypsoTheme theme )
  {
    /* Stop search after first match */
    if( m_foundTheme == null )
    {
      if( m_predicate.decide( theme ) )
        m_foundTheme = theme;
    }

    /* Only needs to recurse if nothing found yet */
    return m_foundTheme == null;
  }

  public IKalypsoTheme getFoundTheme( )
  {
    return m_foundTheme;
  }
}