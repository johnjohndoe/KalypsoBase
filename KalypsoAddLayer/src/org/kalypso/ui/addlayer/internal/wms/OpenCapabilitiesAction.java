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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.ogc.gml.wms.utils.KalypsoWMSUtilities;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.addlayer.internal.AddLayerImages;
import org.kalypso.ui.i18n.Messages;

/**
 * Opens the capabilities document in an external browser.
 *
 * @author Gernot Belger
 */
public class OpenCapabilitiesAction extends Action
{
  private final IWebBrowser m_externalBrowser;

  private final ICapabilitiesData m_data;

  public OpenCapabilitiesAction( final ICapabilitiesData data )
  {
    m_data = data;

    setText( Messages.getString("OpenCapabilitiesAction_0") ); //$NON-NLS-1$
    setToolTipText( Messages.getString("OpenCapabilitiesAction_1") ); //$NON-NLS-1$
    setImageDescriptor( AddLayerImages.getImageDescriptor( AddLayerImages.ICON_OPEN_CAPABILITIES ) );

    m_externalBrowser = initBrowserSupport();
  }

  private IWebBrowser initBrowserSupport( )
  {
    try
    {
      final IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
      return support.getExternalBrowser();
    }
    catch( final PartInitException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void runWithEvent( final Event event )
  {
    try
    {
      final String address = m_data.getAddress();
      final URL serviceURL = new URL( address );

      // REMARK: the request url might actually depend on the used image-provider. We still use the default
      // implementation here.
      final URL capabilitiesRequest = KalypsoWMSUtilities.createCapabilitiesRequest( serviceURL );

      m_externalBrowser.openURL( capabilitiesRequest );
    }
    catch( final PartInitException e )
    {
      e.printStackTrace();
      final Shell shell = event.display.getActiveShell();
      StatusDialog.open( shell, e.getStatus(), getText() );
    }
    catch( final MalformedURLException e )
    {
      final Shell shell = event.display.getActiveShell();
      final IStatus status = new Status( IStatus.WARNING, KalypsoAddLayerPlugin.getId(), Messages.getString("OpenCapabilitiesAction_2"), e ); //$NON-NLS-1$
      StatusDialog.open( shell, status, getText() );
    }
  }
}
