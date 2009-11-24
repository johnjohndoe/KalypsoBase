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
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;

/**
 * Adds additional methods to a Feature A Feature is adaptable, thus allowing Adapter Factories and/or Subclasses to
 * provide another "view" over a feature object. For instance, an observation-feature can be directly represented as an
 * observation.<br>
 * <br>
 * 
 * @see Deegree2Feature
 */
public interface BaseFeature extends IAdaptable
{
  /**
   * returns the property of the feature that matches the submitted propertytype
   */
  Object getProperty( IPropertyType propertyType );

  /**
   * returns all geometry properties of the feature. If no geometry could be found an <tt>GM_Object[]</tt> with zero
   * length will be returned.
   * 
   * @deprecated use {FeatureDeegreeTwo}.getGeometryPropertyValues instead
   */
  @Deprecated
  GM_Object[] getGeometryProperties( );

  /**
   * Returns the default geometry of the <tt>Feature</tt>.
   * 
   * @return default geometry or null, if the <tt>Feature</tt> has none
   * @deprecated use {FeatureDeegreeTwo}.getDefaultGeometryPropertyValue() instead
   */
  @Deprecated
  GM_Object getDefaultGeometryProperty( );

  /**
   * returns the envelope / boundingbox of the feature
   * 
   * @deprecated Use {@link Deegree2Feature#getBoundedBy()} instead
   */
  @Deprecated
  GM_Envelope getEnvelope( );

  public GMLWorkspace getWorkspace( );

  /**
   * Return the parent of this feature, that is, the feature wich contains this feature as inline feature.
   * 
   * @see #getParentRelation()
   */
  public Feature getParent( );

  /**
   * Returns the {@link IRelationType} where this feature resides inside its parent feature.
   * 
   * @see #getParent()
   */
  public IRelationType getParentRelation( );

  public void setProperty( final IPropertyType propertyType, final Object value );

  public void setProperty( final QName propQName, final Object value );

  /**
   * @deprecated use getPropery(PropertyType)
   */
  @Deprecated
  public Object getProperty( final String propLocalName );

  /**
   * @deprecated
   */
  @Deprecated
  public void setProperty( final String propLocalName, final Object value );

  public Object getProperty( final QName propQName );

  /**
   * intended to be called from GMLWorkspace when root feature is set.
   */
  public void setWorkspace( final GMLWorkspace workspace );

  /**
   * @deprecated Use {@link Deegree2Feature#setEnvelopesUpdated()} instead
   * @see org.kalypsodeegree.model.feature.BaseFeature#invalidEnvelope()
   */
  @Deprecated
  public void invalidEnvelope( );

  /**
   * FIXME dimitri, ilya - this is not allowed for basic features <br>
   * <br>
   * Please define your own Feature Binding and let this binding derive from AbstractCachedFeature. So you can easily
   * adjust your implementation and cache geometries whitout changing the default behaviour of features.<br>
   * <br>
   * Notably (up to 2x) improves redraw performance
   */
  public Object getCachedGeometry( );

  public void setCachedGeometry( Object value );

}