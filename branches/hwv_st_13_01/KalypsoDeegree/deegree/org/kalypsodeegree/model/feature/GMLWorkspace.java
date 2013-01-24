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
package org.kalypsodeegree.model.feature;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.event.ModellEventProvider;
import org.kalypsodeegree_impl.model.feature.FeaturePath;
import org.kalypsodeegree_impl.model.feature.IFeatureProviderFactory;

/**
 * @author doemming
 */
public interface GMLWorkspace extends ModellEventProvider
{
  int RESOLVE_ALL = 0;

  int RESOLVE_LINK = 1;

  int RESOLVE_COMPOSITION = 2;

  Feature getRootFeature( );

  IGMLSchema getGMLSchema( );

  /**
   * Returns all features of a certain feature type contained in this workspace.<br>
   * Comparison with feature type is exact, substitution is not considered.
   */
  Feature[] getFeatures( IFeatureType ft );

  /**
   * Returns the feature that has the given id or null if not found.
   */
  Feature getFeature( String id );

  /**
   * resolves the associationlink to a feature, maxOccurs =1
   */
  Feature resolveLink( Feature srcFeature, IRelationType linkProperty );

  /**
   * resolves the associationlink to a feature, maxOccurs =1
   * 
   * @param srcFeature
   * @param linkPropertyName
   * @param resolveMode
   * @return linked feature
   */
  Feature resolveLink( Feature srcFeature, IRelationType linkProperty, int resolveMode );

  /**
   * resolves the associationlink to a feature, maxOccurs >1
   */
  Feature[] resolveLinks( Feature srcFeature, IRelationType linkProperty );

  /**
   * resolves the associationlink to a feature, maxOccurs >1
   * 
   * @param srcFeature
   * @param linkPropertyName
   * @param resolveMode
   * @return features
   */
  Feature[] resolveLinks( Feature srcFeature, IRelationType linkProperty, int resolveMode );

  /**
   * returns all Features that that link to the linkTargetFeature, with the specified linkPropertyname and are type of
   * linkSourceFeatureType or do substitue it
   */
  Feature[] resolveWhoLinksTo( Feature linkTargetfeature, IFeatureType linkSrcFeatureType, IRelationType linkProperty );

  URL getContext( );

  /** Visit all Features of the given IFeatureType */
  void accept( FeatureVisitor fv, IFeatureType ft, int depth );

  /** Visit the given feature */
  void accept( FeatureVisitor fv, Feature feature, int depth );

  /** Visit alle features in the given list */
  void accept( FeatureVisitor fv, List< ? > features, int depth );

  /** Visit alle features denoted by this path */
  void accept( FeatureVisitor fv, String featurePath, int depth );

  /**
   * @deprecated Retrieve type information via GMLSchema. Use {@link GMLSchema#getFeatureType(QName)} instead.
   */
  @Deprecated
  IFeatureType getFeatureType( QName featureQName );

  Object getFeatureFromPath( String featurePath );

  IFeatureType getFeatureTypeFromPath( String featurePath );

  FeaturePath getFeaturepathForFeature( Feature feature );

  String getSchemaLocationString( );

  /**
   * Creates a feature an puts it into this workspace.
   * <p>
   * Generates a unique id throughout this workspace.
   * </p>
   */
  Feature createFeature( Feature parent, IRelationType parentRelation, IFeatureType type );

  /**
   * Creates a feature an puts it into this workspace. Also create subfeatures where apropriate.
   * <p>
   * Generates a unique id throughout this workspace.
   * </p>
   * 
   * @param depth
   *          Number of levels of subfeatures which shall be created. -1 means infinite, 0 means none (only normal
   *          properties are filled with default values).
   */
  Feature createFeature( Feature parent, IRelationType parentRelation, IFeatureType type, int depth );

  /**
   * TODO: commont TODO: we should replace this method by: createAsComposition! First, it is always used as such (that i
   * sfirst created, that this method is called).; Second: a featuree hsould never live without workspace
   * 
   * @param pos
   *          Position at which the new element is inserted into the list. If -1, the new element is added to the end of
   *          the list.
   */
  void addFeatureAsComposition( Feature parent, IRelationType linkProperty, int pos, Feature newFeature ) throws Exception;

  void addFeatureAsAggregation( Feature parent, IRelationType linkProperty, int pos, String featureID ) throws Exception;

  void setFeatureAsAggregation( Feature srcFE, IRelationType linkProperty, int pos, String featureID ) throws Exception;

  void setFeatureAsAggregation( Feature parent, IRelationType linkProperty, String featureID, boolean overwrite ) throws Exception;

  /**
   * removes a related feature from the parent. Works only if the child is linked <br>
   * <i>and the relation is not a composition </i> see also
   * 
   * @see org.kalypsodeegree.model.feature.GMLWorkspace#removeLinkedAsCompositionFeature(org.kalypsodeegree.model.feature.Feature,
   *      java.lang.String, org.kalypsodeegree.model.feature.Feature)
   */
  boolean removeLinkedAsAggregationFeature( Feature parentFeature, IRelationType linkProperty, String childFeatureID );

  /**
   * removes a related feature from the parent. Works only if the child is a composition <br>
   * <i>and the relation is not linked </i>
   */
  boolean removeLinkedAsCompositionFeature( Feature parentFeature, IRelationType linkProperty, Feature childFeature );

  /**
   * return true if these feature are related
   */
  boolean isExistingRelation( Feature f1, Feature f2, IRelationType linkProperty );

  /**
   * @param parent
   * @param linkPropName
   * @param pos
   * @return <code>true</code> if it is a aggregation <br>
   *         <code>false</code> if it is a composition <br>
   *         caution: is link is <code>null</code> return value is undefined
   */
  boolean isAggregatedLink( Feature parent, IRelationType linkProperty, int pos );

  /**
   * @param parentFE
   * @param linkPropName
   * @param linkedFE
   * @param overwrite
   * @throws Exception
   */
  void setFeatureAsComposition( Feature parentFE, IRelationType linkProperty, Feature linkedFE, boolean overwrite ) throws Exception;

  /**
   * @param visitor
   * @param feature
   * @param depth
   * @param featureProperties
   *          properties to follow
   */
  void accept( FeatureVisitor visitor, Feature feature, int depth, IPropertyType[] featureProperties );

  boolean contains( Feature feature );

  boolean isBrokenLink( Feature parentFeature, IPropertyType ftp, int pos );

  /**
   * @deprecated Retrieve type information via GMLSchema. Use {@link GMLSchema#getFeatureType(QName)} or
   *             {@link GMLSchema#getFeatureType(String)} instead.
   * @deprecated use getFeatureType(QName)
   */
  @Deprecated
  IFeatureType getFeatureType( String nameLocalPart );

  /** Return the factory which creates feature providers used to load linked features. */
  IFeatureProviderFactory getFeatureProviderFactory( );

  GMLWorkspace getLinkedWorkspace( String uri );

  /**
   * The namespace context with which this workspace was read from a gm file (if any).<br>
   * May be <code>null</code>.<br>
   * Sometimes needed/necessary, if after reading the complete document, fragments (like xpathes) are going to be
   * evaluated.
   */
  NamespaceContext getNamespaceContext( );

  void setSchemaLocation( String schemaLocation );

}