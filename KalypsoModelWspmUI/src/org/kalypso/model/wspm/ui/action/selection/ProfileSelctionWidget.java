package org.kalypso.model.wspm.ui.action.selection;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.jts.SnapUtilities;
import org.kalypso.jts.SnapUtilities.SNAP_TYPE;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.utilities.tooltip.ToolTipRenderer;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.SLDPainter;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.LineString;

/**
 * @author Dirk Kuch
 */
public class ProfileSelctionWidget extends AbstractWidget
{
  private final ToolTipRenderer m_tooltip = ToolTipRenderer.createStandardTooltip();

  private final SelectedProfilesMapPanelListener m_mapPanelListener = new SelectedProfilesMapPanelListener( this );

  /**
   * The current point on the map screen.
   */
  private Point m_currentPoint;

  private IProfileFeature[] m_profiles;

  public ProfileSelctionWidget( )
  {
    super( "", "" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * This function resets the widget.
   */
  private void reset( )
  {
// if( m_strategy != null )
// {
// m_strategy.dispose();
// m_strategy = null;
// }
    m_currentPoint = null;

    final Cursor cursor = Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR );
    getMapPanel().setCursor( cursor );
  }

  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    mapPanel.addSelectionChangedListener( m_mapPanelListener );
    m_profiles = m_mapPanelListener.doSelection( mapPanel.getSelection() );

    reset();

    /* Init the cursor. */
    final Cursor cursor = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
    mapPanel.setCursor( cursor );
  }

  @Override
  public void finish( )
  {
    getMapPanel().removeSelectionChangedListener( m_mapPanelListener );
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

      final GeoTransform projection = getMapPanel().getProjection();
      final SLDPainter sldPainter = new SLDPainter( projection, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
      sldPainter.paint( g, getClass().getResource( "symbolization/selection.snap.point.sld" ), m_snapPoint.getCoordinate() ); //$NON-NLS-1$

    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

// final IMapPanel mapPanel = getMapPanel();
// if( mapPanel == null )
// return;
//
// if( m_strategy != null )
// {
// m_strategy.paint( g, mapPanel, m_currentPoint );
// }
//
// if( m_tooltip != null )
// {
// final Rectangle bounds = mapPanel.getScreenBounds();
//
// if( m_strategy != null )
// {
// m_standardTooltip.setTooltip( STR_DEFAULT_TOOLTIP + m_strategy.getLabel() );
// }
//
// m_tooltip.paintToolTip( new Point( 5, bounds.height - 5 ), g, bounds );
// }
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

  public void onSelectionChange( final IProfileFeature[] profiles )
  {
    m_profiles = profiles;
  }

}