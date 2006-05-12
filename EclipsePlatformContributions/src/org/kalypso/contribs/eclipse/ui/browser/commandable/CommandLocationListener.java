/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.contribs.eclipse.ui.browser.commandable;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kalypso.contribs.eclipse.ui.browser.AbstractBrowserView;
import org.kalypso.contribs.java.net.UrlResolverSingleton;

/**
 * LocationListener that can excecute CommandURLs and listens to Events from the underlying CommandURL. The event can be
 * used to recieve feedback from any Control generated by the special action. Only listens to ICommandURLActions
 * 
 * @see org.kalypso.contribs.eclipse.ui.browser.commandable.ICommandURLAction
 * @author kuepfer
 */
public class CommandLocationListener implements LocationListener, Listener
{

  private CommandURLBrowserView m_browserView;

  private URL m_context;

  public CommandLocationListener( final CommandURLBrowserView browserView )
  {
    m_browserView = browserView;
  }

  /**
   * @see org.eclipse.swt.browser.LocationListener#changing(org.eclipse.swt.browser.LocationEvent)
   */
  public void changing( LocationEvent event )
  {

    final String url = event.location;
    if( url == null )
      return;

    final CommandURLParser parser = new CommandURLParser( url );
    if( parser.hasCommandUrl() )
    {
      // stop URL first.
      event.doit = false;
      // execute the action embedded in the IntroURL
      final CommandURL browserURL = parser.getCommandURL();
      m_context = m_browserView.getContext();
      browserURL.execute( m_context, this );
    }
    m_browserView.updateNavigationActionsState();
  }

  /**
   * @see org.eclipse.swt.browser.LocationListener#changed(org.eclipse.swt.browser.LocationEvent)
   */
  public void changed( LocationEvent event )
  {
    final String url = event.location;
    if( url == null )
      return;

    // guard against unnecessary History updates.
    final Browser browser = (Browser) event.getSource();
    if( browser.getData( "navigation" ) != null //$NON-NLS-1$
        && browser.getData( "navigation" ).equals( "true" ) ) //$NON-NLS-1$ //$NON-NLS-2$
      return;

    final CommandURLParser parser = new CommandURLParser( url );
    if( !parser.hasProtocol() || parser.getHost() == null || parser.getHost().equals( "" ) ) //$NON-NLS-1$
      // This will filter out two navigation events fired by the browser
      // on a setText. (about:blank and
      // res://C:\WINDOWS\System32\shdoclc.dll/navcancl.htm on windows,
      // and file:/// on Linux)
      return;

    if( event.top == true )
    {
      // we are navigating to a regular fully qualified URL. Event.top
      // is true.
      flagStartOfFrameNavigation();
    }

    if( browser.getData( "frameNavigation" ) == null //$NON-NLS-1$
        && event.top == false )
    {
      // a new url navigation that is not in a top frame. It can
      // be a navigation url due to frames, it can be due to a true
      // single Frame navigation (when you click on a link inside a
      // Frame) or it is an embedded Help System topic navigation.
      flagStartOfFrameNavigation();
      flagStoredTempUrl();
    }
    m_browserView.updateNavigationActionsState();
  }

  public void flagStartOfFrameNavigation( )
  {
    if( m_browserView.getBrowser().getData( "frameNavigation" ) == null ) //$NON-NLS-1$
      m_browserView.getBrowser().setData( "frameNavigation", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void flagEndOfFrameNavigation( )
  {
    m_browserView.getBrowser().setData( "frameNavigation", null ); //$NON-NLS-1$
  }

  public void flagStartOfNavigation( )
  {
    if( m_browserView.getBrowser().getData( "navigation" ) == null ) //$NON-NLS-1$
      m_browserView.getBrowser().setData( "navigation", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void flagEndOfNavigation( )
  {
    m_browserView.getBrowser().setData( "navigation", null ); //$NON-NLS-1$
  }

  public void flagStoredTempUrl( )
  {
    if( m_browserView.getBrowser().getData( "tempUrl" ) == null ) //$NON-NLS-1$
      m_browserView.getBrowser().setData( "tempUrl", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent( Event event )
  {
    if( event.type == SWT.OK )
    {
      try
      {
        // when ever there is a url in the parameter set it is interpeted as
        // the next page to display in the browser
        String nextPageURL = null;
        if( event.data instanceof String )
          nextPageURL = (String) event.data;
        if( nextPageURL != null )
        {
          final URL nextPage = UrlResolverSingleton.resolveUrl( m_context, nextPageURL );
          m_browserView.setURL( nextPage.toString() );
        }
      }
      catch( MalformedURLException e )
      {
        e.printStackTrace();
        MessageDialog.openError( null, "Konfigurationsfehler", "Die n�chste Seite URL ist ung�ltig:" + e.getMessage() );
      }
    }
    else if( event.type == AbstractBrowserView.BACKWARD )
    {
      m_browserView.navigateBack();
    }
    else if( event.type == AbstractBrowserView.FORWARD )
    {
      m_browserView.navigateForward();

    }
    m_browserView.updateNavigationActionsState();

  }

}
