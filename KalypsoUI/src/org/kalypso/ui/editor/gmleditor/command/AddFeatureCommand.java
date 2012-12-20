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
package org.kalypso.ui.editor.gmleditor.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.kalypso.commons.command.ICommand;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureProvider;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;

/**
 * @author Gernot Belger
 */
public class AddFeatureCommand implements ICommand, IFeatureProvider
{
  private final Feature m_parentFeature;

  private final int m_pos;

  private final IRelationType m_propName;

  private final IFeatureType m_type;

  private Feature m_newFeature = null;

  private final GMLWorkspace m_workspace;

  /** A map with key=IPropertyType and value=Object to pass properties when the feature is newly created */
  private final Map<IPropertyType, Object> m_properties = new HashMap<>();

  private final IFeatureSelectionManager m_selectionManager;

  private final int m_depth;

  /**
   * This variable states, if all features in the selection manager should be removed, by adding a feature.
   */
  private boolean m_dropSelection = true;

  /* HACK: this fixes the create fe element speed problem */
  private final boolean m_doFireEvents;

  private final boolean m_doAddOnProcess;

  /**
   * @param If
   *          dropSelection is true, the workspace must be a {@link CommandableWorkspace}.
   */
  public AddFeatureCommand( final GMLWorkspace workspace, final QName type, final Feature parentFeature, final IRelationType propertyName, final int pos, final Map<QName, Object> properties, final IFeatureSelectionManager selectionManager, final int depth )
  {
    this( workspace, GMLSchemaUtilities.getFeatureTypeQuiet( type ), parentFeature, propertyName, pos, convertProperties( GMLSchemaUtilities.getFeatureTypeQuiet( type ), properties ), selectionManager, depth );
  }

  private static Map<IPropertyType, Object> convertProperties( final IFeatureType featureType, final Map<QName, Object> properties )
  {
    final Map<IPropertyType, Object> typedProperties = new HashMap<>();

    for( final Entry<QName, Object> entry : properties.entrySet() )
    {
      final IPropertyType propertyType = featureType.getProperty( entry.getKey() );
      if( propertyType != null )
        typedProperties.put( propertyType, entry.getValue() );
    }

    return typedProperties;
  }

  /**
   * @param If
   *          dropSelection is true, the workspace must be a {@link CommandableWorkspace}.
   */
  public AddFeatureCommand( final GMLWorkspace workspace, final IFeatureType type, final Feature parentFeature, final IRelationType propertyName, final int pos, final Map<IPropertyType, Object> properties, final IFeatureSelectionManager selectionManager, final int depth )
  {
    m_workspace = workspace;
    m_parentFeature = parentFeature;
    m_propName = propertyName;
    m_pos = pos;
    m_type = type;
    m_selectionManager = selectionManager;
    m_depth = depth;
    m_doFireEvents = true;
    m_doAddOnProcess = true;
    if( properties != null )
      m_properties.putAll( properties );
  }

  /**
   * Alternative constructor: instead of specifying the properties and let the command create the feature a newly
   * created feature is provided from outside.
   */
  public AddFeatureCommand( final GMLWorkspace workspace, final Feature parentFeature, final IRelationType propertyName, final int pos, final Feature newFeature, final IFeatureSelectionManager selectionManager )
  {
    this( workspace, parentFeature, propertyName, pos, newFeature, selectionManager, true, true );
  }

  /**
   * Alternative constructor: instead of specifying the properties and let the command create the feature a newly
   * created feature is provided from outside.
   */
  public AddFeatureCommand( final GMLWorkspace workspace, final Feature parentFeature, final IRelationType propertyName, final int pos, final Feature newFeature, final IFeatureSelectionManager selectionManager, final boolean doFireEvents )
  {
    this( workspace, parentFeature, propertyName, pos, newFeature, selectionManager, doFireEvents, true );
  }

  /**
   * Alternative constructor: instead of specifying the properties and let the command create the feature a newly
   * created feature is provided from outside.
   * 
   * @param doAddOnProcess
   *          If false, the new feature will NOT be added/set to the given relation (propertyName). This is necessary if
   *          the feature was already added before.
   */
  public AddFeatureCommand( final GMLWorkspace workspace, final Feature parentFeature, final IRelationType propertyName, final int pos, final Feature newFeature, final IFeatureSelectionManager selectionManager, final boolean doFireEvents, final boolean doAddOnProcess )
  {
    m_workspace = workspace;
    m_parentFeature = parentFeature;
    m_propName = propertyName;
    m_pos = pos;
    m_doFireEvents = doFireEvents;
    m_doAddOnProcess = doAddOnProcess;
    m_newFeature = newFeature;
    m_type = null;
    m_selectionManager = selectionManager;
    m_depth = -1;
  }

  @Override
  public boolean isUndoable( )
  {
    return true;
  }

  @Override
  public void process( ) throws Exception
  {
    if( m_newFeature == null )
    {
      m_newFeature = m_workspace.createFeature( m_parentFeature, m_propName, m_type, m_depth );

      if( m_properties != null )
      {
        for( final Map.Entry<IPropertyType, Object> entry : m_properties.entrySet() )
          m_newFeature.setProperty( entry.getKey(), entry.getValue() );
      }
    }

    /* Add the new feature */
    if( m_doAddOnProcess )
      m_workspace.addFeatureAsComposition( m_parentFeature, m_propName, m_pos, m_newFeature );

    if( m_doFireEvents )
      m_workspace.fireModellEvent( new FeatureStructureChangeModellEvent( m_workspace, m_parentFeature, m_newFeature, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_ADD ) );

    if( m_selectionManager != null && m_dropSelection == true && m_workspace instanceof CommandableWorkspace )
      m_selectionManager.setSelection( new EasyFeatureWrapper[] { new EasyFeatureWrapper( (CommandableWorkspace)m_workspace, m_newFeature ) } );
  }

  @Override
  public void redo( ) throws Exception
  {
    process();
  }

  @Override
  public void undo( ) throws Exception
  {
    if( m_newFeature == null )
      return;

    if( m_propName.isList() )
    {
      final List< ? > list = (List< ? >)m_parentFeature.getProperty( m_propName );
      list.remove( m_newFeature );
    }
    else
      m_parentFeature.setProperty( m_propName, null );

    m_workspace.fireModellEvent( new FeatureStructureChangeModellEvent( m_workspace, m_parentFeature, m_newFeature, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_DELETE ) );
  }

  @Override
  public String getDescription( )
  {
    return Messages.getString( "org.kalypso.ui.editor.gmleditor.command.AddFeatureCommand.0" ); //$NON-NLS-1$
  }

  public Feature getNewFeature( )
  {
    return m_newFeature;
  }

  /**
   * Sets, if a existing selection should be dropped after the creation of the feature.
   */
  public void dropSelection( final boolean dropSelection )
  {
    m_dropSelection = dropSelection;
  }

  public void addProperty( final QName property, final Object value )
  {
    m_properties.put( m_type.getProperty( property ), value );
  }

  public void addProperty( final IPropertyType property, final Object value )
  {
    m_properties.put( property, value );
  }

  /**
   * Returns the new feature after it has been created.
   */
  @Override
  public Feature getFeature( )
  {
    return m_newFeature;
  }
}