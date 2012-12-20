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
package org.kalypso.contribs.eclipse.ide.application;

import java.util.Collections;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.application.DelayedEventsProcessor;
import org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

/**
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
public class KalypsoWorkbenchAdvisor extends IDEWorkbenchAdvisor
{
  public KalypsoWorkbenchAdvisor( final DelayedEventsProcessor processor )
  {
    super( processor );
  }

  @Override
  public void initialize( final IWorkbenchConfigurer configurer )
  {
    super.initialize( configurer );

    // Disable all action sets initially; we never want Kalypso to be cluttered with
    // all this stuff
    final ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
    final IActionSetDescriptor[] sets = reg.getActionSets();
    for( final IActionSetDescriptor actionSet : sets )
      actionSet.setInitiallyVisible( false );

    // Disable all defined activities
    final IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
    workbenchActivitySupport.setEnabledActivityIds( Collections.EMPTY_SET );
  }
}
