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
package org.kalypso.gml.processes.tin;

import java.math.BigDecimal;
import java.net.URL;

import org.apache.commons.lang3.Range;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.progress.IProgressConstants;
import org.kalypso.gml.processes.KalypsoGmlProcessesPlugin;
import org.kalypso.gml.processes.i18n.Messages;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree.model.tin.ITin;

/**
 * Wrapper around a {@link org.kalypsodeegree.model.geometry.GM_TriangulatedSurface} that implementents {@link ITin}
 * 
 * @author Gernot Belger
 */
public class TriangulatedSurfaceTin implements ITin
{
  private static IStatus INIT_STATUS = new Status( IStatus.INFO, KalypsoGmlProcessesPlugin.PLUGIN_ID, Messages.getString( "TriangulatedSurfaceTin_0" ) ); //$NON-NLS-1$

  private IStatus m_loadStatus = INIT_STATUS;

  private final URL m_dataLocation;

  private TriangulatedSurfaceFeature m_surfaceFeature;

  private TinLoadJob m_loadJob;

  private Range<BigDecimal> m_minMax;

  private final String m_mimeType;

  public TriangulatedSurfaceTin( final URL dataLocation, final String mimeType )
  {
    m_dataLocation = dataLocation;
    m_mimeType = mimeType;
  }

  @Override
  public synchronized void dispose( )
  {
    // REAMRK: so dispose can be overwritten, but finalize will still dispose the data
    disposeInternal();
  }

  public synchronized void disposeInternal( )
  {
    if( m_loadJob != null )
      m_loadJob.cancel();

    if( m_surfaceFeature != null )
      m_surfaceFeature.getWorkspace().dispose();

    m_loadJob = null;
    m_surfaceFeature = null;
    m_minMax = null;
    m_loadStatus = INIT_STATUS;
  }

  @Override
  protected void finalize( ) throws Throwable
  {
    disposeInternal();
  }

  @Override
  public synchronized GM_TriangulatedSurface getTriangulatedSurface( )
  {
    if( m_surfaceFeature == null && m_loadJob == null )
    {
      m_loadJob = new TinLoadJob( this );
      m_loadJob.setProperty( IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, true );
      m_loadJob.setProperty( IProgressConstants.KEEP_PROPERTY, false );

      m_loadJob.addJobChangeListener( new JobChangeAdapter()
      {
        @Override
        public void done( final IJobChangeEvent event )
        {
          handleSurfaceLoaded( event );
        }
      } );

      m_loadStatus = new Status( IStatus.INFO, KalypsoGmlProcessesPlugin.PLUGIN_ID, Messages.getString( "TriangulatedSurfaceTin_1" ) ); //$NON-NLS-1$
      m_loadJob.schedule();
    }

    if( m_surfaceFeature == null )
      return null;

    return m_surfaceFeature.getTriangulatedSurface();
  }

  protected synchronized void handleSurfaceLoaded( final IJobChangeEvent event )
  {
    Assert.isTrue( m_loadJob == event.getJob() );

    m_surfaceFeature = m_loadJob.getSurface();
    m_minMax = m_loadJob.getMinMax();
    m_loadStatus = m_loadJob.getResult();
    m_loadJob = null;
  }

  private Range<BigDecimal> getMinMax( )
  {
    /* Trigger load */
    getTriangulatedSurface();

    return m_minMax;
  }

  @Override
  public GM_Envelope getBoundingBox( )
  {
    final GM_TriangulatedSurface surface = getTriangulatedSurface();
    if( surface == null )
      return null;

    return surface.getEnvelope();
  }

  @Override
  public double getElevation( final GM_Point location )
  {
    final GM_TriangulatedSurface surface = getTriangulatedSurface();
    if( surface == null )
      return Double.NaN;

    return surface.getValue( location );
  }

  @Override
  public double getMinElevation( )
  {
    final Range<BigDecimal> minMax = getMinMax();

    if( minMax == null )
      return Double.NaN;

    return m_minMax.getMinimum().doubleValue();
  }

  @Override
  public double getMaxElevation( )
  {
    final Range<BigDecimal> minMax = getMinMax();

    if( minMax == null )
      return Double.NaN;

    return m_minMax.getMaximum().doubleValue();
  }

  URL getDataLocation( )
  {
    return m_dataLocation;
  }

  String getMimeType( )
  {
    return m_mimeType;
  }
}