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
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.IProfileSelectionProvider;
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
  private final IFeatureSelection m_selection;

  private final Collection<IProfileFeature> m_foundProfiles = new LinkedHashSet<IProfileFeature>();

  private final Collection<IProfileFeature> m_selectedProfiles = new LinkedHashSet<IProfileFeature>();

  public ProfileSelection( final ISelection selection )
  {
    m_selection = selection instanceof IFeatureSelection ? (IFeatureSelection) selection : null;

    findProfiles();
  }

  private void findProfiles( )
  {
    if( m_selection == null )
      return;

    final List< ? > items = m_selection.toList();
    for( final Object item : items )
      addItem( item );
  }

  private void addItem( final Object item )
  {
    if( item instanceof FeatureAssociationTypeElement )
      addFeatureAssociation( (FeatureAssociationTypeElement) item );
    else if( item instanceof IProfileFeature )
      addProfileFeature( (IProfileFeature) item );
    else if( item instanceof IProfileSelectionProvider )
      addProfileSelectionProvider( (IProfileSelectionProvider) item, null );
  }

  private void addProfileFeature( final IProfileFeature profile )
  {
    m_selectedProfiles.add( profile );

    final IRelationType rt = (profile).getParentRelation();
    final Feature container = (profile).getOwner();
    if( rt.isList() )
    {
      final FeatureList sisters = (FeatureList) container.getProperty( rt );
      for( final Object sister : sisters )
      {
        if( sister instanceof IProfileFeature )
          m_foundProfiles.add( (IProfileFeature) sister );
      }
    }
  }

  private void addProfileSelectionProvider( final IProfileSelectionProvider item, final IRelationType selectionHint )
  {
    final IProfileFeature[] selectedProfiles = item.getSelectedProfiles( selectionHint );
    final List<IProfileFeature> asList = Arrays.asList( selectedProfiles );
    m_foundProfiles.addAll( asList );
    m_selectedProfiles.addAll( asList );
  }

  private void addFeatureAssociation( final FeatureAssociationTypeElement fate )
  {
    final IRelationType rt = fate.getAssociationTypeProperty();
    final Feature parentFeature = fate.getParentFeature();
    if( parentFeature instanceof IProfileSelectionProvider )
      addProfileSelectionProvider( (IProfileSelectionProvider) parentFeature, rt );
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

}
