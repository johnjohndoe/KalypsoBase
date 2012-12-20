/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.model.wspm.ui.action.base;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.jts.JTSConverter;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.IProfileProvider;
import org.kalypso.model.wspm.core.gml.IProfileProviderListener;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileListener;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.ProfileListenerAdapter;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.model.wspm.ui.action.ProfilesSelection;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.utilities.tooltip.ToolTipRenderer;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

/**
 * @author Dirk Kuch
 */
public class AbstractProfileWidget extends AbstractWidget
{
  private final ToolTipRenderer m_toolTipRenderer = new ToolTipRenderer();

  private final IProfileListener m_listener = new ProfileListenerAdapter()
  {
    @Override
    public void onProfilChanged( final ProfileChangeHint hint )
    {
      repaintMap();
    }
  };

  private final IProfileProviderListener m_providerListener = new IProfileProviderListener()
  {
    @Override
    public void onProfilProviderChanged( final IProfileProvider provider )
    {
      handleOnProfilProviderChanged( provider );
    }
  };

  private ProfilesSelection m_selection;

  private IProfileFeature m_profileFeature;

  private com.vividsolutions.jts.geom.Point m_snapPoint;

  private IProfile m_profile;

  public AbstractProfileWidget( final String name, final String toolTip )
  {
    super( name, toolTip );

    m_selection = null;
    m_profileFeature = null;
    m_snapPoint = null;
  }

  protected void reset( )
  {
    final Cursor cursor = Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR );
    getMapPanel().setCursor( cursor );
  }

  @Override
  public void mouseMoved( final MouseEvent event )
  {
    try
    {
      final IProfileFeature profileFeature = getProfile();
      if( Objects.isNull( profileFeature ) )
        return;

      final com.vividsolutions.jts.geom.Point point = toJtsPosition( event.getPoint() );
      if( Objects.isNull( point ) )
        return;

      final IProfile profile = profileFeature.getProfile();
      final IRangeSelection selection = profile.getSelection();

      m_snapPoint = getSnapPoint( profileFeature.getJtsLine(), point );

      if( Objects.isNull( m_snapPoint, profile ) )
        selection.setCursor( null );
      else
      {
        final double cursor = Profiles.getWidth( profile, m_snapPoint );
        selection.setCursor( cursor );
      }
    }
    catch( final GM_Exception ex )
    {
      ex.printStackTrace();
    }
    catch( final IllegalStateException ex )
    {
      // do nothing - point is not on line!
    }
  }

  protected com.vividsolutions.jts.geom.Point getSnapPoint( )
  {
    return m_snapPoint;
  }

  private com.vividsolutions.jts.geom.Point getSnapPoint( final LineString lineString, final com.vividsolutions.jts.geom.Point position )
  {
    final LocationIndexedLine lineIndex = new LocationIndexedLine( lineString );
    final LinearLocation location = lineIndex.project( position.getCoordinate() );
    location.snapToVertex( lineString, MapUtilities.calculateWorldDistance( getMapPanel(), 10 ) );

    return JTSConverter.toPoint( lineIndex.extractPoint( location ) );
  }

  private com.vividsolutions.jts.geom.Point toJtsPosition( final Point position )
  {
    try
    {
      if( Objects.isNull( position ) )
        return null;

      final GM_Point gmCurrent = MapUtilities.transform( getMapPanel(), position );
      return (com.vividsolutions.jts.geom.Point)JTSAdapter.export( gmCurrent );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

    return null;
  }

  public void setSelection( final ProfilesSelection selection )
  {
    final IProfileFeature profile = selection == null || selection.getSelectedProfiles().length < 1 ? null : selection.getSelectedProfiles()[0];

    if( m_profileFeature == profile )
      return;

    // always set and reset profile listener, because of changed underlying iprofile!
    if( Objects.isNotNull( m_profileFeature ) )
      m_profileFeature.removeProfilProviderListener( m_providerListener );

    m_selection = selection;
    m_profileFeature = profile;

    if( Objects.isNotNull( m_profileFeature ) )
      m_profileFeature.addProfilProviderListener( m_providerListener );

    handleOnProfilProviderChanged( m_profileFeature );
  }

  protected ProfilesSelection getSelection( )
  {
    return m_selection;
  }

  protected IProfileFeature getProfile( )
  {
    return m_profileFeature;
  }

  protected void paintTooltip( final Graphics g )
  {
    final Rectangle screenBounds = getMapPanel().getScreenBounds();
    final Point tooltipPosition = getTooltipPosition( screenBounds );

    m_toolTipRenderer.setTooltip( getToolTip() );
    m_toolTipRenderer.paintToolTip( tooltipPosition, g, screenBounds );
  }

  /**
   * The position of the tooltip to be painted in screen coordinates. Defaults to the lower right corner.<br/>
   * Overwrite to change position.
   */
  protected Point getTooltipPosition( final Rectangle screenBounds )
  {
    final int x = screenBounds.x + screenBounds.width;
    final int y = screenBounds.y + screenBounds.height - 5;

    return new Point( x, y );
  }

  protected void handleOnProfilProviderChanged( final IProfileProvider provider )
  {
    if( m_profile != null )
      m_profile.removeProfilListener( m_listener );
    m_profile = null;

    if( provider != null )
      m_profile = provider.getProfile();

    if( m_profile != null )
      m_profile.addProfilListener( m_listener );

    repaintMap();
  }
}