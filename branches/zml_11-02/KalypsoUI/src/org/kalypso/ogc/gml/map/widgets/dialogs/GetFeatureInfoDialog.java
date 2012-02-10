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
package org.kalypso.ogc.gml.map.widgets.dialogs;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * The get feature info dialog.
 * 
 * @author Holger Albert
 */
public class GetFeatureInfoDialog extends PopupDialog
{
  /**
   * The request URL.
   */
  private URL m_requestUrl;

  /**
   * The constructor.
   * 
   * @param requestUrl
   *          The request Url.
   */
  public GetFeatureInfoDialog( Shell parentShell, URL requestUrl )
  {
    super( parentShell, SWT.RESIZE, true, true, true, true, false, "Feature Info", null );

    m_requestUrl = requestUrl;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( final Composite parent )
  {
    /* Create the main composite. */
    Composite main = (Composite) super.createDialogArea( parent );
    main.setLayout( new GridLayout( 1, false ) );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    try
    {
      /* If there was no url provided show only a notice. */
      if( m_requestUrl == null )
      {
        setTitleText( "Es wurde keine URL angegeben..." );
        return main;
      }

      /* Load the url in the browser. */
      Browser browser = new Browser( main, SWT.BORDER );
      browser.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
      browser.setUrl( m_requestUrl.toExternalForm() );

      return main;
    }
    catch( Exception ex )
    {
      /* Log the error message. */
      KalypsoGisPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex ) );

      /* Show the error message to the user. */
      setTitleText( String.format( "Fehler: %s", ex.getLocalizedMessage() ) );

      return main;
    }
  }

  /**
   * @see org.eclipse.jface.dialogs.PopupDialog#getDialogSettings()
   */
  @Override
  protected IDialogSettings getDialogSettings( )
  {
    return DialogSettingsUtils.getDialogSettings( KalypsoGisPlugin.getDefault(), getClass().getCanonicalName() );
  }
}