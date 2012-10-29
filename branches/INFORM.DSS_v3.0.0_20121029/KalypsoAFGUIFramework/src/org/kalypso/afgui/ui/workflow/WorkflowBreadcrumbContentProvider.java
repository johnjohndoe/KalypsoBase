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
package org.kalypso.afgui.ui.workflow;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.views.ScenarioContentProvider;

/**
 * @author Gernot
 */
public class WorkflowBreadcrumbContentProvider implements ITreeContentProvider
{
  private final ScenarioContentProvider m_scenarioProvider = new ScenarioContentProvider();

  @Override
  public void dispose( )
  {
    m_scenarioProvider.dispose();
  }

  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    m_scenarioProvider.inputChanged( viewer, oldInput, newInput );
  }

  @Override
  public Object[] getElements( final Object inputElement )
  {
    if( inputElement instanceof IWorkspaceRoot )
    {
    final Object[] scenarioElements = m_scenarioProvider.getElements( inputElement );

    final Object[] elements = new Object[scenarioElements.length + 1];
    elements[0] = Messages.getString( "org.kalypso.afgui.views.WorkflowView.0" ); //$NON-NLS-1$
    System.arraycopy( scenarioElements, 0, elements, 1, scenarioElements.length );

    return elements;
    }

    return m_scenarioProvider.getElements( inputElement );
  }

  @Override
  public Object[] getChildren( final Object parentElement )
  {
    if( parentElement instanceof String )
      return new Object[0];

    return m_scenarioProvider.getChildren( parentElement );
  }

  @Override
  public Object getParent( final Object element )
  {
    if( element instanceof String )
      return ResourcesPlugin.getWorkspace().getRoot();

    return m_scenarioProvider.getParent( element );
  }

  @Override
  public boolean hasChildren( final Object element )
  {
    if( element instanceof String )
      return false;

    return m_scenarioProvider.hasChildren( element );
  }
}
