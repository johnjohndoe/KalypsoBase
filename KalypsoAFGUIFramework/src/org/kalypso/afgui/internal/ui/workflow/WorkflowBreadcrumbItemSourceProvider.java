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
package org.kalypso.afgui.internal.ui.workflow;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;

/**
 * @author Gernot Belger
 */
public final class WorkflowBreadcrumbItemSourceProvider extends AbstractSourceProvider
{
  public static final String VARIABLE_MENU_SELECTION = "workflowViewerMenuItemSelection"; //$NON-NLS-1$

  private final Map<String, Object> m_sources = new HashMap<String, Object>();

  @Override
  public String[] getProvidedSourceNames( )
  {
    return new String[] { VARIABLE_MENU_SELECTION };
  }

  @Override
  public Map<String, Object> getCurrentState( )
  {
    return m_sources;
  }

  @Override
  public void dispose( )
  {
  }

  public void setSelectedItem( final Object selectedElement )
  {
    m_sources.put( VARIABLE_MENU_SELECTION, selectedElement );

    fireSourceChanged( 0, VARIABLE_MENU_SELECTION, selectedElement );
  }
}