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
import org.eclipse.swt.SWT;
import org.kalypso.ogc.gml.featureview.IFeatureModifier;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * @author belger
 */
public class LayerTableSorter extends ViewerSorter
{
  /** Die Features werden nach dieser Spalte sortiert */
  private IColumnDescriptor m_column = null;

  private boolean m_bInverse = false;

  public LayerTableSorter( )
  {
    super();
  }

  public LayerTableSorter( final Collator cola )
  {
    super( cola );
  }

  public final IColumnDescriptor getColumn( )
  {
    return m_column;
  }

  public final boolean isInverse( )
  {
    return m_bInverse;
  }

  public final void setInverse( final boolean bInverse )
  {
    m_bInverse = bInverse;
  }

  public final void setColumn( final IColumnDescriptor column )
  {
    m_column = column;
  }

  @Override
  public int compare( final Viewer viewer, final Object e1, final Object e2 )
  {
    final Feature kf1 = (Feature)e1;
    final Feature kf2 = (Feature)e2;

    final IColumnDescriptor column = getColumn();
    if( column == null )
      return 0;

    final IFeatureModifier modifier = column.getModifier();
    if( modifier == null )
      return 0;

    final Object o1 = getCompareValue( kf1, modifier );
    final Object o2 = getCompareValue( kf2, modifier );

    final int sign = isInverse() ? -1 : 1;
    if( o1 == o2 )
      return 0;

    if( o1 == null )
      return sign;

    if( o2 == null )
      return -sign;

    return sign * compareObjects( o1, o2 );
  }

  private Object getCompareValue( final Feature feature, final IFeatureModifier modifier )
  {
    final Object value = modifier.getProperty( feature );
    // BGUFIX: feature link combo always returns null for external features; we use the label in this case (that hat the
    // user actually sees)
    if( value == null )
      return modifier.getLabel( feature );

    return value;
  }

  private int compareObjects( final Object o1, final Object o2 )
  {
    if( o1 instanceof String && o2 instanceof String )
      return ((String)o1).compareTo( (String)o2 );
    else if( o1 instanceof Integer && o2 instanceof Integer )
      return ((Integer)o1).compareTo( (Integer)o2 );
    else if( o1 instanceof Double && o2 instanceof Double )
      return ((Double)o1).compareTo( (Double)o2 );
    else if( o1 instanceof Long && o2 instanceof Long )
      return ((Long)o1).compareTo( (Long)o2 );
    else if( o1 instanceof Float && o1 instanceof Float )
      return ((Float)o1).compareTo( (Float)o2 );
    else if( o1 instanceof Date && o2 instanceof Date )
      return ((Date)o1).compareTo( (Date)o2 );
    else if( o1 instanceof Boolean )
    {
      final String s1 = String.valueOf( o1 );
      final String s2 = String.valueOf( o2 );
      return s1.compareTo( s2 );
    }
    else if( o1 instanceof List && o2 instanceof List )
    {
      // hack: compare the first two items of the lists (e.g. for name or description properties)
      final List< ? > l1 = (List< ? >)o1;
      final List< ? > l2 = (List< ? >)o2;

      if( l1.isEmpty() && l2.isEmpty() )
        return 0;
      else if( l2.isEmpty() )
        return -1;
      else if( l1.isEmpty() )
        return 1;

      return compareObjects( l1.get( 0 ), l2.get( 0 ) );
    }
    else
      return 0;
  }

  @Override
  public boolean isSorterProperty( final Object element, final String property )
  {
    final IColumnDescriptor column = getColumn();
    if( column == null )
      return false;

    final GMLXPath propertyPath = column.getPropertyPath();
    return property.equals( propertyPath );
  }

  public int getSortDirection( )
  {
    final IColumnDescriptor column = getColumn();
    if( column == null )
      return SWT.NONE;

    final GMLXPath propertyPath = column.getPropertyPath();
    if( propertyPath == null )
      return SWT.NONE;

    if( m_bInverse )
      return SWT.UP;
    else
      return SWT.DOWN;
  }
}
