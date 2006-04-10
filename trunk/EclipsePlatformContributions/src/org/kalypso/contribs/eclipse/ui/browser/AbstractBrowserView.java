package org.kalypso.contribs.eclipse.ui.browser;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.IBrowserViewerContainer;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.contribs.eclipse.ui.MementoWithUrlResolver;

public abstract class AbstractBrowserView extends ViewPart implements IBrowserViewerContainer
{
  private IMemento m_memento;

  // Persistance tags.

  private static final String TAG_URL = "url"; //$NON-NLS-1$

  private static final String TAG_SCROLLBARS = "scrolbars";

  private static final String TAG_HORIZONTAL_BAR = "horizontal";

  private static final String TAG_VERTICAL_BAR = "vertical";

  private static final String TAG_SELECTION = "selection";

  protected BrowserViewer m_viewer;

  protected ILocationChangedHandler m_locationChangedHandler;
  
  protected URL m_context = null;

  private LocationListener m_locationListener = new LocationAdapter()
  {
    @Override
    public void changed( final LocationEvent event )
    {
      // just let the handler do the work
      final String href = event.location;
      if( m_locationChangedHandler != null )
        event.doit = m_locationChangedHandler.handleLocationChange( href );
      else
        event.doit = true;
    }
  };

  @Override
  public void init( final IViewSite site, final IMemento memento ) throws PartInitException
  {
    super.init( site, memento );
    m_memento = memento;
  }

  /** Set the handler which will react to location changed events. */
  public void setLocationChangedHandler( final ILocationChangedHandler handler )
  {
    m_locationChangedHandler = handler;
  }

  private void addBrowserListener( )
  {
    m_viewer.getBrowser().addLocationListener( m_locationListener );
  }

  private void removeLocationListener( )
  {
    if( m_viewer.getBrowser().isDisposed() )
      return;
    m_viewer.getBrowser().removeLocationListener( m_locationListener );
  }

  @Override
  public void createPartControl( final Composite parent )
  {
    m_viewer = new BrowserViewer( parent, SWT.NONE );// BrowserViewer.LOCATION_BAR
    m_viewer.setContainer( this );

    // Delete IE Menu
    final MenuManager menuManager = new MenuManager( "#PopupMenu" ); //$NON-NLS-1$
    menuManager.setRemoveAllWhenShown( true );
    final Menu contextMenu = menuManager.createContextMenu( m_viewer.getBrowser() );
    m_viewer.getBrowser().setMenu( contextMenu );
    getSite().registerContextMenu( menuManager, getSite().getSelectionProvider() );

    addBrowserListener();
    if( m_memento != null )
      restoreState( m_memento );
    m_memento = null;
  }

  protected void restoreState( IMemento memento )
  {
    final String urlAsString = memento.getString( TAG_URL );
    Runnable runnable = null;
    if( memento instanceof MementoWithUrlResolver )
    {
      try
      {
        final MementoWithUrlResolver m = (MementoWithUrlResolver) memento;
        final URL url = m.getURLResolver().resolveURL( urlAsString );
        m_context = url;
        final String externalForm = url.toExternalForm();
        runnable = new Runnable()
        {

          public void run( )
          {
            m_viewer.setURL( externalForm );
            if( m_viewer.combo != null )
              m_viewer.combo.setText( externalForm );
            m_viewer.forward();
          }
        };
      }
      catch( Exception e )
      {
        e.printStackTrace();
      }
    }
    if( runnable == null )
    {
      try
      {
        m_context = new URL( urlAsString );
      }
      catch( MalformedURLException e )
      {
        // nothing
      }
      runnable = new Runnable()
      {

        public void run( )
        {
          m_viewer.setURL( urlAsString );
          if( m_viewer.combo != null )
            m_viewer.combo.setText( urlAsString );
          m_viewer.forward();
        }
      };
    }

    getSite().getShell().getDisplay().asyncExec( runnable );

    IMemento scrollbars = memento.getChild( TAG_SCROLLBARS );
    if( scrollbars == null )
      return;
    IMemento horizontal = scrollbars.getChild( TAG_HORIZONTAL_BAR );
    if( horizontal != null )
    {
      int hSelection = horizontal.getInteger( TAG_SELECTION ).intValue();
      ScrollBar horizontalBar = m_viewer.getHorizontalBar();
      if( horizontalBar != null )
        horizontalBar.setSelection( hSelection );
    }
    IMemento vertical = scrollbars.getChild( TAG_VERTICAL_BAR );
    if( vertical != null )
    {
      int vSelection = vertical.getInteger( TAG_SELECTION ).intValue();
      ScrollBar verticalBar = m_viewer.getVerticalBar();
      if( verticalBar != null )
        verticalBar.setSelection( vSelection );
    }
  }

  protected void restoreStateOld( final IMemento memento )
  {
    final String url = memento.getString( TAG_URL );
    // set the url of the browser
    m_viewer.setURL( url );
    if( m_viewer.combo != null )
      m_viewer.combo.setText( url );

    IMemento scrollbars = memento.getChild( TAG_SCROLLBARS );
    if( scrollbars == null )
      return;
    IMemento horizontal = scrollbars.getChild( TAG_HORIZONTAL_BAR );
    if( horizontal != null )
    {
      int hSelection = horizontal.getInteger( TAG_SELECTION ).intValue();
      ScrollBar horizontalBar = m_viewer.getHorizontalBar();
      if( horizontalBar != null )
        horizontalBar.setSelection( hSelection );
    }
    IMemento vertical = scrollbars.getChild( TAG_VERTICAL_BAR );
    if( vertical != null )
    {
      int vSelection = vertical.getInteger( TAG_SELECTION ).intValue();
      ScrollBar verticalBar = m_viewer.getVerticalBar();
      if( verticalBar != null )
        verticalBar.setSelection( vSelection );
    }
  }

  @Override
  public void dispose( )
  {
    removeLocationListener();
    if( m_viewer != null )
      m_viewer.dispose();
    super.dispose();
  }

  public void setHtml( final String html )
  {
    if( m_viewer != null )
      m_viewer.getBrowser().setText( html );
  }

  public void setURL( final String url )
  {
    if( m_viewer == null )
      return;
    
    // BUGFIX: see bugfix below, this is also needed
    setHtml( "<html><body></body></html>" );

    m_viewer.getDisplay().asyncExec( new Runnable()
    {
      public void run( )
      {
        try
        {
          // BUGFIX: Internet explorer may be not yet initialised
          // when the view was just opened.
          // If we dont wait, he sometimes doesnt show the file but instead
          // asks to download it,
          Thread.sleep( 0, 1 );
        }
        catch( final InterruptedException e )
        {
          e.printStackTrace();
        }
        m_viewer.setURL( url );
      }
    } );
  }

  @Override
  public void setFocus( )
  {
    m_viewer.setFocus();
  }

  public boolean close( )
  {
    try
    {
      getSite().getPage().hideView( this );
      return true;
    }
    catch( Exception e )
    {
      return false;
    }
  }

  public IActionBars getActionBars( )
  {
    return getViewSite().getActionBars();
  }

  public void openInExternalBrowser( final String url )
  {
    try
    {
      final URL theURL = new URL( url );
      IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
      support.getExternalBrowser().openURL( theURL );
    }
    catch( final MalformedURLException e )
    {
      // skip
    }
    catch( final PartInitException e )
    {
      // skip
    }
  }

  /**
   * Return true if the filename has a "web" extension.
   * 
   * @param name
   * @return
   */
  public static boolean isWebFile( final String name )
  {
    final String lowerCase = name.toLowerCase();
    return lowerCase.endsWith( "html" ) || lowerCase.endsWith( "htm" ) || lowerCase.endsWith( "gif" ) || lowerCase.endsWith( "png" ) || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        lowerCase.endsWith( "jpg" ) || lowerCase.endsWith( "pdf" ) || lowerCase.endsWith( "txt" ); //$NON-NLS-1$
  }

  @Override
  public void saveState( final IMemento memento )
  {
    if( m_viewer == null )
    {
      if( m_memento != null ) // Keep the old state;
        memento.putMemento( m_memento );
      return;
    }

    // BUGFIX + HACK: do not store memento because restoring
    // it lead sometimes to a bug (IE asks to save the url instead of
    // displaying it)
    // memento.putString( TAG_URL, m_viewer.getURL() );
    memento.putString( TAG_URL, null );

    IMemento scrollbarMemento = memento.createChild( TAG_SCROLLBARS );
    ScrollBar horizontalBar = m_viewer.getHorizontalBar();
    if( horizontalBar != null )
    {
      IMemento horizontal = scrollbarMemento.createChild( TAG_HORIZONTAL_BAR );
      horizontal.putInteger( TAG_SELECTION, horizontalBar.getSelection() );
    }
    ScrollBar verticalBar = m_viewer.getVerticalBar();
    if( verticalBar != null )
    {
      IMemento vertical = scrollbarMemento.createChild( TAG_VERTICAL_BAR );
      vertical.putInteger( TAG_SELECTION, verticalBar.getSelection() );
    }
  }
}
