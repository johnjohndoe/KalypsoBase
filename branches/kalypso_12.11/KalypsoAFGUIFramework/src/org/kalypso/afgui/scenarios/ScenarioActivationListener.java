/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.afgui.scenarios;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.afgui.internal.scenario.ScenarioSelectionPopup;

import de.renew.workflow.connector.cases.IScenario;

/**
 * Handles activation of a scenario via the scenario popup.
 * 
 * @author Gernot Belger
 */
class ScenarioActivationListener implements ISelectionChangedListener, IOpenListener
{
  private final ScenarioSelectionPopup m_selectionPopup;

  private final Shell m_shell;

  private final Point m_mousePosition;

  private final IProject m_project;

  private final SzenarioProjectOpenAction m_action;

  public ScenarioActivationListener( final SzenarioProjectOpenAction action, final Shell shell, final Point mousePosition, final IProject project, final ScenarioSelectionPopup selectionPopup )
  {
    m_action = action;
    m_shell = shell;
    m_mousePosition = mousePosition;
    m_project = project;
    m_selectionPopup = selectionPopup;
  }

  @Override
  public void open( final OpenEvent event )
  {
    handleActivation( (IStructuredSelection)event.getSelection() );
  }

  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    handleActivation( (IStructuredSelection)event.getSelection() );
  }

  private void handleActivation( final IStructuredSelection selection )
  {
    final Object firstElement = selection.getFirstElement();
    if( !(firstElement instanceof IScenario) )
      return;

    m_selectionPopup.close();
    m_action.handlePopupScenarioSelected( m_shell, m_mousePosition, m_project, (IScenario)firstElement );
  }
}