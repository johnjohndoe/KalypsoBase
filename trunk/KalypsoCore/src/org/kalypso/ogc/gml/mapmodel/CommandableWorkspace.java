/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.mapmodel;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.NamespaceContext;

import org.kalypso.commons.command.DefaultCommandManager;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandManager;
import org.kalypso.commons.command.ICommandManagerListener;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;
import org.kalypsodeegree_impl.model.feature.GMLWorkspace_Impl;
import org.kalypsodeegree_impl.model.feature.IFeatureProviderFactory;

/**
 * Decorator über einen Workspace, der diesen um die Fähigkeiten eines
 * {@link org.kalypso.commons.command.ICommandManager ICommandManagers}erweitert
 *
 * @author Gernot Belger
 */
public class CommandableWorkspace implements GMLWorkspace, ICommandManager
{
  private final GMLWorkspace_Impl m_workspace;

  private final ICommandManager m_commandManager = new DefaultCommandManager();

  public CommandableWorkspace( final GMLWorkspace workspace )
  {
    /**
     * it does not make sence decorate something else than the real workspace <br>
     * the UML looks also nicer without recursive dependencies here
     */
    m_workspace = (GMLWorkspace_Impl) workspace;
  }

  /**
   * @see org.kalypsodeegree.model.feature.event.ModellEventProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    m_workspace.dispose();
  }

  @Override
  public void addCommandManagerListener( final ICommandManagerListener l )
  {
    m_commandManager.addCommandManagerListener( l );
  }

  @Override
  public boolean canRedo( )
  {
    return m_commandManager.canRedo();
  }

  @Override
  public boolean canUndo( )
  {
    return m_commandManager.canUndo();
  }

  @Override
  public String getRedoDescription( )
  {
    return m_commandManager.getRedoDescription();
  }

  @Override
  public String getUndoDescription( )
  {
    return m_commandManager.getUndoDescription();
  }

  /**
   * @see org.kalypso.commons.command.ICommandManager#postCommand(org.kalypso.commons.command.ICommand)
   */
  @Override
  public void postCommand( final ICommand command ) throws Exception
  {
    m_commandManager.postCommand( command );
  }

  @Override
  public void redo( ) throws Exception
  {
    m_commandManager.redo();
  }

  @Override
  public void removeCommandManagerListener( final ICommandManagerListener l )
  {
    m_commandManager.removeCommandManagerListener( l );
  }

  @Override
  public void undo( ) throws Exception
  {
    m_commandManager.undo();
  }

  @Override
  public void accept( final FeatureVisitor fv, final Feature feature, final int depth )
  {
    m_workspace.accept( fv, feature, depth );
  }

  @Override
  public void accept( final FeatureVisitor fv, final int depth )
  {
    m_workspace.accept( fv, depth );
  }

  @Override
  public void accept( final FeatureVisitor fv, final List< ? > features, final int depth )
  {
    m_workspace.accept( fv, features, depth );
  }

  @Override
  public void addModellListener( final ModellEventListener listener )
  {
    m_workspace.addModellListener( listener );
  }

  @Override
  public void fireModellEvent( final ModellEvent event )
  {
    m_workspace.fireModellEvent( event );
  }

  @Override
  public URL getContext( )
  {
    return m_workspace.getContext();
  }

  @Override
  public Feature getFeature( final String id )
  {
    return m_workspace.getFeature( id );
  }

  @Deprecated
  @Override
  public Object getFeatureFromPath( final String featurePath )
  {
    return m_workspace.getFeatureFromPath( featurePath );
  }

  @Override
  public Feature getRootFeature( )
  {
    return m_workspace.getRootFeature();
  }

  @Override
  public void removeModellListener( final ModellEventListener listener )
  {
    m_workspace.removeModellListener( listener );
  }

  @Override
  public Feature[] resolveLinks( final Feature srcFeature, final IRelationType linkPropertyName )
  {
    return m_workspace.resolveLinks( srcFeature, linkPropertyName );
  }

  @Override
  public boolean isDirty( )
  {
    return m_commandManager.isDirty();
  }

  @Override
  public void resetDirty( )
  {
    m_commandManager.resetDirty();
  }

  public GMLWorkspace getWorkspace( )
  {
    return m_workspace;
  }

  @Override
  public String getSchemaLocationString( )
  {
    return m_workspace.getSchemaLocationString();
  }

  @Override
  public Feature createFeature( final Feature parent, final IRelationType parentRelation, final IFeatureType type )
  {
    return m_workspace.createFeature( parent, parentRelation, type );
  }

  @Override
  public Feature createFeature( final Feature parent, final IRelationType parentRelation, final IFeatureType type, final int depth )
  {
    return m_workspace.createFeature( parent, parentRelation, type, depth );
  }

  @Override
  public void addFeatureAsComposition( final Feature parent, final IRelationType propName, final int pos, final Feature newFeature ) throws Exception
  {
    m_workspace.addFeatureAsComposition( parent, propName, pos, newFeature );
  }

  @Override
  public boolean removeLinkedAsAggregationFeature( final Feature parentFeature, final IRelationType propName, final String linkFeatureId )
  {
    return m_workspace.removeLinkedAsAggregationFeature( parentFeature, propName, linkFeatureId );
  }

  @Override
  public boolean removeLinkedAsCompositionFeature( final Feature parentFeature, final IRelationType propName, final Feature childFeature )
  {
    return m_workspace.removeLinkedAsCompositionFeature( parentFeature, propName, childFeature );
  }

  @Override
  public void accept( final FeatureVisitor fv, final String featurePath, final int depth )
  {
    m_workspace.accept( fv, featurePath, depth );
  }

  @Override
  public void accept( final FeatureVisitor visitor, final Feature feature, final int depth, final IPropertyType[] featureProperties )
  {
    m_workspace.accept( visitor, feature, depth, featureProperties );
  }

  @Override
  public boolean contains( final Feature feature )
  {
    return m_workspace.contains( feature );
  }

  /**
   * @param parentFeature
   * @param ftp
   * @param pos
   * @return
   */
  @Override
  public boolean isBrokenLink( final Feature parentFeature, final IPropertyType ftp, final int pos )
  {
    return m_workspace.isBrokenLink( parentFeature, ftp, pos );
  }

  @Override
  public IGMLSchema getGMLSchema( )
  {
    return m_workspace.getGMLSchema();
  }

  public ICommandManager getCommandManager( )
  {
    return m_commandManager;
  }

  @Override
  public IFeatureProviderFactory getFeatureProviderFactory( )
  {
    return m_workspace.getFeatureProviderFactory();
  }

  @Override
  public void clear( )
  {
    m_commandManager.clear();
  }

  @Override
  public NamespaceContext getNamespaceContext( )
  {
    return m_workspace.getNamespaceContext();
  }

  @Override
  public void setSchemaLocation( final String schemaLocation )
  {
    m_workspace.setSchemaLocation( schemaLocation );
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    return m_workspace.getAdapter( adapter );
  }
}