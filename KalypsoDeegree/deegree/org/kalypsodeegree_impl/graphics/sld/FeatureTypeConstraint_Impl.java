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
package org.kalypsodeegree_impl.graphics.sld;

import java.util.ArrayList;
import java.util.List;

import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.graphics.sld.Extent;
import org.kalypsodeegree.graphics.sld.FeatureTypeConstraint;
import org.kalypsodeegree.xml.Marshallable;

/**
 * A FeatureTypeConstraint element is used to identify a feature type by well-known name, using the FeatureTypeName
 * element.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version $Revision$ $Date$
 */
class FeatureTypeConstraint_Impl implements FeatureTypeConstraint, Marshallable
{
  private final List<Extent> m_extents = new ArrayList<>();

  private Filter m_filter = null;

  private String m_featureTypeName = null;

  /**
   * constructor initializing the class with the <FeatureTypeConstraint>
   */
  FeatureTypeConstraint_Impl( final String featureTypeName, final Filter filter, final Extent[] extents )
  {
    setFeatureTypeName( featureTypeName );
    setFilter( filter );
    setExtents( extents );
  }

  /**
   * returns the name of the feature type
   *
   * @return the name of the feature type
   */
  @Override
  public String getFeatureTypeName( )
  {
    return m_featureTypeName;
  }

  /**
   * sets the name of the feature type
   *
   * @param featureTypeName
   *          the name of the feature type
   */
  @Override
  public void setFeatureTypeName( final String featureTypeName )
  {
    this.m_featureTypeName = featureTypeName;
  }

  /**
   * returns a feature-filter as defined in WFS specifications.
   *
   * @return the filter of the FeatureTypeConstraints
   */
  @Override
  public Filter getFilter( )
  {
    return m_filter;
  }

  /**
   * sets a feature-filter as defined in WFS specifications.
   *
   * @param filter
   *          the filter of the FeatureTypeConstraints
   */
  @Override
  public void setFilter( final Filter filter )
  {
    this.m_filter = filter;
  }

  /**
   * returns the extent for filtering the feature type
   *
   * @return the extent for filtering the feature type
   */
  @Override
  public Extent[] getExtents( )
  {
    return m_extents.toArray( new Extent[m_extents.size()] );
  }

  /**
   * sets the extent for filtering the feature type
   *
   * @param extents
   *          extents for filtering the feature type
   */
  @Override
  public void setExtents( final Extent[] extents )
  {
    this.m_extents.clear();

    if( extents != null )
    {
      for( final Extent extent : extents )
      {
        addExtent( extent );
      }
    }
  }

  /**
   * Adds an Extent to the Extent-List of a FeatureTypeConstraint
   *
   * @param extent
   *          an extent to add
   */
  @Override
  public void addExtent( final Extent extent )
  {
    m_extents.add( extent );
  }

  /**
   * Removes an Extent from the Extent-List of a FeatureTypeConstraint
   *
   * @param extent
   *          an extent to remove
   */
  @Override
  public void removeExtent( final Extent extent )
  {
    m_extents.remove( m_extents.indexOf( extent ) );
  }

  /**
   * @return the FeatureTypeConstraint as String
   */
  @Override
  public String toString( )
  {
    String ret = getClass().getName() + "\n";
    ret = "featureTypeName = " + m_featureTypeName + "\n";
    ret += "filter = " + m_filter + "\n";
    ret += "extents = " + m_extents + "\n";

    return ret;
  }

  /**
   * exports the content of the FeatureTypeConstraint as XML formated String
   *
   * @return xml representation of the FeatureTypeConstraint
   */
  @Override
  public String exportAsXML( )
  {
    final StringBuffer sb = new StringBuffer( 1000 );
    sb.append( "<FeatureTypeConstraint>" );
    sb.append( "<FeatureTypeName>" ).append( m_featureTypeName );
    sb.append( "</FeatureTypeName>" );
    if( m_filter != null )
    {
      sb.append( m_filter.toXML() );
    }
    if( m_extents != null )
    {
      for( int i = 0; i < m_extents.size(); i++ )
      {
        sb.append( ((Marshallable) m_extents.get( i )).exportAsXML() );
      }
    }
    sb.append( "</FeatureTypeConstraint>" );

    return sb.toString();
  }
}