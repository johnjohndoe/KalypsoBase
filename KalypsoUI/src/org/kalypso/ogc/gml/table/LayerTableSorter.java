/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.table;

import java.text.Collator;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author belger
 */
public class LayerTableSorter extends ViewerSorter
{
  // FIXME: we should instead use the index of the column and get the corresponding feature-modifier from the column;
  // then delegate the comparison to the feature nmodifier or compare the labels of the column
  /** Die Features werden nach dieser Property sortiert */
  private String m_propertyName = null;

  private boolean m_bInverse = false;

  public LayerTableSorter( )
  {
    super();
  }

  public LayerTableSorter( final Collator cola )
  {
    super( cola );
  }

  public final String getPropertyName( )
  {
    return m_propertyName;
  }

  public final boolean isInverse( )
  {
    return m_bInverse;
  }

  public final void setInverse( final boolean bInverse )
  {
    m_bInverse = bInverse;
  }

  public final void setPropertyName( final String propertyName )
  {
    m_propertyName = propertyName;
  }

  @Override
  public int compare( final Viewer viewer, final Object e1, final Object e2 )
  {
    final Feature kf1 = (Feature) e1;
    final Feature kf2 = (Feature) e2;

    final String propertyName = getPropertyName();
    if( propertyName == null )
      return 0;

    final Object o1;
    final Object o2;
    final IPropertyType property = kf1.getFeatureType().getProperty( propertyName );
    if( property == null )
    {
      o1 = kf1.getId();
      o2 = kf2.getId();
    }
    else if( property instanceof IRelationType && !property.isList() )
    {
      o1 = FeatureHelper.resolveLink( kf1, property.getQName() );
      o2 = FeatureHelper.resolveLink( kf2, property.getQName() );
    }
    else
    {
      o1 = kf1.getProperty( property );
      o2 = kf2.getProperty( property );
    }

    final int sign = isInverse() ? -1 : 1;
    final int basicCompare = doCompare( o1, o2 );
    return basicCompare * sign;
  }

  private int doCompare( final Object o1, final Object o2 )
  {
    if( o1 == o2 )
      return 0;
    if( o1 == null )
      return 1;
    if( o2 == null )
      return -1;

    if( o1 instanceof Number && o2 instanceof Number )
      return Double.compare( ((Number) o1).doubleValue(), ((Number) o2).doubleValue() );
    else if( o1 instanceof Date && o2 instanceof Date )
      return ((Date) o1).compareTo( (Date) o2 );
    else if( o1 instanceof Boolean && o2 instanceof Boolean )
    {
      final String s1 = String.valueOf( o1 );
      final String s2 = String.valueOf( o2 );
      return s1.compareTo( s2 );
    }
    else if( o1 instanceof Feature && o2 instanceof Feature )
    {
      // FIXME: better than before, but still not OK: we should rather compare by the string that is really shown to the
      // user instead of the feature's label (luckily often this is the same),
      final Object l1 = FeatureHelper.getAnnotationValue( (Feature) o1, IAnnotation.ANNO_LABEL );
      final Object l2 = FeatureHelper.getAnnotationValue( (Feature) o2, IAnnotation.ANNO_LABEL );
      return doCompare( l1, l2 );
    }
    else if( o1 instanceof List && o2 instanceof List )
    {
      final List< ? > l1 = ((List< ? >) o1);
      final List< ? > l2 = ((List< ? >) o2);

      final Object e1 = l1.isEmpty() ? null : l1.get( 0 );
      final Object e2 = l2.isEmpty() ? null : l2.get( 0 );
      return doCompare( e1, e2 );
    }
    else
    {
      final String s1 = o1.toString();
      final String s2 = o2.toString();
      return s1.compareToIgnoreCase( s2 );
    }
  }

  @Override
  public boolean isSorterProperty( final Object element, final String property )
  {
    return property.equals( m_propertyName );
  }
}
