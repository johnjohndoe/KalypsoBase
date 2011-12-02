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
package org.kalypso.ogc.sensor.timeseries.wq.wechmann;

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.metadata.IMetadataConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;

/**
 * @author Dirk Kuch
 */
public class WQWechmannBoundaryHandler implements ICoreRunnableWithProgress
{

  private final MetadataList m_metadata;

  private final WechmannGroup m_group;

  public WQWechmannBoundaryHandler( final WechmannGroup group, final MetadataList metadata )
  {
    m_group = group;
    m_metadata = metadata;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final Double qMin = getQMin( m_group );
    if( Objects.isNotNull( qMin ) )
      m_metadata.setProperty( IMetadataConstants.WQ_BOUNDARY_Q_MIN, qMin.toString() );

    final Double qMax = getQMax( m_group );
    if( Objects.isNotNull( qMax ) )
      m_metadata.setProperty( IMetadataConstants.WQ_BOUNDARY_Q_MAX, qMax.toString() );

    final Double wMin = getWMin( m_group );
    if( Objects.isNotNull( wMin ) )
      m_metadata.setProperty( IMetadataConstants.WQ_BOUNDARY_W_MIN, wMin.toString() );

    final Double wMax = getWMax( m_group );
    if( Objects.isNotNull( wMax ) )
      m_metadata.setProperty( IMetadataConstants.WQ_BOUNDARY_W_MAX, wMax.toString() );

    return Status.OK_STATUS;
  }

  private Double getQMax( final WechmannGroup group )
  {
    Double ptr = null;

    final Iterator<WechmannSet> iterator = group.iterator();
    while( iterator.hasNext() )
    {
      final WechmannSet set = iterator.next();
      final WechmannParams parameter = set.getMax();

      final double max = parameter.getQ4WGR();
      ptr = Math.max( (Double) Objects.firstNonNull( ptr, max ), max );
    }

    return ptr;
  }

  private Double getQMin( final WechmannGroup group )
  {
    Double ptr = null;

    final Iterator<WechmannSet> iterator = group.iterator();
    while( iterator.hasNext() )
    {
      final WechmannSet set = iterator.next();
      final WechmannParams parameter = set.getMax();

      final Double min = parameter.getQ4W1();
      ptr = Math.min( (Double) Objects.firstNonNull( ptr, min ), min );
    }

    return ptr;
  }

  private Double getWMax( final WechmannGroup group )
  {
    Double ptr = null;

    final Iterator<WechmannSet> iterator = group.iterator();
    while( iterator.hasNext() )
    {
      final WechmannSet set = iterator.next();
      final WechmannParams parameter = set.getMax();

      final double max = parameter.getWGR();
      ptr = Math.max( (Double) Objects.firstNonNull( ptr, max ), max );
    }

    return ptr;
  }

  private Double getWMin( final WechmannGroup group )
  {
    Double ptr = null;

    final Iterator<WechmannSet> iterator = group.iterator();
    while( iterator.hasNext() )
    {
      final WechmannSet set = iterator.next();
      final WechmannParams parameter = set.getMax();

      final Double min = parameter.getW1();
      ptr = Math.min( (Double) Objects.firstNonNull( ptr, min ), min );
    }

    return ptr;
  }
}
