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

import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollectorWithTime;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.property.virtual.IFunctionPropertyType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Holger Albert
 */
public class FeatureComparator
{

  private final Feature m_one;

  private final Feature m_two;

  public FeatureComparator( final Feature one, final Feature two )
  {
    m_one = one;
    m_two = two;
  }

  public IStatus compareFeatures( final Object labelKey ) throws GMLXPathException
  {
    /* The types should be equal. */
    final IFeatureType oneFeatureType = m_one.getFeatureType();
    final IFeatureType twoFeatureType = m_two.getFeatureType();
    if( !oneFeatureType.equals( twoFeatureType ) )
      return new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), Messages.getString( "FeatureComparator_0" ) ); //$NON-NLS-1$

    /* The status collector. */
    final IStatusCollector collector = new StatusCollectorWithTime( KalypsoCorePlugin.getID() );

    /* So we can use the feature type of feature one. */
    final IPropertyType[] oneProperties = oneFeatureType.getProperties();
    for( final IPropertyType onePropertyType : oneProperties )
    {
      /* This values are calculated from other values. */
      if( onePropertyType instanceof IFunctionPropertyType )
        continue;

      /* Get the property via the qname, because the feature property types are bound to the feature. */
      final QName oneName = onePropertyType.getQName();

      /* Get the properties. */
      final Object oneProperty = m_one.getProperty( oneName );
      final Object twoProperty = m_two.getProperty( oneName );
      if( oneProperty == null && twoProperty == null )
        continue;

      /* Check the properties. */
      if( oneProperty == null || twoProperty == null )
      {
        final String label = getLabel( m_one, oneName );
        final Status compareStatus = new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), String.format( Messages.getString( "FeatureComparator_1" ), label ) ); //$NON-NLS-1$
        collector.add( compareStatus );
        continue;
      }

      /* Get some describing variables. */
      final boolean isList = onePropertyType.isList();
      final boolean isRelation = onePropertyType instanceof IRelationType;

      /* Compare. */
      if( isList )
        compareList( m_one, m_two, oneProperty, twoProperty, onePropertyType, oneName, isRelation, collector );
      else
        compareValue( m_one, m_two, oneProperty, twoProperty, onePropertyType, oneName, isRelation, collector );
    }

    return collector.asMultiStatus( String.format( "%s", labelKey ) ); //$NON-NLS-1$
  }

  private void compareList( final Feature one, final Feature two, final Object oneProperty, final Object twoProperty, final IPropertyType onePropertyType, final QName oneName, final boolean isRelation, final IStatusCollector collector ) throws GMLXPathException
  {
    if( isRelation )
    {
      /* Compare the feature lists. */
      final FeatureListComparator comparator = new FeatureListComparator( one, two, new GMLXPath( oneName ), FeatureListComparator.PROPERTY_COUNTER );
      final IStatus compareStatus = comparator.compareList();
      collector.add( compareStatus );

      return;
    }

    /* Cast. */
    final IValuePropertyType oneValueType = (IValuePropertyType)onePropertyType;
    if( oneValueType.isGeometry() )
    {
      /* List of geometries. */
      /* HINT: List of geometries will not be compared at the moment. */

      return;
    }

    /* List of values. */
    /* HINT: List of values will not be compared at the moment. */
  }

  private void compareValue( final Feature one, final Feature two, final Object oneProperty, final Object twoProperty, final IPropertyType onePropertyType, final QName oneName, final boolean isRelation, final IStatusCollector collector ) throws GMLXPathException
  {
    if( isRelation )
    {
      /* Compare the features. */
      final Feature oneFeatureChild = one.getMember( oneName );
      final Feature twoFeatureChild = two.getMember( oneName );
      if( oneFeatureChild instanceof IXLinkedFeature || twoFeatureChild instanceof IXLinkedFeature )
      {
        /* Links. */
        /* HINT: Links will not be compared at the moment. Leads to recursions. */
        return;
      }

      final String label = getLabel( one, oneName );
      final FeatureComparator comparator = new FeatureComparator( oneFeatureChild, twoFeatureChild );
      final IStatus compareStatus = comparator.compareFeatures( label );
      collector.add( compareStatus );

      return;
    }

    final IValuePropertyType oneValueType = (IValuePropertyType)onePropertyType;
    if( oneValueType.isGeometry() )
    {
      /* Compare the geometries. */
      try
      {
        final Geometry oneGeometry = JTSAdapter.export( (GM_Object)oneProperty );
        final Geometry twoGeometry = JTSAdapter.export( (GM_Object)twoProperty );
        if( !oneGeometry.equalsExact( twoGeometry ) )
        {
          final String label = getLabel( one, oneName );
          final Status compareStatus = new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), String.format( Messages.getString( "FeatureComparator_2" ), label ) ); //$NON-NLS-1$
          collector.add( compareStatus );

          return;
        }
      }
      catch( final GM_Exception e )
      {
        collector.add( IStatus.WARNING, "Failed to compare geometries", e );
      }

      return;
    }

    /* Simple types. */
    if( oneProperty instanceof String || oneProperty instanceof Character || oneProperty instanceof Byte || oneProperty instanceof Number || oneProperty instanceof Date
        || oneProperty instanceof Boolean )
    {
      /* Compare the simple properties. */
      if( !ObjectUtils.equals( oneProperty, twoProperty ) )
      {
        final String label = getLabel( one, oneName );
        final Status compareStatus = new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), String.format( Messages.getString( "FeatureComparator_4" ), label, oneProperty, twoProperty ) ); //$NON-NLS-1$
        collector.add( compareStatus );

        return;
      }

      return;
    }

    /* Complex types. */
    /* HINT: Complex types will not be compared at the moment. */
  }

  private String getLabel( final Feature feature, final QName name ) throws GMLXPathException
  {
    final IFeatureType oneFeatureType = feature.getFeatureType();
    final IPropertyType property = (IPropertyType)GMLXPathUtilities.query( new GMLXPath( name ), oneFeatureType );
    final IAnnotation annotation = property.getAnnotation();

    return annotation.getLabel();
  }
}