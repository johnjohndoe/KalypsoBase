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
package org.kalypsodeegree_impl.model.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;

/**
 * Feature with cache for properties.
 *
 * @author Gernot Belger
 */
public class AbstractCachedFeature2 extends Feature_Impl
{
  // TODO: we should move this somehow to the feature type
  private final FeatureCacheDefinition m_cacheDefinition;

  private final Set<QName> m_dirty = new HashSet<>();

  private final Map<QName, Object> m_cache = new HashMap<>();

  private boolean m_lock;

  protected AbstractCachedFeature2( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues, final FeatureCacheDefinition definition )
  {
    super( parent, parentRelation, ft, id, propValues );

    m_cacheDefinition = definition;
  }

  @Override
  public void setProperty( final IPropertyType pt, final Object value )
  {
    setDirty( pt.getQName() );

    super.setProperty( pt, value );
  }

  private void setDirty( final QName changedProperty )
  {
    if( m_lock )
      return;

    final QName[] cachedProperties = m_cacheDefinition.getDirtyProperties( changedProperty );
    setDirtyProperty( cachedProperties );
  }

  /**
   * Set the given property dirty, it will be recalculated on next access.
   */
  protected void setDirtyProperty( final QName[] cachedProperties )
  {
    for( final QName cachedProperty : cachedProperties )
    {
      m_dirty.add( cachedProperty );

      final IPropertyType property = getFeatureType().getProperty( cachedProperty );
      if( property instanceof IValuePropertyType )
      {
        if( ((IValuePropertyType) property).isGeometry() )
          setEnvelopesUpdated();
      }
    }

    dirtyChanged( cachedProperties );
  }

  /**
   * Allows implementors to react to dirty changes to cached properties.<br/>
   *
   * @param cachedProperties
   *          The properties that got dirty and need to be recalculated next time getProperty is called.
   */
  protected void dirtyChanged( @SuppressWarnings("unused") final QName[] cachedProperties )
  {
  }

  @Override
  public void setProperty( final QName propQName, final Object value )
  {
    setDirty( propQName );

    super.setProperty( propQName, value );
  }

  private boolean isDirty( final QName qname )
  {
    return m_dirty.contains( qname );
  }

  /**
   * IMPORTANT: both getProperty implementations are overwritten.
   */
  @Override
  public Object getProperty( final QName property )
  {
    if( m_cacheDefinition.isCached( property ) )
      return getCachedProperty( property );

    return super.getProperty( property );
  }

  /**
   * IMPORTANT: both getProperty implementations are overwritten.
   */
  @Override
  public Object getProperty( final IPropertyType pt )
  {
    final QName property = pt.getQName();

    if( m_cacheDefinition.isCached( property ) )
      return getCachedProperty( property );

    return super.getProperty( pt );
  }

  private synchronized Object getCachedProperty( final QName property )
  {
    final Object currentValue = m_cache.get( property );

    if( !isDirty( property ) )
      return currentValue;

    final Object newValue = recalculateProperty( property, currentValue );
    m_cache.put( property, newValue );
    m_dirty.remove( property );
    return newValue;
  }

  /**
   * Recalculates a cached property.<br>
   * Gets the underlying property from the standard feature implementation by default.<br>
   * Overwrite to recalculate the property in the implementing class. Alternatively, use feature functions (do not
   * overwrite in this case).
   */
  protected Object recalculateProperty( final QName property, @SuppressWarnings("unused") final Object oldValue )
  {
    return super.getProperty( property );
  }

  public void clearCachedProperties( )
  {
    m_cache.clear();

    /* Set all to dirty, so they will be recalculated next time */
    final QName[] cachedProperties = m_cacheDefinition.getCachedProperties();
    for( final QName cachedProp : cachedProperties )
      m_dirty.add( cachedProp );
  }

  protected synchronized void lockCache( )
  {
    m_lock = true;
  }

  protected synchronized void unlockCache( )
  {
    m_lock = false;
  }
}
