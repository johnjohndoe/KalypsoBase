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
package org.kalypso.ogc.gml.featureview.control;

import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * @author Gernot Belger
 */
public class ImageBrowserOpenAction extends Action implements IUpdateable
{
  private final ImageExternalBrowserFeatureControl m_featureControl;

  private final IWebBrowser m_externalBrowser;

  public ImageBrowserOpenAction( final ImageExternalBrowserFeatureControl featureControl )
  {
    m_featureControl = featureControl;

    m_externalBrowser = initBrowserSupport();

    setText( "Open in external browser" );
    setToolTipText( getText() );

    final ImageDescriptor image = KalypsoGisPlugin.getImageProvider().getImageDescriptor( ImageProvider.DESCRIPTORS.EXTERNAL_BROWSER );
    setImageDescriptor( image );

    update();
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
  public void update( )
  {
    final URL location = m_featureControl.getLocation();
    setEnabled( location != null );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.widget.getDisplay().getActiveShell();

    try
    {
      final URL location = m_featureControl.getLocation();
      m_externalBrowser.openURL( location );
    }
    catch( final PartInitException e )
    {
      e.printStackTrace();
      ErrorDialog.openError( shell, getText(), "Failed to open browser", e.getStatus() );
    }
  }
}
