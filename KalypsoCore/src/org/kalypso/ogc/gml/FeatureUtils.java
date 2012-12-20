/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.ogc.gml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.DeleteFeatureCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;

/**
 * some basic feature utils
 *
 * @author Dirk Kuch
 */
public final class FeatureUtils
{
  private FeatureUtils( )
  {
  }

  public static void deleteFeature( final CommandableWorkspace workspace, final Feature feature ) throws Exception
  {
    if( feature == null )
      return;

    final DeleteFeatureCommand command = new DeleteFeatureCommand( feature );
    workspace.postCommand( command );
  }

  public static void deleteFeatures( final CommandableWorkspace workspace, final Feature[] features ) throws Exception
  {
    if( ArrayUtils.isEmpty( features ) )
      return;

    final DeleteFeatureCommand command = new DeleteFeatureCommand( features );
    workspace.postCommand( command );

  }

  public static String getFeatureName( final String namespace, final Feature node )
  {
    /* past -> gmlname ! */
    if( node == null )
      return null;

    final Object objString = node.getProperty( new QName( namespace, "name" ) ); //$NON-NLS-1$
    if( objString instanceof List )
    {
      final List< ? > names = (List< ? >) objString;
      if( names.size() >= 1 )
        return names.get( 0 ).toString();

      return Messages.getString( "org.kalypso.ogc.gml.FeatureUtils.8" ); //$NON-NLS-1$
    }
    else if( !(objString instanceof String) )
      return Messages.getString( "org.kalypso.ogc.gml.FeatureUtils.9" ); //$NON-NLS-1$

    return (String) objString;
  }

  public static void updateProperties( final CommandableWorkspace workspace, final Feature feature, final Map<QName, Object> map ) throws Exception
  {
    final FeatureChange[] changes = getAsFeatureChange( feature, map );
    final ChangeFeaturesCommand chgCmd = new ChangeFeaturesCommand( workspace, changes );

    workspace.postCommand( chgCmd );
  }

  public static FeatureChange[] getAsFeatureChange( final Feature feature, final Map<QName, Object> map )
  {
    final List<FeatureChange> changes = new ArrayList<>();

    final Set<Entry<QName, Object>> entrySet = map.entrySet();
    for( final Entry<QName, Object> entry : entrySet )
    {
      final IPropertyType chgProp = feature.getFeatureType().getProperty( entry.getKey() );
      final FeatureChange change = new FeatureChange( feature, chgProp, entry.getValue() );

      changes.add( change );
    }

    return changes.toArray( new FeatureChange[] {} );
  }

  public static FeatureChange[] getAsFeatureChange( final Feature feature, final QName qname, final Object value )
  {
    final Map<QName, Object> map = new HashMap<>();
    map.put( qname, value );

    return getAsFeatureChange( feature, map );
  }

  public static void updateProperty( final CommandableWorkspace workspace, final Feature feature, final QName qname, final Object value ) throws Exception
  {
    final Map<QName, Object> map = new HashMap<>();
    map.put( qname, value );

    FeatureUtils.updateProperties( workspace, feature, map );
  }

  public static void setInternalLinkedFeature( final CommandableWorkspace workspace, final Feature feature, final QName qname, final Feature linkedFeature ) throws Exception
  {
    final FeatureChange change = getInternalLinkedFeatureAsCommand( feature, qname, linkedFeature );

    final ChangeFeaturesCommand chgCmd = new ChangeFeaturesCommand( workspace, new FeatureChange[] { change } );
    workspace.postCommand( chgCmd );
  }

  public static FeatureChange getInternalLinkedFeatureAsCommand( final Feature feature, final QName qname, final Feature linkedFeature )
  {
    final IPropertyType chgProp = feature.getFeatureType().getProperty( qname );
    final String linkId = linkedFeature == null ? null : linkedFeature.getId();

    return new FeatureChange( feature, chgProp, linkId );
  }

  /**
   * @param value
   *          xyz.gml#featureId
   */
  public static void setExternalLinkedFeature( final CommandableWorkspace workspace, final Feature feature, final QName qname, final String value ) throws Exception
  {
    final ChangeFeaturesCommand chgCmd = new ChangeFeaturesCommand( workspace, new FeatureChange[] { getExternalLinkedFeatureCommand( feature, qname, value ) } );

    workspace.postCommand( chgCmd );
  }

  public static FeatureChange getExternalLinkedFeatureCommand( final Feature feature, final QName qname, final String value )
  {
    final IPropertyType chgProp = feature.getFeatureType().getProperty( qname );
    final IXLinkedFeature impl = FeatureFactory.createXLink( feature, (IRelationType) chgProp, feature.getFeatureType(), value );
    return new FeatureChange( feature, chgProp, impl );
  }

  /**
   * Checks, if one of the isUsed flags has changed. Checks only feature with the given owner.
   */
  public static boolean checkChange( final Feature owner, final FeatureChange[] changes, final QName property )
  {
    for( final FeatureChange change : changes )
    {
      if( change.getFeature().getOwner() == owner && change.getProperty().getQName().equals( property ) )
        return true;
    }

    return false;
  }

}
