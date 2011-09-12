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
package org.kalypso.ogc.gml.map.properties;

import org.eclipse.core.expressions.PropertyTester;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;

/**
 * This property tester tests a mapPanel for a theme containing a property named like the property parameter.<br/>
 * The expected value parameter defines a boolean property of {@link ThemeTester} and is delegated to it.
 * 
 * @author Holger Albert
 */
public class PropertyThemeTester extends PropertyTester
{
  /**
   * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[],
   *      java.lang.Object)
   */
  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    /* Check the receiver. */
    if( !(receiver instanceof IMapPanel) )
      return false;

    /* Cast. */
    IMapPanel mapPanel = (IMapPanel) receiver;

    /* Check the property. */
    if( !"hasProperty".equals( property ) )
      return false;

    /* Check the args. */
    // TODO

    /* Check the expected value. */
    if( !(expectedValue instanceof String) )
      return false;

    /* Cast. */
    String expectedProperty = (String) expectedValue;

    /* Get the map model. */
    IMapModell mapModel = mapPanel.getMapModell();
    if( mapModel == null )
      return false;

    /* Find the themes with the expected property. */
    IKalypsoTheme[] themes = MapModellHelper.findThemeByProperty( mapModel, expectedProperty, IKalypsoThemeVisitor.DEPTH_ZERO );
    if( themes == null || themes.length == 0 )
      return false;

    /* If no arguments were specified, we return true, because then only the existence of the theme does matter. */
    if( args == null || args.length == 0 )
      return true;

    return new ThemeTester().test( themes[0], (String) args[0], new Object[] {}, "true" );
  }
}