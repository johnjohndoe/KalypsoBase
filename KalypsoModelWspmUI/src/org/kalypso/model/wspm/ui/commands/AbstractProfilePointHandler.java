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
package org.kalypso.model.wspm.ui.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.chart.ui.editor.mousehandler.AbstractChartHandler;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.IWspmLayers;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilTheme;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.FindLayerVisitor;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractProfilePointHandler extends AbstractChartHandler
{
  private Double m_breite;

  private IProfil m_profile;

  private final IProfilListener m_listener = new IProfilListener()
  {
    @Override
    public void onProfilChanged( final ProfilChangeHint hint )
    {
      profileChanged( hint );
    }

    @Override
    public void onProblemMarkerChanged( final IProfil source )
    {
      profileProblemMarkerChanged( source );
    }
  };

  public AbstractProfilePointHandler( final IChartComposite chart )
  {
    super( chart );

    doInit( chart );
  }

  private void doInit( final IChartComposite chart )
  {
    final Job job = new Job( "Initializing Profile Point Handler" )
    {

      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        final AbstractProfilTheme theme = findProfileTheme( chart );
        if( Objects.isNull( theme ) )
          return Status.CANCEL_STATUS;

        setProfile( theme.getProfil() );

        return Status.OK_STATUS;
      }
    };
    job.setUser( false );
    job.setSystem( true );

    job.schedule();

    chart.addListener( new IChartModelEventListener()
    {
      @Override
      public void onModelChanged( final IChartModel oldModel, final IChartModel newModel )
      {
        job.schedule();
      }
    } );
  }

  protected void profileChanged( final ProfilChangeHint hint )
  {
    if( hint.isSelectionChanged() || hint.isSelectionCursorChanged() )
      forceRedrawEvent();

  }

  protected void profileProblemMarkerChanged( final IProfil source )
  {
  }

  protected final Double getBreite( )
  {
    return m_breite;
  }

  protected final void setBreite( final Double breite )
  {
    m_breite = breite;
  }

  protected final IProfil getProfile( )
  {
    return m_profile;
  }

  protected final void setProfile( final IProfil profile )
  {
    if( Objects.equal( m_profile, profile ) )
      return;

    if( Objects.isNotNull( m_profile ) )
      m_profile.removeProfilListener( m_listener );

    m_profile = profile;

    if( Objects.isNotNull( m_profile ) )
      m_profile.addProfilListener( m_listener );
  }

  @Override
  public final void mouseMove( final MouseEvent e )
  {
    super.mouseMove( e );

    final IChartComposite chart = getChart();
    final Rectangle bounds = chart.getPlotRect();

    final Point position = ChartHandlerUtilities.screen2plotPoint( new Point( e.x, e.y ), bounds );
    if( !isValid( bounds, position ) )
    {
      doReset();

      return;
    }

    final AbstractProfilTheme theme = findProfileTheme( chart );
    if( Objects.isNull( theme ) )
    {
      doReset();

      return;
    }

    final ICoordinateMapper mapper = theme.getCoordinateMapper();

    setProfile( theme.getProfil() );
    setBreite( mapper.getDomainAxis().screenToNumeric( position.x ).doubleValue() );

    if( isOutOfRange() )
    {
      doReset();
      return;
    }

    doMouseMove( theme, position );
  }

  protected abstract void doMouseMove( AbstractProfilTheme theme, final Point position );

  protected final AbstractProfilTheme findProfileTheme( final IChartComposite chart )
  {
    final IChartModel model = chart.getChartModel();

    final FindLayerVisitor visitor = new FindLayerVisitor( IWspmLayers.LAYER_GELAENDE );
    model.getLayerManager().accept( visitor );

    final IChartLayer layer = visitor.getLayer();

    return (AbstractProfilTheme) layer;
  }

  protected final boolean isValid( final Rectangle bounds, final Point position )
  {
    if( position.x < 0 )
      return false;
    else if( position.x > bounds.width )
      return false;

    return true;
  }

  protected final boolean isOutOfRange( )
  {
    final IProfileRecord p0 = getProfile().getFirstPoint();
    final IProfileRecord pn = getProfile().getLastPoint();

    if( getBreite() < p0.getBreite() )
      return true;
    else if( getBreite() > pn.getBreite() )
      return true;

    return false;
  }

  protected final void doReset( )
  {
    setProfile( null );
    setBreite( null );

    setCursor( SWT.CURSOR_ARROW );
  }

}
