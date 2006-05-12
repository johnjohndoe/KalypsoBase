package org.kalypso.contribs.eclipse.ui.browser.commandable;

import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.kalypso.contribs.eclipse.ui.browser.AbstractBrowserView;

/**
 * BrowserView that can handle and excecute CommandURLs via CommandLocationListener<br>
 * 
 * @author kuepfer
 */
public class CommandURLBrowserView extends AbstractBrowserView
{
  public static final String WEB_BROWSER_VIEW_ID = "org.kalypso.contribs.ui.browser.commandable.CommandURLBrowserView"; //$NON-NLS-1$

  protected CommandLocationListener m_listener = new CommandLocationListener( this );

  public CommandURLBrowserView( )
  {
  }

  /**
   * @see org.kalypso.contribs.eclipse.ui.browser.AbstractBrowserView#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( Composite parent )
  {
    super.createPartControl( parent );
    configuerActionBar();
    updateNavigationActionsState();
    addLocationListener( m_listener );
  }

  protected Browser getBrowser( )
  {
    return m_viewer.getBrowser();
  }

  protected URL getContext( )
  {
    return m_context;
  }

  public void setUrl( final String url )
  {

    super.setURL( url );
  }

  private void configuerActionBar( )
  {
    final IActionBars actionBars = getActionBars();
    final IToolBarManager toolBarManager = actionBars.getToolBarManager();
    actionBars.setGlobalActionHandler( ActionFactory.FORWARD.getId(), forwardAction );
    actionBars.setGlobalActionHandler( ActionFactory.BACK.getId(), backAction );
    toolBarManager.add( backAction );
    toolBarManager.add( forwardAction );
    toolBarManager.update( true );
    actionBars.updateActionBars();
  }

  protected Action backAction = new Action()
  {

    {
      setToolTipText( "Zur�ck" );
      setImageDescriptor( ImageDescriptor.createFromURL( getClass().getResource( "icons/backward_nav.gif" ) ) ); //$NON-NLS-1$
      setDisabledImageDescriptor( ImageDescriptor.createFromURL( getClass().getResource( "icons/backward_nav_gray.gif" ) ) ); //$NON-NLS-1$
    }

    @Override
    public void run( )
    {
      navigateBack();
    }
  };

  protected Action forwardAction = new Action()
  {

    {
      setToolTipText( "Vorw�rts" );
      setImageDescriptor( ImageDescriptor.createFromURL( getClass().getResource( "icons/forward_nav.gif" ) ) ); //$NON-NLS-1$
      setDisabledImageDescriptor( ImageDescriptor.createFromURL( getClass().getResource( "icons/forward_nav_gray.gif" ) ) ); //$NON-NLS-1$
    }

    @Override
    public void run( )
    {
      navigateForward();
    }
  };

  protected void updateNavigationActionsState( )
  {

    // in static html intro, use browser history.
    forwardAction.setEnabled( m_viewer.isForwardEnabled() );
    backAction.setEnabled( m_viewer.isBackEnabled() );
  }

  protected void navigateBack( )
  {
    m_viewer.back();
    updateNavigationActionsState();
  }

  protected void navigateForward( )
  {
    m_viewer.forward();
    updateNavigationActionsState();
  }
}
