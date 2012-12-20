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
package org.kalypsodeegree_impl.gml.binding.commons;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.coverage.RangeSetFile;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * Base implementation of gml:_Coverage
 * 
 * @author Holger Albert
 * @author Gernot Belger
 */
public class AbstractCoverage extends Feature_Impl implements ICoverage
{
  private static final QName QNAME_PROP_RANGE_SET = new QName( NS.GML3, "rangeSet" ); //$NON-NLS-1$

  public AbstractCoverage( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  /**
   * @return Returns the rangeSet. Can be one of {@link RangeSetFile}; TODO: support others
   */
  @Override
  public RangeSetFile getRangeSet( )
  {
    return getProperty( QNAME_PROP_RANGE_SET, RangeSetFile.class );
  }

  @Override
  public void setRangeSet( final Object rangeSet )
  {
    if( !(rangeSet instanceof RangeSetFile) )
      throw new IllegalArgumentException();

    setProperty( QNAME_PROP_RANGE_SET, rangeSet );
  }

  // TODO: find a general solution within the feature API
  @Override
  public GM_Envelope getBoundedBy( )
  {
    try
    {
      final GM_Envelope property = getProperty( QN_BOUNDED_BY, GM_Envelope.class );
      final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
      return geoTransformer.transform( property );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }
}