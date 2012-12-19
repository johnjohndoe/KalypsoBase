package org.kalypso.mt;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.net.URISyntaxException;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IDisposable;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.eclipse.core.runtime.PluginImageProvider;
import org.kalypso.commons.eclipse.core.runtime.PluginImageProvider.ImageKey;
import org.kalypso.mt.input.MTMouseInput;
import org.kalypso.mt.input.MTWin7TouchInput;
import org.kalypso.ogc.gml.map.IMTSceneChangedListener;
import org.kalypso.ogc.gml.map.MapPanel;
import org.kalypso.ogc.gml.map.listeners.IMapPanelMTPaintListener;
import org.kalypso.ogc.gml.widgets.WidgetManager;
import org.mt4j.MTApplication;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MT4jSettings;

import processing.core.PImage;

public class MTMapPanelApp extends MTApplication implements IMapPanelMTPaintListener, IMTSceneChangedListener, IDisposable
{
  static int counter = 0;

  MapPanel m_mapPanel;

  MTDefaultScene m_scene;

  long parentHandle;

  private MTDefaultScene m_defaultScene;

  private final ICommandTarget m_commandTarget;

  WidgetManager m_mgr;

  MTMouseInput m_mouseInput;

  public MTMapPanelApp( final Frame MTframe, final MapPanel mapPanel, final long handle, final ICommandTarget viewCommandTarget )
  {

    m_mapPanel = mapPanel;
    m_commandTarget = viewCommandTarget;

    parentHandle = handle;

    frame = MTframe;
    frame.setResizable( true );

    m_mapPanel.addPostPaintListener( this );

    final MTApplication l_thisPointer = this;

    frame.addComponentListener( new ComponentAdapter()
    {
      @Override
      public void componentResized( final ComponentEvent e )
      {
        l_thisPointer.invokeLater( new Runnable()
        {
          @Override
          public void run( )
          {
            final Frame farm = (Frame)e.getComponent();
            if( farm.isVisible() )
            {
              final Insets insets = farm.getInsets();
              final Dimension windowSize = farm.getSize();
              final int usableW = windowSize.width - insets.left - insets.right;
              final int usableH = windowSize.height - insets.top - insets.bottom;

              // the ComponentListener in PApplet will handle calling size()
              setBounds( insets.left, insets.top, usableW, usableH );
              MT4jSettings.getInstance().windowHeight = usableH;
              MT4jSettings.getInstance().windowWidth = usableW;
            }

            m_mapPanel.componentResized( e );
            if( m_scene != null )
            {
              m_scene.resetCam();
            }
          }
        } );
      }

      @Override
      public void componentShown( final ComponentEvent e )
      {
        if( e.getSource() == frame )
        {
          m_mapPanel.componentShown( e );
        }
      }

    } );

    m_mgr = (WidgetManager)mapPanel.getWidgetManager();
    addMouseWheelListener( m_mgr );
    addKeyListener( m_mgr );
  }

  @Override
  public void setup( )
  {
    super.setup();
  }

  @Override
  public void startUp( )
  {
    m_mouseInput = new MTMouseInput( this, m_mgr );
    getInputManager().registerInputSource( m_mouseInput );

    m_defaultScene = new MTDefaultScene( this, "MTDefaultScene" ); //$NON-NLS-1$
    sceneChangeRequired( m_defaultScene );

    final MTMapPanelApp l_this = this;
    PlatformUI.getWorkbench().getDisplay().syncExec( new Runnable()
    {

      @Override
      public void run( )
      {
        try
        {
          // Check if we run windows 7
          if( System.getProperty( "os.name" ).toLowerCase().contains( "windows 7" ) ) //$NON-NLS-1$ //$NON-NLS-2$
          {
            final MTWin7TouchInput win7input = MTWin7TouchInput.getSingleton();
            win7input.registerNewCanvas( l_this, parentHandle, m_mouseInput );
            getInputManager().registerInputSource( win7input.getInputSource() );
          }

        }
        catch( final Exception e )
        {
//          System.err.println( e.getMessage() );
        }
      }
    } );

  }

  /**
   * @see org.kalypso.ogc.gml.map.IMapPanelPostPaintListener#paint(java.awt.image.BufferedImage)
   */
  @Override
  public void paint( final BufferedImage img )
  {
    if( img != null && m_scene != null )
      m_scene.setBackgroundImage( img );
  }

  /**
   * @see org.mt4j.IPAppletBoth#delay(int)
   */
  @Override
  public void delay( final int napTime )
  {
  }

  public PImage loadPImage( final ImageKey imageKey )
  {
    final PluginImageProvider imageProvider = KalypsoMTPlugin.getImageProvider();

    String imgPath = ""; //$NON-NLS-1$
    try
    {
      imgPath = imageProvider.getTmpURL( imageKey ).toURI().getRawPath();
    }
    catch( final URISyntaxException e1 )
    {
      // in case the requested image fails, i try to return the "default" image.
      // some geometries require "real" image, so that they pass the init
      try
      {
        imgPath = imageProvider.getTmpURL( KalypsoMTProjectImages.DESCRIPTORS.ARROW_RIGHT ).toURI().getRawPath();
      }
      catch( final URISyntaxException e )
      {
        e.printStackTrace();
        return new PImage();
      }
    }

    return loadImage( imgPath );
  }

  /**
   * @see org.kalypso.ogc.gml.map.IMTSceneChangedListener#changeScene(java.lang.Object)
   */
  @Override
  public void sceneChangeRequired( final Object scene )
  {
    // sets default scene
    if( scene == null )
    {
      this.changeScene( m_defaultScene );
      m_scene = m_defaultScene;
    }
    else if( scene instanceof AbstractScene )
    {
      final AbstractScene l_scene = (AbstractScene)scene;
      this.changeScene( l_scene );
      m_scene = (MTDefaultScene)l_scene;
    }

    m_scene.setBackgroundImage( m_mapPanel.getBufferedImage() );

  }

  @Override
  public void dispose( )
  {
    super.dispose();
    MTWin7TouchInput.getSingleton().disposeWin7Dll();
  }

  public MapPanel getMapPanel( )
  {
    return m_mapPanel;
  }

  public ICommandTarget getCommandTarget( )
  {
    return m_commandTarget;
  }

  public MTMouseInput getMouseInput( )
  {
    return m_mouseInput;
  }
}
