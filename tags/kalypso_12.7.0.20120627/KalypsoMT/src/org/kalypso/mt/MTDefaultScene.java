package org.kalypso.mt;

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.kalypso.mt.input.MTMouseInput;
import org.kalypso.mt.input.MouseEvents;
import org.kalypso.ogc.gml.command.ChangeExtentCommand;
import org.kalypso.ogc.gml.map.MapPanel;
import org.kalypso.ogc.gml.widgets.base.PanToWidget;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.mt4j.AbstractMTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTBackgroundImage;
import org.mt4j.input.gestureAction.TapAndHoldVisualizer;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.panProcessor.PanEvent;
import org.mt4j.input.inputProcessors.componentProcessors.panProcessor.PanProcessorTwoFingers;
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.zoomProcessor.ZoomEvent;
import org.mt4j.input.inputProcessors.componentProcessors.zoomProcessor.ZoomProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MT4jSettings;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;

import processing.core.PApplet;
import processing.core.PImage;

public class MTDefaultScene extends AbstractScene
{
  protected MTMapPanelApp mtApp;

  MTBackgroundImage m_backImg;

  MTRectangle m_rect;

  MTComponent backgroundLayer;

  MapPanel m_mapPanel;

  private CursorTracer cursorTracer;

  private TapAndHoldVisualizer m_tapAndHoldVisualizer;

  public MTDefaultScene( final AbstractMTApplication mtApplication, final String name )
  {
    super( mtApplication, name );
    this.mtApp = (MTMapPanelApp) mtApplication;

    m_mapPanel = mtApp.getMapPanel();

    this.getCanvas().unregisterAllInputProcessors();

    // Set the background color
    this.setClearColor( new MTColor( 255, 255, 255, 255 ) );

    m_backImg = new MTBackgroundImage( mtApp, mtApp.loadPImage( KalypsoMTProjectImages.DESCRIPTORS.ARROW_RIGHT ), false );
    m_backImg.unregisterAllInputProcessors();
    this.getCanvas().addChild( m_backImg );

    backgroundLayer = new MTComponent( mtApp );
    backgroundLayer.addChild( m_backImg );
    backgroundLayer.unregisterAllInputProcessors();
    backgroundLayer.setPickable( false );
    this.getCanvas().addChild( backgroundLayer );

    cursorTracer = new CursorTracer( mtApp, this, mtApp.getMouseInput() );
    this.registerGlobalInputProcessor( cursorTracer );

    final int w = MT4jSettings.getInstance().windowWidth;
    final int h = MT4jSettings.getInstance().windowHeight;

    m_rect = new MTRectangle( mtApp, 0, 0, w, h );
    m_rect.setNoFill( true ); // Make it invisible -> only used for dragging
    m_rect.setNoStroke( true );
    m_rect.unregisterAllInputProcessors();
    m_rect.removeAllGestureEventListeners( DragProcessor.class );
    this.getCanvas().addChild( m_rect );

    m_rect.registerInputProcessor( new DragProcessor( mtApp ) );
    m_rect.setGestureAllowance( DragProcessor.class, true );
    m_rect.addGestureListener( DragProcessor.class, new IGestureEventListener()
    {
      @Override
      public boolean processGestureEvent( final MTGestureEvent ge )
      {
        final DragEvent e = (DragEvent) ge;
        final Vector3D v = e.getTo();
        final MTMouseInput m_mouse = mtApp.getMouseInput();
        if( e.getId() == MTGestureEvent.GESTURE_STARTED )
        {
          m_mouse.fakeMousePress( v );
        }
        if( e.getId() == MTGestureEvent.GESTURE_CANCELED )
        {
          m_mouse.fakeMouseCancel();
        }
        if( e.getId() == MTGestureEvent.GESTURE_UPDATED )
        {
          m_mouse.fakeMouseMove( v );
        }

        if( e.getId() == MTGestureEvent.GESTURE_ENDED )
        {
          m_mouse.fakeMouseRelease( v );
        }
        return false;
      }
    } );

    m_rect.registerInputProcessor( new TapAndHoldProcessor( mtApp, 800, false ) );
    m_rect.setGestureAllowance( TapAndHoldProcessor.class, true );
    m_rect.addGestureListener( TapAndHoldProcessor.class, new IGestureEventListener()
    {
      @Override
      public boolean processGestureEvent( final MTGestureEvent ge )
      {
        final TapAndHoldEvent e = (TapAndHoldEvent) ge;
        // final Vector3D v = e.getLocationOnScreen();
        final MTMouseInput m_mouse = mtApp.getMouseInput();

        if( e.getId() == MTGestureEvent.GESTURE_ENDED && e.isHoldComplete() )
        {
          m_mouse.setFakeDrag( true );
        }
        return false;
      }
    } );
    m_tapAndHoldVisualizer = new TapAndHoldVisualizer( mtApp, getCanvas() );
    m_rect.addGestureListener( TapAndHoldProcessor.class, m_tapAndHoldVisualizer );

    m_rect.registerInputProcessor( new PanProcessorTwoFingers( mtApplication ) );
    m_rect.addGestureListener( PanProcessorTwoFingers.class, new MapDrag() );

    m_rect.registerInputProcessor( new ZoomProcessor( mtApplication ) );
    // TODO: suppress warnings is not the solution....
    m_rect.addGestureListener( ZoomProcessor.class, new MapScale() );

    resetCam();
  }

  public void setBackgroundImage( final BufferedImage image )
  {
    if( image == null )
      return;

    mtApp.invokeLater( new Runnable()
    {
      @Override
      public void run( )
      {
        backgroundLayer.removeChild( 0 );
        m_backImg = new MTBackgroundImage( mtApp, new PImage( image ), false );
        m_backImg.unregisterAllInputProcessors();
        m_backImg.setPickable( false );
        backgroundLayer.addChild( m_backImg );
      }
    } );

  }

  // TODO: move into own file
  private class MapDrag implements IGestureEventListener
  {
    PanToWidget widget;
    
    Point originalPoint;

    Point translationVect;

    public MapDrag( )
    {
      widget = new PanToWidget();
      widget.activate( mtApp.getCommandTarget(), m_mapPanel );
    }

    @Override
    public boolean processGestureEvent( final MTGestureEvent g )
    {
      if( g instanceof PanEvent )
      {
        final PanEvent dragEvent = (PanEvent) g;

        final Vector3D tVect = dragEvent.getTranslationVector();

        if( dragEvent.getId() == MTGestureEvent.GESTURE_STARTED )
        {
          originalPoint = new Point( (int) dragEvent.getFirstCursor().getCurrentEvtPosX(), (int) dragEvent.getFirstCursor().getCurrentEvtPosY() );
          
          widget.mousePressed( MouseEvents.toMouseEvent(originalPoint ));
          translationVect = new Point( 0, 0 );
        }
        else if( dragEvent.getId() == MTGestureEvent.GESTURE_UPDATED )
        {
          translationVect.x += tVect.x;
          translationVect.y += tVect.y;
        }
        else
        {
          final Point newPoint = new Point( originalPoint.x + translationVect.x, originalPoint.y + translationVect.y );
          widget.mouseReleased(  MouseEvents.toMouseEvent( newPoint ));
        }

      }
      return true;
    }

    

  }

  private class MapScale implements IGestureEventListener
  {
    private Vector3D lastMiddle;

    double zoomValueAccum;

    @Override
    public boolean processGestureEvent( final MTGestureEvent g )
    {
      if( g instanceof ZoomEvent )
      {
        final ZoomEvent se = (ZoomEvent) g;
        final float scale = se.getCamZoomAmount();
        // System.out.println("X:" + x + " Y:" +y);

        // Add a little panning to scale, so if we can pan while we scale
        final InputCursor c1 = se.getFirstCursor();
        final InputCursor c2 = se.getSecondCursor();
        if( se.getId() == MTGestureEvent.GESTURE_STARTED )
        {
          final Vector3D i1 = c1.getPosition();
          final Vector3D i2 = c2.getPosition();
          lastMiddle = i1.getAdded( i2.getSubtracted( i1 ).scaleLocal( 0.5f ) );
          zoomValueAccum = 0;
        }
        else if( se.getId() == MTGestureEvent.GESTURE_UPDATED )
        {
          final Vector3D i1 = c1.getPosition();
          final Vector3D i2 = c2.getPosition();
          final Vector3D middle = i1.getAdded( i2.getSubtracted( i1 ).scaleLocal( 0.5f ) );
          // final Vector3D middleDiff = middle.getSubtracted( lastMiddle );
          // moveMap((int)middleDiff.x, (int)middleDiff.y);
          lastMiddle = middle;
          zoomValueAccum += scale;
        }
        else
        {

          // Scale the map and the tags
          if( Math.abs( zoomValueAccum ) < 20 )
            return false;

          final GM_Envelope boundingBox = m_mapPanel.getBoundingBox();
          final double ratio = boundingBox.getHeight() / boundingBox.getWidth();

          double dx = (mtApp.width / 2) - zoomValueAccum;
          double dy = (mtApp.height / 2) - zoomValueAccum;

          if( dx * ratio > dy )
            dy = dx * ratio;
          else
            dx = dy / ratio;

          final GeoTransform transform = m_mapPanel.getProjection();
          final double gisMX = transform.getSourceX( lastMiddle.x );
          final double gisMY = transform.getSourceY( lastMiddle.y );

          final double gisX1 = transform.getSourceX( lastMiddle.x - dx );
          final double gisDX = gisMX - gisX1;

          final double gisDY = gisDX * ratio;

          final double gisX2 = gisMX + gisDX;
          final double gisY1 = gisMY - gisDY;
          final double gisY2 = gisMY + gisDY;

          final GM_Envelope zoomBox = GeometryFactory.createGM_Envelope( gisX1, gisY1, gisX2, gisY2, m_mapPanel.getMapModell().getCoordinatesSystem() );

          final ChangeExtentCommand command = new ChangeExtentCommand( m_mapPanel, zoomBox );
          mtApp.getCommandTarget().postCommand( command, null );
          return true;

        }
      }
      return false;
    }
  }

  public void resetCam( )
  {
    final float w = MT4jSettings.getInstance().windowWidth;
    final float h = MT4jSettings.getInstance().windowHeight;

    final Vector3D camPos = new Vector3D( w / 2.0f, h / 2.0f, (h / 2.0f) / PApplet.tan( PApplet.PI * 60.0f / 360.0f ) );
    final Vector3D viewCenterPos = new Vector3D( w / 2.0f, h / 2.0f, 0 );

    getSceneCam().setPosition( camPos );
    getSceneCam().setViewCenterPos( viewCenterPos );

    cursorTracer.getCam().setPosition( camPos );
    cursorTracer.getCam().setViewCenterPos( viewCenterPos );

    m_tapAndHoldVisualizer.getCam().setPosition( camPos );
    m_tapAndHoldVisualizer.getCam().setViewCenterPos( viewCenterPos );

    this.resizeComponents();
  }

  public void resizeComponents( )
  {
    final float w = MT4jSettings.getInstance().windowWidth;
    final float h = MT4jSettings.getInstance().windowHeight;

    m_rect.setSizeLocal( w, h );
  }

}
