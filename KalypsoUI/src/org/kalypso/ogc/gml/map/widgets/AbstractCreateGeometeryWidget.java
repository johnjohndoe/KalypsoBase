/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 *
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 *
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 *
 * and
 *
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact:
 *
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 *
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.ogc.gml.map.widgets;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.widgets.DeprecatedMouseWidget;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A widget which edits a arbitrary geometry. Overwrite it to define the behaviour what really happens (e.g. create new
 * feature or edit existing and so on).
 *
 * @author Holger Albert
 */
public abstract class AbstractCreateGeometeryWidget extends DeprecatedMouseWidget
{
// private static double MIN_DRAG_DISTANCE_PIXEL = 20;
  private static double MIN_DRAG_DISTANCE_PIXEL = 0.02;

  // points in pixel coordinates
  private final List<GM_Point> m_points = new ArrayList<>();

  // this is the point currently under the mouse
  private GM_Point m_currentPoint = null;

  private GM_Object m_validGeometryValue;

  /**
   * If you call this method with super, make sure to call <code>update( getMapPanel() );
   </code> afterwards.
   */
  public AbstractCreateGeometeryWidget( final String name, final String toolTip )
  {
    super( name, toolTip );
  }

  private void clear( )
  {
    m_points.clear();
    m_currentPoint = null;
  }

  /**
   * @throws GM_Exception
   * @throws NotEnoughPointsExeption
   */
  private GM_Object createGeometry( final List<GM_Point> pixelArray ) throws GM_Exception, NotEnoughPointsExeption
  {
    final Class< ? extends GM_Object> geoClass = getGeometryClass();
    if( geoClass == GeometryUtilities.getPolygonClass() && pixelArray.size() < 3 )
      throw new NotEnoughPointsExeption();
    if( geoClass == GeometryUtilities.getLineStringClass() && pixelArray.size() < 2 )
      throw new NotEnoughPointsExeption();

    final List<GM_Position> posArray = getAsGM_Positions( pixelArray );// getPositionArray( pixelArray );
    GM_Object result = null;
    if( geoClass == GeometryUtilities.getPolygonClass() )
      result = getPolygon( posArray );
    else if( geoClass == GeometryUtilities.getLineStringClass() )
      result = getLineString( posArray );
    else if( geoClass == GeometryUtilities.getPointClass() )
      result = getPoint( posArray );
    // TODO support the multis ...
    // test it
    return result;
  }

  private List<GM_Position> getAsGM_Positions( final List<GM_Point> pixelArray )
  {
    final List<GM_Position> lListResult = new ArrayList<>();
    for( final GM_Point lGMPoint : pixelArray )
    {
      lListResult.add( lGMPoint.getPosition() );
    }
    return lListResult;
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#leftClicked(java.awt.Point)
   */
  @Override
  public void leftClicked( final Point p )
  {
    if( !canEdit() )
      return;

    final GM_Point lGMPoint = transform( getMapPanel(), p );
    if( !m_points.isEmpty() && m_points.get( m_points.size() - 1 ).equals( lGMPoint ) )
      return;
    // first test if vaild...
    final List<GM_Point> testList = new ArrayList<>();
    for( final GM_Point point : m_points )
      testList.add( point );
    testList.add( lGMPoint );

    try
    {
      final GM_Object gm_geometry = createGeometry( testList );
      final Geometry geometry = JTSAdapter.export( gm_geometry );
      if( geometry.isValid() && geometry.isSimple() )
      {
        m_validGeometryValue = gm_geometry;
        m_points.add( lGMPoint );
        if( getGeometryClass() == GeometryUtilities.getPointClass() )
          perform();
      }
      else
        m_validGeometryValue = null;
    }
    catch( final NotEnoughPointsExeption e )
    {
      m_points.add( lGMPoint );
      m_validGeometryValue = null;
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
      m_validGeometryValue = null;
    }
    m_currentPoint = lGMPoint;
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#dragged(java.awt.Point)
   */
  @Override
  public void dragged( final Point p )
  {
    final GM_Point lGMPoint = transform( getMapPanel(), p );
    if( m_points.isEmpty() || m_points.get( m_points.size() - 1 ).distance( lGMPoint ) > MIN_DRAG_DISTANCE_PIXEL )
      leftClicked( p );

    // TODO: check if this repaint is really necessary
    final IMapPanel panel = getMapPanel();
    if( panel != null )
      panel.repaintMap();

  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#dragged(java.awt.Point)
   */
  @Override
  public void moved( final Point p )
  {
    if( !m_points.isEmpty() )
    {
      final GM_Point lGMPoint = transform( getMapPanel(), p );
      m_currentPoint = lGMPoint;

// TODO: check if this repaint is necessary for the widget
      final IMapPanel panel = getMapPanel();
      if( panel != null )
        panel.repaintMap();
    }
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    if( !canEdit() )
      return;
    if( !m_points.isEmpty() && m_currentPoint != null )
    {
      final int[][] lArrPositionToDrow = getXYArrayPixel();
      final int[] arrayX = lArrPositionToDrow[0];
      final int[] arrayY = lArrPositionToDrow[1];
      final Class< ? extends GM_Object> geoClass = getGeometryClass();
      if( geoClass == GeometryUtilities.getPolygonClass() )
      {
        // paint polygon
        g.drawPolygon( arrayX, arrayY, arrayX.length );
        drawHandles( g, arrayX, arrayY );
      }
      else if( geoClass == GeometryUtilities.getLineStringClass() )
      {
        // paint linestring
        g.drawPolyline( arrayX, arrayY, arrayX.length );
        drawHandles( g, arrayX, arrayY );
      }
      else if( geoClass == GeometryUtilities.getPointClass() )
      {
        drawHandles( g, arrayX, arrayY );
      }
    }
  }

  private void drawHandles( final Graphics g, final int[] x, final int[] y )
  {
    final int sizeOuter = 6;
    for( int i = 0; i < y.length; i++ )
      g.drawRect( x[i] - sizeOuter / 2, y[i] - sizeOuter / 2, sizeOuter, sizeOuter );
  }

  /**
   * @return all the x and y points as int[][] array including the current point
   */
  private int[][] getXYArrayPixel( )
  {
    final List<Integer> xArray = new ArrayList<>();
    final List<Integer> yArray = new ArrayList<>();
    for( int i = 0; i < m_points.size(); i++ )
    {
      xArray.add( new Integer( (int) getMapPanel().getProjection().getDestX( m_points.get( i ).getX() ) ) );
      yArray.add( new Integer( (int) getMapPanel().getProjection().getDestY( m_points.get( i ).getY() ) ) );
    }
    if( m_currentPoint != null )
    {
      xArray.add( new Integer( (int) getMapPanel().getProjection().getDestX( m_currentPoint.getX() ) ) );
      yArray.add( new Integer( (int) getMapPanel().getProjection().getDestY( m_currentPoint.getY() ) ) );
    }
    final int[] xs = ArrayUtils.toPrimitive( xArray.toArray( new Integer[m_points.size()] ) );
    final int[] ys = ArrayUtils.toPrimitive( yArray.toArray( new Integer[m_points.size()] ) );
    return new int[][] { xs, ys };
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.IWidget#perform()
   */
  public void perform( )
  {
    if( !canEdit() )
      return;

    final GM_Object validGeometryValue = getValidGeometryValue();
    if( validGeometryValue == null ) // nothing to perform
      return;

    try
    {
      performIntern( validGeometryValue );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    clear();
  }

  /**
   * This method transforms the AWT-Point to a GM_Point.
   *
   * @param mapPanel
   *          The MapPanel of the map.
   * @param p
   *          The AWT-Point.
   */
  protected static GM_Point transform( final IMapPanel mapPanel, final Point p )
  {
    if( p == null )
      return null;

    final GeoTransform projection = mapPanel.getProjection();
    final IMapModell mapModell = mapPanel.getMapModell();
    if( mapModell == null || projection == null )
      return null;

    String coordinatesSystem = mapModell.getCoordinatesSystem();
    if( coordinatesSystem == null )
    {
      coordinatesSystem = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    }

    final double x = p.getX();
    final double y = p.getY();

    return GeometryFactory.createGM_Point( projection.getSourceX( x ), projection.getSourceY( y ), coordinatesSystem );
  }

  protected abstract void performIntern( final GM_Object validGeometryValue ) throws Exception;

  private GM_Surface< ? > getPolygon( final List<GM_Position> posArray ) throws GM_Exception
  {
    // close the ring
    posArray.add( posArray.get( 0 ) );
    final GM_Position[] positions = posArray.toArray( new GM_Position[posArray.size()] );
    return GeometryFactory.createGM_Surface( positions, new GM_Position[0][0], getCoordinatesSystem() );
  }

  private GM_Curve getLineString( final List<GM_Position> posArray ) throws GM_Exception
  {
    return GeometryFactory.createGM_Curve( posArray.toArray( new GM_Position[posArray.size()] ), getCoordinatesSystem() );
  }

  /**
   * @return a point
   */
  private GM_Point getPoint( final List<GM_Position> posArray )
  {
    final GM_Position pos = posArray.get( 0 );
    return GeometryFactory.createGM_Point( pos.getX(), pos.getY(), getCoordinatesSystem() );
  }

  @Override
  public void doubleClickedLeft( final Point p )
  {
    if( !canEdit() )
      return;

    if( !m_points.isEmpty() )
    {
      final Class< ? extends GM_Object> geoClass = getGeometryClass();
      if( geoClass == GeometryUtilities.getPolygonClass() && m_points.size() >= 3 )
        perform();
      if( geoClass == GeometryUtilities.getLineStringClass() && m_points.size() >= 2 )
        perform();
    }

  }

  @Override
  public void doubleClickedRight( final Point p )
  {
    // nothing
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget,
   *      org.kalypso.ogc.gml.map.MapPanel)
   */
  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );
    update( mapPanel );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#keyReleased(java.awt.event.KeyEvent)
   */
  @Override
  public void keyReleased( final KeyEvent e )
  {
    if( e.getKeyCode() == KeyEvent.VK_ESCAPE )
    {
      clear();
    }
  }

  private GM_Object getValidGeometryValue( )
  {
    return m_validGeometryValue;
  }

  public static class NotEnoughPointsExeption extends Exception
  {
  }

  protected abstract boolean canEdit( );

  protected abstract void update( final IMapPanel mapPanel );

  protected abstract String getCoordinatesSystem( );

  protected abstract GeoTransform getProjection( );

  protected abstract Class< ? extends GM_Object> getGeometryClass( );
}
