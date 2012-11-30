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
package org.kalypsodeegree.model.feature;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.geometry.GM_Object;

/**
 * Glossary:<br/>
 * <ul>
 * <li>relation</li>Property that references an (either inline, linked or both) {@link Feature}.
 * <li>subFeature: property value of a {@link Feature} that is an inline {@link Feature}</li>
 * <li>link: property value of a {@link Feature} that is non inline but linked to another location (xlinked feature),
 * either within the same or another workspace.</li>
 * <li>member: either a (inline) sub-feature or a link</li>
 * </ul>
 *
 * @author Dirk Kuch
 */
public interface Feature extends BaseFeature, Deegree2Feature
{
  QName QN_NAME = new QName( NS.GML3, "name" ); //$NON-NLS-1$

  QName QN_DESCRIPTION = new QName( NS.GML3, "description" ); //$NON-NLS-1$

  QName QN_BOUNDED_BY = new QName( NS.GML3, "boundedBy" ); //$NON-NLS-1$

  QName QN_LOCATION = new QName( NS.GML3, "location" ); //$NON-NLS-1$

  /** QName of gml's gml:_Feature */
  QName QNAME_FEATURE = new QName( NS.GML3, "_Feature" ); //$NON-NLS-1$

  /** Returns the gml:name property of the bound feature. */
  String getName( );

  /** Sets the gml:name property */
  void setName( String name );

// Defined in Deegree2Feature
// /** Returns the gml:description property of the bound feature. */
// public String getDescription( );

  /** Sets the gml_description property */
  void setDescription( String desc );

  /**
   * Return the gml:location property of the bound feature.<br>
   * REMARK: gml:location is deprecated in the GML3-Schema.
   */
  GM_Object getLocation( );

  /**
   * Sets the gml:location property to the bound feature.<br>
   * REMARK: gml:location is deprecated in the GML3-Schema.
   */
  void setLocation( final GM_Object location );
  
  
  IXLinkedFeature setLink( QName relationName, Feature href );

  /**
   * Same as {@link #setLink(IRelationType, String, IFeatureType)} using the target feature type of the relation as
   * feature type of the xlink.
   */
  IXLinkedFeature setLink( final IRelationType relation, final String href );

  /**
   * Same as {@link #setLink(QName, String, IFeatureType)} using the target feature type of the relation as feature type
   * of the xlink.
   */
  IXLinkedFeature setLink( final QName relationName, final String href );

  /**
   * Creates an xlink to a linked feature and sets it as a property of this feature. If the property was already set, it
   * is replaced by the newly created link.
   *
   * @param relation
   *          Relation of this feature that allows for linked {@link Feature}s.
   * @param href
   *          The reference to the linked feature. Must be of the form <code>url#anchor</code>, where <code>url</code>
   *          my be an absolute, relative (to the context of this {@link Feature}'s {@link GMLWorkspace}-context, or
   *          empty (i.e. a reference within the workspace of this {@link Feature}). <code>anchor</code> is the id of
   *          the referenced {@link Feature}. If this parameter is blank (<code>null</code> or contains only
   *          whitespace), the link is set to {@link null}.
   * @param featureType
   *          Name of the type of the feature this link points to.
   * @throws IllegalArgumentException
   *           If <code>relation</code> is not a property of this feature.
   * @return The freshly created feature.
   */
  IXLinkedFeature setLink( IRelationType relation, String href, QName featureType );

  /**
   * @param featureType
   *          Type of the feature this link points to.
   * @see #setLink(IRelationType, String, QName)
   */
  IXLinkedFeature setLink( IRelationType relation, String href, IFeatureType featureType );

  /**
   * @param relation
   *          Name of a property of this feature (must be a relation) that allows for linked {@link Feature}s.
   * @see #setLink(IRelationType, String)
   * @throws IllegalArgumentException
   *           If <code>relation</code> is not the name of a relation type.
   */
  IXLinkedFeature setLink( QName relation, String href, QName featureType );

  /**
   * @see #setLink(QName, String)
   * @param featureType
   *          Type of the feature this link points to.
   * @throws IllegalArgumentException
   *           If <code>relation</code> is not the name of a relation type.
   */
  IXLinkedFeature setLink( QName relation, String href, IFeatureType featureType );

  /**
   * Creates a sub-feature for the given relation and directly sets it to this property.<br/>
   * If this sub feature is already present, it is replaced by the newly created sub feature.<br/>
   * This method also registers / unregisters the involved feature to/from the workspace.<br/>
   * The relation must not be a list and must allow for inline features.
   *
   * @param relation
   *          The property for which a sub feature is created and set.
   * @param featureType
   *          The feature type of the newly created feature
   * @return The newly created feature
   * @throws IllegalArgumentException
   *           If the relation is either a list or does not allow inline features.
   */
  Feature createSubFeature( IRelationType relation, QName featureTypeName );

  /**
   * Same as {@link #createSubFeature(IRelationType)} but uses the target type of the relation as default.
   */
  Feature createSubFeature( IRelationType relation );

  /**
   * Creates a sub-feature for the relation with the given name.
   *
   * @throws IllegalArgumentExcveption
   *           If the relationName does not point to a suitable relation.
   * @see #createSubFeature(IRelationType relation)
   */
  Feature createSubFeature( QName relationName, QName featureTypeName );

  /**
   * Same as {@link #createSubFeature(QName)} but uses the target type of the relation as default.
   */
  Feature createSubFeature( QName relationName );

  /**
   * Returns a related member feature.<br/>
   * Inline feature gets returned, feature links are returned as xlinked-features.
   */
  Feature getMember( IRelationType relation );

  /**
   * Returns a related member feature.<br/>
   * Inline feature gets returned, feature links are returned as xlinked-features.
   *
   * @param relation
   *          Name of a property of this feature (must be a relation).
   * @throws IllegalArgumentException
   *           If <code>relation</code> is not the name of a relation type.
   */
  Feature getMember( QName relation );

  /**
   * Resolves a related member feature.<br/>
   * Unlike {@link #getMember()}, always a 'real' feature is returned. I.e. if the memebr is a link into another
   * workspace, a feature from that workspace is returned.
   *
   * @param relation
   *          A property with maxOccurs 1.
   */
  Feature resolveMember( IRelationType relation );

  /**
   * Same as {@link #resolveMember(QName)}.
   *
   * @param relation
   *          Name of a property with maxOccurs 1.
   */
  Feature resolveMember( QName relation );

  /**
   * Same as {@link #resolveMember(IRelationType)}.
   */
  Feature[] resolveMembers( IRelationType relation );

  /**
   * Resolves related member features similar to the {@link #getMember(IRelationType)} method.<br/>
   * This method works for properties with <code>maxOccurs=1</code> as well as <code>maxOccurs = unbounded</code>.
   */
  Feature[] resolveMembers( QName relation );

  /**
   * Gives access to an unbound property containing member features (either linked or inline).
   */
  IFeatureBindingCollection<Feature> getMemberList( QName relationName );

  /**
   * @param type
   *          All members of the list are assumed to be of this type; the appropriate typed list is returned.
   */
  <T extends Feature> IFeatureBindingCollection<T> getMemberList( QName relationName, Class<T> type );

  /**
   * Gives access to an unbound property containing member features (either linked or inline).
   */
  IFeatureBindingCollection<Feature> getMemberList( IRelationType relation );

  /**
   * Gives access to an unbound property containing member feature (either linked or inline).
   *
   * @param type
   *          All members of the list are assumed to be of this type; the appropriate typed list is returned.
   */
  <T extends Feature> IFeatureBindingCollection<T> getMemberList( IRelationType relation, Class<T> type );

  /**
   * Removes a member from a relation. The following cases are handled:
   * <ul>
   * <li>minOccurs=1 and inline: property value is identical to <code>toRemove</code>: property is set to
   * <code>null</code>
   * <li>minOccurs>1 and the list contains <code>toRemove</code> (identity check): item removed from the list.</li>
   * </ul>
   *
   * @return The index of the successfully removed member, 0 in case of non-list relations. -1, if no member was
   *         removed.
   */
  int removeMember( IRelationType relation, Object toRemove );

  /**
   * Same as {@link #removeMember(IRelationType, Feature)}
   *
   * @see #removeMember(IRelationType, Feature)
   */
  int removeMember( QName relationName, Object toRemove );
}