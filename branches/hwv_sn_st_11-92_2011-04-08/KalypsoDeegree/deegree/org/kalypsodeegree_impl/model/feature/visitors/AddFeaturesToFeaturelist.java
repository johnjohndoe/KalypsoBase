/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 * 
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always. 
 * 
 * If you intend to use this software in other ways than in kalypso 
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree, 
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.model.feature.visitors;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaCatalog;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.KalypsoGMLSchemaPlugin;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * Fügt neue Features in eine {@link org.kalypsodeegree.model.feature.FeatureList}ein. Dabei wird für jedes besuchte
 * Feature ein neues (Default) Feature erzeugt in welches Werte aus dem besuchten Feature via
 * {@link org.kalypsodeegree_impl.model.feature.FeatureHelper#copyProperties(Feature, Feature, Properties)}übertragen
 * werden.
 * 
 * @author bce
 */
public class AddFeaturesToFeaturelist implements FeatureVisitor
{
  private final FeatureList m_list;

  private final Properties m_propertyMap;

  private final IFeatureType m_featureType;

  private final String m_fromID;

  private final Map<Object, Feature> m_idHash;

  private final String m_fid;

  private final Map<Object, Feature> m_fidHash;

  private static final String REPLACE_COUNT = "${count}";

  private final String m_handleExisting;

  private final String m_targetFeatureType;

  /**
   * @param fid
   *          Anhand dieses Pattern werden die neuen FeatureIds erzeugt. Es wird ein Pattern-Replace mit folgenden
   *          Variablen erzeugt:
   *          <ul>
   *          <li>fromID</li>
   *          <li>toID</li>
   *          <li>fID</li>
   *          <li>count</li>
   *          </ul>
   */
  public AddFeaturesToFeaturelist( final FeatureList list, final Properties propertyMap, final IFeatureType featureType, final String fromID, final String toID, final String handleExisting, final String fid, final String targetFeatureType )
  {
    m_list = list;
    m_featureType = featureType;
    m_propertyMap = propertyMap;
    m_fromID = fromID;
    m_handleExisting = handleExisting;
    m_fid = fid;
    m_targetFeatureType = targetFeatureType;

    // create index for toID
    final IndexFeaturesVisitor visitor = new IndexFeaturesVisitor( toID );
    m_list.accept( visitor );
    m_idHash = visitor.getIndex();

    final IndexFeaturesVisitor fidVisitor = new IndexFeaturesVisitor( null );
    m_list.accept( fidVisitor );
    m_fidHash = fidVisitor.getIndex();
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureVisitor#visit(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public boolean visit( final Feature f )
  {
    try
    {
      final Feature targetFeature = getTargetFeature( f );
      if( targetFeature == null )
        return true;

      FeatureHelper.copyProperties( f, targetFeature, m_propertyMap );

      // den fid-hash aktuell halten
      m_fidHash.put( targetFeature.getId(), targetFeature );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return true;
  }

  private Feature getTargetFeature( final Feature sourceFeature ) throws GMLSchemaException
  {
    final Object fromID = findSourceID( sourceFeature, m_fromID );

    final Feature existingFeature = m_idHash.get( fromID );
    final String fid = createID( existingFeature, fromID );

    if( existingFeature == null || "overwrite".equals( m_handleExisting ) )
    {
      final Feature parentFeature = m_list.getParentFeature();
      final IRelationType parentRelation = m_list.getParentFeatureTypeProperty();

      final IFeatureType targetFeatureType = findTargetFeatureType( sourceFeature );

      final Feature newFeature = FeatureFactory.createFeature( parentFeature, parentRelation, fid, targetFeatureType, true );
      m_list.add( newFeature );
      return newFeature;
    }

    if( "change".equals( m_handleExisting ) )
      return existingFeature;

    if( "nothing".equals( m_handleExisting ) )
      return null;

    throw new IllegalArgumentException( "Argument 'handleExisting' must be one of 'change', 'overwrite' or 'existing', but is: " + m_handleExisting );
  }

  public static Object findSourceID( final Feature sourceFeature, final String fromID )
  {
    if( "#FID#".equals( fromID ))
      return sourceFeature.getId();
    
    final IFeatureType sourceFT = sourceFeature.getFeatureType();
    final IPropertyType idPT = sourceFT.getProperty( fromID );
    final Object property = sourceFeature.getProperty( idPT );

    if( idPT.isList() )
    {
      final List< ? > list = (List< ? >) property;
      if( list.isEmpty() )
        return null;

      return list.get( 0 );
    }

    return property;
  }

  private IFeatureType findTargetFeatureType( final Feature sourceFeature ) throws GMLSchemaException
  {
    if( m_targetFeatureType == null )
      return m_featureType;

    final QName targetName = (QName) sourceFeature.getProperty( m_targetFeatureType );
    final GMLSchemaCatalog schemaCatalog = KalypsoGMLSchemaPlugin.getDefault().getSchemaCatalog();
    final String namespaceURI = targetName.getNamespaceURI();
    final GMLSchema schema = schemaCatalog.getSchema( namespaceURI, (String) null );
    return schema.getFeatureType( targetName );
  }

  private String createID( final Feature feature, final Object fromID )
  {
    final String oldFid = feature == null ? "<none>" : feature.getId();

    String fidhelp = m_fid;
    fidhelp = fidhelp.replaceAll( "\\Q${fromID}\\E", fromID == null ? "<null>" : fromID.toString() );
    // fidhelp = fidhelp.replaceAll( "\\Q${toID}\\E" , toID );
    fidhelp = fidhelp.replaceAll( "\\Q${fID}\\E", oldFid );
    if( fidhelp.indexOf( REPLACE_COUNT ) == -1 )
      fidhelp += REPLACE_COUNT;

    int count = -1;
    while( true )
    {
      final String replace = count == -1 ? "" : ("" + count);
      final String fid = fidhelp.replaceAll( "\\Q" + REPLACE_COUNT + "\\E", replace );

      if( m_fidHash.get( fid ) == null )
        return fid;

      count++;
    }
  }
}
