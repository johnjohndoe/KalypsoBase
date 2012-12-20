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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.contribs.eclipse.core.runtime.AdapterUtils;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.IProfileSelectionProvider;
import org.kalypso.model.wspm.core.gml.WspmProject;
import org.kalypso.model.wspm.core.gml.WspmWaterBody;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ui.editor.gmleditor.part.FeatureAssociationTypeElement;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.IFeatureRelation;

/**
 * Helper class that extracts profiles from a selection.
 *
 * @author Gernot Belger
 */
public class ProfilesSelection
{
  private final Collection<IProfileFeature> m_foundProfiles = new LinkedHashSet<>();

  private final Collection<IProfileFeature> m_selectedProfiles = new LinkedHashSet<>();

  private CommandableWorkspace m_workspace;

  private Feature m_container = null;

  private FeatureList m_containingList = null;

  private final ISelection m_selection;

  /**
   * Mapping between found featurs and the really selected element (not always the feature itself, but sometimes the
   * ProfileReachSegment).
   */
  private final Map<IProfileFeature, Feature> m_profiles2Items = new HashMap<>();

  public ProfilesSelection( final ISelection selection )
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

    addSisters();
  }

  /**
   * Add all elements of the 'containingList' as (non-selected) sister elements to the found features.
   */
  private void addSisters( )
  {
    if( m_containingList != null )
    {
      for( final Object sister : m_containingList )
      {
        final IProfileFeature sisterProfile = AdapterUtils.getAdapter( sister, IProfileFeature.class );
        if( sisterProfile != null )
          addProfile( sisterProfile, sister );
      }
    }
  }

  private void addItem( final Object item )
  {
    if( m_workspace == null )
      m_workspace = AdapterUtils.getAdapter( item, CommandableWorkspace.class );

    if( m_workspace == null && m_selection instanceof IFeatureSelection && item instanceof Feature )
      m_workspace = ((IFeatureSelection)m_selection).getWorkspace( (Feature)item );

    final FeatureList featureList = AdapterUtils.getAdapter( item, FeatureList.class );
    if( featureList != null )
    {
      addFeatureProperty( featureList );
      return;
    }

    final FeatureAssociationTypeElement fate = AdapterUtils.getAdapter( item, FeatureAssociationTypeElement.class );
    if( fate != null )
    {
      addFeatureProperty( (FeatureAssociationTypeElement) item );
      return;
    }

    if( item instanceof WspmWaterBody )
    {
      addFeatureProperty( (IFeatureRelation)((WspmWaterBody)item).getProperty( WspmWaterBody.MEMBER_PROFILE ) );
      return;
    }

    final IProfileFeature profileFeature = AdapterUtils.getAdapter( item, IProfileFeature.class );
    if( profileFeature != null )
    {
      addProfileFeature( item, profileFeature );
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

  private void addProfileFeature( final Object item, final IProfileFeature profile )
  {
    m_selectedProfiles.add( profile );

    if( item instanceof Feature )
      m_profiles2Items.put( profile, (Feature) item );

    if( item instanceof Feature )
    {
      final Feature itemParent = ((Feature) item).getOwner();
      final IRelationType parentRelation = ((Feature) item).getParentRelation();
      setAsContainer( itemParent, parentRelation );
    }
    else
    {
      final IRelationType rt = profile.getParentRelation();
      final Feature container = profile.getOwner();
      setAsContainer( container, rt );
    }
  }

  private void setAsContainer( final Feature container, final IRelationType parentRelation )
  {
    m_container = container;

    if( parentRelation.isList() )
      m_containingList = (FeatureList) container.getProperty( parentRelation );
  }

  private void addProfile( final IProfileFeature profile, final Object item )
  {
    m_foundProfiles.add( profile );

    if( item instanceof Feature )
      m_profiles2Items.put( profile, (Feature) item );

    /* Set the first commandable workspace we find */
    if( m_workspace == null && m_selection instanceof IFeatureSelection )
    {
      m_workspace = ((IFeatureSelection) m_selection).getWorkspace( profile );
    }
  }

  private void addProfileSelectionProvider( final IProfileSelectionProvider item, final IRelationType selectionHint )
  {
    final IProfileFeature[] selectedProfiles = item.getSelectedProfiles( selectionHint );

    for( final IProfileFeature selectedProfile : selectedProfiles )
      addProfile( selectedProfile, null );

    m_selectedProfiles.addAll( Arrays.asList( selectedProfiles ) );
  }

  private void addFeatureProperty( final IFeatureRelation featureList )
  {
    final IRelationType rt = featureList.getPropertyType();
    final Feature parentFeature = featureList.getOwner();
    addParentFeature( parentFeature, rt );

    if( rt != null && WspmProject.QN_MEMBER_WATER_BODY.equals( rt.getQName() ) )
    {
      final List< ? > value = (List< ? >)featureList.getValue();
      for( final Object element : value )
        addItem( element );
    }
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

  /**
   * Returns the really (selected) element for the 'selected' profile.<br/>
   * Often, this is the profile itself, but sometimes it it a containing element (like ProfileReachSegment).
   */
  public Feature getItem( final IProfileFeature profile )
  {
    return m_profiles2Items.get( profile );
  }
}