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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollectorWithTime;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * @author Holger Albert
 */
public class FeatureListComparator
{
  /* If used as unique property, the element number is used as unique key. */
  public static final QName PROPERTY_COUNTER = new QName( StringUtils.EMPTY );

  private final Feature m_referenceFeature;

  private final Feature m_selectedFeature;

  private final GMLXPath m_listPath;

  private final QName m_uniqueProperty;

  private final Map<Object, FeaturePair> m_hash;

  public FeatureListComparator( final Feature referenceFeature, final Feature selectedFeature, final GMLXPath listPath, final QName uniqueProperty )
  {
    m_referenceFeature = referenceFeature;
    m_selectedFeature = selectedFeature;
    m_listPath = listPath;
    m_uniqueProperty = uniqueProperty;
    m_hash = new HashMap<>();
  }

  public IStatus compareList( ) throws Exception
  {
    /* The status collector. */
    final IStatusCollector collector = new StatusCollectorWithTime( KalypsoCorePlugin.getID() );

    /* Get the feature lists. */
    final Object referenceQuery = GMLXPathUtilities.query( m_listPath, m_referenceFeature );
    final Object selectedQuery = GMLXPathUtilities.query( m_listPath, m_selectedFeature );
    if( !(referenceQuery instanceof FeatureList && selectedQuery instanceof FeatureList) )
      throw new IllegalArgumentException( String.format( "GMLXPath '%s' does not point to a feature list...", m_listPath.toString() ) ); //$NON-NLS-1$

    /* Cast. */
    final FeatureList referenceList = (FeatureList) referenceQuery;
    final FeatureList selectedList = (FeatureList) selectedQuery;

    /* Fill the reference (feature one) into the hash. */
    for( int i = 0; i < referenceList.size(); i++ )
    {
      final Feature referenceFeature = (Feature) referenceList.get( i );

      final Object referenceKey = resolveKeyProperty( referenceFeature, i );
      if( m_hash.containsKey( referenceKey ) )
      {
        collector.add( new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), String.format( Messages.getString("FeatureListComparator_1"), referenceKey ) ) ); //$NON-NLS-1$
        continue;
      }

      m_hash.put( referenceKey, new FeaturePair( referenceFeature, null ) );
    }

    /* Fill the selected (feature two) into the hash. */
    for( int i = 0; i < selectedList.size(); i++ )
    {
      final Feature selectedFeature = (Feature) selectedList.get( i );

      final Object selectedKey = resolveKeyProperty( selectedFeature, i );
      if( !m_hash.containsKey( selectedKey ) )
      {
        m_hash.put( selectedKey, new FeaturePair( null, selectedFeature ) );
        continue;
      }

      final FeaturePair featurePair = m_hash.get( selectedKey );
      if( featurePair.getTwo() != null )
      {
        collector.add( new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), String.format( Messages.getString("FeatureListComparator_2"), selectedKey ) ) ); //$NON-NLS-1$
        continue;
      }

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
        collector.add( new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), String.format( Messages.getString("FeatureListComparator_3"), key ) ) ); //$NON-NLS-1$
        continue;
      }

      /* For the feature in the selected list, there is no feature in the reference list. */
      if( one == null && two != null )
      {
        collector.add( new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), String.format( Messages.getString("FeatureListComparator_4"), key ) ) ); //$NON-NLS-1$
        continue;
      }

      /* HINT: Here both features exist. */
      if( one instanceof IXLinkedFeature && two instanceof IXLinkedFeature )
        continue;

      /* Compare the features. */
      final FeatureComparator comparator = new FeatureComparator( one, two );
      final IStatus compareStatus = comparator.compareFeatures( key );
      if( !compareStatus.isOK() )
        collector.add( compareStatus );
    }

    final IFeatureType featureType = m_referenceFeature.getFeatureType();
    final IPropertyType property = (IPropertyType) GMLXPathUtilities.query( m_listPath, featureType );
    final IAnnotation annotation = property.getAnnotation();
    final String label = annotation.getLabel();

    return collector.asMultiStatus( label );
  }

  private Object resolveKeyProperty( final Feature referenceFeature, final int i )
  {
    if( m_uniqueProperty == PROPERTY_COUNTER )
      return String.format( Messages.getString("FeatureListComparator_5"), i ); //$NON-NLS-1$

    return referenceFeature.getProperty( m_uniqueProperty );
  }
}