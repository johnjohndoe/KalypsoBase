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
package org.kalypso.model.wspm.ui.action.insert.point;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.swt.awt.SWT_AWT_Utilities;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.model.wspm.ui.action.ProfilesSelection;
import org.kalypso.model.wspm.ui.action.base.AbstractProfileWidget;
import org.kalypso.model.wspm.ui.action.base.ProfilePainter;
import org.kalypso.model.wspm.ui.action.base.ProfileWidgetHelper;
import org.kalypso.model.wspm.ui.action.base.ProfileWidgetMapPanelListener;
import org.kalypso.model.wspm.ui.i18n.CommonMessages;
import org.kalypso.observation.result.IInterpolationHandler;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.SLDPainter;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Exception;

/**
 * @author Dirk Kuch
 */
public class InsertProfilePointWidget extends AbstractProfileWidget
{
  private final ProfileWidgetMapPanelListener m_mapPanelListener;

  public InsertProfilePointWidget( )
  {
    super( "", "" ); //$NON-NLS-1$ //$NON-NLS-2$

    m_mapPanelListener = new ProfileWidgetMapPanelListener( this );
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

    /* Init the cursor. */
    final Cursor cursor = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
    mapPanel.setCursor( cursor );
  }

  @Override
  public void finish( )
  {
    /* Remove myself as selection changed listener. */
    getMapPanel().removeSelectionChangedListener( m_mapPanelListener );

    /* Reset the selection within this widget. */
    setSelection( (ProfilesSelection) null ); // purge profile change listener

    /* Reset & repaint. */
    reset();
    repaintMap();

    super.finish();
  }

  @Override
  public void mousePressed( final MouseEvent event )
  {
    if( MouseEvent.BUTTON1 != event.getButton() )
      return;

    try
    {
      final IProfileFeature profileFeature = getProfile();
      if( Objects.isNull( profileFeature ) )
        return;

      final com.vividsolutions.jts.geom.Point snapped = getSnapPoint();
      if( ProfileWidgetHelper.isVertexPoint( profileFeature.getJtsLine(), snapped.getCoordinate() ) )
        return;

      final IProfile profile = profileFeature.getProfile();
      final Double cursor = profile.getSelection().getCursor();
      if( Objects.isNull( cursor ) )
        return;

      final IProfileRecord before = profile.findPreviousPoint( cursor );
      final IProfileRecord next = profile.findNextPoint( cursor );
      if( Objects.isNull( before, next ) )
        return;

      final String message = String.format( CommonMessages.INSERT_POINT_CONFIRM, cursor );
      if( !SWT_AWT_Utilities.showSwtMessageBoxConfirm( CommonMessages.INSERT_POINT_TITLE, message ) )
        return;

      final double distance = (cursor - before.getBreite()) / (next.getBreite() - before.getBreite());

      final TupleResult result = profile.getResult();
      final IProfileRecord record = profile.createProfilPoint();
      final IInterpolationHandler interpolation = result.getInterpolationHandler();

      final int index = before.getIndex();
      if( interpolation.doInterpolation( result, record, index, distance ) )
        result.add( index + 1, record );

      final Job job = new Job( "Active point changed" ) //$NON-NLS-1$
      {
        @Override
        protected IStatus run( final IProgressMonitor monitor )
        {
          profile.getSelection().setActivePoints( record );

          return Status.OK_STATUS;
        }
      };

      job.setSystem( true );
      job.setUser( false );

      job.schedule();
    }
    catch( final GM_Exception e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public void paint( final Graphics g )
  {
    final GeoTransform projection = getMapPanel().getProjection();
    final SLDPainter painter = new SLDPainter( projection, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

    final IProfileFeature profile = getProfile();

    // TODO: different symbolization if snap not possible
    ProfilePainter.paintProfilePoints( g, painter, profile );
    ProfilePainter.paintProfilePointMarkers( g, painter, profile );
    ProfilePainter.paintProfileCursor( g, painter, profile, getClass().getResource( "symbolization/selection.snap.point.sld" ), getClass().getResource( "symbolization/selection.snap.vertex.point.sld" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    paintTooltip( g );
  }

  @Override
  protected Point getTooltipPosition( final Rectangle screenBounds )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel == null )
      return super.getTooltipPosition( screenBounds );

    final com.vividsolutions.jts.geom.Point snapPoint = getSnapPoint();
    if( snapPoint != null )
    {
      final GeoTransform projection = mapPanel.getProjection();
      final int x = (int) projection.getDestX( snapPoint.getX() );
      final int y = (int) projection.getDestY( snapPoint.getY() );

      return new Point( x + 10, y - 10 );
    }

    return super.getTooltipPosition( screenBounds );
  }

  @Override
  public String getToolTip( )
  {
    final IProfileFeature profileFeature = getProfile();
    if( Objects.isNull( profileFeature ) )
      return null;

    final IProfile profile = profileFeature.getProfile();
    if( Objects.isNull( profile ) )
      return null;

    final Double cursor = profile.getSelection().getCursor();
    if( Objects.isNull( cursor ) )
      return null;

    final double hoehe = Profiles.getHoehe( profile, cursor );
    return String.format( CommonMessages.INSERT_POINT_TOOLTIP, cursor, hoehe ); //$NON-NLS-1$
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