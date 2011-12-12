/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.model.wspm.ui.profil.wizard.classification.landuse;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter;
import org.kalypso.model.wspm.ui.profil.wizard.ProfilesChooserPage;
import org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.model.ApplyLanduseShapeModel;
import org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.worker.IApplyLanduseData;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.ILanduseModel;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.LanduseProperties;
import org.kalypso.ogc.gml.serialize.ShapeWorkspace;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;

/**
 * @author Dirk Kuch
 */
public class ApplyLanduseDelegate implements IApplyLanduseData
{

  private final ILanduseModel m_model;

  private final ShapeWorkspace m_workspace;

  private final ProfilesChooserPage m_profilePage;

  private IPropertyType m_value;

  public ApplyLanduseDelegate( final ILanduseModel model, final ProfilesChooserPage profilePage ) throws Exception
  {
    m_model = model;
    m_profilePage = profilePage;

    final IFile file = model.getLanduseShape();
    m_workspace = new ShapeWorkspace( file.getLocation().toFile() );
  }

  /**
   * @see org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.worker.IApplyLanduseData#getProfiles()
   */
  @Override
  public IProfileFeature[] getProfiles( )
  {
    return (IProfileFeature[]) m_profilePage.getChoosen();
  }

  /**
   * @see org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.worker.IApplyLanduseData#getFilter()
   */
  @Override
  public IProfilePointFilter getFilter( )
  {
    final ApplyLanduseShapeModel model = (ApplyLanduseShapeModel) m_model;

    return model.getFilter();
  }

  @Override
  public FeatureList getPolyonFeatureList( )
  {
    return m_workspace.getFeatureList();
  }

  @Override
  public IPropertyType getGeometryPropertyType( )
  {
    return m_workspace.getGeometryPropertyType();
  }

  /**
   * @see org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.worker.IApplyLanduseData#getValuePropertyType()
   */
  @Override
  public IPropertyType getValuePropertyType( )
  {
    if( Objects.isNotNull( m_value ) )
      return m_value;

    final FeatureList list = getPolyonFeatureList();
    final Feature root = (Feature) list.get( 0 );
    final IFeatureType type = root.getFeatureType();
    final IPropertyType[] properties = type.getProperties();

    final String column = m_model.getShapeColumn().getName();

    for( final IPropertyType property : properties )
    {
      final QName qn = property.getQName();
      final String localPart = qn.getLocalPart();

      if( StringUtils.equals( column, localPart ) )
      {
        m_value = property;

        return m_value;
      }
    }

    return null;
  }

  @Override
  public Map<String, Object> getAssignmentsFor( final String key )
  {
    final LanduseProperties mapping = m_model.getMapping();
    final String property = mapping.getProperty( key );

    final Map<String, Object> entries = new HashMap<String, Object>();
    if( Strings.isNotEmpty( property ) )
    {
      final String type = m_model.getType();
      entries.put( type, property );
    }

    return entries;
  }

  public void dispose( )
  {
    m_workspace.dispose();
  }

}