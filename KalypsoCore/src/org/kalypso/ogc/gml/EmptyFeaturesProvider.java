/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.ogc.gml;

import java.util.Collections;
import java.util.List;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;

/**
 * @author Gernot Belger
 */
public class EmptyFeaturesProvider extends AbstractFeaturesProvider
{
  private final String m_featurePath;

  public EmptyFeaturesProvider( final String featurePath )
  {
    m_featurePath = featurePath;
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getFeaturePath()
   */
  @Override
  public String getFeaturePath( )
  {
    return m_featurePath;
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getFeatureList()
   */
  @Override
  public FeatureList getFeatureList( )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getFeatureType()
   */
  @Override
  public IFeatureType getFeatureType( )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getFeatures()
   */
  @Override
  public List<Feature> getFeatures( )
  {
    return Collections.emptyList();
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProvider#getWorkspace()
   */
  @Override
  public CommandableWorkspace getWorkspace( )
  {
    return null;
  }

}
