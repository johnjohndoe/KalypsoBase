package org.kalypso.model.wspm.ui.action.selection;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;

import org.apache.commons.lang3.Range;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.jts.JTSConverter;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.model.wspm.ui.action.base.ProfilePainter;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.SLDPainter;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Exception;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

/**
 * @author Dirk Kuch
 */
public class ProfileSelectionWidget extends AbstractProfileSelectionWidget
{
  private final SelectedProfilesMapPanelListener m_mapPanelListener = new SelectedProfilesMapPanelListener( this );

  public ProfileSelectionWidget( )
  {
    super( "", "" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    mapPanel.addSelectionChangedListener( m_mapPanelListener );
    onSelectionChange( m_mapPanelListener.doSelection( mapPanel.getSelection() ) );

    reset();

    /* init the cursor. */
    final Cursor cursor = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
    mapPanel.setCursor( cursor );
  }

  @Override
  public void finish( )
  {
    getMapPanel().removeSelectionChangedListener( m_mapPanelListener );
    onSelectionChange( new IProfileFeature[] {} ); // purge profile change listener

    reset();
    repaintMap();

    super.finish();
  }

  private void updateSelection( )
  {
    if( Objects.isNull( getProfile(), m_snapPoint ) )
      return;

    try
    {
      final IProfil profile = getProfile().getProfil();
      final IRangeSelection selection = profile.getSelection();

      final double pn = Profiles.getWidth( profile, m_snapPoint );

      if( isShiftKeyPressed() )
      {
        final double p0 = Profiles.getWidth( profile, m_p0 );
        selection.setRange( Range.between( p0, pn ) );
      }
      else
      {
        selection.setRange( Range.is( pn ) );
      }

    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
    }
    catch( final IllegalStateException e )
    {
      // if point is not on line
    }
  }

  @Override
  public void leftPressed( final Point p )
  {
    if( !isShiftKeyPressed() )
      m_p0 = m_snapPoint;

    updateSelection();
  }

  private boolean m_shift = false;

  private com.vividsolutions.jts.geom.Point m_snapPoint;

  private com.vividsolutions.jts.geom.Point m_p0;

  @Override
  public void paint( final Graphics g )
  {
    final GeoTransform projection = getMapPanel().getProjection();
    final SLDPainter painter = new SLDPainter( projection, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

    final IProfileFeature profile = getProfile();

    ProfilePainter.paintProfilePoints( g, painter, profile );
    ProfilePainter.paintProfilePointMarkers( g, painter, profile );

    doPaintSelection( g, painter );
    doPaintSnapPoint( g, painter );

    paintTooltip( g );
  }

  @Override
  public String getToolTip( )
  {
    if( Objects.isNull( getProfile(), m_snapPoint ) )
      return null;

    try
    {
      final double width = Profiles.getWidth( getProfile().getProfil(), m_snapPoint );

      return String.format( "Profilpunkt Breite: %.2f m", width );

    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
    }
    catch( final IllegalStateException e )
    {
      // snap point is not on line
    }

    return super.getToolTip();
  }

  private void doPaintSelection( final Graphics g, final SLDPainter painter )
  {
    try
    {
      final IProfileFeature profile = getProfile();
      if( Objects.isNull( profile ) )
        return;

      final IProfil iProfil = profile.getProfil();
      final IRangeSelection selection = iProfil.getSelection();
      if( selection.isEmpty() )
        return;

      final Geometry geometry = toGeometry( profile, selection );
      if( geometry instanceof com.vividsolutions.jts.geom.Point )
      {
        final com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) geometry;
        painter.paint( g, getClass().getResource( "symbolization/selection.points.sld" ), point ); //$NON-NLS-1$
      }
      else if( geometry instanceof LineString )
      {
        final LineString lineString = (LineString) geometry;
        final Geometry selectionGeometry = lineString.buffer( MapUtilities.calculateWorldDistance( getMapPanel(), 8 ) );

        painter.paint( g, getClass().getResource( "symbolization/selection.line.sld" ), selectionGeometry ); //$NON-NLS-1$
      }

    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  private Geometry toGeometry( final IProfileFeature profile, final IRangeSelection selection ) throws Exception
  {
    final Range<Double> range = selection.getRange();
    final Double minimum = range.getMinimum();
    final Double maximum = range.getMaximum();

    if( Objects.equal( minimum, maximum ) )
    {
      final Coordinate coorinate = Profiles.getJtsPosition( profile.getProfil(), minimum );
      return JTSConverter.toPoint( coorinate );
    }
    else
    {
      final Coordinate c1 = Profiles.getJtsPosition( profile.getProfil(), minimum );
      final Coordinate c2 = Profiles.getJtsPosition( profile.getProfil(), maximum );

      return JTSUtilities.createLineString( profile.getJtsLine(), JTSConverter.toPoint( c1 ), JTSConverter.toPoint( c2 ) );
    }
  }

  private void doPaintSnapPoint( final Graphics g, final SLDPainter painter )
  {
    try
    {
      final IProfileFeature profile = getProfile();
      if( Objects.isNull( profile ) )
        return;

      final com.vividsolutions.jts.geom.Point position = getMousePosition();
      if( Objects.isNull( position ) )
        return;

      final LineString curve = profile.getJtsLine();
      m_snapPoint = getSnapPoint( curve, position );
      if( Objects.isNull( m_snapPoint ) )
        return;

      if( isVertexPoint( curve, m_snapPoint.getCoordinate() ) )
        painter.paint( g, getClass().getResource( "symbolization/selection.snap.vertex.point.sld" ), m_snapPoint.getCoordinate() ); //$NON-NLS-1$

      painter.paint( g, getClass().getResource( "symbolization/selection.snap.point.sld" ), m_snapPoint.getCoordinate() ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  private boolean isVertexPoint( final Geometry geometry, final Coordinate point )
  {
    final Coordinate[] coordinates = geometry.getCoordinates();
    for( final Coordinate c : coordinates )
    {
      if( c.distance( point ) < 0.001 )
        return true;
    }

    return false;
  }

  private com.vividsolutions.jts.geom.Point getSnapPoint( final LineString lineString, final com.vividsolutions.jts.geom.Point position )
  {

    final LocationIndexedLine lineIndex = new LocationIndexedLine( lineString );
    final LinearLocation location = lineIndex.project( position.getCoordinate() );
    location.snapToVertex( lineString, MapUtilities.calculateWorldDistance( getMapPanel(), 10 ) );

    return JTSConverter.toPoint( lineIndex.extractPoint( location ) );
  }

  @Override
  public void keyPressed( final KeyEvent e )
  {
    final int keyCode = e.getKeyCode();
    switch( keyCode )
    {
      case KeyEvent.VK_SHIFT:
        m_shift = true;
        break;
    }
  }

  @Override
  public void keyReleased( final KeyEvent e )
  {
    final int keyCode = e.getKeyCode();
    switch( keyCode )
    {
      case KeyEvent.VK_ESCAPE:
        finish();
        break;

      case KeyEvent.VK_SHIFT:
        m_shift = false;
        break;
    }
  }

  protected boolean isShiftKeyPressed( )
  {
    return m_shift;
  }
}