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
package org.kalypso.ogc.gml.compare;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollectorWithTime;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;

/**
 * @author Holger Albert
 */
public class FeatureListComparator
{
  private final Feature m_referenceFeature;

  private final Feature m_selectedFeature;

  private final QName m_listProperty;

  private final QName m_uniqueProperty;

  private final Map<Object, FeaturePair> m_hash;

  public FeatureListComparator( final Feature referenceFeature, final Feature selectedFeature, final QName listProperty, final QName uniqueProperty )
  {
    m_referenceFeature = referenceFeature;
    m_selectedFeature = selectedFeature;
    m_listProperty = listProperty;
    m_uniqueProperty = uniqueProperty;
    m_hash = new HashMap<Object, FeaturePair>();
  }

  public IStatus compareList( ) throws Exception
  {
    /* The status collector. */
    final IStatusCollector collector = new StatusCollectorWithTime( KalypsoCorePlugin.getID() );

    /* Get the feature lists. */
    final FeatureList referenceList = (FeatureList) m_referenceFeature.getProperty( m_listProperty );
    final FeatureList selectedList = (FeatureList) m_selectedFeature.getProperty( m_listProperty );

    /* Fill the reference (feature one) into the hash. */
    for( final Object object : referenceList )
    {
      final Feature referenceFeature = (Feature) object;

      final Object referenceKey = referenceFeature.getProperty( m_uniqueProperty );
      if( m_hash.containsKey( referenceKey ) )
        throw new Exception( String.format( "The property '%s' of elements in the reference list has no unique values...", m_uniqueProperty.getLocalPart() ) );

      m_hash.put( referenceKey, new FeaturePair( referenceFeature, null ) );
    }

    /* Fill the selected (feature two) into the hash. */
    for( final Object object : selectedList )
    {
      final Feature selectedFeature = (Feature) object;

      final Object selectedKey = selectedFeature.getProperty( m_uniqueProperty );
      if( !m_hash.containsKey( selectedKey ) )
      {
        m_hash.put( selectedKey, new FeaturePair( null, selectedFeature ) );
        continue;
      }

      final FeaturePair featurePair = m_hash.get( selectedKey );
      if( featurePair.getTwo() != null )
        throw new Exception( String.format( "The property '%s' of elements in the reference list has no unique values...", m_uniqueProperty.getLocalPart() ) );

      featurePair.setTwo( selectedFeature );
    }

    /* Compare. */
    final Object[] keys = m_hash.keySet().toArray( new Object[] {} );
    for( final Object key : keys )
    {
      /* Get the feature pair. */
      final FeaturePair pair = m_hash.get( key );

      /* Get the features. */
      final Feature one = pair.getOne();
      final Feature two = pair.getTwo();

      /* Should not occur. */
      if( one == null && two == null )
        continue;

      /* For the feature in the reference list, there is no feature in the selected list. */
      if( one != null && two == null )
      {
        collector.add( new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), String.format( "The element '%s' is missing.", key ) ) );
        continue;
      }

      /* For the feature in the selected list, there is no feature in the reference list. */
      if( one == null && two != null )
      {
        collector.add( new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), String.format( "The element '%s' is a new one.", key ) ) );
        continue;
      }

      /* HINT: Here both features exist. */
      /* Compare the features. */
      // TODO
    }

    return collector.asMultiStatusOrOK( String.format( "%s", m_listProperty.getLocalPart() ) );
  }
}