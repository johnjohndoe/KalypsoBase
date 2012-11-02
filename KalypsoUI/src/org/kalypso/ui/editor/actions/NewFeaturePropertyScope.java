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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.IFeatureRelation;

/**
 * @author Gernot Belger
 */
class NewFeaturePropertyScope implements INewScope
{
  private final IRelationType m_targetRelation;

  private final Feature m_parentFeature;

  private final CommandableWorkspace m_workspace;

  private final IFeatureSelectionManager m_selectionManager;

  NewFeaturePropertyScope( final IFeatureRelation property, final CommandableWorkspace workspace, final IFeatureSelectionManager selectionManager )
  {
    this( property.getOwner(), property.getPropertyType(), workspace, selectionManager );
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

    final IAction[] actions = createActions();
    for( final IAction action : actions )
      newMenuManager.add( action );
  }

  @Override
  public IAction[] createActions( )
  {
    final Collection<IAction> actions = new ArrayList<>();

    if( !checkFullList( actions ) )
      return new IAction[0];

    final IFeatureType featureType = m_targetRelation.getTargetFeatureType();
    if( featureType == null )
      return new IAction[0];

    // REMARK: using workspaces context schema here;
    final IGMLSchema contextSchema = m_workspace.getGMLSchema();

    final IFeatureType[] featureTypes = GMLSchemaUtilities.getSubstituts( featureType, contextSchema, false, true );
    for( final IFeatureType ft : featureTypes )
      actions.add( new NewFeatureAction( m_workspace, m_parentFeature, m_targetRelation, ft, m_selectionManager ) );

    return actions.toArray( new IAction[actions.size()] );
  }

  private boolean checkFullList( final Collection<IAction> actions )
  {
    if( !m_targetRelation.isList() )
      return true;

    final List< ? > list = (List< ? >)m_parentFeature.getProperty( m_targetRelation );
    if( list == null )
      return true;

    final int maxOccurs = m_targetRelation.getMaxOccurs();
    if( maxOccurs == -1 || list.size() < maxOccurs )
      return true;

    // Add an action which indicates, that the list is full
    actions.add( new ListFullAction( maxOccurs ) );
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
}
