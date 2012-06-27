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
package org.kalypso.ui.addlayer.internal.wms;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.ui.i18n.Messages;

/**
 * Opens the dialog to choose a favorite WMS server.
 *
 * @author Gernot Belger
 */
public class WMSFavoritesAction extends Action
{
  private final ImportWmsData m_data;

  public WMSFavoritesAction( final ImportWmsData data )
  {
    m_data = data;

    setText( Messages.getString("WMSFavoritesAction.0") ); //$NON-NLS-1$
    setToolTipText( Messages.getString( "org.kalypso.ui.wizard.wms.pages.ImportWmsWizardPage.6" ) ); //$NON-NLS-1$
  }

  @Override
  public void runWithEvent( final Event event )
  {
    /* Create the favorites dialog. */
    final Shell shell = event.display.getActiveShell();

    final CapabilitiesInfo currentService = m_data.getCurrentService();
    final CapabilitiesInfo[] history = m_data.getServiceHistory();

    final WMSFavoritesDialog dialog = new WMSFavoritesDialog( shell, currentService, history );

    /* Open the favorites dialog. */
    if( dialog.open() != Window.OK )
      return;

    /* If here, the user has clicked ok. */
    final CapabilitiesInfo[] serviceHistory = dialog.getServiceHistory();
    final CapabilitiesInfo selectedService = dialog.getSelectedService();
    m_data.setServiceHistory( serviceHistory );
    if( selectedService != null )
      m_data.setCurrentService( selectedService );
  }
}