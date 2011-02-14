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
package org.kalypso.contribs.eclipse.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * This class provides functions for handling views.
 * 
 * @author Holger Albert
 */
public class ViewUtilities
{
  /**
   * The constructor.
   */
  public ViewUtilities( )
  {
  }

  /**
   * This function collects all views with a <code>primaryId</code> and optional a <code>secondary id</code> of the
   * current active workbench window. In some cases it will return a empty list.<br/>
   * These include:<br/>
   * <ul>
   * <li>There is no workbench.</li>
   * <li>There is no active workbench window.</li>
   * <li>There are no pages in the active workbench window.</li>
   * <li>There are no views open.</li>
   * <li>No views matching the given criteria is open.</li>
   * </ul>
   * 
   * @param primaryId
   *          The primary id.
   * @param secondaryId
   *          The secondary id. May be null.
   * @return The views in the active workbench window or a empty list.
   */
  public static IViewPart[] findViewsInActiveWindow( String primaryId, String secondaryId )
  {
    /* Get the active workbench. */
    IWorkbench workbench = PlatformUI.getWorkbench();
    if( workbench == null )
      return new IViewPart[] {};

    /* Get the active workbench window. */
    IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    if( window == null )
      return new IViewPart[] {};

    /* Get all pages. */
    IWorkbenchPage[] pages = window.getPages();
    if( pages == null || pages.length == 0 )
      return new IViewPart[] {};

    /* Collect the views. */
    List<IViewPart> views = new ArrayList<IViewPart>();

    /* Loop the pages. */
    for( IWorkbenchPage page : pages )
    {
      /* Get all view references. */
      IViewReference[] references = page.getViewReferences();

      /* Loop the view references. */
      for( IViewReference reference : references )
      {
        /* Does the primary id match? */
        if( reference.getId().equals( primaryId ) )
        {
          /* If the secondary id is available and it does not match, continue. */
          if( secondaryId != null && !secondaryId.equals( reference.getSecondaryId() ) )
            continue;

          /* Here either both ids matches or the primary id matches, but the secondary id is missing. */
          views.add( reference.getView( false ) );
        }
      }
    }

    return views.toArray( new IViewPart[] {} );
  }

  /**
   * This function will hide the view(s) with the given id(s).
   * 
   * @param primaryId
   *          The primary id.
   * @param secondaryId
   *          The secondary id. If omitted, every view with the given primary id will be hidden. May be null.
   */
  public static void hideView( String primaryId, String secondaryId )
  {
    IViewPart[] views = findViewsInActiveWindow( primaryId, secondaryId );
    if( views == null || views.length == 0 )
      return;

    for( IViewPart view : views )
    {
      IWorkbenchPartSite site = view.getSite();
      IWorkbenchPage page = site.getPage();
      page.hideView( view );
    }
  }
}