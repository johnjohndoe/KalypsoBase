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

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.coverage.RangeSetFile;
import org.kalypsodeegree.model.elevation.IElevationModel;
import org.kalypsodeegree.model.elevation.IElevationModelProvider;
import org.kalypsodeegree.model.tin.ITin;
import org.kalypsodeegree_impl.gml.binding.commons.AbstractCoverage;

/**
 * Feature-Binding for gml:MultiSurfaceCoverage type.
 * 
 * @author Gernot Belger
 */
public class MultiSurfaceCoverage extends AbstractCoverage implements IElevationModelProvider
{
  public static final QName FEATURE_MULTI_SURFACE_COVERAGE = new QName( NS.GML3, "MultiSurfaceCoverage" ); //$NON-NLS-1$

  private TriangulatedSurfaceTin m_tin;

  public MultiSurfaceCoverage( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  public synchronized ITin getAsTin( )
  {
    if( m_tin == null )
    {
      final URL context = getWorkspace().getContext();

      final RangeSetFile rangeSet = getRangeSet();

      final String fileName = rangeSet.getFileName();
      final String mimeType = rangeSet.getMimeType();

      try
      {
        final URL dataLocation = new URL( context, fileName );

        // FIXME: when to dispose?
        m_tin = new TriangulatedSurfaceTin( dataLocation, mimeType )
        {
          @Override
          public synchronized void dispose( )
          {
            // REMARK: suppress dispose from outside
          }
        };
      }
      catch( final MalformedURLException e )
      {
        e.printStackTrace();
      }
    }

    return m_tin;
  }

  @Override
  public IElevationModel getElevationModel( )
  {
    return getAsTin();
  }
}