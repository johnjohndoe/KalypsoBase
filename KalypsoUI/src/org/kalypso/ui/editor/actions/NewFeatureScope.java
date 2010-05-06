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
package org.kalypso.ui.editor.actions;

import java.util.List;
import java.util.Properties;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.commons.command.ICommand;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ui.catalogs.FeatureTypePropertiesCatalog;
import org.kalypso.ui.catalogs.IFeatureTypePropertiesConstants;
import org.kalypso.ui.editor.gmleditor.ui.FeatureAssociationTypeElement;
import org.kalypso.ui.editor.gmleditor.util.command.AddFeatureCommand;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;

/**
 * @author Gernot Belger
 */
public class NewFeatureScope
{
  private final IRelationType m_targetRelation;

  private final Feature m_parentFeature;

  private final CommandableWorkspace m_workspace;

  private final IFeatureSelectionManager m_selectionManager;

  public NewFeatureScope( final CommandableWorkspace workspace, final FeatureList featureList, final IFeatureSelectionManager selectionManager )
  {
    m_selectionManager = selectionManager;
    m_targetRelation = featureList.getParentFeatureTypeProperty();
    m_parentFeature = featureList.getParentFeature();
    m_workspace = workspace;
  }

  public NewFeatureScope( final FeatureAssociationTypeElement fate, final CommandableWorkspace workspace, final IFeatureSelectionManager selectionManager )
  {
    m_selectionManager = selectionManager;
    m_targetRelation = fate.getAssociationTypeProperty();
    m_parentFeature = fate.getParentFeature();
    m_workspace = workspace;
  }

  public NewFeatureScope( final IFeatureSelection selection, final IFeatureSelectionManager selectionManager )
  {
    m_selectionManager = selectionManager;

    final Feature feature = FeatureSelectionHelper.getFirstFeature( selection );

    if( feature == null )
    {
      m_parentFeature = null;
      m_targetRelation = null;
      m_workspace = null;
    }
    else
    {
      m_parentFeature = feature.getOwner();
      m_targetRelation = feature.getParentRelation();
      m_workspace = selectionManager.getWorkspace( feature );
    }
  }

  public IRelationType getTargetRelation( )
  {
    return m_targetRelation;
  }

  public boolean isValid( )
  {
    return m_targetRelation != null && m_workspace != null;
  }

  public IMenuManager createMenu( )
  {
    final IMenuManager newMenuManager = new MenuManager( Messages.getString( "org.kalypso.ui.editor.actions.FeatureActionUtilities.7" ) ); //$NON-NLS-1$
    addMenuItems( newMenuManager );
    return newMenuManager;
  }

  private void addMenuItems( final IMenuManager newMenuManager )
  {
    if( !isValid() )
      return;

    if( !hasInlinableFeatures() )
      return;

    if( !checkSingleInlinedFeature() )
      return;

    if( !checkFullList( newMenuManager ) )
      return;

    final IFeatureType featureType = m_targetRelation.getTargetFeatureType();

    final IGMLSchema contextSchema = m_parentFeature.getWorkspace().getGMLSchema();
    final IFeatureType[] featureTypes = GMLSchemaUtilities.getSubstituts( featureType, contextSchema, false, true );
    for( final IFeatureType ft : featureTypes )
      newMenuManager.add( new NewFeatureAction( this, ft ) );

    newMenuManager.add( new Separator( "additions" ) ); //$NON-NLS-1$
    newMenuManager.add( new NewFeatureFromExternalSchemaAction() );
  }

  private boolean checkFullList( final IMenuManager newMenuManager )
  {
    if( !m_targetRelation.isList() )
      return true;

    final List< ? > list = (List< ? >) m_parentFeature.getProperty( m_targetRelation );
    if( list == null )
      return true;

    final int maxOccurs = m_targetRelation.getMaxOccurs();
    if( maxOccurs == -1 || list.size() < maxOccurs )
      return true;

    // Add an action which indicates, that the list is full
    newMenuManager.add( new ListFullAction( maxOccurs ) );
    return false;
  }

  private boolean checkSingleInlinedFeature( )
  {
    /* Direct properties (maxoccurs = 1) can only be added if not already there. */
    return m_targetRelation.isList() || m_parentFeature.getProperty( m_targetRelation ) == null;
  }

  private boolean hasInlinableFeatures( )
  {
    return m_targetRelation.isInlineAble();
  }

  public void createNewFeature( final IFeatureType featureType ) throws Exception
  {
    final Properties uiProperties = FeatureTypePropertiesCatalog.getProperties( m_workspace.getContext(), featureType.getQName() );
    final String depthStr = uiProperties.getProperty( IFeatureTypePropertiesConstants.FEATURE_CREATION_DEPTH, IFeatureTypePropertiesConstants.FEATURE_CREATION_DEPTH_DEFAULT );
    final int depth = Integer.parseInt( depthStr );

    final ICommand command = new AddFeatureCommand( m_workspace, featureType, m_parentFeature, m_targetRelation, 0, null, m_selectionManager, depth );
    m_workspace.postCommand( command );
  }

  /**
   * Bit HACKY: used for the GML-Tree to create the correct New-Scope.
   */
  public static NewFeatureScope createFromTreeSelection( final CommandableWorkspace workspace, final IStructuredSelection selection, final IFeatureSelectionManager selectionManager )
  {
    final Object elementInScope = selection.getFirstElement();

    if( elementInScope instanceof FeatureAssociationTypeElement )
      return new NewFeatureScope( (FeatureAssociationTypeElement) elementInScope, workspace, selectionManager );

    if( selection instanceof IFeatureSelection )
      return new NewFeatureScope( (IFeatureSelection) selection, selectionManager );

    return null;
  }

}
