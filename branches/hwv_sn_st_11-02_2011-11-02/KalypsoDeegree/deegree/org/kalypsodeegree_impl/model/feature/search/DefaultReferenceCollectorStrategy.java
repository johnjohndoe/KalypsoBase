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
package org.kalypsodeegree_impl.model.feature.search;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IDocumentReference;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.XLinkedFeature_Impl;
import org.kalypsodeegree_impl.model.feature.visitors.CollectorVisitor;
import org.kalypsodeegree_impl.model.feature.visitors.FeatureSubstitutionVisitor;

/**
 * Default implementation of {@link IReferenceCollectorStrategy}, represents the old generic way to find the references.<br/>
 * This strategy is used, if nothing else is specified.
 * 
 * @author Gernot Belger
 */
public class DefaultReferenceCollectorStrategy implements IReferenceCollectorStrategy
{
  private final GMLWorkspace m_workspace;

  private final Feature m_parentFeature;

  private final IRelationType m_parentRelation;

  public DefaultReferenceCollectorStrategy( final GMLWorkspace workspace, final Feature parentFeature, final IRelationType parentRelation )
  {
    m_workspace = workspace;
    m_parentFeature = parentFeature;
    m_parentRelation = parentRelation;
  }

  @Override
  public Feature[] collectReferences( )
  {
    final IFeatureType targetFeatureType = m_parentRelation.getTargetFeatureType();

    final List<Feature> foundFeatures = new ArrayList<Feature>();

    final IDocumentReference[] refs = m_parentRelation.getDocumentReferences();
    for( final IDocumentReference ref : refs )
      collectFromDocument( targetFeatureType, foundFeatures, ref );

    /* Special case: if we already have a link, we may guess the referenced document */
    final Object value = m_parentFeature.getProperty( m_parentRelation );
    if( value instanceof XLinkedFeature_Impl )
    {
      final XLinkedFeature_Impl xlink = (XLinkedFeature_Impl) value;
      final String href = xlink.getHref();
      final int indexOfHref = href.indexOf( '#' );
      final String uri = indexOfHref == -1 ? null : href.substring( 0, indexOfHref );
      final Feature linkedFeature = xlink.getFeature();
      if( linkedFeature != null && uri != null )
        collectFromWorkspace( targetFeatureType, foundFeatures, uri, linkedFeature.getWorkspace() );
    }

    return foundFeatures.toArray( new Feature[foundFeatures.size()] );
  }

  private void collectFromDocument( final IFeatureType targetFeatureType, final List<Feature> foundFeatures, final IDocumentReference ref )
  {
    final String uri = ref.getReference();

    final GMLWorkspace workspace = resolveWorkspace( ref, uri );

    collectFromWorkspace( targetFeatureType, foundFeatures, uri, workspace );
  }

  private GMLWorkspace resolveWorkspace( final IDocumentReference ref, final String uri )
  {
    if( ref == IDocumentReference.SELF_REFERENCE )
      return m_workspace;

    return m_workspace.getLinkedWorkspace( uri );
  }

  private void collectFromWorkspace( final IFeatureType targetFeatureType, final List<Feature> foundFeatures, final String uri, final GMLWorkspace workspace )
  {
    if( workspace == null )
      return;

    final CollectorVisitor collectorVisitor = new CollectorVisitor();
    final FeatureVisitor fv = new FeatureSubstitutionVisitor( collectorVisitor, targetFeatureType );

    workspace.accept( fv, workspace.getRootFeature(), FeatureVisitor.DEPTH_INFINITE );

    final Feature[] features = collectorVisitor.getResults( true );
    for( final Feature feature : features )
    {
      if( workspace == m_workspace )
        foundFeatures.add( feature );
      else
      {
        final String id = feature.getId();
        // REMARK: id == null happens for xlinks, i.e. twice decending into xlinks does not work....
        if( id != null )
        {
          final String href = uri + "#" + id; //$NON-NLS-1$
          final XLinkedFeature_Impl linkedFeature = new XLinkedFeature_Impl( m_parentFeature, m_parentRelation, targetFeatureType, href, "", "", "", "", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
          foundFeatures.add( linkedFeature );
        }
      }
    }
  }
}