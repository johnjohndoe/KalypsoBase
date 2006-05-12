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
package org.kalypso.contribs.eclipse.ui.browser.commandable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.BundleUtility;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.ui.MementoUtils;
import org.kalypso.contribs.eclipse.ui.MementoWithUrlResolver;
import org.kalypso.contribs.eclipse.ui.browser.AbstractBrowserView;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.osgi.framework.Bundle;

/**
 * Implementation of ICommandURL with some build-in predefined commands. The <code>runaction</code> can handle
 * diffrent actions. The <code>perspective</code> action switches to a new perspective using a special Memento through
 * invoking the restoreState method in the Workbench class.
 * 
 * @see org.eclipse.ui.IActionDelegate
 * @see org.eclipse.jface.action.IAction
 * @see org.kalypso.contribs.eclipse.ui.browser.commandable.ICommandURL
 * @see org.eclipse.ui.WorkbenchPage#restoreState(org.eclipse.ui.IMemento, org.eclipse.ui.IPerspectiveDescriptor)
 * @author kuepfer
 */
public class CommandURL implements ICommandURL
{
  /**
   * CommandURL protocol constants.
   */
  public static final String BROWSER_PROTOCOL = "http"; //$NON-NLS-1$

  public static final String BROWSER_HOST_ID = "org.kalypso.command.url"; //$NON-NLS-1$

  /**
   * Constants that represent CommandURL actions.
   */
  private final static String NEXT_PERSPECTIVE = "perspective";

  private final static String RUN_ACTION = "runaction";

  private final static String OPEN_VIEW = "openview";

  private static final String CLOSE_VIEW = "closeview";

  private static final String SHOW_MESSAGE = "showmessage";

  private static final String NAVIGATE = "navigate"; //$NON-NLS-1$

  /**
   * Constants that represent valid runAction and openView keys.
   */
  protected static final String KEY_PLUGIN_ID = "pluginId"; //$NON-NLS-1$

  protected static final String KEY_CLASS = "class"; //$NON-NLS-1$

  protected static final String KEY_DECODE = "decode"; //$NON-NLS-1$

  public static final String KEY_URL = "url"; //$NON-NLS-1$

  protected static final String KEY_PART_ID = "partId";

  protected static final String KEY_ACTIVE_PART_ID = "activePartId";//$NON-NLS-1$

  protected static final String KEY_MESSAGE = "message";

  protected static final String KEY_DIRECTION = "direction"; //$NON-NLS-1$

  /**
   * Constants that represent valid values for predefined action
   */

  protected static final String VALUE_TRUE = "true"; //$NON-NLS-1$

  protected static final String VALUE_FALSE = "false"; //$NON-NLS-1$

  public static final String VALUE_BACKWARD = "backward"; //$NON-NLS-1$

  public static final String VALUE_FORWARD = "forward"; //$NON-NLS-1$

  public static final String VALUE_HOME = "home"; //$NON-NLS-1$

  private String m_action = null;

  private Properties m_parameters = null;

  private URL m_context;

  private Listener m_listener;

  public CommandURL( final String action, final Properties parameters )
  {
    m_action = action;
    m_parameters = parameters;
  }

  /**
   * @see org.kalypso.dss.protocol.IBrowserURL#execute()
   */
  public boolean execute( final URL context, final Listener listener )
  {
    m_context = context;
    m_listener = listener;
    final boolean[] result = new boolean[1];
    Display display = Display.getCurrent();

    BusyIndicator.showWhile( display, new Runnable()
    {

      public void run( )
      {
        result[0] = doExecute();
      }

    } );
    return result[0];
  }

  protected boolean doExecute( )
  {
    if( m_action.equalsIgnoreCase( RUN_ACTION ) )
    {
      return runAction( getParameter( KEY_PLUGIN_ID ), getParameter( KEY_CLASS ), m_parameters );
    }
    else if( m_action.equalsIgnoreCase( NEXT_PERSPECTIVE ) )
    {
      return nextPerspective( getParameter( KEY_URL ) );
    }
    else if( m_action.equalsIgnoreCase( OPEN_VIEW ) )
    {
      return openView( getParameter( KEY_PART_ID ), getParameter( KEY_ACTIVE_PART_ID ) );
    }
    else if( m_action.equalsIgnoreCase( CLOSE_VIEW ) )
    {
      return closeView( getParameter( KEY_PART_ID ) );
    }
    else if( m_action.equalsIgnoreCase( SHOW_MESSAGE ) )
    {
      return showMessage( getParameter( KEY_MESSAGE ) );
    }
    else if( m_action.equalsIgnoreCase( NAVIGATE ) )
      return navigate( getParameter( KEY_DIRECTION ) );

    return false;
  }

  private boolean showMessage( final String message )
  {
    if( message == null || message.length() == 0 )
      return false;
    final String title = "Flows Planer Portal";
    final Display display = Display.getCurrent();
    final Shell shell = display.getActiveShell();
    MessageDialog.openInformation( shell, title, message );
    if( m_listener != null )
    {
      Event event = new Event();
      event.type = SWT.OK;
      event.data = getParameter( KEY_URL );
      m_listener.handleEvent( event );
    }
    return true;
  }

  private boolean closeView( final String viewId )
  {
    final IWorkbenchPage activePage = getActivePage();
    final IViewPart viewPart = activePage.findView( viewId );
    if( viewPart != null )
    {
      activePage.hideView( viewPart );
      return true;
    }
    return false;

  }

  private boolean openView( final String viewId, final String activeViewId )
  {
    final IWorkbenchPage activePage = getActivePage();

    try
    {
      activePage.showView( activeViewId );
      activePage.showView( viewId );
    }
    catch( PartInitException e )
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * @see ICommandURL#getAction()
   */
  public String getAction( )
  {
    return m_action;
  }

  /**
   * Return a parameter defined in the CommandURL. Returns null if the parameter is not defined. If this intro url has a
   * decode=true parameter, then all parameters are returned decoded using UTF-8.
   * 
   * @param parameterId
   * @return
   * @see ICommandURL#getParameter(String)
   */
  public String getParameter( String parameterId )
  {
    // make sure to decode only on return, since we may need to recreate the
    // url when handling custom urls.
    String value = m_parameters.getProperty( parameterId );
    String decode = m_parameters.getProperty( KEY_DECODE );

    if( value != null )
      try
      {
        if( decode != null && decode.equalsIgnoreCase( VALUE_TRUE ) )
          // we are told to decode the parameters of the url through
          // the decode parameter. Assume that parameters are
          // UTF-8 encoded.
          return URLDecoder.decode( value, "UTF-8" ); //$NON-NLS-1$
        return value;
      }
      catch( Exception e )
      {
        // should never be here.
        // Log.error("Failed to decode URL: " + parameterId, e); //$NON-NLS-1$
      }
    return value;

  }

  boolean runAction( final String pluginId, final String className, final Properties parameter )
  {

    if( pluginId == null || className == null )
      return false;
    Bundle bundle = Platform.getBundle( pluginId );
    if( !BundleUtility.isReady( bundle ) )
      return false;

    Class aClass;
    Object aObject;
    try
    {
      aClass = bundle.loadClass( className );
      aObject = aClass.newInstance();
    }
    catch( Exception e )
    {
      return false;
    }

    Object actionObject = aObject;
    try
    {

      if( actionObject instanceof ICommandURLAction )
      {
        ICommandURLAction action = (ICommandURLAction) actionObject;
        if( m_listener != null )
          action.addListener( m_listener );
        action.run( parameter );
        if( m_listener != null )
          action.removeListener( m_listener );
      }
      else if( actionObject instanceof IAction )
      {
        IAction action = (IAction) actionObject;
        action.run();

      }
      else if( actionObject instanceof IActionDelegate )
      {
        final IActionDelegate delegate = (IActionDelegate) actionObject;
        if( delegate instanceof IWorkbenchWindowActionDelegate )
          ((IWorkbenchWindowActionDelegate) delegate).init( PlatformUI.getWorkbench().getActiveWorkbenchWindow() );
        Action proxy = new Action()
        {

          @Override
          public void run( )
          {
            delegate.run( this );
          }
        };
        proxy.run();
      }
      else
        // we could not create the class.
        return false;

    }
    catch( Exception e )
    {
      return false;
    }
    return true;
  }

  private boolean nextPerspective( final String urlWithPageState )
  {

    final IPerspectiveRegistry registry = getWorkbench().getPerspectiveRegistry();
    // Es wird angenommen das der context nie null ist
    final IProject project = ResourceUtilities.findProjectFromURL( m_context );
    final Properties props = new Properties();
    if( project != null )
    {
      props.setProperty( MementoWithUrlResolver.PATH_KEY, getWorkspaceRoot().getLocation().toString() );
      props.setProperty( MementoWithUrlResolver.PROJECT_KEY, project.getName() );
    }
    else
      return false;

    final URL url;
    IMemento memento = null;
    try
    {
      final URL context = ResourceUtilities.createURL( project );
      url = UrlResolverSingleton.resolveUrl( context, urlWithPageState );
      final InputStreamReader reader = new InputStreamReader( url.openStream() );
      final XMLMemento originalMemento = XMLMemento.createReadRoot( reader );
      memento = MementoUtils.createMementoWithUrlResolver( originalMemento, props, new IUrlResolver2()
      {

        public URL resolveURL( String relative ) throws MalformedURLException
        {
          return UrlResolverSingleton.resolveUrl( context, relative );
        }

      } );
    }
    catch( MalformedURLException e )
    {
      e.printStackTrace();
      handleException( e, getActivePage() );
      return false;
    }
    catch( WorkbenchException e )
    {
      e.printStackTrace();
      handleException( e, getActivePage() );
      return false;
    }
    catch( IOException e )
    {
      e.printStackTrace();
      handleException( e, getActivePage() );
      return false;
    }

    MementoUtils.restoreWorkbenchPage( memento );

    return true;
  }

  /**
   * sends a naviagation event to the listener
   */
  private boolean navigate( String direction )
  {

    Event event = new Event();
    if( m_listener != null )
    {
      if( direction.equalsIgnoreCase( VALUE_BACKWARD ) )
      {
        event.type = AbstractBrowserView.BACKWARD;
        m_listener.handleEvent( event );
        return true;
      }
      else if( direction.equalsIgnoreCase( VALUE_FORWARD ) )
      {
        event.type = AbstractBrowserView.FORWARD;
        m_listener.handleEvent( event );
        return true;
      }
    }
    return false;
  }

  /**
   * Handles workbench exception
   */
  private static void handleException( Exception e, IWorkbenchPage page )
  {
    ErrorDialog.openError( page.getActivePart().getSite().getShell(), "Link Error", e.getMessage(), StatusUtil.newStatus( IStatus.WARNING, e.getMessage(), e ) );
  }

  private IWorkbench getWorkbench( )
  {
    return PlatformUI.getWorkbench();
  }

  private IWorkbenchPage getActivePage( )
  {
    return getWorkbench().getActiveWorkbenchWindow().getActivePage();
  }

  private IWorkspaceRoot getWorkspaceRoot( )
  {
    return ResourcesPlugin.getWorkspace().getRoot();
  }

}
