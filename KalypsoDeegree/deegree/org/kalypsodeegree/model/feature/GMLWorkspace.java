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
 * Represents a loaded GML file.
 *
 * @author Andreas von Dömming
 */
public interface GMLWorkspace extends ModellEventProvider
{
  /**
   * Returns the root feature of the gml file represented by this workspace.
   */
  Feature getRootFeature( );

  /**
   * Returns the {@link IGMLSchema} that was used to load / create this workspace.
   *
   * @deprecated Use {@link org.kalypso.gmlschema.GMLSchemaUtilities#getSchemaQuiet(String)} or
   *             {@link org.kalypso.gmlschema.GMLSchemaUtilities#getFeatureTypeQuiet(QName)} instead.
   */
  @Deprecated
  IGMLSchema getGMLSchema( );

  /**
   * Returns all features of a certain feature type contained in this workspace.<br>
   * Comparison with feature type is exact, substitution is not considered.
   */
  // FIXME: does not belong here; move into a visitor / helper
  Feature[] getFeatures( IFeatureType ft );

  /**
   * Returns the feature that has the given id or null if not found.
   */
  Feature getFeature( String id );

  // FIXME: rework this stuff
  /**
   * resolves the associationlink to a feature, maxOccurs =1
   */
  Feature resolveLink( Feature srcFeature, IRelationType linkProperty );

  /**
   * resolves the associationlink to a feature, maxOccurs >1
   */
  Feature[] resolveLinks( Feature srcFeature, IRelationType linkProperty );

  /**
   * returns all Features that that link to the linkTargetFeature, with the specified linkPropertyname and are type of
   * linkSourceFeatureType or do substitute it
   */
  Feature[] resolveWhoLinksTo( Feature linkTargetfeature, IFeatureType linkSrcFeatureType, IRelationType linkProperty );

  URL getContext( );

  // FIXME: remove; use filtered visitor instead
  /** Visit all Features of the given IFeatureType */
  void accept( FeatureVisitor fv, IFeatureType ft, int depth );

  // FIXME: move into feature api
  /** Visit the given feature */
  void accept( FeatureVisitor fv, Feature feature, int depth );

  // FIXME: move into feature list api
  /** Visit alle features in the given list */
  void accept( FeatureVisitor fv, List< ? > features, int depth );

  // FIXME: check
  /** Visit alle features denoted by this path */
  void accept( FeatureVisitor fv, String featurePath, int depth );

  /**
   * @param featureProperties
   *          properties to follow
   */
  // FIXME: check; move into feature api
  void accept( FeatureVisitor visitor, Feature feature, int depth, IPropertyType[] featureProperties );

  /**
   * @deprecated Retrieve type information via GMLSchema. Use {@link GMLSchema#getFeatureType(QName)} instead.
   */
  // FIXME: remove
  @Deprecated
  IFeatureType getFeatureType( QName featureQName );

  /**
   * @deprecated Retrieve type information via GMLSchema. Use {@link GMLSchema#getFeatureType(QName)} or
   *             {@link GMLSchema#getFeatureType(String)} instead.
   * @deprecated use getFeatureType(QName)
   */
  @Deprecated
  // FIXME: remove; move to gml schema
  IFeatureType getFeatureType( String nameLocalPart );

  // FIXME: method does not what it states...
  Object getFeatureFromPath( String featurePath );

  /**
   * Holt den durch den FeaturePath angegebenen Typ Systax des FeaturePath:
   * <code> <propertyName>/.../<propertyName>[featureTypeName] </code> Wobei der featureTypeName optional ist <br/>
   * FIXME: does not belong here...
   */
  IFeatureType getFeatureTypeFromPath( String featurePath );

  // FIXME: check
  FeaturePath getFeaturepathForFeature( Feature feature );

  // FIXME: check; only internally used; still necessary?
  String getSchemaLocationString( );

  // FIXME: check; only internally used; still necessary? Why a setter at all?
  void setSchemaLocation( String schemaLocation );

  /**
   * Creates a feature an puts it into this workspace.<br/>
   * Generates a unique id throughout this workspace.<br/>
   */
  // FIXME: move into feature api
  Feature createFeature( Feature parent, IRelationType parentRelation, IFeatureType type );

  /**
   * Creates a feature an puts it into this workspace. Also create sub features where appropriate.<br/>
   * Generates a unique id throughout this workspace.<br/>
   *
   * @param depth
   *          Number of levels of sub features which shall be created. -1 means infinite, 0 means none (only normal
   *          properties are filled with default values).
   */
  // FIXME: move into feature api
  Feature createFeature( Feature parent, IRelationType parentRelation, IFeatureType type, int depth );

  /**
   * TODO: we should replace this method by: createAsComposition! First, it is always used as such (that is first
   * created, that this method is called).; Second: a featuree hsould never live without workspace
   *
   * @param pos
   *          Position at which the new element is inserted into the list. If -1, the new element is added to the end of
   *          the list.
   */
  // FIXME: move into feature api
  void addFeatureAsComposition( Feature parent, IRelationType linkProperty, int pos, Feature newFeature ) throws Exception;

  // FIXME: move into feature api
  void addFeatureAsAggregation( Feature parent, IRelationType linkProperty, int pos, String featureID ) throws Exception;

  /**
   * @deprecated Should not be used any more. Does not handle external xlinks correctly.
   */
  // FIXME: move into feature api
  @Deprecated
  void setFeatureAsAggregation( Feature srcFE, IRelationType linkProperty, int pos, String featureID ) throws Exception;

  // FIXME: move into feature api
  void setFeatureAsAggregation( Feature parent, IRelationType linkProperty, String featureID, boolean overwrite ) throws Exception;

  /**
   * removes a related feature from the parent. Works only if the child is linked <br>
   * <i>and the relation is not a composition </i> see also
   *
   * @see org.kalypsodeegree.model.feature.GMLWorkspace#removeLinkedAsCompositionFeature(org.kalypsodeegree.model.feature.Feature,
   *      java.lang.String, org.kalypsodeegree.model.feature.Feature)
   */
  // FIXME: move into feature api
  boolean removeLinkedAsAggregationFeature( Feature parentFeature, IRelationType linkProperty, String childFeatureID );

  /**
   * removes a related feature from the parent. Works only if the child is a composition <br>
   * <i>and the relation is not linked </i>
   */
  // FIXME: move into feature api
  boolean removeLinkedAsCompositionFeature( Feature parentFeature, IRelationType linkProperty, Feature childFeature );

  /**
   * @param parent
   * @param linkPropName
   * @param pos
   * @return <code>true</code> if it is a aggregation <br>
   *         <code>false</code> if it is a composition <br>
   *         caution: is link is <code>null</code> return value is undefined
   */
  // FIXME: move into feature api
  boolean isAggregatedLink( Feature parent, IRelationType linkProperty, int pos );

  /**
   * @deprecated Should not be used any more. Does not handle external xlinks correctly.
   */
  // FIXME: move into feature api
  @Deprecated
  void setFeatureAsComposition( Feature parentFE, IRelationType linkProperty, Feature linkedFE, boolean overwrite ) throws Exception;

  boolean contains( Feature feature );

  boolean isBrokenLink( Feature parentFeature, IPropertyType ftp, int pos );

  /** Return the factory which creates feature providers used to load linked features. */
  // FIXME: should only be used internally
  IFeatureProviderFactory getFeatureProviderFactory( );

  // FIXME: should only be used internally
  GMLWorkspace getLinkedWorkspace( String uri );

  /**
   * The namespace context with which this workspace was read from a gm file (if any).<br>
   * May be <code>null</code>.<br>
   * Sometimes needed/necessary, if after reading the complete document, fragments (like xpathes) are going to be
   * evaluated.
   */
  NamespaceContext getNamespaceContext( );
}