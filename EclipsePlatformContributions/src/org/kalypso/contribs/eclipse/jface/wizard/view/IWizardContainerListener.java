/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Bj�rnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universit�t Hamburg-Harburg, Institut f�r Wasserbau, Hamburg, Germany
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
package org.kalypso.contribs.eclipse.jface.wizard.view;

import org.eclipse.jface.wizard.IWizard;

/**
 * Listener for wizard change events.
 * 
 * @author Gernot Belger
 */
public interface IWizardContainerListener
{
  public static int REASON_NONE = 0;

  public static int REASON_FINISHED = 1;

  public static int REASON_CANCELED = 2;

  /**
   * Called after the wizard was changed. Is also called when the wizard was finished or canceled.
   * 
   * @param newwizard
   *          The newly set wizard. May be null
   * @param reason
   *          If the wizard was cancelled or finished, the reason is set to one of those flags.
   */
  public void onWizardChanged( final IWizard newwizard, final int reason );
}
