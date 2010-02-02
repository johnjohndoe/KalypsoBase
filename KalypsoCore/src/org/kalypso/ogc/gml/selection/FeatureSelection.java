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
package org.kalypso.ogc.gml.selection;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.StructuredSelection;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public class FeatureSelection extends AbstractFeatureSelection
{
  private final EasyFeatureWrapper[] m_wrappers;

  private final Map<Feature, EasyFeatureWrapper> m_featureHash = new HashMap<Feature, EasyFeatureWrapper>();

  private final IFeatureSelectionManager m_selectionManager;

  public FeatureSelection( final IFeatureSelectionManager selectionManager, final EasyFeatureWrapper[] wrappers )
  {
    super( new StructuredSelection( wrappers ) );
    m_selectionManager = selectionManager;
    m_wrappers = wrappers;

    for( final EasyFeatureWrapper wrapper : wrappers )
      m_featureHash.put( wrapper.getFeature(), wrapper );
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getAllFeatures()
   */
  @Override
  public EasyFeatureWrapper[] getAllFeatures( )
  {
    return m_wrappers;
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getFocusedFeature()
   */
  @Override
  public Feature getFocusedFeature( )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getFocusedProperty()
   */
  @Override
  public IPropertyType getFocusedProperty( )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getParentFeature(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public Feature getParentFeature( final Feature feature )
  {
    final EasyFeatureWrapper wrapper = m_featureHash.get( feature );
    if( wrapper == null )
      return null;

    return wrapper.getParentFeature();
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getParentFeatureProperty(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public IRelationType getParentFeatureProperty( final Feature feature )
  {
    final EasyFeatureWrapper wrapper = m_featureHash.get( feature );
    if( wrapper == null )
      return null;

    return wrapper.getParentFeatureProperty();
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getSelectionManager()
   */
  @Override
  public IFeatureSelectionManager getSelectionManager( )
  {
    return m_selectionManager;
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getWorkspace(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public CommandableWorkspace getWorkspace( final Feature feature )
  {
    final EasyFeatureWrapper wrapper = m_featureHash.get( feature );
    if( wrapper == null )
      return null;

    return wrapper.getWorkspace();
  }

}
