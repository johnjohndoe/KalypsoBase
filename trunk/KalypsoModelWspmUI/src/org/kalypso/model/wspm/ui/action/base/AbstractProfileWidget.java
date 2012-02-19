/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.jts.JTSConverter;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.IProfileProvider;
import org.kalypso.model.wspm.core.gml.IProfileProviderListener;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.ProfilListenerAdapter;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.utilities.tooltip.ToolTipRenderer;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

/**
 * @author Dirk Kuch
 */
public class AbstractProfileWidget extends AbstractWidget implements IProfileProviderListener
{
  private final ToolTipRenderer m_toolTipRenderer = new ToolTipRenderer();

  private final IProfilListener m_listener = new ProfilListenerAdapter()
  {
    @SuppressWarnings("synthetic-access")
    @Override
    public void onProfilChanged( final ProfilChangeHint hint )
    {
      repaintMap();
    }
  };

  private IProfileFeature m_profile;

  private com.vividsolutions.jts.geom.Point m_snapPoint;

  public AbstractProfileWidget( final String name, final String toolTip )
  {
    super( name, toolTip );
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

      final IProfil profile = profileFeature.getProfil();
      final IRangeSelection selection = profile.getSelection();

      m_snapPoint = getSnapPoint( profileFeature.getJtsLine(), point );

      if( Objects.isNull( m_snapPoint ) )
        selection.setCursor( null );
      else
      {
        final double cursor = Profiles.getWidth( getProfile().getProfil(), m_snapPoint );
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
      return (com.vividsolutions.jts.geom.Point) JTSAdapter.export( gmCurrent );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

    return null;
  }

  public final void onSelectionChange( final IProfileFeature[] profiles )
  {
    if( ArrayUtils.getLength( profiles ) > 1 )
      throw new UnsupportedOperationException();

    if( ArrayUtils.isEmpty( profiles ) )
      doSelectionChange( null );
    else
      doSelectionChange( profiles[0] );

  }

  protected void doSelectionChange( final IProfileFeature profile )
  {
    // always set and reset profile listener, because of changed underlying iprofile!

    if( Objects.isNotNull( m_profile ) )
    {
      m_profile.removeProfilProviderListener( this );
      m_profile.getProfil().removeProfilListener( m_listener );
    }

    m_profile = profile;

    if( Objects.isNotNull( m_profile ) )
    {
      m_profile.addProfilProviderListener( this );
      m_profile.getProfil().addProfilListener( m_listener );
    }

    repaintMap();
  }

  protected IProfileFeature getProfile( )
  {
    return m_profile;
  }

  protected void paintTooltip( final Graphics g )
  {
    final Rectangle screenBounds = getMapPanel().getScreenBounds();
    final Point tooltipPosition = getTooltipPosition( screenBounds );

    m_toolTipRenderer.setTooltip( getToolTip() );
    m_toolTipRenderer.paintToolTip( tooltipPosition, g, screenBounds );
  }

  /**
   * The position of the tool tip to be painted in screen coordinates. Defaults to the lower right corner.<br/>
   * Overwrite to change position.
   */
  protected Point getTooltipPosition( final Rectangle screenBounds )
  {
    final int x = screenBounds.x + screenBounds.width;
    final int y = screenBounds.y + screenBounds.height - 5;

    return new Point( x, y );
  }

  protected final boolean isVertexPoint( final Geometry geometry, final Coordinate point )
  {
    final Coordinate[] coordinates = geometry.getCoordinates();
    for( final Coordinate c : coordinates )
    {
      if( c.distance( point ) < 0.001 )
        return true;
    }

    return false;
  }

  @Override
  public void onProfilProviderChanged( final IProfileProvider provider )
  {
    final Job job = new Job( "Forcing repaint event" ) //$NON-NLS-1$
    {
      @SuppressWarnings("synthetic-access")
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        if( provider instanceof IProfileFeature )
          doSelectionChange( (IProfileFeature) provider );

        repaintMap();

        return Status.OK_STATUS;
      }
    };

    job.setSystem( true );
    job.setUser( false );

    job.schedule();

  }

}
