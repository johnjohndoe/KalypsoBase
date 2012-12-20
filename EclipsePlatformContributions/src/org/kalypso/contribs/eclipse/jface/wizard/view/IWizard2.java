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
package org.kalypso.contribs.eclipse.jface.wizard.view;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

/**
 * Adds some additional methods to the {@link IWizard}
 * 
 * @author belger
 */
public interface IWizard2 extends IWizard
{
  /** Should be called if this wizard is brought to top (for example, if the view containing this wizard is shown) */
  void activate( );

  /**
   * @see #activate()
   */
  void deactivate( );

  /**
   * Finishes a single page
   * 
   * @return false, if something went wrong. Don't change the page now.
   */
  boolean finishPage( final IWizardPage page );

  boolean hasCancelButton( );

  /** The initial browser size in percent of the whole area */
  int getInitialBrowserSize( );

  /** The context-id which to show if help is invoked */
  String getHelpId( );

  /**
   * Saves the contents/state of all pages.
   * 
   * @throws CoreException
   */
  IStatus saveAllPages( IProgressMonitor monitor ) throws CoreException;

  /**
   * @return Return <code>true</code>, if a 'save' button should be visible for this wizard.
   */
  boolean hasSaveButton( );

  /**
   * @return If the user should be aksed before any save.
   */
  boolean doAskForSave( );

  /**
   * Return <code>false</code>, if the wizard should not set the shell-default button (usually the next or finish
   * buttons in the normal wizard container implementations).
   */
  boolean useDefaultButton( );

}
