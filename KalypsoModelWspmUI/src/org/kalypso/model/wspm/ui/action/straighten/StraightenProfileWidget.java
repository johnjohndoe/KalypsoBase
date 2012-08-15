/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.model.wspm.ui.action.straighten;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.swt.awt.SWT_AWT_Utilities;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.model.wspm.ui.action.ProfileSelection;
import org.kalypso.model.wspm.ui.action.base.AbstractProfileWidget;
import org.kalypso.model.wspm.ui.action.base.ProfilePainter;
import org.kalypso.model.wspm.ui.action.base.ProfileWidgetMapPanelListener;
import org.kalypso.model.wspm.ui.dialog.straighten.StraightenProfileDialog;
import org.kalypso.model.wspm.ui.dialog.straighten.StraightenProfileOperation;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.widgets.advanced.utils.SLDPainter;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.transformation.GeoTransform;

/**
 * Straightens a profile between to selected points.
 * 
 * @author Holger Albert
 */
public class StraightenProfileWidget extends AbstractProfileWidget
{
  /**
   * The map panel listener.
   */
  private final ProfileWidgetMapPanelListener m_mapPanelListener;

  /**
   * The first point.
   */
  private com.vividsolutions.jts.geom.Point m_firstPoint;

  /**
   * The second point.
   */
  private com.vividsolutions.jts.geom.Point m_secondPoint;

  /**
   * The constructor.
   */
  public StraightenProfileWidget( )
  {
    super( "Straighten profile", "Straightens a profile between to selected points." );

    m_mapPanelListener = new ProfileWidgetMapPanelListener( this );
    m_firstPoint = null;
    m_secondPoint = null;
  }

  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    /* Add myself as selection changed listener. */
    mapPanel.addSelectionChangedListener( m_mapPanelListener );

    /* Initialize widget with the selection. */
    final ISelection selection = mapPanel.getSelection();
    setSelection( new ProfileSelection( selection ) );

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
    setSelection( (ProfileSelection) null );

    /* Reset & repaint. */
    reset();

    super.finish();
  }

  @Override
  public void mousePressed( final MouseEvent event )
  {
    if( MouseEvent.BUTTON1 != event.getButton() )
      return;

    final com.vividsolutions.jts.geom.Point snapPoint = getSnapPoint();

    if( m_firstPoint == null )
    {
      m_firstPoint = snapPoint;
      return;
    }

    m_secondPoint = snapPoint;

    final Shell shell = SWT_AWT_Utilities.findActiveShell();
    final Display display = shell.getDisplay();
    display.syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        perfomStraightening( shell );
      }
    } );
  }

  @Override
  public void paint( final Graphics g )
  {
    final GeoTransform projection = getMapPanel().getProjection();
    final SLDPainter painter = new SLDPainter( projection, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

    final IProfileFeature profile = getProfile();

    ProfilePainter.paintProfilePoints( g, painter, profile );
    ProfilePainter.paintProfilePointMarkers( g, painter, profile );

    paintSelectedPoints( g, painter, getClass().getResource( "symbolization/selection.snap.point.sld" ) ); //$NON-NLS-1$
    paintSnapPoint( g, painter, getClass().getResource( "symbolization/selection.snap.point.sld" ) ); //$NON-NLS-1$

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

    final IProfil profile = profileFeature.getProfil();
    if( Objects.isNull( profile ) )
      return null;

    final Double cursor = profile.getSelection().getCursor();
    if( Objects.isNull( cursor ) )
      return null;

    final double hoehe = Profiles.getHoehe( profile, cursor );
    return String.format( "Width %.4f m, Height %.4f m", cursor, hoehe );
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

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void setSelection( final ISelection selection )
  {
    m_firstPoint = null;
    m_secondPoint = null;

    super.setSelection( selection );
  }

  protected void perfomStraightening( final Shell shell )
  {
    try
    {
      /* Open the straighten profile dialog. */
      final StraightenProfileDialog straightenDialog = new StraightenProfileDialog( shell, getSelection(), getProfile(), m_firstPoint, m_secondPoint );
      if( straightenDialog.open() != Window.OK )
        return;

      /* Execute the straighten profile operation. */
      final StraightenProfileOperation operation = new StraightenProfileOperation( straightenDialog.getData() );
      final IProgressService progressService = (IProgressService) PlatformUI.getWorkbench().getService( IProgressService.class );
      final IStatus status = RunnableContextHelper.execute( progressService, true, false, operation );

      /* Open the status dialog. */
      final StatusDialog statusDialog = new StatusDialog( shell, status, "Profil begradigen" );
      statusDialog.open();
    }
    finally
    {
      m_firstPoint = null;
      m_secondPoint = null;

      /* Reset selection in order to reset listeners to IProfile; else strange effekt (nothing moves anymore) */
      setSelection( getSelection() );

      repaintMap();
    }
  }

  private void paintSelectedPoints( final Graphics g, final SLDPainter painter, final URL sldLinePoint )
  {
    try
    {
      if( m_firstPoint != null )
        painter.paint( g, sldLinePoint, m_firstPoint );

      if( m_secondPoint != null )
        painter.paint( g, sldLinePoint, m_secondPoint );
    }
    catch( final CoreException ex )
    {
      ex.printStackTrace();
    }
  }

  private void paintSnapPoint( final Graphics g, final SLDPainter painter, final URL sldLinePoint )
  {
    try
    {
      final com.vividsolutions.jts.geom.Point snapPoint = getSnapPoint();
      if( snapPoint != null )
        painter.paint( g, sldLinePoint, snapPoint );
    }
    catch( final CoreException ex )
    {
      ex.printStackTrace();
    }
  }
}