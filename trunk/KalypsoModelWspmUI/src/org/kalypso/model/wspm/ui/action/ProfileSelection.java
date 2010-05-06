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
package org.kalypso.model.wspm.ui.action;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.contribs.eclipse.core.runtime.AdapterUtils;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.IProfileSelectionProvider;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ui.editor.gmleditor.ui.FeatureAssociationTypeElement;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;

/**
 * Helper class that extracts profiles from a selection.
 * 
 * @author Gernot Belger
 */
public class ProfileSelection
{
  private final Collection<IProfileFeature> m_foundProfiles = new LinkedHashSet<IProfileFeature>();

  private final Collection<IProfileFeature> m_selectedProfiles = new LinkedHashSet<IProfileFeature>();

  private CommandableWorkspace m_workspace;

  private Feature m_container = null;

  private final ISelection m_selection;

  public ProfileSelection( final ISelection selection )
  {
    m_selection = selection;
    findProfiles();
  }

  private void findProfiles( )
  {
    if( !(m_selection instanceof IStructuredSelection) )
      return;

    final List< ? > items = ((IStructuredSelection) m_selection).toList();
    for( final Object item : items )
      addItem( item );
  }

  private void addItem( final Object item )
  {
    if( m_workspace == null )
      m_workspace = AdapterUtils.getAdapter( item, CommandableWorkspace.class );

    final FeatureList featureList = AdapterUtils.getAdapter( item, FeatureList.class );
    if( featureList != null )
    {
      addFeatureList( featureList );
      return;
    }

    final FeatureAssociationTypeElement fate = AdapterUtils.getAdapter( item, FeatureAssociationTypeElement.class );
    if( fate != null )
    {
      addFeatureAssociation( (FeatureAssociationTypeElement) item );
      return;
    }

    final IProfileFeature profileFeature = AdapterUtils.getAdapter( item, IProfileFeature.class );
    if( profileFeature != null )
    {
      addProfileFeature( profileFeature );
      return;
    }

    final IProfileSelectionProvider profileSelection = AdapterUtils.getAdapter( item, IProfileSelectionProvider.class );
    if( profileSelection != null )
    {
      m_container = AdapterUtils.getAdapter( item, Feature.class );
      addProfileSelectionProvider( profileSelection, null );
      return;
    }
  }

  private void addProfileFeature( final IProfileFeature profile )
  {
    m_selectedProfiles.add( profile );

    final IRelationType rt = (profile).getParentRelation();
    final Feature container = (profile).getOwner();
    m_container = container;
    if( rt.isList() )
    {
      final FeatureList sisters = (FeatureList) container.getProperty( rt );
      for( final Object sister : sisters )
      {
        if( sister instanceof IProfileFeature )
          addProfile( (IProfileFeature) sister );
      }
    }
  }

  private void addProfile( final IProfileFeature... profiles )
  {
    m_foundProfiles.addAll( Arrays.asList( profiles ) );

    /* Set the first commandable workspace we find */
    for( final IProfileFeature profile : profiles )
    {
      if( m_workspace != null )
        return;

      if( m_selection instanceof IFeatureSelection )
        m_workspace = ((IFeatureSelection) m_selection).getWorkspace( profile );
    }

  }

  private void addProfileSelectionProvider( final IProfileSelectionProvider item, final IRelationType selectionHint )
  {
    final IProfileFeature[] selectedProfiles = item.getSelectedProfiles( selectionHint );
    final List<IProfileFeature> asList = Arrays.asList( selectedProfiles );
    addProfile( selectedProfiles );
    m_selectedProfiles.addAll( asList );
  }

  private void addFeatureList( final FeatureList featureList )
  {
    final IRelationType rt = featureList.getParentFeatureTypeProperty();
    final Feature parentFeature = featureList.getParentFeature();
    addParentFeature( parentFeature, rt );
  }

  private void addFeatureAssociation( final FeatureAssociationTypeElement fate )
  {
    final IRelationType rt = fate.getAssociationTypeProperty();
    final Feature parentFeature = fate.getParentFeature();
    addParentFeature( parentFeature, rt );
  }

  private void addParentFeature( final Feature parentFeature, final IRelationType property )
  {
    if( parentFeature instanceof IProfileSelectionProvider )
    {
      addProfileSelectionProvider( (IProfileSelectionProvider) parentFeature, property );
      m_container = parentFeature;
    }
  }

  public boolean hasProfiles( )
  {
    return m_foundProfiles.size() > 0;
  }

  public IProfileFeature[] getProfiles( )
  {
    return m_foundProfiles.toArray( new IProfileFeature[m_foundProfiles.size()] );
  }

  public IProfileFeature[] getSelectedProfiles( )
  {
    return m_selectedProfiles.toArray( new IProfileFeature[m_selectedProfiles.size()] );
  }

  public CommandableWorkspace getWorkspace( )
  {
    return m_workspace;
  }

  public Feature getContainer( )
  {
    return m_container;
  }
}
