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
package org.kalypso.ui.internal.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @author Gernot Belger
 */
public class WorkbenchWindowPropertyTest extends PropertyTester
{
  private final static String PROPERTY_IS_EDITOR_AREA_VISIBLE = "isEditorAreaVisible"; //$NON-NLS-1$

  private final static String PROPERTY_IS_VIEW_VISIBLE = "isViewVisible"; //$NON-NLS-1$

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( !(receiver instanceof IWorkbenchWindow) )
      throw new IllegalArgumentException();

    final IWorkbenchWindow window = (IWorkbenchWindow)receiver;

    if( PROPERTY_IS_EDITOR_AREA_VISIBLE.equals( property ) )
      return testIsEditorAreaVisible( window, expectedValue );

    if( PROPERTY_IS_VIEW_VISIBLE.equals( property ) )
      return testIsViewVisible( window, expectedValue );

    throw new IllegalArgumentException();
  }

  private boolean testIsViewVisible( final IWorkbenchWindow window, final Object expectedValue )
  {
    final String viewID = ObjectUtils.toString( expectedValue );
    if( window == null )
      return false;

    final IWorkbenchPage page = window.getActivePage();
    if( page == null )
      return false;

    final IViewPart part = page.findView( viewID );
    if( part == null )
      return false;

    return page.isPartVisible( part );
  }

  private boolean testIsEditorAreaVisible( final IWorkbenchWindow window, final Object expectedValue )
  {
    final boolean expected = parseBoolean( expectedValue, true );

    final IWorkbenchPage activePage = window.getActivePage();
    if( activePage == null )
      return false == expected;

    return activePage.isEditorAreaVisible() == expected;
  }

  private boolean parseBoolean( final Object expected, final boolean defaultValue )
  {
    if( expected == null )
      return defaultValue;

    if( expected instanceof Boolean )
      return ((Boolean)expected).booleanValue();

    if( expected instanceof String )
    {
      final String expectedString = expected.toString();
      if( StringUtils.isBlank( expectedString ) )
        return defaultValue;

      return Boolean.parseBoolean( expectedString );
    }

    return defaultValue;
  }
}