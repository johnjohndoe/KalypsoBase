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

import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.sort.JMSpatialIndex;

/**
 * @author Gernot Belger
 */
public interface FeatureList extends List, JMSpatialIndex, IFeatureRelation
{
  /**
   * Gets ALL features in this list. Resolves any links.
   */
  <T extends Feature> T[] toFeatures( T[] features );

  Feature[] toFeatures( );

  /**
   * Visit all Features in the list.
   * 
   * @param depth
   *          One of {@link FeatureVisitor#DEPTH_INFINITE}...
   */
  void accept( FeatureVisitor visitor, int depth );

  /** Visit all Features in the list. */
  void accept( FeatureVisitor visitor );

  /**
   * The feature containing this list.
   * 
   * @return The parent feature, <code>null</code> if the list has no parent feature.
   */
  @Override
  Feature getOwner( );

  /**
   * Returns the index of the first occurrence of a link to the specified element
   * in this list, or -1 if this list does not contains a link to this element.
   */
  int indexOfLink( final Feature targetFeature );

  /**
   * Same as {@link #insertRef(size(), Feature)}
   */
  <T extends Feature> IXLinkedFeature addLink( T toAdd ) throws IllegalArgumentException, IllegalStateException;

  /**
   * Same as {@link #insertRef(size(), href)}
   */
  IXLinkedFeature addLink( String href ) throws IllegalArgumentException, IllegalStateException;

  /**
   * Same as {@link #insertRef(size(), href, featureTypeName)}
   */
  IXLinkedFeature addLink( String href, QName featureTypeName ) throws IllegalArgumentException, IllegalStateException;

  /**
   * Same as {@link #insertRef(size(), href, featureType)}
   */
  IXLinkedFeature addLink( String href, IFeatureType featureType ) throws IllegalArgumentException, IllegalStateException;

  /**
   * Add this feature as a link to this list.<br>
   * If a feature of the same workspace as the list's owner is given, an internal reference is created, else an xlink to
   * the external feature is inserted.<br>
   * The <code>href</code> is built using the context of the target workspace. Tries to make it relative to the context
   * of this workspace.<br/>
   * Does not check, if the given feature is already contained in this list (the list may be allowed to contain multiple
   * links to the same feature or even a feature and a link to the same feature at the same time).
   * 
   * @param toLink
   *          a wrapper wrapping the feature to be added as list
   * @return The freshly created link
   * @throws IllegalArgumentException
   *           If the list may not contain references (either at all or to this kind of features) according to its
   *           definition.
   * @throws IllegalStateException
   *           If maxOccurs of this list is exceeded.
   * @throws {@link NullPointerException} If the argument toLink is null
   */
  <T extends Feature> IXLinkedFeature insertLink( int index, T toLink ) throws IllegalArgumentException, IllegalStateException;

  /**
   * Same as {@link #insertLink(int, Feature)}, using a given href.
   * 
   * @see Feature#setLink(org.kalypso.gmlschema.property.relation.IRelationType, String) for the interpreatation of the
   *      parameters.
   */
  IXLinkedFeature insertLink( int index, String href ) throws IllegalArgumentException, IllegalStateException;

  /**
   * Same as {@link #insertLink(int, Feature)}, using a given href.
   * 
   * @see Feature#setLink(org.kalypso.gmlschema.property.relation.IRelationType, String) for the interpreatation of the
   *      parameters.
   */
  IXLinkedFeature insertLink( int index, String href, QName featureTypeName ) throws IllegalArgumentException, IllegalStateException;

  /**
   * Same as {@link #insertLink(int, Feature)}, using a given href.
   * 
   * @see Feature#setLink(org.kalypso.gmlschema.property.relation.IRelationType, String) for the interpreatation of the
   *      parameters.
   */
  IXLinkedFeature insertLink( int index, String href, IFeatureType featureType ) throws IllegalArgumentException, IllegalStateException;

  /**
   * Removes all elements with the given indices.<br/>
   * For some implementations this is considerably faster than iteration over {@link #remove(int)}.
   */
  void removeAll( int[] indices );

  /**
   * Removes a link from this list that links to the given feature.<br/>
   * If more than one element links to the given feature, only the first will be removed.
   * 
   * @return <code>true</code> If the list was changed by this operation.
   * @throws IllegalArgumentException
   *           If the given feature is an {@link IXLinkedFeature}.
   */
  boolean removeLink( Feature targetFeature );

  /**
   * Removes links from this list that links to the given feature.<br/>
   * If more than one element links to the given feature, only the first will be removed.
   * 
   * @return The number of elements that actually have been removed.
   * @throws IllegalArgumentException
   *           If the given feature is an {@link IXLinkedFeature}.
   */
  public int removeLinks( final Feature[] targetFeatures );

  boolean containsLinkTo( Feature targetFeature );

  Feature getResolved( int index );

  <T extends Feature> List<T> queryResolved( GM_Envelope env, List<T> result );

  <T extends Feature> List<T> queryResolved( GM_Position env, List<T> result );

  // TODO: uncomment only if implemented

// /**
// * Same as {@link #insertNew(size(), newChildType, 'uniqueRandomFeatureId', Feature.class)}
// *
// * @see #insertNew(int, QName, String, Class)
// */
// Feature addNew( QName newChildType );

// /**
// * Same as {@link #insertNew(size(), newChildType, newFeatureId, Feature.class)}
// *
// * @see #insertNew(int, QName, String, Class)
// */
// Feature addNew( QName newChildType, String newFeatureId );

// /**
// * Same as {@link #insertNew(size(), newChildType, 'uniqueRandomFeatureId', classToAdapt)}
// *
// * @see #insertNew(int, QName, String, Class)
// */
// <T extends Feature> T addNew( QName newChildType, Class<T> classToAdapt );

// /**
// * Same as {@link #insertNew(size(), newChildType, newFeatureId, classToAdapt)}
// *
// * @see #insertNew(int, QName, String, Class)
// */
// <T extends Feature> T addNew( QName newChildType, String newFeatureId, Class<T> classToAdapt );

// /**
// * Same as {@link #insertNew(index, newChildType, 'uniqueRandomFeatureId', Feature.class)}
// *
// * @see #insertNew(int, QName, String, Class)
// */
// Feature insertNew( int index, QName newChildType );

// /**
// * Same as {@link #insertNew(index, newChildType, newFeatureId, Feature.class)}
// *
// * @see #insertNew(int, QName, String, Class)
// */
// Feature insertNew( int index, QName newChildType, String newFeatureId );

// /**
// * Same as {@link #insertNew( index, newChildType, 'uniqueRandomFeatureId', classToAdapt)}
// *
// * @see #insertNew(int, QName, String, Class)
// */
// <T extends Feature> T insertNew( int index, QName newChildType, Class<T> classToAdapt );

// /**
// * Creates and adds a new feature of the specified type into the feature collection at the specified position.<br>
// * The newly created feature is hooked into the {@link GMLWorkspace} hierarchy of the lists parent feature.<br>
// * Triggers a model-event on the containing workspace.
// *
// * @param index
// * index at which the specified element is to be inserted.
// * @param newChildType
// * The type of the element to add, cannot be <code>null</code>.
// * @throws IllegalArgumentException
// * If some aspect of the specified newChildType prevents it from being added to this list. E.g.
// * <ul>
// * <li/>The underlying feature collection does not accepts elements of the specified type.
// * <li/>The type cannot be cast or is not adaptable to the <code>classToAdapt</code>.
// * <li>The given <code>newFeatureId</code> is already registered in the workspace of this list's parent
// * feature.</li>
// * </ul>
// * @throw {@link NullPointerException} If one of the arguments is <code>null</code>.
// * @throws IndexOutOfBoundsException
// * if the index is out of range (index &lt; 0 || index &gt; size()).
// */
// <T extends Feature> T insertNew( int index, QName newChildType, String newFeatureId, Class<T> classToAdapt );

// /**
// * Same as {@link #insertNew(index, newChildType, newFeatureId, classToAdapt)}, additionally sets the given
// * properties.
// *
// * @param properties
// * Property values to be set to the feature. Must correspond to the feature type of the newly created
// * feature.
// * @see org.kalypso.gmlschema.feature.IFeatureType#getProperties()
// * @see #insertNew(int, QName, String, Class)
// */
// <T extends Feature> T insertNew( int index, QName newChildType, String newFeatureId, Class<T> classToAdapt, Object[]
// properties );
}
