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
package org.kalypso.model.wspm.ui.profil.wizard.intersectRoughness;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.assignment.AssignmentBinder;
import org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter;
import org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.worker.IApplyLanduseData;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Dirk Kuch
 */
public class IntersectRoughnessesLanduseDelegate implements IApplyLanduseData
{

  private final IProfilePointFilter m_pointFilters;

  private final FeatureList m_polygoneFeatures;

  private final IPropertyType m_polygoneGeomType;

  private final IPropertyType m_polygoneValueType;

  private final IProfileFeature[] m_profiles;

  private final AssignmentBinder m_assignment;

  public IntersectRoughnessesLanduseDelegate( final IntersectRoughnessPage page, final IProfileFeature[] profiles ) throws Exception
  {
    m_profiles = profiles;
    m_pointFilters = page.getSelectedPointFilter();
    m_polygoneFeatures = page.getPolygoneFeatures();
    m_polygoneGeomType = page.getPolygoneGeomProperty();
    m_polygoneValueType = page.getPolygoneValueProperty();

    /* Load assignment */
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IPath assignmentPath = page.getAssignmentPath();
    final IFile assignmentFile = workspace.getRoot().getFile( assignmentPath );
    final URL assignmentUrl = ResourceUtilities.createURL( assignmentFile );

    final GMLWorkspace assignmentWorkspace = GmlSerializer.createGMLWorkspace( assignmentUrl, null );
    m_assignment = new AssignmentBinder( assignmentWorkspace );
  }

  @Override
  public IProfileFeature[] getProfiles( )
  {
    return m_profiles;
  }

  @Override
  public IProfilePointFilter getFilter( )
  {
    return m_pointFilters;
  }

  @Override
  public FeatureList getPolyonFeatureList( )
  {
    return m_polygoneFeatures;
  }

  @Override
  public IPropertyType getGeometryPropertyType( )
  {
    return m_polygoneGeomType;
  }

  @Override
  public IPropertyType getValuePropertyType( )
  {
    return m_polygoneValueType;
  }

  @Override
  public Map<String, Object> getAssignmentsFor( final String string )
  {
    return m_assignment.getAssignmentsFor( string );
  }
}
