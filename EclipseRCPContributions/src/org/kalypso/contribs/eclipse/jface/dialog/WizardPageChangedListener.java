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
package org.kalypso.contribs.eclipse.jface.dialog;

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardDialog;

/**
 * {@link org.eclipse.jface.dialogs.IPageChangedListener} that encapsulates the coomon wizar dpage changing pattern.
 *
 * @author Gernot Belger
 */
public class WizardPageChangedListener implements IPageChangedListener, IPageChangingListener
{
  /**
   * Implements the common pattern for registering/unregfistering this listener.<br/>
   * call this in your {@link IWizard#setContainer(IWizardContainer)} implementation.
   */
  public void setContainer( final IWizardContainer oldContainer, final IWizardContainer container )
  {
    if( oldContainer instanceof IPageChangeProvider )
      ((IPageChangeProvider)oldContainer).removePageChangedListener( this );

    if( oldContainer instanceof WizardDialog )
      ((WizardDialog)oldContainer).removePageChangingListener( this );

    if( container instanceof WizardDialog )
      ((WizardDialog)container).addPageChangingListener( this );

    if( container instanceof IPageChangeProvider )
      ((IPageChangeProvider)container).addPageChangedListener( this );
  }

  /**
   * Empty implementation, overwrite to implement.
   */
  @Override
  public void pageChanged( final PageChangedEvent event )
  {
  }

  /**
   * Empty implementation, overwrite to implement.
   */
  @Override
  public void handlePageChanging( final PageChangingEvent event )
  {
  }
}