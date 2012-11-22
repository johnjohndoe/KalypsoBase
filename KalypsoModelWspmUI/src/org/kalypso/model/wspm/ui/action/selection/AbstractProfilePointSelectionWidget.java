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
package org.kalypso.model.wspm.ui.action.selection;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.apache.commons.lang3.Range;
import org.eclipse.jface.viewers.ISelection;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.model.wspm.ui.action.ProfilesSelection;
import org.kalypso.model.wspm.ui.action.base.AbstractProfileWidget;
import org.kalypso.model.wspm.ui.action.base.ProfilePainter;
import org.kalypso.model.wspm.ui.action.base.ProfileWidgetMapPanelListener;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.SLDPainter;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Exception;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractProfilePointSelectionWidget extends AbstractProfileWidget
{
  private final ProfileWidgetMapPanelListener m_mapPanelListener;

  private final boolean m_viewMode;

  private com.vividsolutions.jts.geom.Point m_p0;

  public AbstractProfilePointSelectionWidget( final boolean viewMode )
  {
    super( "", "" ); //$NON-NLS-1$ //$NON-NLS-2$

    m_mapPanelListener = new ProfileWidgetMapPanelListener( this );
    m_viewMode = viewMode;
    m_p0 = null;
  }

  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    /* Add myself as selection changed listener. */
    mapPanel.addSelectionChangedListener( m_mapPanelListener );

    /* Initialize widget with the selection. */
    final ISelection selection = mapPanel.getSelection();
    setSelection( new ProfilesSelection( selection ) );

    /* Reset. */
    reset();

    /* init the cursor. */
    final Cursor cursor = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
    mapPanel.setCursor( cursor );
  }

  @Override
  public void finish( )
  {
    /* Remove myself as selection changed listener. */
    getMapPanel().removeSelectionChangedListener( m_mapPanelListener );

    /* Reset the selection within this widget. */
    setSelection( (ProfilesSelection)null ); // purge profile change listener

    /* Reset & repaint. */
    reset();
    repaintMap();

    super.finish();
  }

  private void updateSelection( final boolean shiftDown )
  {
    if( m_viewMode )
      return;

    if( Objects.isNull( getProfile() ) )
      return;

    try
    {
      final IProfile profile = getProfile().getProfile();
      final IRangeSelection selection = profile.getSelection();

      final Double cursor = selection.getCursor();
      if( Objects.isNull( cursor ) )
        return;

      if( shiftDown )
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
  public void mousePressed( final MouseEvent e )
  {
    if( m_viewMode )
      return;

    if( MouseEvent.BUTTON1 != e.getButton() )
      return;

    final boolean shiftDown = e.isShiftDown();
    if( !shiftDown )
      m_p0 = getSnapPoint();

    updateSelection( shiftDown );
  }

  @Override
  public void paint( final Graphics g )
  {
    final GeoTransform projection = getMapPanel().getProjection();
    if( projection == null )
      return;

    final SLDPainter painter = new SLDPainter( projection, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

    final IProfileFeature profile = getProfile();

    ProfilePainter.paintProfilePoints( g, painter, profile );
    ProfilePainter.paintProfilePointMarkers( g, painter, profile );
    ProfilePainter.paintProfileCursor( g, painter, profile, getClass().getResource( "symbolization/selection.snap.point.sld" ), getClass().getResource( "symbolization/selection.snap.vertex.point.sld" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    ProfilePainter.paintSelection( getMapPanel(), profile, g, painter );

    paintTooltip( g );
  }

  @Override
  public String getToolTip( )
  {
    if( Objects.isNull( getProfile() ) )
      return null;

    final IProfileFeature profileFeature = getProfile();
    final IProfile profile = profileFeature.getProfile();
    final IRangeSelection selection = profile.getSelection();
    final Double cursor = selection.getCursor();
    if( Objects.isNull( cursor ) )
      return null;

    final double hoehe = Profiles.getHoehe( profile, cursor );

    return String.format( Messages.getString( "AbstractProfilePointSelectionWidget_0" ), cursor, hoehe ); //$NON-NLS-1$
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
    }
  }
}