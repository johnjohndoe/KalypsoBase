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
package org.kalypso.model.wspm.ui.action.selection;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.IProfileProvider;
import org.kalypso.model.wspm.core.gml.IProfileProviderListener;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.utilities.tooltip.ToolTipRenderer;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

/**
 * @author Dirk Kuch
 */
public class AbstractProfileSelectionWidget extends AbstractWidget implements IProfileProviderListener
{

  private final ToolTipRenderer m_toolTipRenderer = new ToolTipRenderer();

  private final IProfilListener m_listener = new IProfilListener()
  {
    @Override
    public void onProfilChanged( final ProfilChangeHint hint )
    {
      repaintMap();
    }

    @Override
    public void onProblemMarkerChanged( final IProfil source )
    {
    }
  };

  private Point m_currentPoint;

  private IProfileFeature m_profile;

  public AbstractProfileSelectionWidget( final String name, final String toolTip )
  {
    super( name, toolTip );
  }

  protected void reset( )
  {
    m_currentPoint = null;

    final Cursor cursor = Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR );
    getMapPanel().setCursor( cursor );
  }

  @Override
  public void moved( final Point p )
  {
    m_currentPoint = p;

    repaintMap();
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

  private void doSelectionChange( final IProfileFeature profile )
  {
    if( Objects.equal( m_profile, profile ) )
      return;

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
  }

  protected IProfileFeature getProfile( )
  {
    return m_profile;
  }

  protected com.vividsolutions.jts.geom.Point getMousePosition( )
  {
    try
    {
      if( Objects.isNull( m_currentPoint ) )
        return null;

      final GM_Point gmCurrent = MapUtilities.transform( getMapPanel(), m_currentPoint );
      return (com.vividsolutions.jts.geom.Point) JTSAdapter.export( gmCurrent );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

    return null;
  }

  protected void paintTooltip( final Graphics g )
  {
    final Rectangle screenBounds = getMapPanel().getScreenBounds();
    final Point point = new Point( 5, Double.valueOf( screenBounds.getHeight() ).intValue() );

    m_toolTipRenderer.setTooltip( getToolTip() );
    m_toolTipRenderer.paintToolTip( point, g, screenBounds );
  }

  @Override
  public void onProfilProviderChanged( final IProfileProvider provider )
  {
    new Job( "Forcing repaint event" )
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        if( provider instanceof IProfileFeature )
          doSelectionChange( (IProfileFeature) provider );

        repaintMap();

        return Status.OK_STATUS;
      }
    };

  }

}
