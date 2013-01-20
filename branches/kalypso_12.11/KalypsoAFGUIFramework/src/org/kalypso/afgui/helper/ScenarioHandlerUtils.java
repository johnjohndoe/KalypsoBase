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
package org.kalypso.afgui.helper;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.renew.workflow.connector.cases.IScenario;

/**
 * Common code used by scenario handlers.
 * 
 * @author Gernot Belger
 */
public final class ScenarioHandlerUtils
{
  private ScenarioHandlerUtils( )
  {
    throw new UnsupportedOperationException();
  }

  public static IScenario findScenario( final ExecutionEvent event )
  {
    final ISelection selection = HandlerUtil.getCurrentSelection( event );
    return findSelectedScenario( selection );
  }

  /**
   * Get the selected scenario from a selection (first element of selection ).
   * 
   * @return the selected scenario of <code>null</code>
   */
  private static IScenario findSelectedScenario( final ISelection selection )
  {
    if( !(selection instanceof IStructuredSelection) )
      return null;

    final IStructuredSelection structSel = (IStructuredSelection)selection;

    if( structSel.isEmpty() )
      return null;

    final Object o = structSel.getFirstElement();
    if( o instanceof IScenario )
      return (IScenario)o;

    return null;
  }
}
