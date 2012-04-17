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

import java.util.Date;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.metadata.IMetadataConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;

/**
 * @author Dirk Kuch
 */
public class WQWechmannBoundaryHandler implements ICoreRunnableWithProgress
{

  private final MetadataList m_metadata;

  private final WechmannGroup m_group;

  private final DateRange m_dateRange;

  public WQWechmannBoundaryHandler( final WechmannGroup group, final MetadataList metadata, final DateRange dateRange )
  {
    m_group = group;
    m_metadata = metadata;
    m_dateRange = dateRange;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final WechmannSet set = findSet();
    if( Objects.isNull( set ) )
      return Status.CANCEL_STATUS;

    final Double qMin = getQMin( set );
    if( Objects.isNotNull( qMin ) )
      m_metadata.setProperty( IMetadataConstants.WQ_BOUNDARY_Q_MIN, qMin.toString() );

    final Double qMax = getQMax( set );
    if( Objects.isNotNull( qMax ) )
      m_metadata.setProperty( IMetadataConstants.WQ_BOUNDARY_Q_MAX, qMax.toString() );

    final Double wMin = getWMin( set );
    if( Objects.isNotNull( wMin ) )
      m_metadata.setProperty( IMetadataConstants.WQ_BOUNDARY_W_MIN, wMin.toString() );

    final Double wMax = getWMax( set );
    if( Objects.isNotNull( wMax ) )
      m_metadata.setProperty( IMetadataConstants.WQ_BOUNDARY_W_MAX, wMax.toString() );

    return Status.OK_STATUS;
  }

  private WechmannSet findSet( )
  {
    final long requestFrom = m_dateRange.getFrom().getTime();

    double diff = Double.MAX_VALUE;
    WechmannSet ptr = null;

    final Iterator<WechmannSet> iterator = m_group.iterator();
    while( iterator.hasNext() )
    {
      final WechmannSet set = iterator.next();
      final Date validity = set.getValidity();

      final double d = Math.abs( validity.getTime() - requestFrom );
      if( d < diff )
      {
        diff = d;
        ptr = set;
      }
    }

    return ptr;
  }

  private Double getQMax( final WechmannSet set )
  {
    final WechmannParams parameter = set.getMax();

    return parameter.getQ4WGR();
  }

  private Double getQMin( final WechmannSet set )
  {
    final WechmannParams parameter = set.getMin();

    return parameter.getQ4W1();
  }

  private Double getWMax( final WechmannSet set )
  {
    final WechmannParams parameter = set.getMax();

    return parameter.getWGR();
  }

  private Double getWMin( final WechmannSet set )
  {
    final WechmannParams parameter = set.getMin();

    return parameter.getW1();
  }
}
