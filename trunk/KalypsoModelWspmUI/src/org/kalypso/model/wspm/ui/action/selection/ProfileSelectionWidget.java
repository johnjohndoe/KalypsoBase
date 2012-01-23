package org.kalypso.model.wspm.ui.action.selection;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.java.lang.Arrays;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.jts.JTSConverter;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.jts.SnapUtilities;
import org.kalypso.jts.SnapUtilities.SNAP_TYPE;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.SLDPainter;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * @author Dirk Kuch
 */
public class ProfileSelectionWidget extends AbstractWidget
{
  private final SelectedProfilesMapPanelListener m_mapPanelListener = new SelectedProfilesMapPanelListener( this );

  private Point m_currentPoint;

  private IProfileFeature[] m_profiles;

  public ProfileSelectionWidget( )
  {
    super( "", "" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * This function resets the widget.
   */
  private void reset( )
  {
    m_currentPoint = null;

    final Cursor cursor = Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR );
    getMapPanel().setCursor( cursor );
  }

  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {

    super.activate( commandPoster, mapPanel );

    mapPanel.addSelectionChangedListener( m_mapPanelListener );
    onSelectionChange( m_mapPanelListener.doSelection( mapPanel.getSelection() ) );

    reset();

    /* Init the cursor. */
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
    if( Objects.isNull( m_profile, m_snapPoint ) )
      return;

    try
    {
      final IProfil profile = m_profile.getProfil();
      final IRangeSelection selection = profile.getSelection();

      final double breite = Profiles.getWidth( m_profile.getProfil(), m_snapPoint );
      selection.setRange( Range.is( breite ) );
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public void leftPressed( final Point p )
  {
    updateSelection();
  }

  @Override
  public void moved( final Point p )
  {
    m_currentPoint = p;

    repaintMap();
  }

  private com.vividsolutions.jts.geom.Point m_snapPoint;

  private IProfileFeature m_profile;

  @Override
  public void paint( final Graphics g )
  {
    final GeoTransform projection = getMapPanel().getProjection();
    final SLDPainter painter = new SLDPainter( projection, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

    doPaintSelection( g, painter );
    doPaintSnapPoint( g, painter );

  }

  private void doPaintSelection( final Graphics g, final SLDPainter painter )
  {
    if( Arrays.isEmpty( m_profiles ) )
      return;

    for( final IProfileFeature profile : m_profiles )
    {
      try
      {
        final IProfil iProfil = profile.getProfil();
        final IRangeSelection selection = iProfil.getSelection();
        if( selection.isEmpty() )
          continue;

        final Geometry geometry = toGeometry( profile, selection );
        if( geometry instanceof com.vividsolutions.jts.geom.Point )
        {
          final com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) geometry;
          painter.paint( g, getClass().getResource( "symbolization/selection.points.sld" ), point ); //$NON-NLS-1$
        }
        else if( geometry instanceof LineString )
        {
          final LineString lineString = (LineString) geometry;
          final Geometry selectionGeometry = lineString.buffer( 3.0 );

          painter.paint( g, getClass().getResource( "symbolization/selection.line.sld" ), selectionGeometry ); //$NON-NLS-1$
        }

      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
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
      if( Objects.isNull( m_currentPoint ) )
        return;

      final GM_Point gmCurrent = MapUtilities.transform( getMapPanel(), m_currentPoint );
      final com.vividsolutions.jts.geom.Point position = (com.vividsolutions.jts.geom.Point) JTSAdapter.export( gmCurrent );

      m_profile = findClosestProfile( position );
      if( Objects.isNull( m_profile ) )
        return;

      final LineString lineString = m_profile.getJtsLine();

      m_snapPoint = SnapUtilities.snapToLine( lineString, position.buffer( 2 ), SNAP_TYPE.SNAP_TO_POINT );
      if( Objects.isNull( m_snapPoint ) )
        m_snapPoint = SnapUtilities.snapToLine( lineString, position.buffer( lineString.getCentroid().distance( position ) + 1.0 ), SNAP_TYPE.SNAP_TO_LINE );

      if( Objects.isNull( m_snapPoint ) )
        return;

      painter.paint( g, getClass().getResource( "symbolization/selection.snap.point.sld" ), m_snapPoint.getCoordinate() ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  private IProfileFeature findClosestProfile( final com.vividsolutions.jts.geom.Point point )
  {
    if( ArrayUtils.isEmpty( m_profiles ) )
      return null;
    else if( ArrayUtils.getLength( m_profiles ) == 1 )
      return m_profiles[0];

    double distance = Double.MAX_VALUE;
    IProfileFeature ptr = null;

    for( final IProfileFeature profile : m_profiles )
    {
      try
      {
        if( Objects.isNull( ptr ) )
          ptr = profile;
        else
        {
          final LineString curve = profile.getJtsLine();
          final double d = point.distance( curve.getCentroid() );

          if( d < distance )
          {
            ptr = profile;
            distance = d;
          }
        }
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
    }

    return ptr;
  }

  @Override
  public void keyReleased( final KeyEvent e )
  {
    final int keyCode = e.getKeyCode();
    switch( keyCode )
    {
      case KeyEvent.VK_ESCAPE:
// activate( getCommandTarget(), getMapPanel() );
// repaintMap();
        break;

      case KeyEvent.VK_BACK_SPACE:
// if( m_strategy != null )
// {
// m_strategy.removeLastPoint();
// repaintMap();
// }
        break;

      case KeyEvent.VK_SPACE:
// m_strategyExtendProfile = !m_strategyExtendProfile;
// final IDialogSettings settings = getSettings();
// if( settings != null )
// {
// settings.put( SETTINGS_MODE, m_strategyExtendProfile );
// }
// activate( getCommandTarget(), getMapPanel() );
// repaintMap();
        break;
    }
  }

  private final IProfilListener m_listener = new IProfilListener()
  {
    @Override
    public void onProfilChanged( final ProfilChangeHint hint )
    {
      if( hint.isSelectionChanged() )
        repaintMap();
    }

    @Override
    public void onProblemMarkerChanged( final IProfil source )
    {
    }
  };

  public void onSelectionChange( final IProfileFeature[] profiles )
  {
    if( Arrays.isNotEmpty( m_profiles ) )
      for( final IProfileFeature profile : m_profiles )
      {
        profile.getProfil().removeProfilListener( m_listener );
      }

    m_profiles = profiles;

    if( Arrays.isNotEmpty( m_profiles ) )
      for( final IProfileFeature profile : m_profiles )
      {
        profile.getProfil().addProfilListener( m_listener );
      }
  }
}