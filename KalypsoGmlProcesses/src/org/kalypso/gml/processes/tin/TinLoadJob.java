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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.gml.processes.KalypsoGmlProcessesPlugin;
import org.kalypso.gml.processes.i18n.Messages;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree.model.geometry.MinMaxSurfacePatchVisitor;

/**
 * Helper that loads the tin in a job.
 * 
 * @author Gernot Belger
 */
public class TinLoadJob extends Job
{
  private final TriangulatedSurfaceTin m_triangulatedSurfaceTin;

  private TriangulatedSurfaceFeature m_surface;

  private Range<BigDecimal> m_minMax;

  public TinLoadJob( final TriangulatedSurfaceTin triangulatedSurfaceTin )
  {
    super( "TIN Loader" ); //$NON-NLS-1$

    m_triangulatedSurfaceTin = triangulatedSurfaceTin;

    setUser( false );
  }

  @Override
  protected IStatus run( final IProgressMonitor monitor )
  {
    final URL dataLocation = m_triangulatedSurfaceTin.getDataLocation();
    final String mimeType = m_triangulatedSurfaceTin.getMimeType();

    try
    {
      Assert.isTrue( "application/gml+xml".equals( mimeType ) ); //$NON-NLS-1$

      final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( dataLocation, null, monitor );

      m_surface = (TriangulatedSurfaceFeature) workspace.getRootFeature();

      /* Determine min/max */
      final GM_TriangulatedSurface triangulatedSurface = m_surface.getTriangulatedSurface();

      final MinMaxSurfacePatchVisitor<GM_Triangle> minMaxVisitor = new MinMaxSurfacePatchVisitor<>();
      final GM_Envelope surfaceEnvelope = m_surface.getEnvelope();
      triangulatedSurface.acceptSurfacePatches( surfaceEnvelope, minMaxVisitor, new NullProgressMonitor() );

      final BigDecimal min = minMaxVisitor.getMin();
      final BigDecimal max = minMaxVisitor.getMax();

      m_minMax = Range.between( min, max );

      return Status.OK_STATUS;
    }
    catch( final Exception e )
    {
      final String message = String.format( Messages.getString("TinLoadJob_0"), dataLocation ); //$NON-NLS-1$
      return new Status( IStatus.ERROR, KalypsoGmlProcessesPlugin.PLUGIN_ID, message, e );
    }
  }

  public TriangulatedSurfaceFeature getSurface( )
  {
    return m_surface;
  }

  public Range<BigDecimal> getMinMax( )
  {
    return m_minMax;
  }
}