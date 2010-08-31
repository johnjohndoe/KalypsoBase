/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.gml.binding.commons;

import java.net.URL;

import ogc31.www.opengis.net.gml.FileType;
import ogc31.www.opengis.net.gml.FileValueModelType;

import org.kalypso.contribs.ogc31.KalypsoOGC31JAXBcontext;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;
import org.kalypsodeegree_impl.model.feature.IFeatureProviderFactory;

/**
 * @author Gernot Belger
 */
public class CoverageCollection extends Feature_Impl implements ICoverageCollection
{
  private IFeatureBindingCollection<ICoverage> m_coverages = null;

  public static ICoverageCollection createCoverageCollection( final URL context, final IFeatureProviderFactory providerFactory ) throws GMLSchemaException
  {
    GMLWorkspace workspace = FeatureFactory.createGMLWorkspace( ICoverageCollection.QNAME, context, providerFactory );
    return (ICoverageCollection) workspace.getRootFeature();
  }

  public CoverageCollection( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  @Override
  public synchronized IFeatureBindingCollection<ICoverage> getCoverages( )
  {
    if( m_coverages == null )
      m_coverages = new FeatureBindingCollection<ICoverage>( this, ICoverage.class, QNAME_PROP_COVERAGE_MEMBER );

    return m_coverages;
  }

  public static ICoverage addCoverage( final ICoverageCollection coverages, final RectifiedGridDomain domain, final String externalResource, final String mimeType )
  {
    final FileType rangeSetFile = KalypsoOGC31JAXBcontext.GML3_FAC.createFileType();

    // file name relative to the gml
    rangeSetFile.setFileName( externalResource );
    rangeSetFile.setMimeType( mimeType );
    rangeSetFile.setFileStructure( FileValueModelType.RECORD_INTERLEAVED );

    final RectifiedGridCoverage coverage = (RectifiedGridCoverage) coverages.getCoverages().addNew( RectifiedGridCoverage.QNAME );

    coverage.setDescription( "Imported via Kalypso" );
    coverage.setGridDomain( domain );
    coverage.setRangeSet( rangeSetFile );

    coverage.setEnvelopesUpdated();

    return coverage;
  }
}