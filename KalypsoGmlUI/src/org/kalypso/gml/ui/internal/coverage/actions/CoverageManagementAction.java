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
package org.kalypso.gml.ui.internal.coverage.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Base action for the coverage management widget.
 * 
 * @author Holger Albert
 */
public abstract class CoverageManagementAction extends Action
{
  /**
   * The action will be shown in the coverage management widget.
   */
  public static final String ROLE_WIDGET = "widget";

  /**
   * The action will be shown in the import wizard of the coverage management widget.
   */
  public static final String ROLE_WIZARD = "wizard";

  /**
   * The id of the coverage management action.
   */
  private String m_actionId;

  /**
   * The role of the coverage management action.
   */
  private String m_actionRole;

  /**
   * The constructor.
   * 
   * @param text
   *          The action's text, or null if there is no text.
   * @param image
   *          The action's image, or null if there is no image.
   */
  public CoverageManagementAction( final String text, final ImageDescriptor image )
  {
    super( text, image );

    init( null, ROLE_WIDGET );
  }

  /**
   * This function initializes the coverage management action.
   * 
   * @param actionId
   *          The id of the coverage management action.
   * @param actionRole
   *          The role of the coverage management action.
   */
  public void init( final String actionId, final String actionRole )
  {
    m_actionId = actionId;
    m_actionRole = actionRole;
  }

  /**
   * This function returns the id of the coverage management action.
   * 
   * @return The id of the coverage management action.
   */
  public String getActionId( )
  {
    return m_actionId;
  }

  /**
   * This function returns the role of the coverage management action.
   * 
   * @return The role of the coverage management action.
   */
  public String getActionRole( )
  {
    return m_actionRole;
  }
}