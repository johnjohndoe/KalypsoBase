package de.openali.odysseus.chart.framework.view.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.view.IChartViewer;

/**
 * Displays a chart in a canvas using no sub components. ONLY FOR TESTING PURPOSES
 * 
 * @author burtscher1
 */
public class ChartCanvas extends Canvas implements PaintListener, IChartViewer
{

  private final IChartModel m_model;

  private final HashMap<POSITION, ArrayList<IAxis>> m_axisPosMap = new LinkedHashMap<POSITION, ArrayList<IAxis>>();

  private final HashMap<IAxis, Rectangle> m_axisBoundsMap = new HashMap<IAxis, Rectangle>();

  private Rectangle m_plotRect;

  public ChartCanvas( final Composite parent, final int style, final IChartModel model )
  {
    super( parent, style );
    addPaintListener( this );

    m_model = model;
  }

  public void paint( final GC gc )
  {
    layout();

    // weissen Hintergrund zeichnen
    gc.setBackground( gc.getDevice().getSystemColor( SWT.COLOR_WHITE ) );
    gc.fillRectangle( gc.getClipping() );

    // Achsen zeichnen
    final IMapperRegistry mr = m_model.getMapperRegistry();
    for( final IAxis a : mr.getAxes() )
    {
      final Rectangle rect = m_axisBoundsMap.get( a );
      if( rect != null )
      {
        final IAxisRenderer ar = a.getRenderer();
        if( ar != null )
          ar.paint( gc, a, rect );
      }
    }

    // Layer zeichnen
    final ILayerManager lm = m_model.getLayerManager();

    if( m_plotRect != null )
    {
      gc.setClipping( m_plotRect );

      final Transform t = new Transform( gc.getDevice() );
      t.translate( m_plotRect.x, m_plotRect.y );

      gc.setTransform( t );
      for( final IChartLayer l : lm.getLayers() )
        if( l.isVisible() )
          l.paint( gc );
      t.dispose();
      gc.setTransform( null );

      gc.setAlpha( 128 );
      gc.setBackground( gc.getDevice().getSystemColor( SWT.COLOR_BLUE ) );
      gc.fillRectangle( m_plotRect );
      System.out.println( "Plot: " + m_plotRect );
      System.out.println( "Clip: " + gc.getClipping() );
    }

  }

  @Override
  public void layout( )
  {
    fillPosMap();
    // für jede Axis wird eine BoundingBox erstellt
    calcBounds();
    // angefangen wird unten, dann weiter im Uhrzeigersinn

  }

  private void calcBounds( )
  {
    final int width = getBounds().width;
    final int height = getBounds().height;

    // map der Breite der AxisSpaces
    final HashMap<POSITION, Integer> spaceWidthMap = new HashMap<POSITION, Integer>();
    for( final POSITION p : m_axisPosMap.keySet() )
    {
      int w = 0;
      for( final IAxis a : m_axisPosMap.get( p ) )
      {
        final IAxisRenderer renderer = a.getRenderer();
        if( renderer != null )
          w += renderer.getAxisWidth( a );
      }
      spaceWidthMap.put( p, w );
    }

    // Position des Plots
    final int left = spaceWidthMap.get( POSITION.LEFT );
    final int top = spaceWidthMap.get( POSITION.TOP );
    final int bottom = spaceWidthMap.get( POSITION.BOTTOM );
    final int right = spaceWidthMap.get( POSITION.RIGHT );

    m_plotRect = new Rectangle( left, top, width - (left + right), height - (top + bottom) );

    int offset = 0;
    for( final IAxis a : m_axisPosMap.get( POSITION.BOTTOM ) )
    {
      final IAxisRenderer renderer = a.getRenderer();
      if( renderer != null )
      {
        final int rHeight = renderer.getAxisWidth( a );
        final int rWidth = width - spaceWidthMap.get( POSITION.LEFT ) - spaceWidthMap.get( POSITION.RIGHT );
        final int rX = spaceWidthMap.get( POSITION.LEFT );
        final int rY = height - spaceWidthMap.get( POSITION.BOTTOM ) + offset;
        offset += rHeight;
        m_axisBoundsMap.put( a, new Rectangle( rX, rY, rWidth, rHeight ) );
        a.setScreenHeight( rWidth );
      }
    }

    offset = 0;
    for( final IAxis a : m_axisPosMap.get( POSITION.LEFT ) )
    {
      final IAxisRenderer renderer = a.getRenderer();
      if( renderer != null )
      {
        final int rWidth = renderer.getAxisWidth( a );
        final int rHeight = height - spaceWidthMap.get( POSITION.TOP ) - spaceWidthMap.get( POSITION.BOTTOM );
        final int rY = spaceWidthMap.get( POSITION.TOP );
        final int rX = offset;
        offset += rWidth;
        m_axisBoundsMap.put( a, new Rectangle( rX, rY, rWidth, rHeight ) );
        a.setScreenHeight( rHeight );
      }
    }

    offset = 0;
    for( final IAxis a : m_axisPosMap.get( POSITION.TOP ) )
    {
      final IAxisRenderer renderer = a.getRenderer();
      if( renderer != null )
      {
        final int rHeight = renderer.getAxisWidth( a );
        final int rWidth = width - spaceWidthMap.get( POSITION.LEFT ) - spaceWidthMap.get( POSITION.RIGHT );
        final int rX = spaceWidthMap.get( POSITION.LEFT );
        final int rY = offset;
        offset += rHeight;
        m_axisBoundsMap.put( a, new Rectangle( rX, rY, rWidth, rHeight ) );
        a.setScreenHeight( rWidth );
      }
    }

    offset = 0;
    for( final IAxis a : m_axisPosMap.get( POSITION.RIGHT ) )
    {
      final IAxisRenderer renderer = a.getRenderer();
      if( renderer != null )
      {
        final int rWidth = renderer.getAxisWidth( a );
        final int rHeight = height - spaceWidthMap.get( POSITION.TOP ) - spaceWidthMap.get( POSITION.BOTTOM );
        final int rY = spaceWidthMap.get( POSITION.TOP );
        final int rX = width - spaceWidthMap.get( POSITION.RIGHT ) + offset;
        offset += rWidth;
        m_axisBoundsMap.put( a, new Rectangle( rX, rY, rWidth, rHeight ) );
        a.setScreenHeight( rHeight );
      }
    }
  }

  private void fillPosMap( )
  {
    initAxisPosMap();

    final ILayerManager lm = m_model.getLayerManager();
    for( final IChartLayer l : lm.getLayers() )
    {
      putAxisInPosMap( l.getCoordinateMapper().getDomainAxis() );
      putAxisInPosMap( l.getCoordinateMapper().getTargetAxis() );
    }
    // // umdrehen, da wir nicht von innen nach aussen, sondern von aussen
    // nach
    // // innen gehen
    // for (ArrayList<IAxis> list : m_axisPosMap.values())
    // Collections.reverse(list);
  }

  private void initAxisPosMap( )
  {
    m_axisPosMap.clear();
    m_axisPosMap.put( POSITION.BOTTOM, new ArrayList<IAxis>() );
    m_axisPosMap.put( POSITION.LEFT, new ArrayList<IAxis>() );
    m_axisPosMap.put( POSITION.TOP, new ArrayList<IAxis>() );
    m_axisPosMap.put( POSITION.RIGHT, new ArrayList<IAxis>() );

  }

  private void putAxisInPosMap( final IAxis axis )
  {
    final ArrayList<IAxis> list = m_axisPosMap.get( axis.getPosition() );
    if( !list.contains( axis ) )
      list.add( axis );
  }

  @Override
  public void paintControl( final PaintEvent e )
  {
    paint( e.gc );

  }

  public IChartModel getModel( )
  {
    return m_model;
  }

  @Override
  public IChartModel getChartModel( )
  {
    return m_model;
  }

}
