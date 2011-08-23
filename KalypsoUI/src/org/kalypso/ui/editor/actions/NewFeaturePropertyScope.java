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
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.core.catalog.FeatureTypePropertiesCatalog;
import org.kalypso.core.catalog.IFeatureTypePropertiesConstants;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ui.editor.gmleditor.command.AddFeatureCommand;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.IFeatureProperty;

/**
 * @author Gernot Belger
 */
class NewFeaturePropertyScope implements INewScope
{
  private final IRelationType m_targetRelation;

  private final Feature m_parentFeature;

  private final CommandableWorkspace m_workspace;

  private final IFeatureSelectionManager m_selectionManager;

  NewFeaturePropertyScope( final IFeatureProperty property, final CommandableWorkspace workspace, final IFeatureSelectionManager selectionManager )
  {
    this( property.getParentFeature(), property.getPropertyType(), workspace, selectionManager );
  }

  NewFeaturePropertyScope( final Feature parentFeature, final IRelationType targetRelation, final CommandableWorkspace workspace, final IFeatureSelectionManager selectionManager )
  {
    m_selectionManager = selectionManager;
    m_targetRelation = targetRelation;
    m_parentFeature = parentFeature;
    m_workspace = workspace;
  }

  private boolean isValid( )
  {
    return m_targetRelation != null && m_workspace != null;
  }

  @Override
  public IMenuManager createMenu( )
  {
    final IMenuManager newMenuManager = new MenuManager( Messages.getString( "org.kalypso.ui.editor.actions.FeatureActionUtilities.7" ) ); //$NON-NLS-1$
    addMenuItems( newMenuManager );
    return newMenuManager;
  }

  @Override
  public void addMenuItems( final IMenuManager newMenuManager )
  {
    addMenuItemWithoutAddition( newMenuManager );

    newMenuManager.add( new Separator( "additions" ) ); //$NON-NLS-1$
  }

  void addMenuItemWithoutAddition( final IMenuManager newMenuManager )
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
    if( Objects.isNull( featureType ) )
      return;

    final IGMLSchema contextSchema = m_parentFeature.getWorkspace().getGMLSchema();
    final IFeatureType[] featureTypes = GMLSchemaUtilities.getSubstituts( featureType, contextSchema, false, true );
    for( final IFeatureType ft : featureTypes )
      newMenuManager.add( new NewFeatureAction( this, ft ) );

    /* Not yet implemented, makes no sense to show it for now ... */
    // newMenuManager.add( new NewFeatureFromExternalSchemaAction() );
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

  void createNewFeature( final IFeatureType featureType ) throws Exception
  {
    final Properties uiProperties = FeatureTypePropertiesCatalog.getProperties( m_workspace.getContext(), featureType.getQName() );
    final String depthStr = uiProperties.getProperty( IFeatureTypePropertiesConstants.FEATURE_CREATION_DEPTH );
    final int depth = NumberUtils.parseQuietInt( depthStr, 0 );

    final ICommand command = new AddFeatureCommand( m_workspace, featureType, m_parentFeature, m_targetRelation, 0, null, m_selectionManager, depth );
    m_workspace.postCommand( command );
  }
}
