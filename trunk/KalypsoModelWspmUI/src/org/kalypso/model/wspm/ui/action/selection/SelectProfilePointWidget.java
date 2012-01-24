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

/**
 * @author Dirk Kuch
 */
public class SelectProfilePointWidget extends AbstractProfileSelectionWidget
{
  private final SelectedProfilesMapPanelListener m_mapPanelListener = new SelectedProfilesMapPanelListener( this );

  public SelectProfilePointWidget( )
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

    if( Objects.isNull( getProfile() ) )
      return;

    try
    {
      final IProfil profile = getProfile().getProfil();
      final IRangeSelection selection = profile.getSelection();

      final Double cursor = selection.getCursor();
      if( Objects.isNull( cursor ) )
        return;

      if( isShiftKeyPressed() )
      {
        final double p0 = Profiles.getWidth( profile, m_p0 );
        selection.setRange( Range.between( p0, cursor ) );
      }
      else
      {
        selection.setRange( Range.is( cursor ) );
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
      m_p0 = getSnapPoint();

    updateSelection();
  }

  private boolean m_shift = false;

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

    ProfilePainter.doPaintProfileCursor( g, painter, profile, getClass().getResource( "symbolization/selection.snap.point.sld" ), getClass().getResource( "symbolization/selection.snap.vertex.point.sld" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    paintTooltip( g );
  }

  @Override
  public String getToolTip( )
  {

    if( Objects.isNull( getProfile() ) )
      return null;

    final IProfileFeature profileFeature = getProfile();
    final IRangeSelection selection = profileFeature.getProfil().getSelection();
    final Double cursor = selection.getCursor();
    if( Objects.isNull( cursor ) )
      return null;

    return String.format( "Profilpunkt Breite: %.2f m", cursor );
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