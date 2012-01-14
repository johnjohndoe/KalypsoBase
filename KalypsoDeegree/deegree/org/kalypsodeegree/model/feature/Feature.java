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

import org.eclipse.core.runtime.IAdaptable;
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
public interface Feature extends BaseFeature, Deegree2Feature, IAdaptable
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
   *          the referenced {@link Feature}.
   * @param featureType
   *          Name of the type of the feature this link points to.
   * @throws IllegalArgumentException
   *           If <code>relation</code> is not a property of this feature.
   * @return The freshly created feature.
   */
  IXLinkedFeature createLink( IRelationType relation, String href, QName featureType );

  /**
   * @param featureType
   *          Type of the feature this link points to.
   * @see #createLink(IRelationType, String, QName)
   */
  IXLinkedFeature createLink( IRelationType relation, String href, IFeatureType featureType );

  /**
   * @param relation
   *          Name of a property of this feature (must be a relation) that allows for linked {@link Feature}s.
   * @see #createLink(IRelationType, String)
   * @throws IllegalArgumentException
   *           If <code>relation</code> is not the name of a relation type.
   */
  IXLinkedFeature createLink( QName relation, String href, QName featureType );

  /**
   * @see #createLink(QName, String)
   * @param featureType
   *          Type of the feature this link points to.
   * @throws IllegalArgumentException
   *           If <code>relation</code> is not the name of a relation type.
   */
  IXLinkedFeature createLink( QName relation, String href, IFeatureType featureType );

  // FIXME: do it!
// Feature createSubFeature( IRelationType relation );
//
// Feature createSubFeature( QName relation );

  /**
   * Resolves a related member feature.<br/>
   * Inline feature gets returned, feature links are returned as xlinked-features.
   */
  Feature getMember( IRelationType relation );

  /**
   * Resolves a related member feature.<br/>
   * Inline feature gets returned, feature links are returned as xlinked-features.
   *
   * @param relation
   *          Name of a property of this feature (must be a relation).
   * @throws IllegalArgumentException
   *           If <code>relation</code> is not the name of a relation type.
   */
  Feature getMember( QName relation );
}
